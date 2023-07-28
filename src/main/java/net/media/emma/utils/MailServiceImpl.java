package net.media.emma.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MailServiceImpl {

	@Autowired
	private EmailConfig config;

	@Async
	public void sendMail(String to, String subject, String message) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();

		mailMessage.setFrom("shubham.t@media.net");
		mailMessage.setTo(to);
		mailMessage.setSubject(subject);
		mailMessage.setText(message);

		config.javaMailSender().send(mailMessage);

	}

}
