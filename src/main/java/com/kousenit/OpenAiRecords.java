package com.kousenit;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

public class OpenAiRecords {
    // Listing the models. The explicit @JsonProperty shows the mapping once;
    // the mapper's SNAKE_CASE naming strategy would handle it on its own.
    public record ModelList(List<Model> data) {
        public record Model(String id, long created, @JsonProperty("owned_by") String ownedBy) {}
    }

    // Embeddings: a list of strings in, one vector per string out, plus what it cost
    public record EmbeddingRequest(String model, List<String> input) {}

    public record EmbeddingResponse(List<Embedding> data, Usage usage) {
        public record Embedding(int index, double[] embedding) {}

        public record Usage(int promptTokens, int totalTokens) {}
    }

    // Generating images
    public record ImageRequest(
            String model, String prompt, Integer n, String quality, String outputFormat, String size) {}

    public record ImageResponse(long created, List<Image> data) {
        public record Image(String b64Json, String revisedPrompt) {}
    }

    // Vector stores: every snake_case field maps through the naming strategy
    public record VectorStoreList(
            String object, List<VectorStore> data, String firstId, String lastId, boolean hasMore) {}

    public record VectorStore(
            String id,
            String object,
            String name,
            String status,
            long usageBytes,
            long createdAt,
            FileCounts fileCounts,
            Map<String, Object> metadata,
            Object expiresAfter,
            Object expiresAt,
            long lastActiveAt) {}

    public record FileCounts(int inProgress, int completed, int failed, int cancelled, int total) {}
}
