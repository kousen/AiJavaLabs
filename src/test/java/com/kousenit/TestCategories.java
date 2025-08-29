package com.kousenit;

/**
 * JUnit 5 test categories for managing test execution.
 * Use these tags with @Tag annotation to categorize tests.
 */
public interface TestCategories {
    String INTEGRATION = "integration";  // Tests that call external APIs
    String LOCAL = "local";              // Tests using local Ollama
    String EXPENSIVE = "expensive";      // Tests using costly models (DALL-E, GPT-5)
    String CHEAP = "cheap";              // Tests using low-cost models
    String QUICK = "quick";              // Fast tests good for demos
    String SLOW = "slow";                // Tests that take significant time
    String OPENAI = "openai";            // Tests requiring OpenAI API
    String DEMO = "demo";                // Good for live demonstrations
}