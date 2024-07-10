package com.kousenit;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class TextToSpeechServiceTest {
    @Test
    void generateMp3() {
        var service = new TextToSpeechService();
        Path result = service.generateMp3(
                "tts-1",
                """
                   This is an attempt to generate an audio file
                   from text using the OpenAI API, and
                   write it to a file directly.
                   """,
                "alloy"
        );

        assertNotNull(result);
        System.out.println("Generated audio file: " + result.toAbsolutePath());
    }
}