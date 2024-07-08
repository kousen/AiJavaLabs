package com.kousenit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OpenAiRecords {
    // Listing the models
    public record ModelList(List<Model> data) {
        public record Model(
                String id,
                long created,
                @SerializedName("owned_by") String ownedBy) {
        }
    }

    // Generating images
    public record ImageRequest(
            String model,
            String prompt,
            Integer n,
            String quality,
            String responseFormat,
            String size,
            String style
            ) {}

    public record ImageResponse(
            long created,
            List<Image> data) {
        public record Image(
                String url,
                String revisedPrompt) {}
    }
}