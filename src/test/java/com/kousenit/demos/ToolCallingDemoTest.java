package com.kousenit.demos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.exception.ToolErrorVisibleToLlmException;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.json.JsonObjectSchema;
import dev.langchain4j.model.chat.response.ChatResponse;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Runs the tool-calling loop with a scripted model instead of OpenAI, so these
 * tests need no API key. The scripted model asks for one tool call with the
 * given arguments, then answers with whatever result LangChain4j sent back.
 */
class ToolCallingDemoTest {

    @Test
    void travel_tools_can_be_called_directly() {
        var tools = new ToolCallingDemo.TravelTools();

        assertThat(tools.weather("Hartford")).contains("Hartford, CT");
        assertThat(tools.drivingDistance("Hartford", "Boston")).isEqualTo(102);
        assertThat(tools.fuelCost(102, 30, 3.65)).contains("$12.41");
    }

    @Test
    void unsupported_route_is_an_error_the_model_may_see() {
        var tools = new ToolCallingDemo.TravelTools();

        assertThatThrownBy(() -> tools.drivingDistance("Hartford", "Chicago"))
                .isInstanceOf(ToolErrorVisibleToLlmException.class)
                .hasMessageContaining("Unsupported route");
    }

    @Test
    void parameters_with_defaults_are_not_required_in_the_schema() {
        var model = new ScriptedModel("fuelCost", "{\"miles\":102,\"mpg\":30,\"pricePerGallon\":3.65}");

        ToolCallingDemo.assistant(model).chat("Hartford to Boston at 30 mpg and $3.65");

        JsonObjectSchema fuelCost = model.toolSpecifications.stream()
                .filter(spec -> spec.name().equals("fuelCost"))
                .map(spec -> spec.parameters())
                .findFirst()
                .orElseThrow();
        assertThat(fuelCost.required()).containsExactly("miles");
        assertThat(fuelCost.properties()).containsKeys("miles", "mpg", "pricePerGallon");
    }

    @Test
    void omitted_arguments_with_defaults_are_filled_in() {
        var model = new ScriptedModel("fuelCost", "{\"miles\":102}");

        ToolCallingDemo.assistant(model).chat("What would fuel cost for Hartford to Boston?");

        assertThat(model.toolResults).containsExactly("3.40 gallons, about $12.41");
    }

    @Test
    void omitted_required_argument_is_reported_to_the_model_instead_of_failing() {
        var model = new ScriptedModel("fuelCost", "{\"mpg\":30,\"pricePerGallon\":3.65}");

        String answer = ToolCallingDemo.assistant(model).chat("What would fuel cost?");

        assertThat(answer).isEqualTo("final answer");
        assertThat(model.toolResults).singleElement().asString().contains("miles", "missing");
    }

    @Test
    void tool_errors_written_for_the_model_reach_it() {
        var model = new ScriptedModel("drivingDistance", "{\"from\":\"Hartford\",\"to\":\"Chicago\"}");

        ToolCallingDemo.assistant(model).chat("How far is Hartford to Chicago?");

        assertThat(model.toolResults).singleElement().asString().contains("Unsupported route");
    }

    @Test
    void omitted_string_argument_arrives_as_null_and_is_reported() {
        var model = new ScriptedModel("weather", "{}");

        ToolCallingDemo.assistant(model).chat("What is the weather?");

        assertThat(model.toolResults).singleElement().asString().contains("city name is required");
    }

    /** First call: ask for one tool. Second call: record the tool result and answer. */
    static class ScriptedModel implements ChatModel {
        final List<ToolSpecification> toolSpecifications = new ArrayList<>();
        final List<String> toolResults = new ArrayList<>();
        private final String toolName;
        private final String arguments;

        ScriptedModel(String toolName, String arguments) {
            this.toolName = toolName;
            this.arguments = arguments;
        }

        @Override
        public ChatResponse chat(ChatRequest request) {
            toolSpecifications.clear();
            toolSpecifications.addAll(request.toolSpecifications());
            List<ChatMessage> messages = request.messages();
            if (messages.get(messages.size() - 1) instanceof ToolExecutionResultMessage result) {
                toolResults.add(result.text());
                return ChatResponse.builder()
                        .aiMessage(AiMessage.from("final answer"))
                        .build();
            }
            ToolExecutionRequest toolCall = ToolExecutionRequest.builder()
                    .id("call_1")
                    .name(toolName)
                    .arguments(arguments)
                    .build();
            return ChatResponse.builder().aiMessage(AiMessage.from(toolCall)).build();
        }
    }
}
