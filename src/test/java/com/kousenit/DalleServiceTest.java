package com.kousenit;

import static com.kousenit.OpenAiRecords.*;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class DalleServiceTest {
    private final DalleService service = new DalleService();

    @Test
    void generate_image() {
        var imageRequest = new ImageRequest(
                "dall-e-3", "Generate a picture of cats playing cards", 1, "standard", "url", "1024x1024", "natural");
        ImageResponse imageResponse = service.generateImage(imageRequest);
        System.out.println(imageResponse.data().getFirst().revisedPrompt());
        System.out.println(imageResponse.data().getFirst().url());
    }
}
