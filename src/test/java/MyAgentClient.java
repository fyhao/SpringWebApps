import java.net.URI;

import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MyAgentClient {
    WebSocketClient webSocketClient;
    WebSocketSession webSocketSession;
    int port;
    TestingWebApplicationTests that;
    public String agentid;
    CompletableFuture<WebSocketSession> agentEstablished;
    CompletableFuture<Map<String, Object>> incomingJsonMapReceived;
    public List<String> taskidList = new ArrayList<String>();
    
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
                    if(jsonMap.get("action").equals("incomingTask")) {
                    	String taskid = (String)jsonMap.get("taskid");
                    	taskidList.add(taskid);
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
    public Map<String,Object> waitNextJsonMap() {
    	CompletableFuture<Map<String, Object>> received = waitNextIncomingTextMessage();
    	Map<String, Object> jsonMap;
		try {
			jsonMap = received.get(2, SECONDS);
	    	return jsonMap;
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    public String waitNextKey(String key) {
    	Map<String, Object> jsonMap = waitNextJsonMap();
    	return (String)jsonMap.get(key);
    }
    public String waitNextAction() {
    	return waitNextKey("action");
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
        int i = 0;
        while(++i < 10 && !waitNextAction().equals("agentUnregistered")) {
        	
        }
        try {
        	Thread.sleep(1000);
        } catch (Exception ex) {}
    }
    public void setAgentStatus(String status) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "setAgentStatus");
        jsonMap.put("agentid", agentid);
        jsonMap.put("status", status);
        sendMessage(webSocketSession, jsonMap);
    }
    public void closeTask(String taskid) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "closeTask");
        jsonMap.put("agentid", agentid);
        jsonMap.put("taskid", taskid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void requestTransferToAgent(MyAgentClient agent2, String taskid) {
        String targetAgentid = agent2.agentid;
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "requestTransferToAgent");
        jsonMap.put("agentid", agentid);
        jsonMap.put("targetAgentid", targetAgentid);
        jsonMap.put("taskid", taskid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void requestTransferToSkill(String skill, String taskid) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "requestTransferToSkill");
        jsonMap.put("agentid", agentid);
        jsonMap.put("targetSkill", skill);
        jsonMap.put("taskid", taskid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void startTyping(String conversationid) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "startTyping");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void stopTyping(String conversationid) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "stopTyping");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void inviteConference(MyAgentClient agent2, String conversationid) {
    	String targetAgentid = agent2.agentid;
    	Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "inviteConference");
        jsonMap.put("agentid", agentid);
        jsonMap.put("targetAgentid", targetAgentid);
        jsonMap.put("conversationid", conversationid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void acceptInvite(String conversationid) {
    	Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "acceptedInvite");
        jsonMap.put("agentid", agentid);
        jsonMap.put("conversationid", conversationid);
        sendMessage(webSocketSession, jsonMap);
    }
    public void bargeinConversation(String conversationid) {
    	Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "bargeinConversation");
        jsonMap.put("agentid", agentid);
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