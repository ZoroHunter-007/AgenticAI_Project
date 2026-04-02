package com.AgenticAi.AIProject.Planner;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.AgenticAi.AIProject.Executor.ExecutionContext;
import com.AgenticAi.AIProject.Gemini.GeminiClient;
import com.AgenticAi.AIProject.Gemini.LLMMessage;
import com.AgenticAi.AIProject.Memory.AgentMemory;
import com.AgenticAi.AIProject.Memory.MemoryItem;
import com.AgenticAi.AIProject.Parser.PlanParser;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class PlanningEngine {

    private final GeminiClient geminiClient;
    private final PlanParser planParser;
    private final AgentMemory agentMemory;

    public Mono<Plan> createPlan(ExecutionContext context,
                                 String userMessage) {

        // ✅ Extract userId correctly from context
        Long userId = context.getUser().getId();

        /* ===============================
           SHORT TERM MEMORY
        =============================== */

        List<String> shortMemory =
                agentMemory.getShortMemoryContext(userId);

        String shortMemoryBlock = shortMemory.isEmpty()
                ? "No previous conversation."
                : String.join("\n", shortMemory);

        /* ===============================
           LONG TERM MEMORY
        =============================== */

        List<MemoryItem> longMemory =
                agentMemory.getLongMemoryContext(userId);

        String longMemoryBlock = longMemory.isEmpty()
                ? "No stored user facts."
                : longMemory.stream()
                        .map(item -> item.getType() + ": " + item.getValue())
                        .collect(Collectors.joining("\n"));

        /* ===============================
           PLANNING PROMPT
        =============================== */

        String planningPrompt = """
You are an advanced structured AI planning engine.

Convert the user message into a structured execution plan.

RULES:

1. Add MEMORY step only if the user shares new personal facts.
2. Add TOOL step with toolName "SEARCH" if external data is required.
3. Add THINK step only if reasoning is required.
4. ALWAYS end with a RESPONSE step.

Conversation Memory:
%s

Long-Term Memory:
%s

Current User Message:
%s

OUTPUT RULES:
- Return ONLY raw JSON.
- No markdown.
- No explanations.
- Must start with { and end with }.
- Do NOT include labels like THINK: or RESPONSE: inside input fields.

FORMAT:
{
  "goal": "short description",
  "steps": [
    { "type": "THINK", "input": "reasoning task" },
    { "type": "MEMORY", "input": "memory content" },
    { "type": "RESPONSE", "input": "final answer instruction" }
  ]
}
""".formatted(shortMemoryBlock, longMemoryBlock, userMessage);

        List<LLMMessage> messages = List.of(
                new LLMMessage("user", planningPrompt)
        );

        /* ===============================
           CALL GEMINI + SAFE PARSE
        =============================== */

        return geminiClient.generate(messages)
                .doOnNext(raw -> System.out.println("RAW PLAN RESPONSE:\n" + raw))
                .flatMap(raw -> {
                    try {
                        Plan plan = planParser.parse(raw);
                        return Mono.just(plan);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return Mono.error(
                                new IllegalStateException("Invalid plan JSON", e)
                        );
                    }
                });
    }
}