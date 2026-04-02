package com.AgenticAi.AIProject.Entity;

import java.util.UUID;

import lombok.Data;

@Data

public class ChatRequest {

	private UUID sessionId;
	private String message;
}
