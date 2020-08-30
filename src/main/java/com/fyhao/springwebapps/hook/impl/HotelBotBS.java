package com.fyhao.springwebapps.hook.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.hook.BotServiceHook;
import com.fyhao.springwebapps.hook.HookBS;
import com.fyhao.springwebapps.service.AgentAvailabilityService;
import com.fyhao.springwebapps.service.MessagingService;
import com.fyhao.springwebapps.service.QueueService;
import com.fyhao.springwebapps.service.TaskService;

@Component
@HookBS
public class HotelBotBS implements BotServiceHook {
    static Logger logger = LoggerFactory.getLogger(HotelBotBS.class);
    @Autowired
    MessagingService messagingService;
    @Autowired
    AgentAvailabilityService agentAvailabilityService;
    @Autowired
    TaskService taskService;
    @Autowired
    QueueService queueService;
    @Override
    public void processCustomerMessage(Conversation conversation, String input) {
        if(conversation.getChannel().startsWith("webchathotel")) {
            processHotelBot(conversation, input);
        }
    }
    public void processHotelBot(Conversation conversation, String input) {
        logger.info("HotelBotBS processHotelBot " + conversation.getId().toString() + " " + input);
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
                logger.info("HotelBotBS receive do you know?");
                String agentName = null;
                queueService.addToQueue(conversation, "hotel");
                /*
                if(conversation.getChannel().equals("webchathotel")) {
                	agentName = agentAvailabilityService.findAgent(conversation, "hotel");
                }
                else {
                	agentName = agentAvailabilityService.queueSkill(conversation, "hotel");
                }
                logger.info("HotelBotBS agentavailabilityService found agent " + agentName);
                if(agentName != null) {
                    conversation.saveContext("state", "agent");
                    conversation.saveContext("agentName", agentName);
                    logger.info("HotelBotBS going to assign task to " + agentName);
                    messagingService.sendBotMessage(conversation.getId().toString(), "Sorry I am not understand. Will handover to agent.");
                    
                    taskService.assignTask(conversation, agentName);
                }
                else {
                	messagingService.sendBotMessage(conversation.getId().toString(), "Sorry I am not understand. But agent not available.");
                    
                }
                */
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