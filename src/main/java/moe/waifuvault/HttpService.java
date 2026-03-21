package moe.waifuvault;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class HttpService implements AutoCloseable {

    protected final Gson gson = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    private String baseUrl = "https://waifuvault.moe";
    private final HttpClient httpClient;

    protected HttpService(final HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected String baseUrl() {
        return baseUrl;
    }

    protected void setBaseUrl(final String url) {
        this.baseUrl = url;
    }

    protected <T> HttpResponse<T> send(final HttpRequest request,
                                       final HttpResponse.BodyHandler<T> handler) throws IOException, InterruptedException {
        return httpClient.send(request, handler);
    }

    @Override
    public void close() {
        httpClient.close();
    }
}
