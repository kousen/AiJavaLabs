package com.kousenit;

import static com.kousenit.OpenAiRecords.*;
import static com.kousenit.OpenAiRecords.ModelList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class OpenAiServiceTest {
    private final OpenAiService service = new OpenAiService();

    @Test
    void listModels() {
        List<String> models = service.listModels().data().stream()
                .map(ModelList.Model::id)
                .sorted()
                .toList();

        models.forEach(System.out::println);
        assertTrue(new HashSet<>(models).containsAll(List.of("dall-e-3", "gpt-4.1-nano", "tts-1", "whisper-1")));
    }

    @Test
    void list_and_delete_VectorStores() {
        VectorStoreList vectorStoreList = service.listVectorStores();
        vectorStoreList.data().stream()
                .peek(System.out::println)
                .map(VectorStore::id)
                .forEach(service::deleteVectorStore);
    }
}
