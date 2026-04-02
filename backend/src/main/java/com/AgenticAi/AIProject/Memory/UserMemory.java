package com.AgenticAi.AIProject.Memory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class UserMemory {

    private final Deque<String> shortMemory = new ArrayDeque<>();
    private final List<MemoryItem> longMemory = new ArrayList<>();

    public Deque<String> getShortMemory() {
        return shortMemory;
    }

    public List<MemoryItem> getLongMemory() {
        return longMemory;
    }
}
