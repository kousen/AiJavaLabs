package com.kousenit.demos;

import com.kousenit.OpenAiRecords.EmbeddingResponse;
import com.kousenit.OpenAiService;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Retrieval-augmented generation with nothing hidden: chunk a document, embed the chunks
 * with one raw HTTP call, rank them against the question with a dot product, and paste the
 * best ones into the prompt. {@code EasyRAGDemo} does the same thing through LangChain4j.
 */
public class RagUnderTheHoodDemo {
    private static final Path BOOK = Path.of("src/main/resources/documents/pg1661_adventures_of_sherlock_holmes.txt");

    record Scored(String chunk, double score) {}

    /** Paragraphs merged until each chunk is at least minChars long. Crude, and good enough to see RAG work. */
    static List<String> chunk(String text, int minChars) {
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String paragraph : text.split("\\R\\s*\\R")) {
            current.append(paragraph.strip()).append("\n\n");
            if (current.length() >= minChars) {
                chunks.add(current.toString().strip());
                current.setLength(0);
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString().strip());
        }
        return chunks;
    }

    /** Cosine similarity: 1.0 means the same direction, 0.0 means unrelated. This is the whole "vector search". */
    static double cosine(double[] a, double[] b) {
        double dot = 0, normA = 0, normB = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    static List<Scored> topK(double[] question, List<String> chunks, List<double[]> vectors, int k) {
        List<Scored> scored = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            scored.add(new Scored(chunks.get(i), cosine(question, vectors.get(i))));
        }
        scored.sort(Comparator.comparingDouble(Scored::score).reversed());
        return scored.subList(0, Math.min(k, scored.size()));
    }

    static String prompt(String question, List<Scored> context) {
        return """
                Answer the question using only the context below.
                If the answer is not in the context, say so.

                Context:
                %s

                Question: %s
                """
                .formatted(context.stream().map(Scored::chunk).collect(Collectors.joining("\n---\n")), question);
    }

    public static void main(String[] args) {
        String text;
        try {
            text = Files.readString(BOOK);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        // 1. Chunk. The model sees chunks, never the whole book.
        List<String> chunks = chunk(text, 800);

        // 2. Embed every chunk, 100 per request. One vector per chunk comes back.
        OpenAiService openAi = new OpenAiService();
        List<double[]> vectors = new ArrayList<>();
        int tokens = 0;
        for (int from = 0; from < chunks.size(); from += 100) {
            EmbeddingResponse response = openAi.embed(chunks.subList(from, Math.min(from + 100, chunks.size())));
            response.data().forEach(e -> vectors.add(e.embedding()));
            tokens += response.usage().totalTokens();
        }
        System.out.printf(
                "%d chunks, %d dimensions each, %d tokens to embed%n",
                chunks.size(), vectors.getFirst().length, tokens);

        // 3. Embed the question the same way, then rank the chunks with a dot product.
        String question = "Who is Irene Adler, and why does Holmes keep her photograph?";
        double[] q = openAi.embed(List.of(question)).data().getFirst().embedding();
        List<Scored> best = topK(q, chunks, vectors, 3);
        best.forEach(s -> System.out.printf(
                "%.3f  %s...%n", s.score(), s.chunk().replace('\n', ' ').substring(0, 70)));

        // 4. Paste the best chunks into the prompt. That is the "augmented" in RAG.
        ChatModel model = OpenAiChatModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("gpt-6-luna")
                .build();
        System.out.println("\n" + model.chat(prompt(question, best)));
    }
}
