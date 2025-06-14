package com.kousenit;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiImageModelName;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

public class OpenAiLC4jTest {

    private final ChatModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .logRequests(true)
            .logResponses(true)
            .modelName(OpenAiChatModelName.GPT_4_1_NANO)
            .build();

    @Test
    void openai() {
        System.out.println(model.chat("When did your training data end?"));
    }

    @Test
    void perplexity_via_openai() {
        ChatModel perplexity = OpenAiChatModel.builder()
                .apiKey(System.getenv("PERPLEXITY_API_KEY"))
                .baseUrl("https://api.perplexity.ai")
                .modelName("sonar")
                .logRequests(true)
                .logResponses(true)
                .build();

        System.out.println(
                perplexity.chat(
                        """
                        What are today's top news stories
                        in the AI field?
                        """));
    }

    @Test
    void gemini_from_lc4j() {
        ChatModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLEAI_API_KEY"))
                .modelName("gemini-2.0-flash-exp")
                .build();

        System.out.println(
                gemini.chat(
                        """
                        What are today's top news stories
                        in the AI field?
                        """));
    }

    @Test
    void generateImage() {
        ImageModel model = OpenAiImageModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName(OpenAiImageModelName.DALL_E_3)
                .build();

        Response<Image> response = model.generate(
                """
                A warrior cat rides a
                dragon into battle.
                """);
        System.out.println(response);
    }
}
