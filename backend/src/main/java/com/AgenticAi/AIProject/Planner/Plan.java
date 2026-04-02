package com.AgenticAi.AIProject.Planner;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class Plan {

	private String goal;
	private List<PlanStep>steps;
	
}
