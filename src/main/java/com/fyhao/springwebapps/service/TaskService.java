package com.fyhao.springwebapps.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fyhao.springwebapps.dto.ContextDto;
import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.AgentTerminal;
import com.fyhao.springwebapps.entity.Context;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Task;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.model.ConversationRepository;
import com.fyhao.springwebapps.model.TaskRepository;
import com.fyhao.springwebapps.util.Util;
import com.fyhao.springwebapps.ws.AgentSocketHandler;
import com.fyhao.springwebapps.ws.ChannelSocketHandler;

@Service
public class TaskService {
    static Logger logger = LoggerFactory.getLogger(TaskService.class);
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    AgentRepository agentRepository;
    @Autowired
    AgentTerminalService agentTerminalService;
    @Autowired
    ConversationRepository conversationRepository;
    public int assignTask(Conversation conversation, String agentid) {
        logger.info("TaskService assignTask " + conversation.getId().toString() + " " + agentid);
        Agent agent = agentRepository.findByName(agentid);
        if(agent == null) {
            return 101;
        }
        // #64 double check concurrent task here
        int activeTaskCount = agent.getActiveTaskCount();
        int maxConcurrentTask = agent.getMaxConcurrentTask();
        if(activeTaskCount >= maxConcurrentTask) {
        	return 102;
        }
        
        Task task = new Task();
        task.setStatus("Open");
        task.setCreatedTime(Util.getSQLTimestamp(new Date()));
        task.setConversation(conversation);
        task.setAgent(agent);
        agent.getTasks().add(task);
        taskRepository.save(task);
        conversation.setTask(task);
        conversation.addActivityWithAgent("conversationAssigned", agentid);
        conversationRepository.save(conversation);
        agentRepository.save(agent);
        Map<String,Object> contexts = new HashMap<String,Object>();
        for(Context context : conversation.getContexts()) {
        	contexts.put(context.getKey(), context.getValue());
        }
        AgentSocketHandler.sendAgentIncomingTaskEvent(agentid, conversation.getId().toString(), task.getId().toString(), contexts);
        ChannelSocketHandler.sendAgentJoinedEvent(agentid, conversation.getId().toString());
        return 0;
    }
    public int closeTask(String agentid, String taskid) {
    	logger.info("TaskService closeTask " + agentid + " " + taskid);
        Agent agent = agentRepository.findByName(agentid);
        if(agent == null) {
            return 101;
        }
        Optional<Task> taskobj = taskRepository.findById(UUID.fromString(taskid));
        if(taskobj.isEmpty()) {
        	return 102;
        }
        Task task = taskobj.get();
        task.getConversation().endConversation();
        task.getConversation().addActivityWithAgent("conversationClosed", agentid);
        conversationRepository.save(task.getConversation());
        task.setStatus("Closed");
        taskRepository.save(task);
        AgentSocketHandler.sendAgentTaskClosedEvent(agentid, taskid);
    	logger.info("TaskService closeTask success " + agentid + " " + taskid);
        return 0;
    }
    public int requestTransferToAgent(String agentid, String taskid, String targetAgentid) {
        logger.info("TaskService requestTransferToAgent " + agentid + " " + taskid + " " + targetAgentid);
        if(agentid.equals(targetAgentid)) {
            logger.info("TaskService requestTransferToAgent 104 " + taskid);
            return 104;
        }
        Agent agent = agentRepository.findByName(agentid);
        if(agent == null) {
            logger.info("TaskService requestTransferToAgent 101 " + taskid);
            return 101;
        }
        Optional<Task> taskobj = taskRepository.findById(UUID.fromString(taskid));
        if(taskobj.isEmpty()) {
            logger.info("TaskService requestTransferToAgent 102 " + taskid);
        	return 102;
        }
        Task task = taskobj.get();
        Agent agent2 = agentRepository.findByName(targetAgentid);
        if(agent2 == null) {
            logger.info("TaskService requestTransferToAgent 103 " + taskid);
            return 103;
        }
        if(agent2.getAgentTerminal() == null) { // not registered / not logged in
            logger.info("TaskService requestTransferToAgent 105 " + taskid);
            return 105;
        }
        if(!agent2.getAgentTerminal().getStatus().equals(AgentTerminal.READY)) { // not ready
            logger.info("TaskService requestTransferToAgent 106 " + taskid);
            return 106;
        }
        if(agent2.getActiveTaskCount() >= agent2.getMaxConcurrentTask()) { // reached max concurrent task
            logger.info("TaskService requestTransferToAgent 107 " + taskid);
            return 107;
        }
        Conversation conversation = task.getConversation();
        if(conversation == null) { 
            logger.info("TaskService requestTransferToAgent 108 " + taskid);
            return 108;
        }
        task.setAgent(agent2);
        taskRepository.save(task);
        Map<String,Object> contexts = new HashMap<String,Object>();
        for(Context context : conversation.getContexts()) {
        	contexts.put(context.getKey(), context.getValue());
        }
        conversation.addTransferToAgentActivity("conversationTransferredToAgent", agentid, targetAgentid);
        conversationRepository.save(conversation);
        AgentSocketHandler.sendAgentIncomingTaskEvent(targetAgentid, conversation.getId().toString(), task.getId().toString(), contexts);
        ChannelSocketHandler.sendAgentJoinedEvent(targetAgentid, conversation.getId().toString());
        return 0;
    }
    public int requestTransferToSkill(String agentid, String taskid, String targetSkill) {
        logger.info("TaskService requestTransferToSkill " + agentid + " " + taskid + " " + targetSkill);
        Agent agent = agentRepository.findByName(agentid);
        if(agent == null) {
            logger.info("TaskService requestTransferToSkill 101 " + taskid);
            return 101;
        }
        Optional<Task> taskobj = taskRepository.findById(UUID.fromString(taskid));
        if(taskobj.isEmpty()) {
            logger.info("TaskService requestTransferToSkill 102 " + taskid);
        	return 102;
        }
        Task task = taskobj.get();
         // during transfer to skill, set current agent state as busy first, so that skill hunt will not find him
        String oldstatus = agentTerminalService.getAgentStatus(agentid);
        agentTerminalService.setAgentStatus(agentid, AgentTerminal.BUSY);
        AgentTerminal term = agentTerminalService.getMostAvailableAgent(targetSkill);
        if(term == null) {
        	agentTerminalService.setAgentStatus(agentid, oldstatus);
            logger.info("TaskService requestTransferToSkill 103 " + taskid);
            return 103;
        }
        agentTerminalService.setAgentStatus(agentid, oldstatus);
        Conversation conversation = task.getConversation();
        if(conversation == null) { 
            logger.info("TaskService requestTransferToSkill 104 " + taskid);
            return 104;
        }
        task.setAgent(term.getAgent());
        taskRepository.save(task);
        Map<String,Object> contexts = new HashMap<String,Object>();
        for(Context context : conversation.getContexts()) {
        	contexts.put(context.getKey(), context.getValue());
        }
        conversation.addTransferToAgentActivity("conversationTransferredToSkill", agentid, targetSkill);
        conversationRepository.save(conversation);
        AgentSocketHandler.sendAgentIncomingTaskEvent(term.getAgent().getName(), conversation.getId().toString(), task.getId().toString(), contexts);
        ChannelSocketHandler.sendAgentJoinedEvent(term.getAgent().getName(), conversation.getId().toString());
        return 0;
    }
    public int agentStartTyping(String agentid, String conversationid) {
        ChannelSocketHandler.sendAgentStartedTypingEvent(agentid, conversationid);
        return 0;
    }
    public int agentStopTyping(String agentid, String conversationid) {
        ChannelSocketHandler.sendAgentStoppedTypingEvent(agentid, conversationid);
        return 0;
    }
    public int inviteConference(String agentid, String targetAgentid, String conversationid) {
    	Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversationid));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        if(agentid.equals(targetAgentid)) {
        	return 101;
        }
        String activeAgentsStr = conversation.findContext("activeAgents");
        if(activeAgentsStr == null) {
        	return 102;
        }
        String[] activeAgents = activeAgentsStr.split("\\,");
        if(!Arrays.asList(activeAgents).contains(agentid)) {
        	return 103;
        }
        if(Arrays.asList(activeAgents).contains(targetAgentid)) {
        	return 104;
        }
        String bargeinAgentsStr = conversation.findContext("bargeinAgents");
        if(bargeinAgentsStr != null) {
        	if(Arrays.asList(bargeinAgentsStr.split("\\,")).contains(targetAgentid)) {
        		return 105;
        	}
        }
        String invitedAgent = conversation.findContext("invitedAgent");
        if(invitedAgent != null && !invitedAgent.isEmpty()) {
        	return 106;
        }
        conversation.saveContext("invitedAgent", targetAgentid);
        conversationRepository.save(conversation);
        AgentSocketHandler.sendAgentInvitedEvent(targetAgentid, conversationid);
    	return 0;
    }
    public int acceptInvite(String agentid, String conversationid) {
    	Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversationid));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        String activeAgentsStr = conversation.findContext("activeAgents");
        if(activeAgentsStr == null) {
        	return 102;
        }
        String[] activeAgents = activeAgentsStr.split("\\,");
        if(Arrays.asList(activeAgents).contains(agentid)) {
        	return 103;
        }
        String bargeinAgentsStr = conversation.findContext("bargeinAgents");
        if(bargeinAgentsStr != null) {
        	if(Arrays.asList(bargeinAgentsStr.split("\\,")).contains(agentid)) {
        		return 105;
        	}
        }
        String invitedAgent = conversation.findContext("invitedAgent");
        if(invitedAgent == null) {
        	return 106;
        }
        if(!invitedAgent.equals(agentid)) {
        	return 107;
        }
        List<String> activeAgentList = new ArrayList<String>(Arrays.asList(activeAgents));
        activeAgentList.add(agentid);
        conversation.saveContext("activeAgents", joinString(activeAgentList));
        conversation.saveContext("invitedAgent", "");
        conversationRepository.save(conversation);
        Task task = conversation.getTask();
        Map<String,Object> contexts = new HashMap<String,Object>();
        for(Context context : conversation.getContexts()) {
        	contexts.put(context.getKey(), context.getValue());
        }
        AgentSocketHandler.sendAgentIncomingTaskEvent(agentid, conversation.getId().toString(), task.getId().toString(), contexts);
        ChannelSocketHandler.sendAgentJoinedEvent(agentid, conversation.getId().toString());
        return 0;
    }
    public int bargeinConversation(String agentid, String conversationid) {
    	Optional<Conversation> conv = conversationRepository.findById(UUID.fromString(conversationid));
        if(conv.isEmpty()) {
            return 100;
        }
        Conversation conversation = conv.get();
        String activeAgentsStr = conversation.findContext("activeAgents");
        if(activeAgentsStr == null) {
        	return 102;
        }
        String[] activeAgents = activeAgentsStr.split("\\,");
        if(Arrays.asList(activeAgents).contains(agentid)) {
        	return 103;
        }
        String invitedAgent = conversation.findContext("invitedAgent");
        if(invitedAgent != null && invitedAgent.equals(agentid)) {
        	return 107;
        }
        String bargeinAgentsStr = conversation.findContext("bargeinAgents");
        List<String> bargeinAgentList = new ArrayList<String>();
        if(bargeinAgentsStr != null) {
        	if(Arrays.asList(bargeinAgentsStr.split("\\,")).contains(agentid)) {
        		return 105;
        	}
        	bargeinAgentList = Arrays.asList(bargeinAgentsStr.split("\\,"));
        }
        bargeinAgentList.add(agentid);
        conversation.saveContext("bargeinAgents", joinString(bargeinAgentList));
        conversationRepository.save(conversation);
        return 0;
    }
    private String joinString(List<String> list) {
    	String s = "";
    	String comma = "";
    	for(String item : list) {
    		s += comma;
    		s += item;
    		comma = ",";
    	}
    	return s;
    }
    public int getAgentTasksCount(String agentid) {
        Agent agent = agentRepository.findByName(agentid);
        if(agent == null) {
            return -1;
        }
        return agent.getTasks().size();
    }
    public int getAgentActiveTasksCount(String agentid) {
    	Agent agent = agentRepository.findByName(agentid);
    	if(agent == null) {
            return -1;
        }
    	return agent.getActiveTaskCount();
    }
}