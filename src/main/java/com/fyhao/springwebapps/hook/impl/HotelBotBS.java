package com.fyhao.springwebapps.hook.impl;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.BotServiceHook;
import com.fyhao.springwebapps.hook.HookBS;
import com.fyhao.springwebapps.service.AgentAvailabilityService;
import com.fyhao.springwebapps.service.MessagingService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@HookBS
public class HotelBotBS implements BotServiceHook {
    @Autowired
    MessagingService messagingService;
    @Autowired
    AgentAvailabilityService agentAvailabilityService;
    @Override
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
            conversation.saveContext("finalbookinginfo","");
            if(input.equals("book hotel")) {
                botmenu = "menubookhoteltime";
                messagingService.sendBotMessage(conversation.getId().toString(), "When you want to book hotel?");
            }
            else if(input.equals("do you know about abcde?")) {
                messagingService.sendBotMessage(conversation.getId().toString(), "Sorry I am not understand. Will handover to agent.");
                conversation.saveContext("hint", "1");
                String agentName = agentAvailabilityService.findAgent(conversation);
                if(agentName != null) {
                    conversation.saveContext("state", "agent");
                }
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
                botmenu = "home";
                conversation.saveContext("finalbookinginfo", "book time: " + conversation.findContext("entitytime"));
                messagingService.sendBotMessage(conversation.getId().toString(), "Thank you for booking with us. What else we can help?");
            }
            else {
                botmenu = "home";
                messagingService.sendBotMessage(conversation.getId().toString(), "Thank you see you next time.");
            }
        }
        conversation.saveContext("botmenu", botmenu);
    }
}