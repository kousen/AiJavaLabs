package com.kousenit.demos;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.kousenit.demos.LMStudioRecords.*;

class LMStudioServiceTest {

    @Test
    void getModels() {
        LMStudioService service = new LMStudioService();
        List<LMStudioService.Model> models = service.getModels();
        models.forEach(System.out::println);
    }

    @Test
    void simpleChat() {
        LMStudioService service = new LMStudioService();
        String question = """
            LM Studio provides MCP support via a config file.
            Once I've configured an MCP service inside it,
            how do I use it? Presumably I need a model with
            tool support, but what do I do beyond that?
            """;
        ChatCompletionResponse response = service.chat(
                new ChatRequest(
                        "gpt-oss-20b",
                        List.of(
                                // new Message("system", "Always answer in rhymes.", null, null),
                                new Message("user", question, null, null)
                        ),
                        0.7,
                        -1,
                        false
                )
        );
        System.out.println(response.choices().getFirst().message().content());
        System.out.println(response.model());
        System.out.println(response.usage());
    }

}