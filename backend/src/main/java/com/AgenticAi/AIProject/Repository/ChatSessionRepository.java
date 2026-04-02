package com.AgenticAi.AIProject.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgenticAi.AIProject.Entity.ChatSession;
import com.AgenticAi.AIProject.Entity.User;

public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    // ✅ Get latest chat session for a user
    Optional<ChatSession> findTopByUserOrderByCreatedAtDesc(User user);

    // ✅ Get all sessions of a user (for chat sidebar)
    List<ChatSession> findByUserOrderByCreatedAtDesc(User user);

}