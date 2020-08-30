package com.fyhao.springwebapps.hook.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.dto.CQueueDto;
import com.fyhao.springwebapps.dto.SkillDto;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Skill;
import com.fyhao.springwebapps.hook.BotServiceHook;
import com.fyhao.springwebapps.hook.HookBS;
import com.fyhao.springwebapps.service.AgentAvailabilityService;
import com.fyhao.springwebapps.service.AgentProfileService;
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
    @Autowired
    AgentProfileService agentProfileService;
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
		else if(input.startsWith("listqueue")) {
			List<Map<String, Object>> list = queueService.getCQueueList();
			List<String> queueList = new ArrayList<String>();
			for(Map<String,Object> item : list) {
				String queuename = (String)item.get("queuename");
				queueList.add(queuename);
			}
			String msg = "List of queues: " + queueList.toString();
			messagingService.sendBotMessage(id, msg);
		}
		else if(input.startsWith("addqueue:")) {
			String prof = input.substring("addqueue:".length());
			String[] arr = prof.split("\\:");
			if(arr.length != 5) {
				messagingService.sendBotMessage(id, "addqueue:name:waittime:skilllist:maxlimit:queuepriority");
				return;
			}
        	String cqueuename = arr[0];
        	long maxwaittime = Long.parseLong(arr[1]);
        	String skilllist = arr[2];
        	CQueueDto dto = new CQueueDto();
        	dto.setName(cqueuename);
        	dto.setMaxwaittime(maxwaittime);
        	dto.setSkilllist(skilllist);
        	dto.setMaxlimit(Long.parseLong(arr[3]));
        	dto.setPriority(Long.parseLong(arr[4]));
        	agentProfileService.createCQueueProfile(dto);
        	messagingService.sendBotMessage(id, "New queue is created");
		}
		else if(input.equals("listskill")) {
			String msg = "List of skills: ";
			for(Skill skill: agentProfileService.getAllSkills()) {
				msg += ", " + skill.getName() + " ";
			}
			messagingService.sendBotMessage(id, msg);
		}
		else if(input.startsWith("addskill:")) {
			String skill = input.substring("addskill:".length());
			SkillDto skillDto = new SkillDto();
			skillDto.setName(skill);
			agentProfileService.createSkillProfile(skillDto);
			messagingService.sendBotMessage(id, "New skill created");
		}
		else if(input.startsWith("assignskill:")) {
			String prof = input.substring("assignskill:".length());
			String[] arr = prof.split("\\:");
			String agentid = arr[0];
			String skill = arr[1];
			AgentSkillDto dto = new AgentSkillDto();
			dto.setAction(AgentSkillDto.ASSIGNED_TO_AGENT);
			dto.setAgent(agentid);
			dto.setSkill(skill);
			agentProfileService.assignAgentSkillAction(dto);
			messagingService.sendBotMessage(id, "Assigned");
		}
		else {
			messagingService.sendBotMessage(id, "Invalid command");
		}
	}
}
