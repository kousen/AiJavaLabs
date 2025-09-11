package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;

public class LMStudioTest {
    @Test
    void queryGptOss() {
        ChatModel model = OpenAiChatModel.builder()
                .baseUrl("http://localhost:1234")
                .modelName("gpt-oss-20b")
                .build();
        System.out.println(model.chat("Why is the sky blue?"));
    }
}
