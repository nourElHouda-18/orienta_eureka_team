package com.nourbouhafara.riasecapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Small OpenRouter chat-completions client used by the hackathon assistant.
 *
 * IMPORTANT: the API key is intentionally NOT stored in the repository. For a demo,
 * add OPENROUTER_API_KEY=... to local.properties. For a production app, proxy LLM calls
 * through your own backend so a secret key is never shipped inside the APK.
 */
public final class LlmClient {

    public interface Callback {
        void onSuccess(String answer);
        void onError(String message);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String CHAT_URL = "https://openrouter.ai/api/v1/chat/completions";

    private LlmClient() {}

    public static boolean isConfigured() {
        return BuildConfig.OPENROUTER_API_KEY != null
                && !BuildConfig.OPENROUTER_API_KEY.trim().isEmpty();
    }

    public static void ask(String profileContext, String question, Callback callback) {
        if (!isConfigured()) {
            callback.onError("LLM_NOT_CONFIGURED");
            return;
        }

        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(CHAT_URL);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(60000);
                connection.setDoOutput(true);
                connection.setRequestProperty("Authorization", "Bearer " + BuildConfig.OPENROUTER_API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("X-Title", "RIASEC Explorer");

                JSONObject payload = new JSONObject();
                payload.put("model", BuildConfig.OPENROUTER_MODEL);
                payload.put("max_tokens", 500);
                payload.put("temperature", 0.4);

                JSONArray messages = new JSONArray();
                messages.put(new JSONObject()
                        .put("role", "system")
                        .put("content", buildSystemPrompt()));
                messages.put(new JSONObject()
                        .put("role", "user")
                        .put("content", profileContext + "\n\nUser question: " + question));
                payload.put("messages", messages);

                byte[] bytes = payload.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream output = connection.getOutputStream()) {
                    output.write(bytes);
                }

                int code = connection.getResponseCode();
                InputStream stream = code >= 200 && code < 300
                        ? connection.getInputStream()
                        : connection.getErrorStream();
                String responseBody = readAll(stream);

                if (code < 200 || code >= 300) {
                    callback.onError("LLM request failed (HTTP " + code + "). " + compactApiError(responseBody));
                    return;
                }

                String answer = extractText(responseBody);
                if (answer == null || answer.trim().isEmpty()) {
                    callback.onError("The LLM returned an empty response.");
                } else {
                    callback.onSuccess(answer.trim());
                }
            } catch (Exception exception) {
                callback.onError("Could not contact the LLM: " + exception.getMessage());
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    private static String buildSystemPrompt() {
        return "You are the Support Assistant inside RIASEC Explorer, a vocational-interest hackathon app. "
                + "Explain the user's RIASEC profile and the broad study/career domains produced by the app's trained CatBoost model. "
                + "Be concise, supportive, and evidence-aware. RIASEC describes interests, not ability, personality, destiny, or a guaranteed career match. "
                + "The classifier predicts only these broad domains: Arts & Design, Business & Management, Education, Health, "
                + "Humanities & Communication, STEM, and Social & Public Sciences. "
                + "Do not pretend that the model predicts a specific university, institution, exact major, salary, admission chance, or job. "
                + "You may give examples of fields that belong to a recommended domain, but label them as examples to explore. "
                + "When explaining why a domain was recommended, refer to the supplied RIASEC scores/Holland code and model probabilities without treating them as certainty. "
                + "Keep answers usually under 180 words unless the user asks for more detail.";
    }

    private static String readAll(InputStream stream) throws Exception {
        if (stream == null) return "";
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
        }
        return builder.toString();
    }

    private static String extractText(String json) throws Exception {
        JSONObject root = new JSONObject(json);
        JSONArray choices = root.optJSONArray("choices");
        if (choices == null || choices.length() == 0) return "";

        JSONObject first = choices.optJSONObject(0);
        if (first == null) return "";
        JSONObject message = first.optJSONObject("message");
        if (message == null) return "";

        Object content = message.opt("content");
        if (content instanceof String) {
            return ((String) content).trim();
        }

        // Some providers may return content as an array of typed parts.
        if (content instanceof JSONArray) {
            JSONArray parts = (JSONArray) content;
            StringBuilder answer = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.optJSONObject(i);
                if (part == null) continue;
                String text = part.optString("text", "");
                if (!text.isEmpty()) {
                    if (answer.length() > 0) answer.append('\n');
                    answer.append(text);
                }
            }
            return answer.toString();
        }

        return "";
    }

    private static String compactApiError(String json) {
        try {
            JSONObject root = new JSONObject(json);
            JSONObject error = root.optJSONObject("error");
            if (error != null) {
                String message = error.optString("message", "");
                if (!message.isEmpty()) return message;
            }
        } catch (Exception ignored) {
        }
        if (json == null) return "";
        String clean = json.replace('\n', ' ').trim();
        return clean.length() > 180 ? clean.substring(0, 180) + "…" : clean;
    }
}
