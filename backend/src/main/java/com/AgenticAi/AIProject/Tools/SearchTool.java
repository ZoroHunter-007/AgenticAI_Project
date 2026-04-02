package com.AgenticAi.AIProject.Tools;

import java.util.List;

import org.springframework.stereotype.Component;

import com.AgenticAi.AIProject.Gemini.GeminiClient;
import com.AgenticAi.AIProject.Gemini.LLMMessage;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SearchTool implements Tools {

    private final GeminiClient geminiClient;

    @Override
    public Mono<String> execute(String input) {

        String prompt = """
        Perform a web-style search for:
        %s

        Return factual and structured results only.
        Do not hallucinate unknown data.
        """.formatted(input);

        List<LLMMessage> messages = List.of(
                new LLMMessage("user", prompt)
        );

        return geminiClient.generate(messages);
    }

    @Override
    public String getName() {
        return "SEARCH";
    }
}