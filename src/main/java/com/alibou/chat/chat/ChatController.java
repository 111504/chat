package com.alibou.chat.chat;

import com.alibou.chat.service.TicketService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final TicketService ticketService;

    public ChatController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    //客戶端向 /app/chat.sendMessage 發起訊息 ，所有訂閱 /topic的客戶端都會接收到該訊息。
    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(
            @Payload ChatMessage chatMessage
    ) {
        return chatMessage;
    }

    //讓新用戶加入聊天室 將此事件廣播給所有訂閱者知道
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        //Add username in web socket session
        headerAccessor.getSessionAttributes().put("username",chatMessage.getSender());
        return chatMessage;
    }

    @MessageMapping("/chat.grabTicket")
    @SendTo("/topic/public")
    public ChatMessage grabTicket(@Payload ChatMessage chatMessage) {
        String response = ticketService.grabTicket(chatMessage.getSender());

        // 返回的 ChatMessage 包含了搶票的結果
        ChatMessage responseMessage = new ChatMessage();
        responseMessage.setType(MessageType.CHAT);
        responseMessage.setSender("系統");
        responseMessage.setContent(response);
        return responseMessage;
    }

}

