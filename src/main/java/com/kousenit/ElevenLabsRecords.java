package com.kousenit;

import java.util.List;

public class ElevenLabsRecords {
    public record VoiceList(List<Voice> voices) {}

    public record Voice(String voiceId, String name, String category) {}

    /** The request body for text-to-speech; model_id is optional on the API side, so this keeps it explicit. */
    public record SpeechRequest(String text, String modelId) {}
}
