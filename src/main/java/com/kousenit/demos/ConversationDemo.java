package com.kousenit.demos;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import java.util.Objects;

/**
 * The same statelessness demo as the raw Ollama lab, now through LangChain4j.
 *
 * <p>An AI Service without memory sends exactly one message per call, so the model cannot
 * know your name on the second call. Add a {@code ChatMemory} and the service appends every
 * user and assistant message to a list and resends the list each time. That is all "memory"
 * is: the messages list from the raw lab, plus a policy for trimming it.
 */
public class ConversationDemo {
    private static final String OPENROUTER_BASE_URL = "https://openrouter.ai/api/v1";

    interface Assistant {
        String chat(String message);
    }

    /** Every call is independent: the request carries only the message you pass in. */
    static Assistant stateless(ChatModel model) {
        return AiServices.builder(Assistant.class).chatModel(model).build();
    }

    /** The service keeps the last ten messages and resends them with every call. */
    static Assistant withMemory(ChatModel model) {
        return AiServices.builder(Assistant.class)
                .chatModel(model)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .build();
    }

    public static void main(String[] args) {
        String apiKey = Objects.requireNonNull(
                System.getenv("OPENROUTER_API_KEY"), "Set the OPENROUTER_API_KEY environment variable");

        ChatModel model = OpenAiChatModel.builder()
                .baseUrl(OPENROUTER_BASE_URL)
                .apiKey(apiKey)
                .modelName("openai/gpt-6-luna")
                .logRequests(false) // true shows the messages list growing with memory on
                .build();

        System.out.println("=== Without memory ===");
        Assistant forgetful = stateless(model);
        System.out.println(forgetful.chat("My name is Inigo Montoya."));
        System.out.println(forgetful.chat("What's my name? Answer in one short sentence."));

        System.out.println("\n=== With MessageWindowChatMemory ===");
        Assistant attentive = withMemory(model);
        System.out.println(attentive.chat("My name is Inigo Montoya."));
        System.out.println(attentive.chat("What's my name? Answer in one short sentence."));
    }
}
