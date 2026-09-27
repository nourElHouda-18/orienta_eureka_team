package com.nourbouhafara.riasecapp;

import android.content.Context;

import java.util.Locale;

/** Offline fallback for the three core explanation questions. */
public final class LocalCareerAssistant {

    private LocalCareerAssistant() {}

    public static String answer(Context context, String question) {
        String q = question == null ? "" : question.toLowerCase(Locale.ROOT);
        String code = AppStorage.getHollandCode(context);
        float[] scores = AppStorage.getScores(context);
        float[] probabilities = AppStorage.getProbabilities(context);
        int[] top = AppStorage.getTopIndices(context);

        if (q.contains("what is") && q.contains("riasec")) {
            return "RIASEC is a vocational-interest framework with six dimensions: Realistic, Investigative, Artistic, Social, Enterprising, and Conventional. "
                    + "Your answers are summarized into six scores and a three-letter Holland Code. It describes activities and work environments you tend to prefer; it does not measure intelligence or determine one perfect career.";
        }

        if ((q.contains("why") && (q.contains("domain") || q.contains("field") || q.contains("choose")))
                || q.contains("recommend")) {
            if (code == null || code.isEmpty() || top == null || probabilities == null) {
                return "Complete the RIASEC assessment first and I can explain how your scores relate to the recommended domains.";
            }
            StringBuilder b = new StringBuilder();
            b.append("Your Holland Code is ").append(code).append(". ");
            if (scores != null && scores.length == 6) {
                b.append("Your strongest interest dimensions guide the profile, while the CatBoost model also uses all 48 item responses plus the six scores. ");
            }
            b.append("The model's top recommendations are ");
            for (int i = 0; i < Math.min(3, top.length); i++) {
                int idx = top[i];
                if (i > 0) b.append(i == 2 ? ", and " : ", ");
                b.append(RiasecOnnxModelHelper.DOMAINS[idx]);
                if (idx >= 0 && idx < probabilities.length) {
                    b.append(" (").append(String.format(Locale.US, "%.1f%%", probabilities[idx] * 100f)).append(")");
                }
            }
            b.append(". These are compatibility rankings for exploration, not guarantees.");
            return b.toString();
        }

        if ((q.contains("what can") || q.contains("do with") || q.contains("career") || q.contains("study"))
                && top != null && top.length > 0) {
            StringBuilder b = new StringBuilder("Examples you could explore within your recommended domains:\n");
            for (int i = 0; i < Math.min(3, top.length); i++) {
                String domain = RiasecOnnxModelHelper.DOMAINS[top[i]];
                b.append("\n• ").append(domain).append(": ").append(examples(domain));
            }
            b.append("\n\nThese are examples, not predictions of one exact major or job.");
            return b.toString();
        }

        return "I can explain your RIASEC profile, why the model ranked your domains, and examples of fields to explore. "
                + "For free-form AI chat, add a free OpenRouter API key to local.properties as described in LLM_SETUP.md.";
    }

    public static String buildProfileContext(Context context) {
        StringBuilder b = new StringBuilder();
        b.append("RIASEC Explorer user profile:\n");

        String code = AppStorage.getHollandCode(context);
        if (code != null && !code.isEmpty()) b.append("Holland Code: ").append(code).append('\n');

        float[] s = AppStorage.getScores(context);
        if (s != null && s.length == 6) {
            b.append(String.format(Locale.US,
                    "RIASEC scores (1-5): R=%.2f, I=%.2f, A=%.2f, S=%.2f, E=%.2f, C=%.2f\n",
                    s[0], s[1], s[2], s[3], s[4], s[5]));
        }

        float[] p = AppStorage.getProbabilities(context);
        int[] top = AppStorage.getTopIndices(context);
        if (p != null && top != null) {
            b.append("Top model domains:\n");
            for (int i = 0; i < Math.min(3, top.length); i++) {
                int idx = top[i];
                if (idx >= 0 && idx < RiasecOnnxModelHelper.DOMAINS.length && idx < p.length) {
                    b.append(i + 1).append(". ")
                            .append(RiasecOnnxModelHelper.DOMAINS[idx])
                            .append(" — ")
                            .append(String.format(Locale.US, "%.1f%%", p[idx] * 100f))
                            .append('\n');
                }
            }
        } else {
            b.append("No completed RIASEC result is currently stored.\n");
        }

        return b.toString();
    }

    private static String examples(String domain) {
        switch (domain) {
            case "STEM":
                return "computer science, data/AI, engineering, mathematics, physics, or related technical fields.";
            case "Health":
                return "medicine and health sciences, nursing, public health, rehabilitation, or other healthcare-related fields.";
            case "Business & Management":
                return "management, entrepreneurship, economics, finance, marketing, or hospitality-related fields.";
            case "Social & Public Sciences":
                return "psychology, sociology, law, political/public studies, criminology, or international affairs.";
            case "Humanities & Communication":
                return "languages, literature, history, media, journalism, communication, or related humanities.";
            case "Arts & Design":
                return "visual design, digital media, fine arts, architecture-related creative work, or other design disciplines.";
            case "Education":
                return "teaching, educational sciences, learning support, training, or curriculum-related fields.";
            default:
                return "related study programs and careers within this broad domain.";
        }
    }
}
