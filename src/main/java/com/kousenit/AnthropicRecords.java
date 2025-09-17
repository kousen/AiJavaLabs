package com.kousenit;

import java.time.Instant;
import java.util.List;

public class AnthropicRecords {

    public record ModelsResponse(
            List<Model> data,
            String firstId,
            boolean hasMore,
            String lastId
    ) {}

    public record Model(
            Instant createdAt,
            String displayName,
            String id,
            String type
    ) {}
}
