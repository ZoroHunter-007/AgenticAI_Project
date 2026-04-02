package com.AgenticAi.AIProject.Gemini;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;


@Service
public class GeminiClient {

    private final WebClient webClient;

    @Value("${gemini.api.key}")
    private String apikey;

    @Value("${gemini.model.primary}")
    private String primaryModel;

    @Value("${gemini.model.fallback}")
    private String fallBackModel;

    public GeminiClient(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    public Mono<String> generate(List<LLMMessage> messages) {
        return call(primaryModel, messages)
            .onErrorResume(WebClientResponseException.class, ex -> {
                System.out.println("Primary model failed: " + ex.getStatusCode());
                System.out.println("Trying fallback model...");
                // ✅ Try fallback instead of returning error!
                return call(fallBackModel, messages)
                    .onErrorResume(ex2 -> {
                        System.out.println("Fallback also failed: " + ex2.getMessage());
                        return Mono.just("⚠️ System is busy. Please try again in a moment.");
                    });
            })
            .onErrorResume(ex -> {
                System.out.println("Unexpected error: " + ex.getMessage());
               
                return call(fallBackModel, messages)
                    .onErrorResume(ex2 -> Mono.just("⚠️ System is busy. Please try again in a moment."));
            });
    }

    private Mono<String> call(String model, List<LLMMessage> messages) {

        String url =
            "https://generativelanguage.googleapis.com/v1/models/"
            + model
            + ":generateContent?key=" + apikey;

        // Convert LLMMessage → GeminiRequest structure
        List<Content> contents = messages.stream()
                .map(msg -> new Content(
                        msg.getRole(),
                        List.of(new Part(msg.getContent()))
                ))
                .toList();

        GeminiRequest request = new GeminiRequest(contents);

        return webClient.post()
                .uri(url)
                .bodyValue(request) // 🔥 Auto-serialized by Jackson
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::extractText);
    }

    // ✅ CORRECT + DEFENSIVE PARSER
    private String extractText(JsonNode response) {
        try {
            JsonNode candidates = response.get("candidates");
            if (candidates == null || !candidates.isArray() || candidates.isEmpty()) {
                return "⚠️ No response from AI.";
            }

            JsonNode content = candidates.get(0).get("content");
            if (content == null) {
                return "⚠️ Empty AI response.";
            }

            JsonNode parts = content.get("parts");
            if (parts == null || !parts.isArray() || parts.isEmpty()) {
                return "⚠️ AI returned no text.";
            }

            JsonNode textNode = parts.get(0).get("text");
            if (textNode == null) {
                return "⚠️ AI response missing text.";
            }

            return textNode.asText();

        } catch (Exception e) {
            return "⚠️ I couldn’t understand the response properly.";
        }
    }
    
    public record Part(String text) {}
    public record Content(String role, List<Part> parts) {}
    public record GeminiRequest(List<Content> contents) {}
}
