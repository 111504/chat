package com.alibou.chat.config;
import com.alibou.chat.chat.ChatMessage;
import com.alibou.chat.chat.MessageType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ScheduledTask implements SchedulingConfigurer {
    private final SimpMessageSendingOperations messagingTemplate;

    public ScheduledTask(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    // 從配置文件中讀取系統消息的間隔時間
    @Value("${scheduler.system-message-interval-1}")
    private long systemMessageInterval;

    @Value("${scheduler.system-message-interval-2}")
    private long systemMessageInterval2;
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        // 使用 ScheduledExecutorService 來執行定時任務
        //newScheduledThreadPool 使用線程池來管理兩個線程
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(2);

        taskRegistrar.setScheduler(executorService);
        /*
         * @param 執行任務
         * @param 延遲時間
         * @param 執行間隔
         * @param 時間單位
         */
        executorService.scheduleAtFixedRate(() -> sendSystemMessage("這是來自第一個線程的廣播消息"), 0, systemMessageInterval, TimeUnit.MILLISECONDS);
        // 註冊第二個定時廣播任務
        executorService.scheduleAtFixedRate(() -> sendSystemMessage("系統消息2：這是來自第二個線程的廣播消息"), 0, systemMessageInterval2, TimeUnit.MILLISECONDS);
    }

    // 發送系統消息
    private void sendSystemMessage(String content) {
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("系統");
        message.setContent(content);

        // 發送到 /topic/public，所有訂閱的用戶都能收到
        messagingTemplate.convertAndSend("/topic/public", message);
    }
}
