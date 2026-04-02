package com.AgenticAi.AIProject.Entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "chat_id")
    private ChatSession chatSession;

    private String role; // USER or AI

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdAt;
}
