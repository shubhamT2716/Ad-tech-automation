package net.media.emma.utils;

import java.util.Map;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MailServiceImpl {

	private JavaMailSender javaMailSender;
	private SpringTemplateEngine thymeleaf;

	public MailServiceImpl(JavaMailSender javaMailSender, SpringTemplateEngine thymeleaf) {
		this.javaMailSender = javaMailSender;
		this.thymeleaf = thymeleaf;
	}

	@Async
	public void sendMail(String to, String templateName, Map<String, JsonNode> templateModel) {
		Thread t = Thread.currentThread();

		log.info("Is active " + t.isAlive());

		String templateText = templateLoder(templateName, templateModel);
		MimeMessagePreparator message = prepareMessage(to, templateText);
		javaMailSender.send(message);

	}

	public String templateLoder(String user, Map<String, JsonNode> result) {

		Context context = new Context();
		context.setVariable("name", user);
		context.setVariable("result", result);
		return thymeleaf.process("SchedulerReport", context);
	}

	private MimeMessagePreparator prepareMessage(String to, String templateText) {

		return new MimeMessagePreparator() {

			public void prepare(jakarta.mail.internet.MimeMessage mimeMessage) throws jakarta.mail.MessagingException {

				MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				message.setFrom("shubham.t@media.net");
				message.setTo(to);
				message.setSubject("Daily Scheduler Report");
				// message.setText( templateText + "my text <img src='cid:myLogo'>", true);
				message.setText(templateText, true);

//				message.addInline("image-1", new ClassPathResource("email-templates/images/image-1.png"));
//				// message.addInline("image-2", new
//				// ClassPathResource("email-templates/images/image-2.png"));
//				message.addInline("image-3", new ClassPathResource("email-templates/images/image-3.png"));
//				message.addInline("image-4", new ClassPathResource("email-templates/images/image-4.png"));
//				// message.addInline("image-5", new
//				// ClassPathResource("email-templates/images/image-5.png"));
//				message.addInline("image-6", new ClassPathResource("email-templates/images/image-6.png"));
//				message.addInline("image-7", new ClassPathResource("email-templates/images/image-7.jpeg"));

				// message.addAttachment("myDocument.pdf", new
				// ClassPathResource("doc/myDocument.pdf"));
			}
		};
	}

}
