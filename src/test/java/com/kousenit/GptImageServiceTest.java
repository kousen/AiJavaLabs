package com.kousenit;

import static com.kousenit.OpenAiRecords.*;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class GptImageServiceTest {
    private final GptImageService service = new GptImageService();

    @Test
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.OPENAI)
    @Tag(TestCategories.EXPENSIVE)
    @Tag(TestCategories.SLOW)
    void generate_image() {
        var imageRequest = new ImageRequest(
                "gpt-image-2", "Generate a picture of cats playing cards", 1, "medium", "png", "1024x1024");
        ImageResponse imageResponse = service.generateImage(imageRequest);
        System.out.println(imageResponse.data().getFirst().revisedPrompt());
        System.out.println("Base64 image bytes: "
                + imageResponse.data().getFirst().b64Json().length());
    }
}
