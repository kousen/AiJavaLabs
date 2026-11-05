package com.kousenit.demos;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

@EnabledIf("com.kousenit.demos.LMStudioAvailable#isRunning") // skipped unless LM Studio is on :1234
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
