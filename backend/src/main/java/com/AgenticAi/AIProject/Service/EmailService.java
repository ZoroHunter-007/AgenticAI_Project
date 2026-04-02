package com.AgenticAi.AIProject.Service;



import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.AgenticAi.AIProject.Entity.User;



@Service
public class EmailService {

	private final JavaMailSender javaMailSender;
	
	public EmailService(JavaMailSender javaMailSender) {
		// TODO Auto-generated constructor stub
		this.javaMailSender=javaMailSender;
	}
	
	public void sendMail(User user) {
		String body = """
				Hi %s 👋,

				Welcome to Agentic AI 🤖✨

				Your account has been created successfully. You can now sign in and start using the platform 🚀

				If you didn’t create this account, you can safely ignore this email.

				Thanks,  
				Agentic AI Team 💙
				""".formatted(user.getDisplayName());

		SimpleMailMessage msg=new SimpleMailMessage();
		msg.setTo(user.getEmail());
		msg.setSubject("Verfication Email from Agentic AI Platform");
		msg.setText(body);
		
		javaMailSender.send(msg);
	}
}
