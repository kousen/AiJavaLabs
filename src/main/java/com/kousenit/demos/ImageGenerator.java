package com.kousenit.demos;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public class ImageGenerator {
    private final String modelName = "gemini-2.5-flash-image-preview";
    private static final String API_KEY = System.getenv("GOOGLEAI_API_KEY");
    private final GenerateContentConfig config = GenerateContentConfig.builder()
            .responseModalities("TEXT", "IMAGE")
            .build();

    public void generateImage(String prompt) {
        try (Client client = new Client.Builder()
                .apiKey(API_KEY)
                .build()) {
            var response = client.models.generateContent(
                    modelName,
                    prompt,
                    config);
            saveImage(response);
        }
    }

    public void editImage(String fileName, String newPrompt) {
        try (Client client = new Client.Builder()
                .apiKey(API_KEY)
                .build()) {
            var path = Path.of(fileName);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("File does not exist: " + fileName);
            }
            var response = client.models.generateContent(
                    modelName,
                    Content.fromParts(
                            Part.fromBytes(Files.readAllBytes(path), "image/png"),
                            Part.fromText(newPrompt)
                    ),
                    config);
            saveImage(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void saveImage(GenerateContentResponse response) {
        for (Part part : Objects.requireNonNull(response.parts())) {
            if (part.inlineData().isPresent()) {
                var blob = part.inlineData().get();
                if (blob.data().isPresent()) {
                    try {
                        Files.write(Paths.get("generated_%s.png".formatted(System.currentTimeMillis())),
                                blob.data().get());
                        break;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }


    public static void main(String[] args) {
        var generator = new ImageGenerator();

        generator.generateImage("""
                Yankee Stadium baseball field
                set on fire by a dragon
                wearing a T-shirt bearing a
                logo similar to the Boston Red Sox
                """);

        generator.editImage("stadium.png", """
                Focus in on the dragon,
                from the point of view of
                a batter standing at home plate.
                """);
    }
}
