package com.AgenticAi.AIProject.Tools;

import reactor.core.publisher.Mono;

public interface Tools {

	String getName();
	Mono<String>execute(String input);
}
