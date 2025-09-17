package com.kousenit;

import org.junit.jupiter.api.Test;

import static com.kousenit.AnthropicRecords.*;

public class AnthropicServiceTest {

    private final AnthropicService service = new AnthropicService();

    @Test
    void listModels() {
        ModelsResponse modelsResponse = service.listModels();
        modelsResponse.data().forEach(System.out::println);
    }

}
