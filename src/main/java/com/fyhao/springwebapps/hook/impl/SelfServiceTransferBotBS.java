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
public class SelfServiceTransferBotBS implements BotServiceHook {
    static Logger logger = LoggerFactory.getLogger(SelfServiceTransferBotBS.class);
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
		if(conversation.getChannel().startsWith("webchatselfservicetransfer")) {
            processBot(conversation, input);
        }
	}
	public void processBot(Conversation conversation, String input) {
		System.out.println("ssbot input: " + input);
		String id = conversation.getId().toString();
		if(input.startsWith("queue:")) {
			String queueToGo = input.substring("queue:".length());
			conversation.saveContext("queueToGo", queueToGo);
			messagingService.sendBotMessage(id, "set queueToGo = " + queueToGo);
		}
		else if(input.startsWith("checkqueue")) {
			String queueToGo = conversation.findContext("queueToGo");
			if(queueToGo == null) {
				messagingService.sendBotMessage(id, "queueToGo is null, send queue:xxxx to set queueToGo");
				return;
			}
			messagingService.sendBotMessage(id, "check queueToGo = " + queueToGo);
		}
		else if(input.startsWith("transfer")) {
			String queueToGo = conversation.findContext("queueToGo");
			if(queueToGo == null) {
				messagingService.sendBotMessage(id, "queueToGo is null, send queue:xxxx to set queueToGo");
				return;
			}
			queueService.addToQueue(conversation, queueToGo);
			messagingService.sendBotMessage(id, "You added into queue: " + queueToGo);
		}
		else {
			messagingService.sendBotMessage(id, "Invalid command");
		}
	}
}
