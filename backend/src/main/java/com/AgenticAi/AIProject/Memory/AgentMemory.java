package com.AgenticAi.AIProject.Memory;

import java.util.List;

import com.AgenticAi.AIProject.Entity.User;

public interface AgentMemory {

    void addShortMemory(Long userId, String message);

    void addLongMemory(User user, MemoryItem item);

    List<String> getShortMemoryContext(Long userId);

    List<MemoryItem> getLongMemoryContext(Long userId);
}

