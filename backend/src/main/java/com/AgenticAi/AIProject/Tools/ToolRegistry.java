package com.AgenticAi.AIProject.Tools;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ToolRegistry {

    private final List<Tools> tools;

    private Map<String, Tools> toolMap;

    @PostConstruct
    public void init() {
        toolMap = tools.stream()
                .collect(Collectors.toMap(
                        tool -> tool.getName(),
                        tool -> tool
                ));

        System.out.println("Registered tools: " + toolMap.keySet());
    }


    public Tools getTool(String name) {
        return toolMap.get(name);
    }
}
