package com.kousenit;

import static org.assertj.core.api.Assertions.assertThat;

import com.kousenit.OpenRouterRecords.Model;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

class OpenRouterServiceTest {
    private final OpenRouterService service = new OpenRouterService();

    @Test
    @Tag(TestCategories.CHEAP) // free: the catalog endpoint needs no key
    @Tag(TestCategories.QUICK)
    @Tag(TestCategories.DEMO)
    void text_models_include_the_ones_the_course_uses() {
        List<Model> models = service.listModels();
        List<String> ids = models.stream().map(Model::id).toList();

        System.out.println(models.size() + " text models");
        assertThat(ids).contains("openai/gpt-5-nano", "google/gemini-3-flash-preview");
    }

    @Test
    @Tag(TestCategories.CHEAP)
    @Tag(TestCategories.QUICK)
    void image_models_say_so_in_their_architecture() {
        List<Model> models = service.listModels("image");

        models.stream()
                .limit(5)
                .forEach(m -> System.out.printf(
                        "%-40s in=%s out=%s $%s/M prompt%n",
                        m.id(),
                        m.architecture().inputModalities(),
                        m.architecture().outputModalities(),
                        m.pricing().promptPerMillion()));
        assertThat(models)
                .isNotEmpty()
                .allMatch(m -> m.architecture().outputModalities().contains("image"));
    }
}
