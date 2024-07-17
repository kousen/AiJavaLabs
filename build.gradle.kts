plugins {
    id("java")
}

group = "com.kousenit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // LangChain4j Easy RAG example
    implementation("dev.langchain4j:langchain4j:0.32.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.32.0")
    implementation("dev.langchain4j:langchain4j-easy-rag:0.32.0") {
        exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    }

    // Gson parser
    implementation("com.google.code.gson:gson:2.11.0")

    // Logging
    implementation("org.slf4j:slf4j-simple:2.0.7")

    // Testing libraries
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.26.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Security issue with mime4j from langchain4j
    implementation("org.apache.james:apache-mime4j-core:0.8.11")
}

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")
}