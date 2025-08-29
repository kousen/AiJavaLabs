plugins {
    id("java")
    id("com.diffplug.spotless") version "7.2.1"
}

group = "com.kousenit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

dependencies {
    implementation(platform("dev.langchain4j:langchain4j-bom:1.4.0"))

    // LangChain4j Easy RAG example
    implementation("dev.langchain4j:langchain4j")
    implementation("dev.langchain4j:langchain4j-open-ai")
    implementation("dev.langchain4j:langchain4j-ollama")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini")
    implementation("dev.langchain4j:langchain4j-easy-rag") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }

    // Security fix
    implementation("org.apache.poi:poi-ooxml:5.4.1")

    // Gson parser
    implementation("com.google.code.gson:gson:2.13.1")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.17")

    // Testing libraries
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    //testImplementation(platform("org.junit:junit-bom:6.0.0-RC2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:4.0.0-M1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}

// Test task for local Ollama tests only (free, no API costs)
tasks.register<Test>("testLocal") {
    useJUnitPlatform {
        includeTags("local")
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    description = "Run only local Ollama tests (free)"
    group = "verification"
}

// Test task for cheap API tests
tasks.register<Test>("testCheap") {
    useJUnitPlatform {
        includeTags("cheap")
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    description = "Run only tests using low-cost models"
    group = "verification"
}

// Test task for quick demo tests
tasks.register<Test>("testDemo") {
    useJUnitPlatform {
        includeTags("demo")
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    description = "Run quick tests suitable for live demonstrations"
    group = "verification"
}

// Test task excluding expensive tests
tasks.register<Test>("testNotExpensive") {
    useJUnitPlatform {
        excludeTags("expensive")
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    description = "Run all tests except expensive ones (excludes DALL-E)"
    group = "verification"
}

// Test task for only OpenAI tests
tasks.register<Test>("testOpenAI") {
    useJUnitPlatform {
        includeTags("openai")
    }
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
    description = "Run only OpenAI API tests"
    group = "verification"
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat("2.73.0")
    }

    format("misc") {
        target("*.gradle", "*.gradle.kts", "*.md", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }
}
