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
public class ThinkStepStrategy implements StepExecutionStrategy {

	private final GeminiClient geminiClient;
	private final AgentRules agentRules;


    @Override
    public StepType getType() {
        return StepType.THINK;
    }

    @Override
    public Mono<String> execute(ExecutionContext context, PlanStep step) {

       String thinkingPrompt=agentRules.applyThinkingRules(step.getInput());
       
       List<LLMMessage>messages=List.of(
    		   new LLMMessage("user", thinkingPrompt));
       
       return geminiClient.generate(messages)
    		   .doOnNext(reasoning ->
    		   context.getVariables().put("reasoningTask", reasoning)
    		   );
    }
}