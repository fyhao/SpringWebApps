import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class MyAgentClient {
    WebSocketClient webSocketClient;
    WebSocketSession webSocketSession;
    int port;
    TestingWebApplicationTests that;
    String agentid;
    CompletableFuture<WebSocketSession> agentEstablished;
    CompletableFuture<Map<String, Object>> incomingJsonMapReceived;
    public MyAgentClient(TestingWebApplicationTests that, String agentid) {
        port = that.port;
        this.that = that;
        this.agentid = agentid;
    }
    public void start() {
        try {
            webSocketClient = new StandardWebSocketClient();

            webSocketSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
                
                @Override
                public void handleTextMessage(WebSocketSession session, TextMessage message) {
                    JsonParser springParser = JsonParserFactory.getJsonParser();
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
                    if(incomingJsonMapReceived != null) {
                        incomingJsonMapReceived.complete(jsonMap);
                        incomingJsonMapReceived = null;
                    }
                }

                @Override
                public void afterConnectionEstablished(WebSocketSession session) {
                    if(agentEstablished != null) {
                        agentEstablished.complete(session);
                        agentEstablished = null;
                    }
                }
            }, new WebSocketHttpHeaders(), URI.create("ws://localhost:" + port + "/agent")).get();

        } catch (Exception e) {
            // LOGGER.error("Exception while accessing websockets", e);
        }
    }
    public CompletableFuture<WebSocketSession> waitNextAgentEstablishedEvent() {
        agentEstablished = new CompletableFuture<WebSocketSession>();
        return agentEstablished;
    }
    public CompletableFuture<Map<String, Object>> waitNextIncomingTextMessage() {
        incomingJsonMapReceived = new CompletableFuture<Map<String, Object>>();
        return incomingJsonMapReceived;
    }
    public void sendChatMessage(String conversationid, String chatMessage) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "sendChatMessage");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("agentid", agentid);
        jsonMap.put("chatMessage", chatMessage);
        sendMessage(webSocketSession, jsonMap);
    }
    public void registerAgentSession() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "register");
        jsonMap.put("agentid", agentid);
        jsonMap.put("serverport", port);
        sendMessage(webSocketSession, jsonMap);
    }
    public void unregisterAgentSesssion() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "unregister");
        jsonMap.put("agentid", agentid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void setAgentStatus(String status) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "setAgentStatus");
        jsonMap.put("agentid", agentid);
        jsonMap.put("status", status);
        sendMessage(webSocketSession, jsonMap);
    }

    public void sendMessage(WebSocketSession session, Map<String, Object> jsonMap) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(jsonMap);
            session.sendMessage(new TextMessage(message));
        } catch (Exception ex) {

        }
    }
}