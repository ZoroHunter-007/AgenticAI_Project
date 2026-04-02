package com.AgenticAi.AIProject.Executor;

import java.util.List;
import java.util.Map;

import com.AgenticAi.AIProject.Entity.ChatMessage;
import com.AgenticAi.AIProject.Entity.ChatSession;
import com.AgenticAi.AIProject.Entity.User;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Getter
@RequiredArgsConstructor
public class ExecutionContext {

    private final User user;
    private final ChatSession session;
    private final List<ChatMessage> history;
    private final Map<String, Object> variables;

    private String lastResult;

    public void updateResult(String result) {
        this.lastResult = result;
    }
}
