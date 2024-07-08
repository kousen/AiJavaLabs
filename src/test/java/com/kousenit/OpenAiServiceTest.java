package com.kousenit;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static com.kousenit.OpenAiRecords.ModelList;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiServiceTest {
    private final OpenAiService service = new OpenAiService();

    @Test
    void listModels() {
        List<String> models = service.listModels().data().stream()
                .map(ModelList.Model::id)
                .sorted()
                .toList();

        models.forEach(System.out::println);
        assertTrue(new HashSet<>(models).containsAll(
                List.of("dall-e-3", "gpt-3.5-turbo", "gpt-4o",
                        "tts-1", "whisper-1")));
    }
}