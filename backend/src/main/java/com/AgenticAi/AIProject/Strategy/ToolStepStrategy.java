package com.AgenticAi.AIProject.Strategy;

import org.springframework.stereotype.Component;

import com.AgenticAi.AIProject.Executor.ExecutionContext;
import com.AgenticAi.AIProject.Planner.PlanStep;
import com.AgenticAi.AIProject.Planner.StepType;
import com.AgenticAi.AIProject.Tools.ToolRegistry;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@Getter
@RequiredArgsConstructor
public class ToolStepStrategy implements StepExecutionStrategy{

	private final ToolRegistry toolRegistry;

	@Override
	public StepType getType() {
		
		return StepType.TOOL;
	}

	@Override
	public Mono<String> execute(ExecutionContext context, PlanStep planStep) {
		var tool=toolRegistry.getTool(planStep.getToolName());
		
		if(tool==null) {
		return Mono.error(new RuntimeException("Tool not found: "+planStep.getToolName()));
		}
		
		return tool.execute(planStep.getInput());
	}
	
}
