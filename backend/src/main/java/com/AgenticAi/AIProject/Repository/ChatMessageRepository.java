package com.AgenticAi.AIProject.Repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.AgenticAi.AIProject.Entity.ChatMessage;
import com.AgenticAi.AIProject.Entity.ChatSession;



public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByChatSessionOrderByCreatedAt(ChatSession session);
    void deleteByChatSession(ChatSession chatSession);

}