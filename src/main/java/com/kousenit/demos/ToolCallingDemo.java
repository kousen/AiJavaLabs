package com.kousenit.demos;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.SystemMessage;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

/**
 * Demonstrates LangChain4j tool calling with ordinary Java methods.
 *
 * <p>The model decides when to call these tools. LangChain4j turns the
 * annotated Java methods into tool specifications, executes the selected
 * method, and sends the result back to the model so it can answer the user.
 */
public class ToolCallingDemo {

    interface TravelAssistant {
        @SystemMessage(
                """
                You are a practical travel assistant.
                Use the provided Java tools whenever the user asks about
                weather, distance, drive time, or estimated travel cost.
                Explain which tool results shaped your answer.
                """)
        String chat(String message);
    }

    public static class TravelTools {
        private static final Map<String, Weather> WEATHER = Map.of(
                "hartford", new Weather("Hartford, CT", 72, "partly cloudy"),
                "boston", new Weather("Boston, MA", 66, "cool and breezy"),
                "new york", new Weather("New York, NY", 75, "warm and humid"),
                "portland", new Weather("Portland, ME", 61, "foggy near the water"));

        private static final Map<Route, Integer> DISTANCES = Map.of(
                new Route("hartford", "boston"), 102,
                new Route("hartford", "new york"), 116,
                new Route("hartford", "portland"), 198,
                new Route("boston", "portland"), 112);

        @Tool("Get a small weather summary for a city")
        public String weather(@P("city name, such as Hartford or Boston") String city) {
            Weather weather = WEATHER.get(normalize(city));
            if (weather == null) {
                return "No weather data available for " + city;
            }
            return "%s: %d F and %s".formatted(weather.city(), weather.temperatureF(), weather.summary());
        }

        @Tool("Estimate driving distance in miles between two supported cities")
        public int drivingDistance(@P("starting city") String from, @P("destination city") String to) {
            Route route = new Route(normalize(from), normalize(to));
            Integer miles = DISTANCES.get(route);
            if (miles != null) {
                return miles;
            }
            miles = DISTANCES.get(route.reversed());
            if (miles != null) {
                return miles;
            }
            throw new IllegalArgumentException("Unsupported route: " + from + " to " + to);
        }

        @Tool("Estimate fuel cost for a trip")
        public String fuelCost(
                @P("driving distance in miles") int miles,
                @P("vehicle fuel economy in miles per gallon") double mpg,
                @P("fuel price per gallon") double pricePerGallon) {
            BigDecimal gallons = BigDecimal.valueOf(miles).divide(BigDecimal.valueOf(mpg), 2, RoundingMode.HALF_UP);
            BigDecimal cost =
                    gallons.multiply(BigDecimal.valueOf(pricePerGallon)).setScale(2, RoundingMode.HALF_UP);
            return "%s gallons, about $%s".formatted(gallons, cost);
        }

        private static String normalize(String value) {
            return value.toLowerCase().replaceAll(",.*", "").trim();
        }

        private record Weather(String city, int temperatureF, String summary) {}

        private record Route(String from, String to) {
            Route reversed() {
                return new Route(to, from);
            }
        }
    }

    public static void main(String[] args) {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("OPENAI_API_KEY is not set. Set it before running this demo.");
            return;
        }

        ChatModel model = OpenAiChatModel.builder()
                .apiKey(apiKey)
                .modelName("gpt-5-nano")
                .temperature(0.0)
                .logRequests(false)
                .logResponses(false)
                .build();

        TravelAssistant assistant = AiServices.builder(TravelAssistant.class)
                .chatModel(model)
                .tools(new TravelTools())
                .build();

        String question =
                """
                I am driving from Hartford to Boston.
                What is the weather like at both ends,
                how far is the trip, and what would fuel cost
                at 30 mpg and $3.65 per gallon?
                """;

        System.out.println("Question:\n" + question.strip());
        System.out.println("\nAnswer:\n" + assistant.chat(question));
    }
}
