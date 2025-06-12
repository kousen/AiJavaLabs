plugins {
    id("java")
    id("com.diffplug.spotless") version "7.0.4"
}

group = "com.kousenit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("dev.langchain4j:langchain4j-bom:1.0.1"))

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
    testImplementation(platform("org.junit:junit-bom:5.13.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}

spotless {
    java {
        target("src/**/*.java")
        palantirJavaFormat("2.67.0")
    }

    format("misc") {
        target("*.gradle", "*.gradle.kts", "*.md", ".gitignore")
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
    }
}
