package com.AgenticAi.AIProject.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class UserResponse {

	private String email;
	private String displayName;
	private String profileImage;
}
