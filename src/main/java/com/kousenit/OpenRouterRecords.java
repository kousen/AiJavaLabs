package com.kousenit;

import java.math.BigDecimal;
import java.util.List;

/** Just the parts of OpenRouter's model catalog this course uses; Jackson ignores the rest. */
public class OpenRouterRecords {
    public record ModelList(List<Model> data, int totalCount) {}

    public record Model(String id, String name, int contextLength, Architecture architecture, Pricing pricing) {}

    public record Architecture(List<String> inputModalities, List<String> outputModalities) {}

    /** Prices arrive as strings in dollars per token, so convert once and keep the precision. */
    public record Pricing(String prompt, String completion) {
        private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

        public BigDecimal promptPerMillion() {
            return perMillion(prompt);
        }

        public BigDecimal completionPerMillion() {
            return perMillion(completion);
        }

        private static BigDecimal perMillion(String perToken) {
            return perToken == null
                    ? BigDecimal.ZERO
                    : new BigDecimal(perToken).multiply(MILLION).stripTrailingZeros();
        }
    }
}
