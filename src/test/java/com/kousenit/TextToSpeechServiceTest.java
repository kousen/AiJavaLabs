package com.kousenit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Path;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class TextToSpeechServiceTest {
    @Test
    @Tag(TestCategories.INTEGRATION)
    @Tag(TestCategories.OPENAI)
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void generateMp3() {
        var service = new TextToSpeechService();
        Path result = service.generateMp3(
                "gpt-4o-mini-tts",
                """
                   This is an attempt to generate an audio file
                   from text using the OpenAI API, and
                   write it to a file directly.
                   """,
                "coral");

        assertNotNull(result);
        System.out.println("Generated audio file: " + result.toAbsolutePath());
    }
}
