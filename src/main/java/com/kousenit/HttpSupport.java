package com.kousenit;

import java.net.http.HttpResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Status-code handling shared by the raw HTTP services.
 *
 * A failed request still returns a body, and for OpenAI, OpenRouter, and Anthropic that body is
 * JSON of the form {"error": {"message": ...}}. Feeding it to readValue produces a confusing
 * mapping error, so check the status first and surface the provider's own message.
 */
public final class HttpSupport {
    private static final ObjectMapper MAPPER = JsonMapper.builder().build();

    private HttpSupport() {}

    /** Returns the response when it is 2xx; otherwise throws with the provider's error message. */
    public static HttpResponse<String> requireSuccess(HttpResponse<String> response) {
        int status = response.statusCode();
        if (status / 100 == 2) {
            return response;
        }
        // 429 responses often say how long to wait; a retry loop would read this header
        String retryAfter = response.headers()
                .firstValue("Retry-After")
                .map(seconds -> " (Retry-After: %ss)".formatted(seconds))
                .orElse("");
        throw new ApiException(
                status,
                "HTTP %d from %s: %s%s".formatted(status, response.uri(), errorMessage(response.body()), retryAfter));
    }

    /** The message inside an {"error": {...}} envelope, or the raw body when there is none. */
    static String errorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "(empty body)";
        }
        try {
            JsonNode message = MAPPER.readTree(body).at("/error/message");
            return message.isMissingNode() ? body : message.asString();
        } catch (JacksonException e) {
            return body; // not JSON at all, for example an HTML page from a proxy
        }
    }
}
