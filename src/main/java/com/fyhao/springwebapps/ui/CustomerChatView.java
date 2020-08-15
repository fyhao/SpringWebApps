package com.fyhao.springwebapps.ui;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

@Route(value = "ui/customerchat", layout = MainView.class)
public class CustomerChatView extends Div  implements AfterNavigationObserver{
	public CustomerChatView() {
		showChatView();
	}
	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		
	}
	VerticalLayout v = new VerticalLayout();
	Label lastAction = new Label();
	void showChatView() {
		v.add(lastAction);
		TextField emailTF = new TextField();
		emailTF.setValue("test@test.com");
		v.add(emailTF);
		TextField channelTF = new TextField();
		channelTF.setValue("webchathotelqueue");
		v.add(channelTF);
		Button connectBtn = new Button("Connect with chat care");
		v.add(connectBtn);
		connectBtn.addClickListener( e -> {
			email = emailTF.getValue();
			channel = channelTF.getValue();
			conversationid = createconversationwithchannel(email, channel);
			connectWS();
		});
		renderChatBox();
		
		add(v);
	}
	WebSocketClient webSocketClient;
    WebSocketSession webSocketSession;
    String email = "";
    String channel = "";
    String conversationid = "";
    int port = 8080;
    
    private String createconversationwithchannel(String email, String channel) {
    	RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject("http://localhost:" + port + "/webchat/createconversationwithchannel?email=" + email + "&channel=" + channel,
                String.class);
    }
    public void connectWS() {
    	UI ui = v.getUI().get();
    	webSocketClient = new StandardWebSocketClient();
    	
        try {
			webSocketSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
			    
			    @Override
			    public void handleTextMessage(WebSocketSession session, TextMessage message) {
			        JsonParser springParser = JsonParserFactory.getJsonParser();
			        ObjectMapper objectMapper = new ObjectMapper();
			        Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
			        String action = (String)jsonMap.get("action");
			        System.out.println("customer action: " + action);
			        
			        ui.access(() -> {
			        	lastAction.setText(action);
			        });
			        if(action.equals("chatMessageReceived")) {
			        	String conversationid = (String) jsonMap.get("conversationid");
                        String content = (String) jsonMap.get("content");
                        appendMessageReply(content);
			        }
			    }
			    @Override
			    public void afterConnectionEstablished(WebSocketSession session) {
			        System.out.println("After connection established - customer");
			        registerCustomerSession(session);
			    }
			}, new WebSocketHttpHeaders(), URI.create("ws://localhost:" + port + "/channel")).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void registerCustomerSession(WebSocketSession webSocketSession) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "register");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("serverport", port);
        sendMessage(webSocketSession, jsonMap);
        System.out.println("registering customer session: " + (webSocketSession != null));
    }
    public void sendChatMessage(String chatMessage) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "sendChatMessage");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("chatMessage", chatMessage);
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
    VerticalLayout chatBox = new VerticalLayout();
    void renderChatBox() {
    	v.add(chatBox);
    	TextField messageTF = new TextField();
		v.add(messageTF);
		messageTF.setValue("do you know about abcde?");
		Button sendChatBtn = new Button("Send");
		v.add(sendChatBtn);
		sendChatBtn.addClickListener( e -> {
			String message = messageTF.getValue();
			sendChatMessage(message);
			appendSelfReply(message);
		});
    }
    void appendSelfReply(String content) {
    	UI ui = chatBox.getUI().get();
    	ui.access( () -> {
    		Label label = new Label("You: " + content);
    		label.setWidthFull();
    		label.getStyle().set("text-align", "right");
    		chatBox.add(label);
    	});
    	maintainLastNChat();
    }
    void appendMessageReply(String content) {
    	UI ui = chatBox.getUI().get();
    	ui.access( () -> {
    		Label label = new Label("System: " + content);
    		label.setWidthFull();
    		label.getStyle().set("text-align", "left");
    		chatBox.add(label);
    	});
    	maintainLastNChat();
    }
    void maintainLastNChat() {
    	UI ui = chatBox.getUI().get();
    	ui.access( () -> {
    		while(chatBox.getComponentCount() > 10) {
    			chatBox.remove(chatBox.getComponentAt(0));
    		}
    	});
    }
}
