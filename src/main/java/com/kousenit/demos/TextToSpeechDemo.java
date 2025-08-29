package com.kousenit.demos;

import com.kousenit.TextToSpeechService;
import java.nio.file.Path;

/**
 * Demo for Text-to-Speech generation using OpenAI's TTS API.
 * Creates an MP3 file from text input - fast with gpt-4o-mini-tts.
 */
public class TextToSpeechDemo {
    public static void main(String[] args) {
        System.out.println("=== Text-to-Speech Demo ===\n");
        
        TextToSpeechService service = new TextToSpeechService();
        
        String text = """
            Welcome to the AI Java Labs training course!
            Today we'll explore how to integrate AI services
            into Java applications using LangChain4j.
            """;
        
        System.out.println("Converting text to speech...");
        System.out.println("Text: " + text.trim());
        
        Path audioFile = service.generateMp3(
            "gpt-4o-mini-tts",  // Fast TTS model
            text,
            "nova"  // Voice options: nova, alloy, echo, fable, onyx, shimmer
        );
        
        System.out.println("\n✓ Audio file created: " + audioFile.toAbsolutePath());
        System.out.println("  You can play this file with any audio player");
        
        System.out.println("\n--- Demo Complete ---");
    }
}