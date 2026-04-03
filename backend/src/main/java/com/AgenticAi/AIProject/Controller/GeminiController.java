package com.AgenticAi.AIProject.Controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.AgenticAi.AIProject.Custom.CustomUserDetails;
import com.AgenticAi.AIProject.Entity.ChatMessage;
import com.AgenticAi.AIProject.Entity.ChatRequest;
import com.AgenticAi.AIProject.Entity.ChatSession;
import com.AgenticAi.AIProject.Entity.User;
import com.AgenticAi.AIProject.Gemini.ChatAgent;
import com.AgenticAi.AIProject.Repository.ChatMessageRepository;
import com.AgenticAi.AIProject.Service.ChatSessionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081","http://localhost"}, allowCredentials = "true")
 public class GeminiController {

    private final ChatAgent chatAgent;
    private final ChatSessionService chatSessionService;
    private final ChatMessageRepository chatMessageRepository;

    // ✅ MAIN CHAT ENDPOINT
 // ✅ MAIN CHAT ENDPOINT
    @PostMapping(
            value = "/api/ai",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public Map<String, Object> ai(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ChatRequest chatRequest) {

        if (userDetails == null) {
            return Map.of("reply", "Please login again.");
        }

        User user = userDetails.getUser();
        UUID sessionId = chatRequest.getSessionId();
        ChatSession session;

        // ✅ THE FIX: Handle brand new chats
        if (sessionId == null) {
            // Auto-create a new session in the database
            session = chatSessionService.createNew(user);
        } else {
            // Fetch the existing session
            session = chatSessionService.getById(sessionId);
            
            // Safe security check (prevents NullPointerExceptions)
            if (session == null || !session.getUser().getId().equals(user.getId())) {
                throw new RuntimeException("Unauthorized session access");
            }
        }

        // Send prompt to Gemini
        String reply = chatAgent.chatBlocking(
                session,
                chatRequest.getMessage().trim()
        );

        // ✅ Return BOTH the reply and the sessionId so React can update the UI
        return Map.of(
            "reply", reply,
            "sessionId", session.getId()
        );
    }

    // ✅ GET LATEST SESSION (for page load)
    @GetMapping("/api/session/latest")
    public ChatSession getLatestSession(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            throw new RuntimeException("User not authenticated");
        }

        return chatSessionService.getLatest(userDetails.getUser());
    }

    // ✅ GET CHAT HISTORY
    @GetMapping("/api/chat/{sessionId}")
    public List<ChatMessage> getChatHistory(
            @PathVariable UUID sessionId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        ChatSession session = chatSessionService.getById(sessionId);

        // Optional security check (recommended)
        if (!session.getUser().getId()
                .equals(userDetails.getUser().getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return chatMessageRepository
                .findByChatSessionOrderByCreatedAt(session);
    }

    // Debug
    @GetMapping("/api/debug-auth")
    public String debugAuth(Authentication authentication) {
        if (authentication == null) {
            return "NOT AUTHENTICATED";
        }
        return "AUTHENTICATED: " + authentication.getName();
    }
}