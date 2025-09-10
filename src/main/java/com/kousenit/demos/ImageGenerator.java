package com.kousenit.demos;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

public class ImageGenerator {
    public static void main(String[] args) {
        try (Client client = new Client.Builder()
                .apiKey(System.getenv("GOOGLEAI_API_KEY"))
                .build()) {
            var response = client.models.generateContent(
                    "gemini-2.5-flash-image-preview",  // nano banana
                    """
                    Yankee Stadium baseball field
                    set on fire by a dragon
                    wearing a T-shirt bearing a
                    logo similar to the Boston Red Sox
                    """,
                    GenerateContentConfig.builder()
                            .responseModalities("TEXT", "IMAGE")
                            .build());

            for (Part part : Objects.requireNonNull(response.parts())) {
                if (part.inlineData().isPresent()) {
                    var blob = part.inlineData().get();
                    if (blob.data().isPresent()) {
                        try {
                            Files.write(Paths.get("stadium.png"), blob.data().get());
                            break;
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }
}
