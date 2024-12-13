package com.kousenit;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiImageModelName;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;

public class OpenAiLC4jTest {

    private final ChatLanguageModel model = OpenAiChatModel.builder()
            .apiKey(System.getenv("OPENAI_API_KEY"))
            .modelName(OpenAiChatModelName.GPT_4_O_MINI)
            .build();

    @Test
    void openai() {
        System.out.println(model.generate("When did your training data end?"));
    }

    @Test
    void perplexity_via_openai() {
        ChatLanguageModel perplexity = OpenAiChatModel.builder()
                .apiKey(System.getenv("PERPLEXITY_API_KEY"))
                .baseUrl("https://api.perplexity.ai")
                .modelName("llama-3.1-sonar-small-128k-online")
                .logRequests(true)
                .logResponses(true)
                .build();

        System.out.println(perplexity.generate("""
                        What are today's top news stories
                        in the AI field?
                        """));
    }

    @Test
    void gemini_via_openai() {
        ChatLanguageModel gemini = OpenAiChatModel.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/openai/")
                .apiKey(System.getenv("GOOGLEAI_API_KEY"))
                .modelName("gemini-2.0-flash-exp")
                .logRequests(true)
                .logResponses(true)
                .build();

        System.out.println(gemini.generate("""
                        What are today's top news stories
                        in the AI field?
                        """));
    }

    @Test
    void gemini_from_lc4j() {
        ChatLanguageModel gemini = GoogleAiGeminiChatModel.builder()
                .apiKey(System.getenv("GOOGLEAI_API_KEY"))
                .modelName("gemini-2.0-flash-exp")
                .build();

        System.out.println(gemini.generate("""
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

        Response<Image> response = model.generate("""
                A warrior cat rides a
                dragon into battle.
                """);
        System.out.println(response);

    }
}
