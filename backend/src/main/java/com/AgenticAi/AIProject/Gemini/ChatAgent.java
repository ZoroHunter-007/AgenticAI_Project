package com.AgenticAi.AIProject.Gemini;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.AgenticAi.AIProject.Entity.ChatMessage;
import com.AgenticAi.AIProject.Entity.ChatSession;
import com.AgenticAi.AIProject.Executor.ExecutionContext;
import com.AgenticAi.AIProject.Executor.PlanExecutor;
import com.AgenticAi.AIProject.Planner.PlanningEngine;
import com.AgenticAi.AIProject.Repository.ChatMessageRepository;
import com.AgenticAi.AIProject.Rules.AgentRules;
import com.AgenticAi.AIProject.Service.ChatSessionService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatAgent {

    private final PlanningEngine planningEngine;
    private final PlanExecutor planExecutor;
    private final GeminiClient geminiClient;
    private final AgentRules agentRules;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionService chatSessionService;

    /* ===================================================== */
    /* 🔥 MAIN ENTRY                                         */
    /* ===================================================== */

    public Mono<Object> chat(ChatSession session, String message) {

        // Save USER message immediately
        saveMessage(session, "USER", message);
        updateTitleIfNeeded(session, message);

        return Mono.defer(() -> {

           
            ExecutionContext context = buildExecutionContext(session);

            if (shouldUsePlanning(message) && isComplex(message)) {

                return planningEngine
                        .createPlan(context, message)
                        .flatMap(plan -> planExecutor.execute(context, plan));
            }

            return normalChat(session, message);

        }).flatMap(response -> {

            // Save AI response once (centralized)
            saveMessage(session, "AI", response);
            return Mono.just(response);
        });
    }
   

    private ExecutionContext buildExecutionContext(ChatSession session) {

        List<ChatMessage> history =
                chatMessageRepository
                        .findByChatSessionOrderByCreatedAt(session);

        return new ExecutionContext(
                session.getUser(),
                session,
                history,
                new HashMap<>()
        );
    }

    /* ===================================================== */
    /* 🧠 NORMAL CHAT (1 API CALL ONLY)                     */
    /* ===================================================== */

    private Mono<String> normalChat(ChatSession session,
                                    String latestMessage) {

        List<ChatMessage> history =
                chatMessageRepository
                        .findByChatSessionOrderByCreatedAt(session);

        List<LLMMessage> messages = new ArrayList<>();

        // Inject system instruction safely
        messages.add(new LLMMessage(
                "user",
                "SYSTEM INSTRUCTION:\n" + agentRules.getSystemPrompt()
        ));

        // Inject conversation history
        for (ChatMessage msg : history) {

            String geminiRole =
                    msg.getRole().equalsIgnoreCase("USER")
                            ? "user"
                            : "model";

            messages.add(new LLMMessage(
                    geminiRole,
                    msg.getContent()
            ));
        }

    
        return geminiClient.generate(messages)
                .flatMap(response -> {
                    updateTitleIfNeeded(session, latestMessage);
                    return Mono.just(response); 
                });
    }

    /* ===================================================== */
    /* 💾 SAVE MESSAGE                                      */
    /* ===================================================== */

    private void saveMessage(ChatSession session,
                             String role,
                             String content) {

        ChatMessage msg = new ChatMessage();
        msg.setChatSession(session);
        msg.setRole(role);
        msg.setContent(content);
        msg.setCreatedAt(LocalDateTime.now());

        chatMessageRepository.save(msg);
    }

    /* ===================================================== */
    /* 🏷 AUTO TITLE UPDATE                                  */
    /* ===================================================== */

    private void updateTitleIfNeeded(ChatSession session,
                                     String firstUserMessage) {

        if (session == null || firstUserMessage == null) return;

        if ("New Chat".equals(session.getTitle())) {

            String cleanMessage = firstUserMessage
                    .replaceAll("\\s+", " ")
                    .trim();

            if (cleanMessage.isEmpty()) return;

            String title = cleanMessage.length() > 30
                    ? cleanMessage.substring(0, 30) + "..."
                    : cleanMessage;

            session.setTitle(title);
            chatSessionService.save(session);
        }
    }

    /* ===================================================== */
    /* 🔍 PLANNING TRIGGER                                  */
    /* ===================================================== */

    private boolean shouldUsePlanning(String message) {

        String msg = message.toLowerCase();

        return msg.contains("find")
                || msg.contains("search")
                || msg.contains("check")
                || msg.contains("compare")
                || msg.contains("latest")
                || msg.contains("analyze");
    }

    /* ===================================================== */
    /* 🧠 COMPLEXITY FILTER (Quota Saver)                   */
    /* ===================================================== */

    private boolean isComplex(String message) {
        return message.length() > 25; // Skip planning for short/simple queries
    }

    /* ===================================================== */
    /* 🧱 BLOCKING WRAPPER                                  */
    /* ===================================================== */

    public String chatBlocking(ChatSession session,
                               String message) {
        return (String) chat(session, message).block();
    }
}