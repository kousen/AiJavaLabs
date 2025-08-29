package com.kousenit;

import com.kousenit.demos.ResponsesApiDemo;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test for ResponsesApiDemo - demonstrates the new Responses API.
 * Note: This API may still be in preview/beta and might not be available.
 */
class ResponsesApiDemoTest {
    
    @Test
    @Tag(TestCategories.DEMO)
    @Tag(TestCategories.CHEAP)
    @EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".*")
    void testResponsesApiDemo() {
        // This test runs the entire demo
        // If the Responses API isn't available yet, it will catch the exception
        // and print an informative message
        assertDoesNotThrow(() -> {
            System.out.println("""
                
                === Running Responses API Demo Test ===
                Note: This tests the new Responses API which may still be in preview.
                If it fails, the API might not be available yet.
                """);
            
            // Run the main method which includes both simple and web search demos
            ResponsesApiDemo.main(new String[]{});
        });
    }
    
    @Test
    @Tag(TestCategories.LOCAL)
    void testResponsesApiDemoStructure() {
        // This is a simple local test to verify the class exists and can be instantiated
        assertDoesNotThrow(() -> {
            ResponsesApiDemo demo = new ResponsesApiDemo();
            System.out.println("ResponsesApiDemo class structure is valid");
        });
    }
}