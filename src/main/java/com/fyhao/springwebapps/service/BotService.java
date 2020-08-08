package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.entity.Conversation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BotService {

    @Autowired
    MessagingService messagingService;
    public void processCustomerMessage(Conversation conversation, String input) {
        if(conversation.getChannel().equals("webchathotel")) {
            processHotelBot(conversation, input);
        }
    }
    public void processHotelBot(Conversation conversation, String input) {
        String botmenu = conversation.findContext("botmenu");
        if(botmenu == null) {
            botmenu = "home";
        }
        if(botmenu.equals("home")) {
            if(input.equals("book hotel")) {
                botmenu = "menubookhoteltime";
                messagingService.sendBotMessage(conversation.getId().toString(), "When you want to book hotel?");
            }
            else {
                messagingService.sendBotMessage(conversation.getId().toString(), "This is abc hotel.");
            }
        }
        else if(botmenu.equals("menubookhoteltime")) {
            botmenu = "menubookhoteltimeconfirm";
            conversation.saveContext("entitytime", input);
            messagingService.sendBotMessage(conversation.getId().toString(), "Confirm to book hotel on " + input + "?");
        }
        else if(botmenu.equals("menubookhoteltimeconfirm")) {
            if(input.equals("yes")) {
                conversation.saveContext("finalbookinginfo", "book time: " + conversation.findContext("entitytime"));
                messagingService.sendBotMessage(conversation.getId().toString(), "Thank you for booking with us.");
            }
            else {
                messagingService.sendBotMessage(conversation.getId().toString(), "Thank you see you next time.");
            }
        }
        conversation.saveContext("botmenu", botmenu);
    }
}