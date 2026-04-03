package com.AgenticAi.AIProject.Gemini;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
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
                    // 429 = Rate Limit, 400 = Bad Request (Retry with Fallback)
                    System.err.println("Primary failed (" + ex.getStatusCode() + "). Trying fallback...");
                    return Mono.delay(java.time.Duration.ofSeconds(2))
                               .then(call(fallBackModel, messages));
                })
                .onErrorResume(ex -> {
                    System.err.println("Critical API Error: " + ex.getMessage()); 
                    return Mono.just("⚠️ System is currently under maintenance. Please try again in 15 seconds.");
                });
    }

    private Mono<String> call(String model, List<LLMMessage> messages) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                   + model + ":generateContent?key=" + apikey;

        // 1. Role mapping: Gemini ONLY accepts "user" and "model"
        List<Content> contents = messages.stream()
                .map(msg -> new Content(
                        msg.getRole().equalsIgnoreCase("user") ? "user" : "model",
                        List.of(new Part(msg.getContent()))
                ))
                .toList();

        // 2. UPDATED TOOL SPEC: google_search is the 2026 standard
        List<Tool> tools = List.of(new Tool(new GoogleSearch()));
        
        GeminiRequest request = new GeminiRequest(contents, tools);

        return webClient.post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(this::extractText);
    }

    private String extractText(JsonNode response) {
        try {
            // Check for grounding metadata (search results) first
            JsonNode candidate = response.path("candidates").get(0);
            return candidate.path("content").path("parts").get(0).path("text").asText();
        } catch (Exception e) {
            return "⚠️ Response parsing failed. Please check logs.";
        }
    }

    // --- JSON Records for 2026 Spec ---
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GeminiRequest(List<Content> contents, List<Tool> tools) {}

    public record Content(String role, List<Part> parts) {}

    public record Part(String text) {}

    public record Tool(
            @JsonProperty("googleSearch") 
            GoogleSearch googleSearch
        ) {}

        public record GoogleSearch() {}
}