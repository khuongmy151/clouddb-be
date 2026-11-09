package com.example.clouddbbe.module.mail.service;

import java.util.Map;


public interface MailService {

    /**
     * Gửi mail HTML sử dụng template Thymeleaf, chạy bất đồng bộ (@Async).
     *
     * @param to           Địa chỉ email người nhận
     * @param subject      Tiêu đề email
     * @param templateName Tên file template (VD: "welcome", không cần đuôi .html)
     * @param variables    Map chứa các biến động để truyền vào template
     */
    void sendHtmlMail(String to, String subject, String templateName, Map<String, Object> variables);
}
