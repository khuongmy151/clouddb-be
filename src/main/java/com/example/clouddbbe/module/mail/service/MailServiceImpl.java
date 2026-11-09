package com.example.clouddbbe.module.mail.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class MailServiceImpl implements MailService{

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;



    @Async("mailTaskExecutor")
    @Override
    public void sendHtmlMail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            // 1. Tạo MimeMessage
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();

            // 2. Dùng Helper (true = hỗ trợ đính kèm, UTF-8 để tránh lỗi font tiếng Việt)
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);

            // 3. Xử lý Thymeleaf Template
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            // Render template ra chuỗi HTML
            String htmlContent = templateEngine.process("mail/" + templateName, context);

            // 4. Set nội dung HTML (true = isHtml)
            helper.setText(htmlContent, true);

            // 5. Gửi mail
            javaMailSender.send(mimeMessage);
            log.info("Đã gửi mail thành công tới: {}", to);

        } catch (MessagingException e) {
            log.error("Lỗi khi gửi mail tới {}: {}", to, e.getMessage());
        }
    }
}
