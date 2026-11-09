package com.example.clouddbbe.core.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AsyncConfig {

    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 1. Core Pool Size: Bằng số luồng của CPU (4)
        // Luôn có sẵn 4 luồng để xử lý ngay lập tức.
        executor.setCorePoolSize(4);

        // 2. Max Pool Size: Gấp đôi số luồng CPU (8)
        // Khi hàng đợi đầy, có thể nâng lên tối đa 8 luồng để gửi mail kịp thời.
        executor.setMaxPoolSize(8);

        // 3. Queue Capacity: 100
        // Nếu có 100 request gửi mail cùng lúc, hệ thống sẽ xếp hàng chờ.
        // Chỉ khi hàng đợi này đầy (quá 100), nó mới kích hoạt các luồng từ 4 lên 8.
        executor.setQueueCapacity(100);

        // 4. Keep Alive: 60 giây
        // Những luồng nào vượt quá 4 (từ luồng 5 đến 8) nếu rảnh rỗi 60s sẽ tự hủy để nhả RAM.
        executor.setKeepAliveSeconds(60);

        // 5. Tên luồng: Giúp dễ debug trong log
        executor.setThreadNamePrefix("MailSender-");

        // 6. Chính sách khi hàng đợi và luồng đều quá tải
        // CallerRunsPolicy: Nếu quá tải, luồng chính (API) sẽ tự gửi mail thay vì văng lỗi (chấp nhận chậm API đó 1 chút còn hơn mất mail)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
