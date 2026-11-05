package com.kousenit.demos;

import static org.assertj.core.api.Assertions.assertThat;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Proves what memory does to the request, with no model and no key: the scripted model
 * records the messages list LangChain4j sent on each call.
 */
class ConversationDemoTest {

    @Test
    void without_memory_each_request_carries_one_message() {
        var model = new RecordingModel();
        var assistant = ConversationDemo.stateless(model);

        assistant.chat("My name is Inigo Montoya.");
        assistant.chat("What's my name?");

        assertThat(model.requests).hasSize(2);
        assertThat(model.requests.get(1)).singleElement().isInstanceOf(UserMessage.class);
    }

    @Test
    void with_memory_the_second_request_carries_the_first_exchange() {
        var model = new RecordingModel();
        var assistant = ConversationDemo.withMemory(model);

        assistant.chat("My name is Inigo Montoya.");
        assistant.chat("What's my name?");

        List<ChatMessage> second = model.requests.get(1);
        assertThat(second).hasSize(3);
        assertThat(second.get(0)).isInstanceOf(UserMessage.class);
        assertThat(second.get(1))
                .isInstanceOf(AiMessage.class)
                .extracting("text")
                .isEqualTo("reply 1");
        assertThat(second.get(2)).isInstanceOf(UserMessage.class);
    }

    @Test
    void the_window_drops_the_oldest_exchanges() {
        var model = new RecordingModel();
        var assistant = ConversationDemo.withMemory(model);

        for (int i = 1; i <= 8; i++) {
            assistant.chat("message " + i); // each call adds a user and an assistant message
        }

        List<String> eighth =
                model.requests.get(7).stream().map(Object::toString).toList();
        // Unbounded, this request would carry 15 messages. The window held it to the last ten plus the new one.
        assertThat(eighth).hasSizeLessThan(15);
        assertThat(eighth).noneMatch(m -> m.contains("\"message 1\"") || m.contains("\"message 2\""));
        assertThat(eighth.get(0)).contains("message 3");
        assertThat(eighth.get(eighth.size() - 1)).contains("message 8");
    }

    /** Records each request's messages and answers "reply N". */
    static class RecordingModel implements ChatModel {
        final List<List<ChatMessage>> requests = new ArrayList<>();

        @Override
        public ChatResponse chat(ChatRequest request) {
            requests.add(List.copyOf(request.messages()));
            return ChatResponse.builder()
                    .aiMessage(AiMessage.from("reply " + requests.size()))
                    .build();
        }
    }
}
