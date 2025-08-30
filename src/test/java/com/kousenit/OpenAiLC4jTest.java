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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

public class OpenAiLC4jTest {

    private final ChatModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .logRequests(true)
            .logResponses(true)
            .modelName("gpt-5-nano")
            .build();

    @Test
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.OPENAI)
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void openai() {
        System.out.println(model.chat("When did your training data end?"));
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "PERPLEXITY_API_KEY", matches = ".+")
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.QUICK)
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
    @EnabledIfEnvironmentVariable(named = "GOOGLEAI_API_KEY", matches = ".+")
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.QUICK)
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
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.OPENAI)
    @Tag(TestCategories.EXPENSIVE)
    @Tag(TestCategories.SLOW)
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
