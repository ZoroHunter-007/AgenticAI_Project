package com.AgenticAi.AIProject.Executor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.AgenticAi.AIProject.Planner.Plan;
import com.AgenticAi.AIProject.Planner.PlanStep;
import com.AgenticAi.AIProject.Planner.StepType;
import com.AgenticAi.AIProject.Strategy.StepExecutionStrategy;

import reactor.core.publisher.Mono;

@Component
public class PlanExecutor {

    private final Map<StepType, StepExecutionStrategy> strategyMap;

    public PlanExecutor(List<StepExecutionStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(
                        StepExecutionStrategy::getType,
                        s -> s
                ));
    }
    public Mono<String> execute(ExecutionContext context, Plan plan) {

        Mono<Void> flow = Mono.empty();

        for (PlanStep step : plan.getSteps()) {

            StepExecutionStrategy strategy =
                    strategyMap.get(step.getType());

            if (strategy == null) {
                return Mono.error(
                        new IllegalStateException(
                                "No strategy for: " + step.getType()
                        )
                );
            }

            flow = flow.then(
                    strategy.execute(context, step)
                            .doOnNext(context::updateResult)
                            .then()
            );
        }

        return flow.then(Mono.fromSupplier(context::getLastResult));
    }
}