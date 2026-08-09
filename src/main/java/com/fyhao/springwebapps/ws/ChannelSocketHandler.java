package com.fyhao.springwebapps.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.service.MessagingService;

@Component
public class ChannelSocketHandler extends TextWebSocketHandler {
    static Logger logger = LoggerFactory.getLogger(ChannelSocketHandler.class);

    public static List<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>();
    private final MessagingService messagingService;

    public ChannelSocketHandler(MessagingService messagingService) {
        this.messagingService = messagingService;
    }
    
    public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {
        JsonParser springParser = JsonParserFactory.getJsonParser();
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("ChannelSocketHandler handleTextMessage: " + message.getPayload());
        Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
        if(jsonMap.get("action").equals("register")) {
            String conversationid = (String)jsonMap.get("conversationid");
            Map<String,Object> response = new HashMap<String,Object>();
            response.put("action", "connectionready");
            response.put("conversationid", conversationid);
            session.getAttributes().put("conversationid", conversationid);
            String responseMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(responseMessage));
            checkExistingMessageForCustomer(conversationid);
        }
        else if(jsonMap.get("action").equals("sendChatMessage")) {
            logger.info("ChannelSocketHandler customer to system sendChatMessage");
            String conversationid = (String)jsonMap.get("conversationid");
            logger.info("ChannelSocketHandler conversationid " + conversationid);
            String chatMessage = (String)jsonMap.get("chatMessage");
            logger.info("ChannelSocketHandler chatMessage " + chatMessage);
            sendCustomerMessage(conversationid, chatMessage);
        }
        else if(jsonMap.get("action").equals("startTyping")) {
            logger.info("ChannelSocketHandler customer to system startTyping");
            String conversationid = (String)jsonMap.get("conversationid");
            logger.info("ChannelSocketHandler conversationid " + conversationid);
            sendCustomerStartTyping(conversationid);
        }
        else if(jsonMap.get("action").equals("stopTyping")) {
            logger.info("ChannelSocketHandler customer to stopTyping startTyping");
            String conversationid = (String)jsonMap.get("conversationid");
            logger.info("ChannelSocketHandler conversationid " + conversationid);
            sendCustomerStopTyping(conversationid);
        }
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("ChannelSocketHandler afterConnectionEstablished");
        sessions.add(session);
	}
	
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        logger.info("ChannelSocketHandler afterConnectionClosed");
        for(WebSocketSession s : sessions) {
			if(s.getId().equals(session.getId())) {
				sessions.remove(s);
				break;
			}
		}
    }
    public static void sendAgentJoinedEvent(String agentid, String conversationid) {
        logger.info("ChannelSocketHandler sendAgentJoinedEvent " + conversationid);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "agentJoined");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        sendCommandToCustomer(conversationid, jsonMap);
    }
    public static void sendAgentStartedTypingEvent(String agentid, String conversationid) {
        logger.info("ChannelSocketHandler sendAgentStartedTypingEvent " + conversationid);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "agentStartedTyping");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("agentid", agentid);
        sendCommandToCustomer(conversationid, jsonMap);
    }
    public static void sendAgentStoppedTypingEvent(String agentid, String conversationid) {
        logger.info("ChannelSocketHandler sendAgentStartedTypingEvent " + conversationid);
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "agentStoppedTyping");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("agentid", agentid);
        sendCommandToCustomer(conversationid, jsonMap);
    }
    public static void sendCommandToCustomer(String conversationid, Map<String,Object> jsonMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        for(WebSocketSession session : sessions) {
            if(session.getAttributes() != null) {
                String sa = (String)session.getAttributes().get("conversationid");
                if(sa != null && sa.equals(conversationid)) {
                    try {
                        String responseMessage = objectMapper.writeValueAsString(jsonMap);
                        session.sendMessage(new TextMessage(responseMessage));
                    } catch (Exception ex) {}
                }
            }
        }
    }
    public static void sendChatMessageToCustomer(String conversationid, String message) {
    	sendChatMessageToCustomer(conversationid, message, new java.util.Date());
    }
    public static void sendChatMessageToCustomer(String conversationid, String message, java.util.Date date) {
        logger.info("ChannelSocketHandler.sendChatMessageToCustomer " + conversationid + " - " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        for(WebSocketSession session : sessions) {
            String id = (String)session.getAttributes().get("conversationid");
            if(id.equals(conversationid)) {
                Map<String,Object> response = new HashMap<String,Object>();
                response.put("action", "chatMessageReceived");
                response.put("conversationid", conversationid);
                response.put("content", message);
                response.put("senttime", date.toString());
                try {
                    String responseMessage = objectMapper.writeValueAsString(response);
                    session.sendMessage(new TextMessage(responseMessage));
                } catch (Exception ex) {}
            }
		}
    }
    public void sendCustomerMessage(String conversationid, String message) {
        logger.info("ChannelSocketHandler sendCustomerMessage " + conversationid + " - " + message);
        messagingService.sendCustomerMessage(conversationid, message);
    }
    //sendCustomerStartTyping
    public void sendCustomerStartTyping(String conversationid) {
        logger.info("ChannelSocketHandler sendCustomerStartTyping " + conversationid );
        messagingService.sendCustomerStartTyping(conversationid);
    }
    public void sendCustomerStopTyping(String conversationid) {
        logger.info("ChannelSocketHandler sendCustomerStopTyping " + conversationid );
        messagingService.sendCustomerStopTyping(conversationid);
    }
    //checkExistingMessageForCustomer(conversationid);
    public void checkExistingMessageForCustomer(String conversationid) {
        logger.info("ChannelSocketHandler checkExistingMessageForCustomer " + conversationid );
        messagingService.checkExistingMessageForCustomer(conversationid);
    }
}
