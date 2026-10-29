package com.kousenit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.net.ssl.SSLSession;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class HttpSupportTest {

    @Test
    @Tag(TestCategories.LOCAL)
    void success_passes_the_response_through() {
        HttpResponse<String> ok = stub(200, "{\"data\": []}", Map.of());
        assertThat(HttpSupport.requireSuccess(ok)).isSameAs(ok);
    }

    @Test
    @Tag(TestCategories.LOCAL)
    void failure_surfaces_the_provider_message_and_retry_hint() {
        HttpResponse<String> limited = stub(
                429,
                """
                {"error": {"message": "Rate limit reached for gpt-5-nano", "type": "rate_limit_error"}}
                """,
                Map.of("Retry-After", List.of("20")));

        assertThatThrownBy(() -> HttpSupport.requireSuccess(limited))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("HTTP 429")
                .hasMessageContaining("Rate limit reached for gpt-5-nano")
                .hasMessageContaining("Retry-After: 20s")
                .extracting(e -> ((ApiException) e).isRateLimited())
                .isEqualTo(true);
    }

    @Test
    @Tag(TestCategories.LOCAL)
    void non_json_bodies_are_reported_as_is() {
        assertThat(HttpSupport.errorMessage("<html>Bad Gateway</html>")).isEqualTo("<html>Bad Gateway</html>");
        assertThat(HttpSupport.errorMessage("{\"error\": \"model not found\"}")).contains("model not found");
    }

    // A hand-rolled HttpResponse: enough to exercise the helper without a network
    private static HttpResponse<String> stub(int status, String body, Map<String, List<String>> headers) {
        URI uri = URI.create("https://api.example.com/v1/models");
        return new HttpResponse<>() {
            public int statusCode() {
                return status;
            }

            public HttpRequest request() {
                return HttpRequest.newBuilder(uri).build();
            }

            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            public HttpHeaders headers() {
                return HttpHeaders.of(headers, (a, b) -> true);
            }

            public String body() {
                return body;
            }

            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            public URI uri() {
                return uri;
            }

            public HttpClient.Version version() {
                return HttpClient.Version.HTTP_1_1;
            }
        };
    }
}
