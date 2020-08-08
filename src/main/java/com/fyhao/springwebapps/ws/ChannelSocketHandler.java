package com.fyhao.springwebapps.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChannelSocketHandler extends TextWebSocketHandler {
    static Logger logger = LoggerFactory.getLogger(ChannelSocketHandler.class);

    public void handleTextMessage(WebSocketSession session, TextMessage message)
			throws InterruptedException, IOException {
        JsonParser springParser = JsonParserFactory.getJsonParser();
        ObjectMapper objectMapper = new ObjectMapper();
        logger.info("ChannelSocketHandler handleTextMessage: " + message.getPayload());
        Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
        if(jsonMap.get("action").equals("register")) {
            String conversationid = (String)jsonMap.get("conversationid");
            Map<String,Object> response = new HashMap<String,Object>();
            response.put("action", "ready");
            response.put("conversationid", conversationid);
            String responseMessage = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(responseMessage));
        }
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		logger.info("ChannelSocketHandler afterConnectionEstablished");
	}
	
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		
		logger.info("ChannelSocketHandler afterConnectionClosed");
	}
	

}