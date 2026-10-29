package com.kousenit;

import static org.assertj.core.api.Assertions.assertThat;

import com.kousenit.ElevenLabsRecords.Voice;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "ELEVENLABS_API_KEY", matches = ".+")
class ElevenLabsServiceTest {
    private final ElevenLabsService service = new ElevenLabsService();

    @Test
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.QUICK)
    void lists_voices() {
        List<Voice> voices = service.listVoices();
        voices.stream().limit(5).forEach(v -> System.out.printf("%s  %s  (%s)%n", v.voiceId(), v.name(), v.category()));
        assertThat(voices).isNotEmpty();
    }

    @Test
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.DEMO)
    void generates_speech_with_a_premade_voice() throws IOException {
        Voice george = service.voiceNamed("George"); // one of ElevenLabs' public premade voices
        Path audio = service.textToSpeech(george.voiceId(), "Java developers already know how to call a REST service.");
        try {
            System.out.println("Generated " + audio.toAbsolutePath() + " (" + Files.size(audio) + " bytes)");
            assertThat(Files.size(audio)).isGreaterThan(10_000);
        } finally {
            Files.deleteIfExists(audio); // keep generated audio out of the repository
        }
    }
}
