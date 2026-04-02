package com.AgenticAi.AIProject.Strategy;

import org.springframework.stereotype.Component;

import com.AgenticAi.AIProject.Executor.ExecutionContext;
import com.AgenticAi.AIProject.Memory.AgentMemory;
import com.AgenticAi.AIProject.Memory.MemoryItem;
import com.AgenticAi.AIProject.Planner.PlanStep;
import com.AgenticAi.AIProject.Planner.StepType;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class MemoryStepStrategy implements StepExecutionStrategy{

	private final AgentMemory agentMemory;

	@Override
	public StepType getType() {
		// TODO Auto-generated method stub
		return StepType.MEMORY;
	}

	@Override
	public Mono<String> execute(ExecutionContext context, PlanStep planStep) {
		// TODO Auto-generated method stub
		
		agentMemory.addLongMemory(context.getUser(), new MemoryItem("FACT",planStep.getInput()));
		return Mono.just("Memory stored");
	}
	
	
}
