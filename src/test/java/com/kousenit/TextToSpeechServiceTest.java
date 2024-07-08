package com.kousenit;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextToSpeechServiceTest {
    @Test
    void testGenerateMp3() {
        var service = new TextToSpeechService();
        byte[] result = service.generateMp3(
                "tts-1",
                """
                Now that I know how to generate audio from text,
                I can use this feature in my applications.
                """,
                "fable"
        );

        FileUtils.saveBinaryFile(result, "test.mp3");

        assertNotNull(result);
        assertTrue(result.length > 0);
    }
}