package com.alibou.chat.service;


import org.springframework.stereotype.Service;

@Service
public class TicketService {
    private int availableTickets  = 50;//假設有50張票

    // synchronized 保證每次只有一個線程能執行這個方法
    public synchronized String grabTicket(String username) {
        if (availableTickets  > 0) {
            availableTickets --;
            return username + " 搶到票了! 剩餘票數：" + availableTickets ;
        } else {
            return username + " 沒有搶到票，票已經搶完了";
        }
    }
}
