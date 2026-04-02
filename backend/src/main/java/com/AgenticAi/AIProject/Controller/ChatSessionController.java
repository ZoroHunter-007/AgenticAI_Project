package com.AgenticAi.AIProject.Controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.AgenticAi.AIProject.Custom.CustomUserDetails;
import com.AgenticAi.AIProject.Entity.ChatMessage;
import com.AgenticAi.AIProject.Entity.ChatSession;
import com.AgenticAi.AIProject.Repository.ChatMessageRepository;
import com.AgenticAi.AIProject.Service.ChatSessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/chats")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081"}, allowCredentials = "true")
@RequiredArgsConstructor
public class ChatSessionController {

    private final ChatSessionService chatSessionService;
    private final ChatMessageRepository messageRepository;

    // 🔹 Sidebar: load chat history
    @GetMapping
    public List<ChatSession> getChats(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return chatSessionService.getUserChats(userDetails.getUser());
    }

//     /🔹 New Chat button
    @PostMapping
    public ChatSession newChat(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        return chatSessionService.createNew(userDetails.getUser());
    }

    // 🔹 Load messages of selected chat
    @GetMapping("/{chatId}/messages")
    public List<ChatMessage> getMessages(@PathVariable UUID chatId) {
        return messageRepository.findByChatSessionOrderByCreatedAt(
                chatSessionService.getById(chatId)
        );
    }
    

 // 🔹 Delete chat session
    @DeleteMapping("/{chatId}")
    public void deleteChat(
            @PathVariable UUID chatId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        chatSessionService.deleteChat(chatId, userDetails.getUser());
    }
}
