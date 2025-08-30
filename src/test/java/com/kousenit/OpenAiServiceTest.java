package com.kousenit;

import static com.kousenit.OpenAiRecords.*;
import static com.kousenit.OpenAiRecords.ModelList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.Test;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiServiceTest {
    private final OpenAiService service = new OpenAiService();

    @Test
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.OPENAI)
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void listModels() {
        List<String> models = service.listModels().data().stream()
                .map(ModelList.Model::id)
                .sorted()
                .toList();

        models.forEach(System.out::println);
        assertTrue(new HashSet<>(models).containsAll(
                List.of("dall-e-3", "gpt-5-nano", "gpt-4o-mini-tts", "whisper-1")));
    }

    @Test
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.OPENAI)
    @Tag(TestCategories.CHEAP)
    void list_and_delete_VectorStores() {
        VectorStoreList vectorStoreList = service.listVectorStores();
        vectorStoreList.data().stream()
                .peek(System.out::println)
                .map(VectorStore::id)
                .forEach(service::deleteVectorStore);
    }
}
