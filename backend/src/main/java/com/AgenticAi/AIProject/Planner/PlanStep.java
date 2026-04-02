package com.AgenticAi.AIProject.Planner;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlanStep {

	private StepType type;
	private String input;
	private String toolName;
}
