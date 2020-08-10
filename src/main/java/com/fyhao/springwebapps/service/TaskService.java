package com.fyhao.springwebapps.service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Task;
import com.fyhao.springwebapps.model.TaskRepository;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.util.Util;
import com.fyhao.springwebapps.ws.AgentSocketHandler;
import com.fyhao.springwebapps.ws.ChannelSocketHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    static Logger logger = LoggerFactory.getLogger(TaskService.class);
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    AgentRepository agentRepository;
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
        taskRepository.save(task);
        AgentSocketHandler.sendAgentIncomingTaskEvent(agentid, conversation.getId().toString(), task.getId().toString());
        ChannelSocketHandler.sendAgentJoinedEvent(conversation.getId().toString());
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
        // TODO Check if conversation ended, if not ended, cannot close task
        task.setStatus("Closed");
        taskRepository.save(task);
        AgentSocketHandler.sendAgentTaskClosedEvent(agentid, taskid);
    	logger.info("TaskService closeTask success " + agentid + " " + taskid);
        return 0;
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