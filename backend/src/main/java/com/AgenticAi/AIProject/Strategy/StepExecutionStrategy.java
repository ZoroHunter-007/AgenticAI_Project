package com.AgenticAi.AIProject.Strategy;

import com.AgenticAi.AIProject.Executor.ExecutionContext;
import com.AgenticAi.AIProject.Planner.PlanStep;
import com.AgenticAi.AIProject.Planner.StepType;

import reactor.core.publisher.Mono;

public interface StepExecutionStrategy {

	StepType getType();
	
	Mono<String> execute(ExecutionContext context, PlanStep planStep);
}
