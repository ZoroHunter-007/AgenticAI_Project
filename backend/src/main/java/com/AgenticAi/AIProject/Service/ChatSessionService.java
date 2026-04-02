package com.AgenticAi.AIProject.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.AgenticAi.AIProject.Entity.ChatSession;
import com.AgenticAi.AIProject.Entity.User;
import com.AgenticAi.AIProject.Repository.ChatMessageRepository;
import com.AgenticAi.AIProject.Repository.ChatSessionRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ChatSessionService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    // ✅ Create new session
    public ChatSession createNew(User user) {
        ChatSession session = new ChatSession();
        session.setUser(user);
        session.setTitle("New Chat");
        session.setCreatedAt(LocalDateTime.now());
        return sessionRepository.save(session);
    }

    // ✅ Get all sessions of a user (for sidebar / history)
    public List<ChatSession> getUserChats(User user) {
        return sessionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    // ✅ Get latest session
    public ChatSession getLatest(User user) {
        return sessionRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseGet(() -> createNew(user));
    }

    // ✅ Get by session ID
    public ChatSession getById(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chat not found"));
    }

    // ✅ Explicit optional getter (if needed)
    public ChatSession getLatestOrThrow(User user) {
        return sessionRepository
                .findTopByUserOrderByCreatedAtDesc(user)
                .orElseThrow(() -> new RuntimeException("No chat session found"));
    }
    public ChatSession save(ChatSession session) {
        return sessionRepository.save(session);
    }
    
    @Transactional  // ✅ MUST have this for delete operations
    public void deleteChat(UUID chatId, User user) {
        ChatSession session = getById(chatId);
        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        messageRepository.deleteByChatSession(session); // delete messages first
        sessionRepository.delete(session);           // then delete session
    }
}