package com.AgenticAi.AIProject.Gemini;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LLMMessage {


    private String role;   // "user" or "model"
    private String content;
}
