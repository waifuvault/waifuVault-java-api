package moe.waifuvault.mocks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.ToNumberPolicy;

import java.net.http.HttpResponse;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

public final class MockResponses {

    private static final Gson GSON = new GsonBuilder()
            .setObjectToNumberStrategy(ToNumberPolicy.LONG_OR_DOUBLE)
            .create();

    @SuppressWarnings("unchecked")
    public static HttpResponse<Object> json(final int status, final Object data) {
        final HttpResponse<Object> response = mock(HttpResponse.class);
        lenient().when(response.statusCode()).thenReturn(status);
        lenient().when(response.body()).thenReturn(GSON.toJson(data));
        return response;
    }

    @SuppressWarnings("unchecked")
    public static HttpResponse<Object> bytes(final int status, final byte[] data) {
        final HttpResponse<Object> response = mock(HttpResponse.class);
        lenient().when(response.statusCode()).thenReturn(status);
        lenient().when(response.body()).thenReturn(data);
        return response;
    }

    private MockResponses() {
    }
}
