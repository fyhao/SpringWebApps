package com.fyhao.springwebapps.ws;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.service.MessagingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public static void sendCommandToAgent(String agentid, Map<String,Object> jsonMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        for(WebSocketSession session : sessions) {
            String sa = (String)session.getAttributes().get("agentid");
            if(sa.equals(agentid)) {
                try {
                    String responseMessage = objectMapper.writeValueAsString(jsonMap);
                    logger.info("AgentSocketHandler.sendCommandToAgent " + agentid + " - " + responseMessage);
                    session.sendMessage(new TextMessage(responseMessage));
                } catch (Exception ex) {}
            }
        }
    }
    
    public void sendCustomerMessage(String conversationid, String message, Integer port) {
        logger.info("AgentSocketHandler sendCustomerMessage " + conversationid + " - " + message);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=" + URLEncoder.encode(message),
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
}
    