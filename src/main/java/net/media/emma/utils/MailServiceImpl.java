package net.media.emma.utils;


import net.media.emma.cop.testsuites.yaml.Scheduler;
import net.media.emma.cop.testsuites.yaml.TestReportModel;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component

public class MailServiceImpl {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine thymeleaf;

    public MailServiceImpl(JavaMailSender javaMailSender, SpringTemplateEngine thymeleaf) {
        this.javaMailSender = javaMailSender;
        this.thymeleaf = thymeleaf;
    }

    @Async
    public void sendMail(String to, String templateName, List<TestReportModel> templateModel, String zipFile,
                         String testResult) {
       
        String templateText = templateLoder(templateName, templateModel, zipFile, testResult);
        MimeMessagePreparator message = prepareMessage(to, templateText);
        javaMailSender.send(message);

    }


    public String templateLoder(String user, List<TestReportModel> result, String zipFile, String testResult) {

        Context context = new Context();
        List<Context> contextList = new ArrayList<>();
        
        Map<String, String> consoleError=new HashMap<>();
        

        for (TestReportModel model : result) {

            Context modelContext = new Context();
            modelContext.setVariable("PublisherUrl", model.getTestMetaData().getPublisherUrl());
            modelContext.setVariable("harDownloadLink", model.getTestMetaData().getHarFileDownloadLink());
            modelContext.setVariable("dateTime", model.getTestMetaData().getDateTime());
            modelContext.setVariable("bidexSize", model.getController().getTotalSizeDownloaded());
            modelContext.setVariable("bidexTime", model.getController().getTotalResponseTime());
            modelContext.setVariable("ConsoleMessage", model.getController().getConsoleMessages());
            modelContext.setVariable("Env", model.getTestMetaData().getEnvironment());
            modelContext.setVariable("UserAgent", model.getTestMetaData().getUserAgent());
            contextList.add(modelContext);
            if(!model.getController().getConsoleMessages().isEmpty())
            {
            	consoleError.put(Scheduler.getHostFromUrl(model.getTestMetaData().getPublisherUrl()), model.getController().getConsoleMessages().toString());
            }
            

        }
        
        context.setVariable("contextList", contextList);
        context.setVariable("ZipFile", zipFile);
        context.setVariable("testResult", testResult);
        context.setVariable("consoleError", consoleError);

//		return thymeleaf.process("smokeTest", context);
//		return thymeleaf.process("smokeTestEmail", context);
        return thymeleaf.process("smokeTestSchedulerReport", context);

    }

    private MimeMessagePreparator prepareMessage(String to, String templateText) {

        return new MimeMessagePreparator() {

            public void prepare(jakarta.mail.internet.MimeMessage mimeMessage) throws jakarta.mail.MessagingException {

                String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm").format(new Date());

                MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                message.setFrom("shubham.t@media.net");
                message.setTo(to);
                message.setSubject("Smoke Test Report- " + timeStamp);
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
