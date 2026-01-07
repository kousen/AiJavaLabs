package com.kousenit;

import static com.kousenit.AnthropicRecords.*;

import org.junit.jupiter.api.Test;

public class AnthropicServiceTest {

    private final AnthropicService service = new AnthropicService();

    @Test
    void listModels() {
        ModelsResponse modelsResponse = service.listModels();
        modelsResponse.data().forEach(System.out::println);
    }
}
