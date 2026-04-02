package com.AgenticAi.AIProject.Parser;

import org.springframework.stereotype.Component;

import com.AgenticAi.AIProject.Planner.Plan;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PlanParser {

    private final ObjectMapper objectMapper;

    public Plan parse(String rawOutput) {

        try {
            String cleaned = extractJson(rawOutput);
            return objectMapper.readValue(cleaned, Plan.class);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Invalid plan JSON from LLM. Raw output:\n" + rawOutput,
                    e
            );
        }
    }

    private String extractJson(String raw) {

        if (raw == null || raw.isBlank()) {
            throw new RuntimeException("LLM returned empty response");
        }

        // Remove markdown wrappers
        raw = raw.replaceAll("```json", "")
                 .replaceAll("```", "")
                 .trim();

        // Extract only JSON block
        int firstBrace = raw.indexOf("{");
        int lastBrace = raw.lastIndexOf("}");

        if (firstBrace == -1 || lastBrace == -1) {
            throw new RuntimeException("No valid JSON object found in response");
        }

        return raw.substring(firstBrace, lastBrace + 1);
    }
}

