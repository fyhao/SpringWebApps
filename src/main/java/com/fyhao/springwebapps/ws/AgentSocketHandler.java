package com.fyhao.springwebapps.ws;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.ContextDto;
@Component
public class AgentSocketHandler extends TextWebSocketHandler {
    static Logger logger = LoggerFactory.getLogger(AgentSocketHandler.class);

    public static List<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>();
    
    public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {
        JsonParser springParser = JsonParserFactory.getJsonParser();
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("AgentSocketHandler handleTextMessage: " + message.getPayload());
        Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
        if(jsonMap.get("action").equals("register")) {
            Integer serverport = (Integer)jsonMap.get("serverport");
            String agentid = (String)jsonMap.get("agentid");
            registeragent(agentid, serverport);
            Map<String,Object> response = new HashMap<String,Object>();
            response.put("action", "connectionready");
            session.getAttributes().put("serverport", serverport);
            session.getAttributes().put("agentid", agentid);
            String responseMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(responseMessage));
        }
        else if(jsonMap.get("action").equals("unregister")) {
            Integer serverport = (Integer)session.getAttributes().get("serverport");
            String agentid = (String)jsonMap.get("agentid");
            unregisteragent(agentid, serverport);
        }
        else if(jsonMap.get("action").equals("setAgentStatus")) {
            Integer serverport = (Integer)session.getAttributes().get("serverport");
            String agentid = (String)jsonMap.get("agentid");
            String status = (String)jsonMap.get("status");
            setagentstatus(agentid, status, serverport);
        }
        else if(jsonMap.get("action").equals("sendChatMessage")) {
            logger.info("AgentSocketHandler agent to customer sendChatMessage");
            String conversationid = (String)jsonMap.get("conversationid");
            logger.info("AgentSocketHandler conversationid " + conversationid);
            String chatMessage = (String)jsonMap.get("chatMessage");
            Integer serverport = (Integer)session.getAttributes().get("serverport");
            logger.info("AgentSocketHandler chatMessage " + chatMessage);
            String agentid = (String)jsonMap.get("agentid");
            sendAgentMessage(conversationid, agentid, chatMessage, serverport);
        }
        else if(jsonMap.get("action").equals("closeTask")) {
        	logger.info("AgentSocketHandler agent to system closeTask");
        	String agentid = (String)jsonMap.get("agentid");
        	String taskid = (String)jsonMap.get("taskid");
        	Integer serverport = (Integer)session.getAttributes().get("serverport");
        	closetask(agentid, taskid, serverport);
        }
        else if(jsonMap.get("action").equals("requestTransferToAgent")) {
        	logger.info("AgentSocketHandler agent to system requestTransferToAgent");
        	String agentid = (String)jsonMap.get("agentid");
        	String targetAgentid = (String)jsonMap.get("targetAgentid");
        	String taskid = (String)jsonMap.get("taskid");
        	Integer serverport = (Integer)session.getAttributes().get("serverport");
        	requestTransferToAgent(agentid, targetAgentid, taskid, serverport);
        }
        else if(jsonMap.get("action").equals("requestTransferToSkill")) {
        	logger.info("AgentSocketHandler agent to system requestTransferToSkill");
        	String agentid = (String)jsonMap.get("agentid");
        	String targetSkill = (String)jsonMap.get("targetSkill");
        	String taskid = (String)jsonMap.get("taskid");
        	Integer serverport = (Integer)session.getAttributes().get("serverport");
        	requestTransferToSkill(agentid, targetSkill, taskid, serverport);
        }
        else if(jsonMap.get("action").equals("startTyping")) {
        	logger.info("AgentSocketHandler agent to system startTyping");
        	String agentid = (String)jsonMap.get("agentid");
        	String conversationid = (String)jsonMap.get("conversationid");
        	Integer serverport = (Integer)session.getAttributes().get("serverport");
        	startTyping(agentid, conversationid, serverport);
        }
        else if(jsonMap.get("action").equals("stopTyping")) {
        	logger.info("AgentSocketHandler agent to system stopTyping");
        	String agentid = (String)jsonMap.get("agentid");
        	String conversationid = (String)jsonMap.get("conversationid");
        	Integer serverport = (Integer)session.getAttributes().get("serverport");
        	stopTyping(agentid, conversationid, serverport);
        }
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("AgentSocketHandler afterConnectionEstablished");
        sessions.add(session);
	}
	
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("AgentSocketHandler afterConnectionClosed");
        for(WebSocketSession s : sessions) {
			if(s.getId().equals(session.getId())) {
				sessions.remove(s);
				break;
			}
		}
    }
    public static void sendAgentUnregisteredEvent(String agentid, boolean status, String msg) {
        logger.info("AgentSocketHandler.sendAgentUnregisteredEvent " + agentid + " " + status + " " + msg);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "agentUnregistered");
        jsonMap.put("agentid", agentid);
        jsonMap.put("status", status ? "1" : "0");
        jsonMap.put("msg", msg);
        sendCommandToAgent(agentid, jsonMap);
    }
    public static void sendAgentStatusChangedEvent(String agentid, String oldstatus, String newstatus) {
        logger.info("AgentSocketHandler.sendAgentStatusChangedEvent " + agentid + " " + oldstatus + " " + newstatus);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "agentStatusChanged");
        jsonMap.put("agentid", agentid);
        jsonMap.put("oldstatus", oldstatus);
        jsonMap.put("newstatus", newstatus);
        sendCommandToAgent(agentid, jsonMap);
    }
    public static void sendAgentIncomingTaskEvent(String agentid, String conversationid, String taskid, Map<String,Object> listOfContexts) {
        logger.info("AgentSocketHandler.sendAgentIncomingTaskEvent " + agentid + " " + conversationid + " " + taskid);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "incomingTask");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("taskid", taskid);
        jsonMap.put("context", listOfContexts);
        sendCommandToAgent(agentid, jsonMap);
    }
    public static void sendAgentTaskClosedEvent(String agentid, String taskid) {
    	logger.info("AgentSocketHandler.sendAgentTaskClosedEvent " + agentid + " " + taskid);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "taskClosed");
        jsonMap.put("agentid", agentid);
        jsonMap.put("taskid", taskid);
        sendCommandToAgent(agentid, jsonMap);
    }
    //sendCustomerStartedTypingEvent
    public static void sendCustomerStartedTypingEvent(String agentid, String conversationid) {
        logger.info("AgentSocketHandler.sendCustomerStartedTypingEvent " + agentid + " " + conversationid);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "customerStartedTyping");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        sendCommandToAgent(agentid, jsonMap);
    }
    public static void sendCustomerStoppedTypingEvent(String agentid, String conversationid) {
        logger.info("AgentSocketHandler.sendCustomerStoppedTypingEvent " + agentid + " " + conversationid);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "customerStoppedTyping");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        sendCommandToAgent(agentid, jsonMap);
    }
    public static void sendCommandToAgent(String agentid, Map<String,Object> jsonMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        for(WebSocketSession session : sessions) {
            if(session.getAttributes() != null) {
                String sa = (String)session.getAttributes().get("agentid");
                if(sa != null && sa.equals(agentid)) {
                    try {
                        String responseMessage = objectMapper.writeValueAsString(jsonMap);
                        logger.info("AgentSocketHandler.sendCommandToAgent " + agentid + " - " + responseMessage);
                        session.sendMessage(new TextMessage(responseMessage));
                    } catch (Exception ex) {}
                }
            }
        }
    }
    
    public static void sendCustomerMessage(String conversationid, String taskid, String agentid, String message) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "chatMessageReceived");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("taskid", taskid);
        jsonMap.put("content", message);
        sendCommandToAgent(agentid, jsonMap);
    }
    private void sendAgentMessage(String conversationid, String agentname, String input, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject("http://localhost:" + port + "/webchat/sendagentmessage?id=" + conversationid + "&agentname=" + agentname + "&input=" + URLEncoder.encode(input),
                String.class);
    }


    public String registeragent(String agentName, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/agentterminal/registeragent", request,
                String.class);
        return resp.getBody();
    }
    public String unregisteragent(String agentName, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/agentterminal/unregisteragent", request,
                String.class);
        return resp.getBody();
    }
    private String setagentstatus(String agentName, String status, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentName);
        dto.setStatus(status);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/agentterminal/setagentstatus", request,
                String.class);
        return resp.getBody();
    }
    ///closetask(agentid, taskid, serverport);
    private String closetask(String agentid, String taskid, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentid);
        dto.setTaskid(taskid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/task/closetask", request,
                String.class);
        return resp.getBody();
    }
    private String requestTransferToAgent(String agentid, String targetAgentid, String taskid, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentid);
        dto.setTaskid(taskid);
        dto.setTargetagentid(targetAgentid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/task/requesttransfertoagent", request,
                String.class);
        return resp.getBody();
    }
    private String requestTransferToSkill(String agentid, String targetSkill, String taskid, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentid);
        dto.setTaskid(taskid);
        dto.setTargetskill(targetSkill);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/task/requesttransfertoskill", request,
                String.class);
        return resp.getBody();
    }
    //startTyping(agentid, conversationid, serverport);
    private String startTyping(String agentid, String conversationid, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentid);
        dto.setConversationid(conversationid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/task/agentstarttyping", request,
                String.class);
        return resp.getBody();
    }
    private String stopTyping(String agentid, String conversationid, Integer port) {
        RestTemplate restTemplate = new RestTemplate();
        AgentProfileDto dto = new AgentProfileDto();
        dto.setName(agentid);
        dto.setConversationid(conversationid);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/task/agentstoptyping", request,
                String.class);
        return resp.getBody();
    }
}
    