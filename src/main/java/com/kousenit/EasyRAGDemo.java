package com.kousenit;

import static com.kousenit.Utils.*;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocuments;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import java.util.List;

public class EasyRAGDemo {
    public interface Assistant {
        String chat(String userMessage);
    }

    private static ContentRetriever createContentRetriever(List<Document> documents) {
        // Here, we create and empty in-memory store for our documents and their embeddings.
        InMemoryEmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        // Here, we are ingesting our documents into the store.
        // Under the hood, a lot of "magic" is happening, but we can ignore it for now.
        EmbeddingStoreIngestor.ingest(documents, embeddingStore);

        // Lastly, let's create a content retriever from an embedding store.
        return EmbeddingStoreContentRetriever.from(embeddingStore);
    }

    public static void main(String[] args) {
        List<Document> documents = loadDocuments(toPath("documents/"), glob("*.txt"));

        ChatLanguageModel chatLanguageModel = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-4o-mini")
                .build();

        // Second, let's create an assistant that will have access to our documents
        Assistant assistant = AiServices.builder(Assistant.class)
                .chatLanguageModel(chatLanguageModel)
                .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
                .contentRetriever(createContentRetriever(documents))
                .build();

        // Lastly, let's start the conversation with the assistant.
        startConversationWith(assistant);
        System.exit(0);
    }
}
