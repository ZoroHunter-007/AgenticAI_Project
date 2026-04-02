package com.AgenticAi.AIProject.Strategy;

import java.util.List;

import org.springframework.stereotype.Component;

import com.AgenticAi.AIProject.Executor.ExecutionContext;
import com.AgenticAi.AIProject.Gemini.GeminiClient;
import com.AgenticAi.AIProject.Gemini.LLMMessage;
import com.AgenticAi.AIProject.Planner.PlanStep;
import com.AgenticAi.AIProject.Planner.StepType;
import com.AgenticAi.AIProject.Rules.AgentRules;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ResponseStepStrategy implements StepExecutionStrategy {

    private final GeminiClient geminiClient;
    private final AgentRules agentRules;

    @Override
    public StepType getType() {
        return StepType.RESPONSE;
    }

    @Override
    public Mono<String> execute(ExecutionContext context, PlanStep step) {

        String reasoning = (String) context.getVariables().get("reasoningTask");

        String userMessage = context.getHistory().stream()
                .filter(msg -> msg.getRole().equalsIgnoreCase("USER"))
                .reduce((first, second) -> second)
                .map(msg -> msg.getContent())
                .orElse("");

        String finalPrompt = """
        		SYSTEM INSTRUCTION:
        		%s

        		USER QUESTION:
        		%s

        		INTERNAL REASONING (do not reveal):
        		%s

        		RESPONSE STYLE RULES:
        		- Use clear headings.
        		- Use bullet points where helpful.
        		- Use relevant professional emojis (⚡ 🚀 🔍 📌 🧠).
        		- Do NOT overuse emojis.
        		- Keep it clean and technical.
        		- Do NOT reveal internal reasoning.
        		- Do NOT include THINK or RESPONSE labels.
        		- Do Not use ## and ** in heading formatting.
        		- Use Numbered lists for steps and bullet points for details.
        		- Always ask the question user satisfies with the answer at the end of the response.
        		- And always ask if they want to continue with another question or task after that.
        		Provide the final answer only.
        		""".formatted(
        		        agentRules.getSystemPrompt(),
        		        userMessage,
        		        reasoning != null ? reasoning : "None"
        		);

        return geminiClient.generate(
                List.of(new LLMMessage("user", finalPrompt))
        );
    }
}