package com.AgenticAi.AIProject.Memory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.AgenticAi.AIProject.Entity.User;
import com.AgenticAi.AIProject.Entity.UserMemoryEntity;
import com.AgenticAi.AIProject.Repository.UserMemoryRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ImpleAgentMemory implements AgentMemory {

    // 🔹 Short-term memory (fast, in RAM)
    private final Map<Long, UserMemory> shortMemoryStore =
            new ConcurrentHashMap<>();

    // 🔹 Persistent long-term memory (DB)
    private final UserMemoryRepository memoryRepository;

    private UserMemory getOrCreate(Long userId) {
        return shortMemoryStore.computeIfAbsent(userId,
                id -> new UserMemory());
    }

    // =============================
    // SHORT MEMORY (IN-MEMORY)
    // =============================

    @Override
    public void addShortMemory(Long userId, String message) {

        UserMemory userMemory = getOrCreate(userId);

        if (userMemory.getShortMemory().size() > 6) {
            userMemory.getShortMemory().pollFirst();
        }

        userMemory.getShortMemory().addLast(message);
    }

    @Override
    public List<String> getShortMemoryContext(Long userId) {
        return List.copyOf(getOrCreate(userId).getShortMemory());
    }

    // =============================
    // LONG MEMORY (DATABASE)
    // =============================

    @Override
    public void addLongMemory(User user, MemoryItem item) {

        UserMemoryEntity entity = UserMemoryEntity.builder()
                .user(user)
                .type(item.getType())
                .content(item.getValue())
                .build();

        memoryRepository.save(entity);
    }

    @Override
    public List<MemoryItem> getLongMemoryContext(Long userId) {

        return memoryRepository.findByUserId(userId)
                .stream()
                .map(entity -> new MemoryItem(
                        entity.getType(),
                        entity.getContent()
                ))
                .collect(Collectors.toList());
    }
}