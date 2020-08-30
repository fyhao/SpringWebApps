package com.fyhao.springwebapps.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fyhao.springwebapps.dto.CustomEvent;
import com.fyhao.springwebapps.dto.QueueDto;
import com.fyhao.springwebapps.entity.AgentTerminal;
import com.fyhao.springwebapps.entity.Conversation;
import com.fyhao.springwebapps.model.ConversationRepository;

@Service
public class QueueService implements ApplicationListener<CustomEvent> {
	
    static List<QueueDto> queues = new ArrayList<QueueDto>();
    static Map<String, ArrayList<QueueDto>> listOfQueues = new HashMap<String, ArrayList<QueueDto>>();
    static List<Map<String, Object>> cqueueList = new ArrayList<Map<String,Object>>();
	static {
        // mimic queue setting data
        Map<String, Object> cqueue = new HashMap<String, Object>();
        cqueue.put("queuename", "hotel");
        cqueue.put("maxwaittime", 5000L);
        cqueue.put("skillList", "hotel");
        cqueueList.add(cqueue);
        for(Map<String,Object> item : cqueueList) {
            String queuename = (String)item.get("queuename");
            ArrayList<QueueDto> queues = new ArrayList<QueueDto>();
            listOfQueues.put(queuename, queues);
        }
    }
	@Autowired
	EventPublisher publisher;

	public void addToQueue(Conversation conversation, String skillName) {
		QueueDto queue = new QueueDto();
		queue.setConversation(conversation);
		queue.setEnteredTime(new Date());
		queue.setStatus("active");
        //queues.add(queue);
        listOfQueues.get(skillName).add(queue);
    	conversation.addActivityWithSkill("conversationQueued", skillName);
    	conversationRepository.save(conversation);
    	publisher.publishEvent("conversationQueued");
	}
	
	public static long maxWaitTime = 5000;
	
	@Autowired
	AgentTerminalService agentTerminalService;
	@Autowired
	MessagingService messagingService;
	@Autowired
	TaskService taskService;
	@Autowired
	ConversationRepository conversationRepository;
	
	@Scheduled(fixedRate = 100)
	@Transactional
	public void checkExpiry() {
        for(Map.Entry<String,ArrayList<QueueDto>> entry : listOfQueues.entrySet()) {
            String queueName = entry.getKey();
            long maxWaitTime = (long)cqueueList.stream().filter(x -> {
                return x.get("queuename").equals(queueName);
            }).findFirst().get().get("maxwaittime");
            List<QueueDto> queues = entry.getValue();
            for(QueueDto q : queues) {
                if(!q.getStatus().equals("active")) continue;
                Date now = new Date();
                Conversation conversation1 = q.getConversation();
                if(now.getTime() - q.getEnteredTime().getTime() > maxWaitTime) {
                    q.setStatus("toBeRemoved");
                    messagingService.sendBotMessage(conversation1.getId().toString(), "Sorry I am not understand. But agent not available.");
                    continue;
                }
            }
            for(int i = queues.size() - 1; i >= 0; i--) {
                if(!queues.get(i).getStatus().equals("active")) {
                    queues.remove(i);
                }
            }
        }
	}
	//@Scheduled(fixedRate = 20)
	//@Transactional
	public void checkQueue() {
		 for(Map.Entry<String,ArrayList<QueueDto>> entry : listOfQueues.entrySet()) {
	        String queueName = entry.getKey();
	        String skillList = (String)cqueueList.stream().filter(x -> {
                return x.get("queuename").equals(queueName);
            }).findFirst().get().get("skillList");
	        List<QueueDto> queues = entry.getValue();
	        for(QueueDto q : queues) {
                if(!q.getStatus().equals("active")) continue;
                Date now = new Date();
                Conversation conversation1 = q.getConversation();
                AgentTerminal term = agentTerminalService.getMostAvailableAgent(skillList);
                if(term != null) {
                    q.setStatus("toBeRemoved");
                    String agentName = term.getAgent().getName();
                    if(agentName != null) {
                        Conversation conversation = conversationRepository.findById(conversation1.getId()).get();
                        conversation.saveContext("state", "agent");
                        conversation.saveContext("agentName", agentName);
                        conversation.addActivityWithAgent("conversationOffered", agentName);
                        conversationRepository.save(conversation);
                        messagingService.sendBotMessage(conversation.getId().toString(), "Sorry I am not understand. Will handover to agent.");
                        taskService.assignTask(conversation, agentName);
                    }
                    continue;
                }
            }
        }
	}
	@Bean
	public ThreadPoolTaskScheduler taskScheduler() {
		ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

	    scheduler.setPoolSize(2);
	    scheduler.setThreadNamePrefix("scheduled-task-");
	    scheduler.setDaemon(true);

	    return scheduler;
	}
	@Override
	public void onApplicationEvent(CustomEvent event) {
		String[] checkQueueEvents = new String[] {"agentRegistered", "agentReady", "conversationQueued"};
		if(Arrays.asList(checkQueueEvents).contains(event.getAction())) {
			checkQueue();
		}
	}
}
