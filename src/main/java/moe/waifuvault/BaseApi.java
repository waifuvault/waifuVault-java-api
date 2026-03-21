package moe.waifuvault;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import moe.waifuvault.models.GenericSuccess;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

abstract class BaseApi {

    private final HttpService http;

    protected BaseApi(final HttpService http) {
        this.http = http;
    }

    protected <T> T get(final String path, final Map<String, String> params, final Class<T> type) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, params)))
                .GET()
                .build();
        return sendJsonRequest(request, type);
    }

    protected <T> T postJson(final String path, final Object body, final Class<T> type) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, null)))
                .POST(HttpRequest.BodyPublishers.ofString(gson().toJson(body)))
                .header("Content-Type", "application/json")
                .build();
        return sendJsonRequest(request, type);
    }

    protected <T> T patchJson(final String path, final Object body, final Class<T> type) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, null)))
                .method("PATCH", HttpRequest.BodyPublishers.ofString(gson().toJson(body)))
                .header("Content-Type", "application/json")
                .build();
        return sendJsonRequest(request, type);
    }

    protected <T> T delete(final String path, final Map<String, String> params, final Class<T> type) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, params)))
                .DELETE()
                .build();
        return sendJsonRequest(request, type);
    }

    protected GenericSuccess deleteRaw(final String path) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, null)))
                .DELETE()
                .build();
        sendStringRequest(request);
        return new GenericSuccess(true, "deleted");
    }

    protected byte[] getBytes(final String url, final String password) {
        final HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET();
        if (password != null) {
            builder.header("x-password", password);
        }
        return sendBytesRequest(builder.build());
    }

    protected byte[] postJsonBytes(final String path, final Object body) {
        final HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(path, null)))
                .POST(HttpRequest.BodyPublishers.ofString(gson().toJson(body)))
                .header("Content-Type", "application/json")
                .build();
        return sendBytesRequest(request);
    }

    protected <T> T sendJsonRequest(final HttpRequest request, final Class<T> type) {
        try {
            final HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);
            return gson().fromJson(response.body(), type);
        } catch (final IOException e) {
            throw new WaifuException(0, "Request Error", e.getMessage());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WaifuException(0, "Request Error", "Request was interrupted");
        }
    }

    protected String buildUrl(final String path, final Map<String, String> params) {
        final StringBuilder sb = new StringBuilder(http.baseUrl()).append("/rest");
        if (path != null && !path.isEmpty()) {
            sb.append("/").append(path);
        }
        if (params != null) {
            final StringBuilder qs = new StringBuilder();
            boolean first = true;
            for (final Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (!first) {
                    qs.append("&");
                }
                qs.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                first = false;
            }
            if (!qs.isEmpty()) {
                sb.append("?").append(qs);
            }
        }
        return sb.toString();
    }

    protected String baseUrl() {
        return http.baseUrl();
    }

    protected Gson gson() {
        return http.gson;
    }

    private void sendStringRequest(final HttpRequest request) {
        try {
            final HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            checkError(response);
        } catch (final IOException e) {
            throw new WaifuException(0, "Request Error", e.getMessage());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WaifuException(0, "Request Error", "Request was interrupted");
        }
    }

    private byte[] sendBytesRequest(final HttpRequest request) {
        try {
            final HttpResponse<byte[]> response = http.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
            }
            if (response.statusCode() == 403) {
                throw new WaifuException(403, "Forbidden", "Password is incorrect");
            }
            final String body = new String(response.body(), StandardCharsets.UTF_8);
            throw parseError(response.statusCode(), body);
        } catch (final IOException e) {
            throw new WaifuException(0, "Request Error", e.getMessage());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WaifuException(0, "Request Error", "Request was interrupted");
        }
    }

    private WaifuException parseError(final int statusCode, final String body) {
        try {
            final ErrorResponse err = gson().fromJson(body, ErrorResponse.class);
            if (err != null && err.name != null) {
                return new WaifuException(err.status, err.name, err.message);
            }
        } catch (final JsonSyntaxException ignored) {
        }
        return new WaifuException(statusCode, "Error", body);
    }

    private void checkError(final HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return;
        }
        throw parseError(response.statusCode(), response.body());
    }

    private static class ErrorResponse {
        String name;
        String message;
        int status;
    }
}
