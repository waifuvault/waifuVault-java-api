package moe.waifuvault;

import moe.waifuvault.models.FileUpload;
import moe.waifuvault.models.GenericSuccess;
import moe.waifuvault.models.ModifyEntryPayload;
import moe.waifuvault.models.WaifuFile;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

class FileApi extends BaseApi {

    public FileApi(final HttpService config) {
        super(config);
    }

    public WaifuFile uploadFile(final FileUpload options) {
        final Map<String, String> params = new LinkedHashMap<>();
        if (options.getExpires() != null) {
            params.put("expires", options.getExpires());
        }
        if (options.getHideFilename() != null) {
            params.put("hide_filename", options.getHideFilename().toString());
        }
        if (options.getOneTimeDownload() != null) {
            params.put("one_time_download", options.getOneTimeDownload().toString());
        }

        final HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                .uri(URI.create(buildUrl(options.getBucketToken(), params)));

        if (options.getClientIP() != null) {
            reqBuilder.header("X-Forwarded-For", options.getClientIP());
            reqBuilder.header("X-Real-IP", options.getClientIP());
        }

        if (options.isFileUpload()) {
            final String boundary = UUID.randomUUID().toString();
            reqBuilder.header("Content-Type", "multipart/form-data; boundary=" + boundary);
            reqBuilder.method("PUT", buildMultipartBody(boundary, options.getFileBytes(),
                    options.getFilename(), options.getPassword()));
        } else {
            final StringBuilder formBody = new StringBuilder();
            formBody.append("url=").append(URLEncoder.encode(options.getUrl(), StandardCharsets.UTF_8));
            if (options.getPassword() != null) {
                formBody.append("&password=").append(URLEncoder.encode(options.getPassword(), StandardCharsets.UTF_8));
            }
            reqBuilder.header("Content-Type", "application/x-www-form-urlencoded");
            reqBuilder.method("PUT", HttpRequest.BodyPublishers.ofString(formBody.toString()));
        }

        return sendJsonRequest(reqBuilder.build(), WaifuFile.class);
    }

    public WaifuFile fileInfo(final String token, final boolean formatted) {
        final Map<String, String> params = formatted ? Map.of("formatted", "true") : null;
        return get(token, params, WaifuFile.class);
    }

    public GenericSuccess deleteFile(final String token) {
        return deleteRaw(token);
    }

    public byte[] getFile(final String token, final String password) {
        final WaifuFile info = fileInfo(token, false);
        return getBytes(info.url(), password);
    }

    public byte[] getFileByFilename(final String filename, final String password) {
        return getBytes(baseUrl() + "/f/" + filename, password);
    }

    public WaifuFile modifyEntry(final String token, final ModifyEntryPayload opts) {
        return patchJson(token, opts, WaifuFile.class);
    }

    private HttpRequest.BodyPublisher buildMultipartBody(final String boundary, final byte[] fileBytes,
                                                         final String filename, final String password) {
        final List<byte[]> parts = new ArrayList<>();
        final String sep = "--" + boundary + "\r\n";

        parts.add(sep.getBytes(StandardCharsets.UTF_8));
        parts.add(("Content-Disposition: form-data; name=\"file\"; filename=\"" + filename + "\"\r\n")
                .getBytes(StandardCharsets.UTF_8));
        parts.add("Content-Type: application/octet-stream\r\n\r\n".getBytes(StandardCharsets.UTF_8));
        parts.add(fileBytes);
        parts.add("\r\n".getBytes(StandardCharsets.UTF_8));

        if (password != null) {
            parts.add(sep.getBytes(StandardCharsets.UTF_8));
            parts.add("Content-Disposition: form-data; name=\"password\"\r\n\r\n".getBytes(StandardCharsets.UTF_8));
            parts.add(password.getBytes(StandardCharsets.UTF_8));
            parts.add("\r\n".getBytes(StandardCharsets.UTF_8));
        }

        parts.add(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(parts);
    }
}
