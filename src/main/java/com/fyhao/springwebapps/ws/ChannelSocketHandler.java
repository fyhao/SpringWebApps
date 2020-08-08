package com.fyhao.springwebapps.ws;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.service.MessagingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChannelSocketHandler extends TextWebSocketHandler {
    static Logger logger = LoggerFactory.getLogger(ChannelSocketHandler.class);

    
    public static List<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>();
    
    public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {
        JsonParser springParser = JsonParserFactory.getJsonParser();
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("ChannelSocketHandler handleTextMessage: " + message.getPayload());
        Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
        if(jsonMap.get("action").equals("register")) {
            String conversationid = (String)jsonMap.get("conversationid");
            Integer serverport = (Integer)jsonMap.get("serverport");
            Map<String,Object> response = new HashMap<String,Object>();
            response.put("action", "ready");
            response.put("conversationid", conversationid);
            session.getAttributes().put("conversationid", conversationid);
            session.getAttributes().put("serverport", serverport);
            String responseMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(responseMessage));
        }
        else if(jsonMap.get("action").equals("sendChatMessage")) {
            logger.info("ChannelSocketHandler customer to system sendChatMessage");
            String conversationid = (String)jsonMap.get("conversationid");
            logger.info("ChannelSocketHandler conversationid " + conversationid);
            String chatMessage = (String)jsonMap.get("chatMessage");
            Integer serverport = (Integer)session.getAttributes().get("serverport");
            logger.info("ChannelSocketHandler chatMessage " + chatMessage);
            sendCustomerMessage(conversationid, chatMessage, serverport);
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
    
    public static void sendChatMessageToCustomer(String conversationid, String message) {
        logger.info("ChannelSocketHandler.sendChatMessageToCustomer " + conversationid + " - " + message);
        ObjectMapper objectMapper = new ObjectMapper();
        for(WebSocketSession session : sessions) {
            String id = (String)session.getAttributes().get("conversationid");
            if(id.equals(conversationid)) {
                Map<String,Object> response = new HashMap<String,Object>();
                response.put("action", "chatMessageReceived");
                response.put("conversationid", conversationid);
                response.put("content", message);
                try {
                    String responseMessage = objectMapper.writeValueAsString(response);
                    session.sendMessage(new TextMessage(responseMessage));
                } catch (Exception ex) {}
            }
		}
    }
    public void sendCustomerMessage(String conversationid, String message, Integer port) {
        logger.info("ChannelSocketHandler sendCustomerMessage " + conversationid + " - " + message);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getForObject("http://localhost:" + port + "/webchat/sendmessage?id=" + conversationid + "&input=" + URLEncoder.encode(message),
                String.class);
    }
}