package io.akeyless.secrets;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.akeyless.client.ApiClient;
import io.akeyless.client.ApiException;
import io.akeyless.client.api.V2Api;
import io.akeyless.client.model.Auth;
import io.akeyless.client.model.AuthOutput;
import io.akeyless.client.model.GetDynamicSecretValue;
import io.akeyless.client.model.GetRotatedSecretValue;
import io.akeyless.client.model.GetSecretValue;

import java.util.Collections;
import java.util.Map;

final class AkeylessClient
{
    private static final Gson GSON = new Gson();
    private static final String DEFAULT_API_URL = "https://api.akeyless.io";

    private final AkeylessSecretProviderConfig config;
    private V2Api api;
    private String token;

    AkeylessClient(AkeylessSecretProviderConfig config)
    {
        this.config = config;
    }

    String getSecretValue(String secretPath, String jsonKey, SecretType secretType)
            throws ApiException
    {
        String normalizedPath = normalizeItemPath(secretPath);
        String value = switch (secretType) {
            case STATIC -> fetchStaticSecret(normalizedPath);
            case DYNAMIC -> fetchDynamicSecret(normalizedPath);
            case ROTATED -> fetchRotatedSecret(normalizedPath);
        };
        if (jsonKey != null && !jsonKey.isEmpty()) {
            return extractJsonField(value, jsonKey);
        }
        return value;
    }

    private synchronized V2Api api()
    {
        if (api == null) {
            String apiUrl = config.getApiUrl();
            String url = apiUrl == null || apiUrl.isBlank() ? DEFAULT_API_URL : apiUrl.trim();
            String basePath = url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
            ApiClient client = new ApiClient();
            client.setBasePath(basePath);
            api = new V2Api(client);
        }
        return api;
    }

    private synchronized String getToken()
            throws ApiException
    {
        String apiToken = config.getApiToken();
        if (apiToken != null && !apiToken.isBlank()) {
            return apiToken.trim();
        }
        if (token != null && !token.isEmpty()) {
            return token;
        }
        if (config.getAuthenticationType() == AuthenticationType.API_TOKEN) {
            throw new ApiException("api-token authentication requires the api-token property");
        }
        Auth auth = AkeylessAuthenticator.buildAuth(config);
        AuthOutput authOutput = api().auth(auth);
        token = authOutput != null ? authOutput.getToken() : null;
        if (token == null || token.isEmpty()) {
            throw new ApiException("Akeyless authentication did not return a token");
        }
        return token;
    }

    private String fetchStaticSecret(String path)
            throws ApiException
    {
        GetSecretValue body = new GetSecretValue();
        body.setToken(getToken());
        body.setNames(Collections.singletonList(path));
        Map<String, Object> out = api().getSecretValue(body);
        return mapOutputToString(out, path);
    }

    private String fetchDynamicSecret(String path)
            throws ApiException
    {
        GetDynamicSecretValue body = new GetDynamicSecretValue();
        body.setToken(getToken());
        body.setName(trimLeadingSlash(path));
        body.setJson(true);
        Map<String, Object> out = api().getDynamicSecretValue(body);
        if (out == null || out.isEmpty()) {
            throw new ApiException("Empty dynamic secret value for: " + path);
        }
        return GSON.toJson(out);
    }

    private String fetchRotatedSecret(String path)
            throws ApiException
    {
        GetRotatedSecretValue body = new GetRotatedSecretValue();
        body.setToken(getToken());
        body.setNames(trimLeadingSlash(path));
        body.setJson(true);
        Map<String, Object> out = api().getRotatedSecretValue(body);
        if (out == null || out.isEmpty()) {
            throw new ApiException("Empty rotated secret value for: " + path);
        }
        return GSON.toJson(out);
    }

    private static String mapOutputToString(Map<String, Object> out, String path)
            throws ApiException
    {
        if (out == null || out.isEmpty()) {
            throw new ApiException("No value returned for secret: " + path);
        }
        Object value = out.get(path);
        if (value == null && out.size() == 1) {
            value = out.values().iterator().next();
        }
        if (value == null) {
            throw new ApiException("Empty value for secret: " + path);
        }
        if (value instanceof String stringValue) {
            return stringValue;
        }
        if (value instanceof byte[] bytes) {
            return new String(bytes);
        }
        if (value instanceof Map<?, ?> map) {
            if (map.containsKey("value") && map.get("value") instanceof String stringValue) {
                return stringValue;
            }
            return GSON.toJson(map);
        }
        return value.toString();
    }

    private static String extractJsonField(String value, String key)
    {
        String trimmed = value == null ? "" : value.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new IllegalArgumentException("Secret value is not JSON; cannot extract key: " + key);
        }
        JsonElement element = JsonParser.parseString(trimmed);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Secret value is not a JSON object; cannot extract key: " + key);
        }
        JsonObject object = element.getAsJsonObject();
        if (!object.has(key)) {
            throw new IllegalArgumentException("Key '" + key + "' not found in secret JSON");
        }
        JsonElement field = object.get(key);
        if (field.isJsonNull()) {
            throw new IllegalArgumentException("Key '" + key + "' is null in secret JSON");
        }
        if (field.isJsonPrimitive()) {
            return field.getAsString();
        }
        return field.toString();
    }

    static String normalizeItemPath(String name)
    {
        String path = name == null ? "" : name.trim();
        if (path.isEmpty()) {
            return path;
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    static String resolveSecretPath(String basePath, String secretPath)
    {
        String path = secretPath == null ? "" : secretPath.trim();
        if (path.isEmpty()) {
            throw new IllegalArgumentException("secret path must not be empty");
        }
        if (path.startsWith("/")) {
            return normalizeItemPath(path);
        }
        String base = basePath == null ? "" : basePath.trim();
        if (base.isEmpty()) {
            return normalizeItemPath(path);
        }
        if (!base.startsWith("/")) {
            base = "/" + base;
        }
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        return normalizeItemPath(base + "/" + path);
    }

    private static String trimLeadingSlash(String path)
    {
        if (path == null || path.isEmpty()) {
            return path;
        }
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
