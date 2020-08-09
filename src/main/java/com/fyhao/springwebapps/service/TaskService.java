package com.fyhao.springwebapps.service;

import java.util.Date;

import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.entity.Task;
import com.fyhao.springwebapps.model.TaskRepository;
import com.fyhao.springwebapps.model.AgentRepository;
import com.fyhao.springwebapps.util.Util;
import com.fyhao.springwebapps.ws.AgentSocketHandler;

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
        // TODO check concurrent task here
        Task task = new Task();
        task.setStatus("Open");
        task.setCreatedTime(Util.getSQLTimestamp(new Date()));
        task.setConversation(conversation);
        task.setAgent(agent);
        taskRepository.save(task);
        AgentSocketHandler.sendAgentIncomingTaskEvent(agentid, conversation.getId().toString());
        return 0;
    }

    public int getAgentTasksCount(String agentid) {
        Agent agent = agentRepository.findByName(agentid);
        if(agent == null) {
            return -1;
        }
        return agent.getTasks().size();
    }
}