import static java.util.concurrent.TimeUnit.SECONDS;

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

public class MyCustomerClient {
    WebSocketClient webSocketClient;
    WebSocketSession webSocketSession;
    int port;
    TestingWebApplicationTests that;
    String conversationid;
    CompletableFuture<WebSocketSession> customerEstablished;
    CompletableFuture<Map<String, Object>> incomingJsonMapReceived;
    public MyCustomerClient(TestingWebApplicationTests that, String conversationid) {
        port = that.port;
        this.that = that;
        this.conversationid = conversationid;
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
                    if(customerEstablished != null) {
                        customerEstablished.complete(session);
                        customerEstablished = null;
                    }
                }
            }, new WebSocketHttpHeaders(), URI.create("ws://localhost:" + port + "/channel")).get();

        } catch (Exception e) {
            // LOGGER.error("Exception while accessing websockets", e);
        }
    }
    public CompletableFuture<WebSocketSession> waitNextCustomerEstablishedEvent() {
        customerEstablished = new CompletableFuture<WebSocketSession>();
        return customerEstablished;
    }
    public CompletableFuture<Map<String, Object>> waitNextIncomingTextMessage() {
        incomingJsonMapReceived = new CompletableFuture<Map<String, Object>>();
        return incomingJsonMapReceived;
    }
    public void sendChatMessage(String chatMessage) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "sendChatMessage");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("chatMessage", chatMessage);
        sendMessage(webSocketSession, jsonMap);
    }
    public String sendChatMessageWait(String chatMessage) {
    	sendChatMessage(chatMessage);
    	return waitNextMessage();
    }
    public String waitNextMessage() {
    	Map<String, Object> jsonMap = new HashMap<String, Object>();
    	CompletableFuture<Map<String,Object>> customerIncomingReceived = waitNextIncomingTextMessage();
        try {
        	jsonMap = customerIncomingReceived.get(2, SECONDS);
        	if(jsonMap.get("action").equals("chatMessageReceived")) {
        		return (String)jsonMap.get("content");
        	}
        } catch (Exception ex) {}
        return null;
    }
    public void registerCustomerSession() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "register");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("serverport", port);
        sendMessage(webSocketSession, jsonMap);
    }
    public void unregisterAgentSesssion() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "unregister");
        jsonMap.put("serverport", port);
        sendMessage(webSocketSession, jsonMap);
        
        try {
        	Thread.sleep(1000);
        } catch (Exception ex) {}
    }
    public void startTyping() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "startTyping");
        jsonMap.put("conversationid", conversationid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void stopTyping() {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "stopTyping");
        jsonMap.put("conversationid", conversationid);
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