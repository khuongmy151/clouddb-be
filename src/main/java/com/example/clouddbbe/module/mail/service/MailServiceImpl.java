package com.example.clouddbbe.module.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async("mailTaskExecutor")
    @Override
    public void sendHtmlMail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // 1. Tạo MimeMessage
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // 2. Dùng Helper
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail, "CloudDB");
            helper.setTo(to);
            helper.setSubject(subject);

            // 3. Xử lý Thymeleaf Template
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            String htmlContent = templateEngine.process(templateName, context);

            // 4. Set nội dung HTML
            helper.setText(htmlContent, true);

            // 5. Gửi mail
            javaMailSender.send(mimeMessage);
            log.info("Đã gửi mail thành công tới: {}", to);

        } catch (Exception e) {
            log.error("Lỗi khi gửi mail tới {}: {}", to, e.getMessage());
        }
    }
}