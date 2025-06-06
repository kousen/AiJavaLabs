package com.kousenit;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import java.util.Map;

public class OpenAiRecords {
    // Listing the models
    public record ModelList(List<Model> data) {
        public record Model(String id, long created, @SerializedName("owned_by") String ownedBy) {}
    }

    // Generating images
    public record ImageRequest(
            String model, String prompt, Integer n, String quality, String responseFormat, String size, String style) {}

    public record ImageResponse(long created, List<Image> data) {
        public record Image(String url, String revisedPrompt) {}
    }

    // Vector stores
    public record VectorStoreList(
            @SerializedName("object") String object,
            @SerializedName("data") List<VectorStore> data,
            @SerializedName("first_id") String firstId,
            @SerializedName("last_id") String lastId,
            @SerializedName("has_more") boolean hasMore) {}

    public record VectorStore(
            @SerializedName("id") String id,
            @SerializedName("object") String object,
            @SerializedName("name") String name,
            @SerializedName("status") String status,
            @SerializedName("usage_bytes") long usageBytes,
            @SerializedName("created_at") long createdAt,
            @SerializedName("file_counts") FileCounts fileCounts,
            @SerializedName("metadata") Map<String, Object> metadata,
            @SerializedName("expires_after") Object expiresAfter,
            @SerializedName("expires_at") Object expiresAt,
            @SerializedName("last_active_at") long lastActiveAt) {}

    public record FileCounts(
            @SerializedName("in_progress") int inProgress,
            @SerializedName("completed") int completed,
            @SerializedName("failed") int failed,
            @SerializedName("cancelled") int cancelled,
            @SerializedName("total") int total) {}
}
