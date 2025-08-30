package com.kousenit;

import static com.kousenit.OpenAiRecords.*;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class DalleServiceTest {
    private final DalleService service = new DalleService();

    @Test
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.OPENAI)
    @Tag(TestCategories.EXPENSIVE)
    @Tag(TestCategories.SLOW)
    void generate_image() {
        var imageRequest = new ImageRequest(
                "dall-e-3", "Generate a picture of cats playing cards", 1, "standard", "url", "1024x1024", "natural");
        ImageResponse imageResponse = service.generateImage(imageRequest);
        System.out.println(imageResponse.data().getFirst().revisedPrompt());
        System.out.println(imageResponse.data().getFirst().url());
    }
}
