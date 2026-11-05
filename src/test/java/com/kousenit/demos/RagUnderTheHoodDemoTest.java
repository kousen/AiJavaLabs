package com.kousenit.demos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The Java half of RAG, with no model: chunking, similarity, ranking, and the prompt. */
class RagUnderTheHoodDemoTest {

    @Test
    void cosine_is_one_for_the_same_direction_and_zero_for_unrelated() {
        assertThat(RagUnderTheHoodDemo.cosine(new double[] {1, 2, 3}, new double[] {2, 4, 6}))
                .isCloseTo(1.0, within(1e-9));
        assertThat(RagUnderTheHoodDemo.cosine(new double[] {1, 0}, new double[] {0, 1}))
                .isCloseTo(0.0, within(1e-9));
    }

    @Test
    void chunks_merge_paragraphs_until_the_minimum_size() {
        String text = "one\n\ntwo\n\nthree\n\nfour";

        List<String> chunks = RagUnderTheHoodDemo.chunk(text, 8);

        assertThat(chunks).containsExactly("one\n\ntwo", "three\n\nfour");
    }

    @Test
    void top_k_ranks_chunks_by_similarity_to_the_question() {
        List<String> chunks = List.of("about cats", "about kings", "about code");
        List<double[]> vectors = List.of(new double[] {1, 0}, new double[] {0.7, 0.7}, new double[] {0, 1});

        var best = RagUnderTheHoodDemo.topK(new double[] {0, 1}, chunks, vectors, 2);

        assertThat(best).extracting(RagUnderTheHoodDemo.Scored::chunk).containsExactly("about code", "about kings");
    }

    @Test
    void prompt_carries_the_context_and_the_question() {
        var context = List.of(new RagUnderTheHoodDemo.Scored("Irene Adler is the woman.", 0.9));

        String prompt = RagUnderTheHoodDemo.prompt("Who is Irene Adler?", context);

        assertThat(prompt).contains("Irene Adler is the woman.").contains("Question: Who is Irene Adler?");
    }
}
