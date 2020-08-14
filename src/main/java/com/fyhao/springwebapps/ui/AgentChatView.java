package com.fyhao.springwebapps.ui;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;


@Route(value = "ui/agentchat", layout = MainView.class)
public class AgentChatView extends Div  implements AfterNavigationObserver{

	public AgentChatView() {
		showChatView();
	}
	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		
	}
	
	VerticalLayout v = new VerticalLayout();
	
	void showChatView() {
		
        	v.add(new Label("test"));
		add(v);
	}
	Registration broadcasterRegistration;

    // Creating the UI shown separately
	WebSocketClient webSocketClient;
    WebSocketSession webSocketSession;
    String agentid = "agent1";
    int port = 8080;
    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        
        webSocketClient = new StandardWebSocketClient();

        try {
			webSocketSession = webSocketClient.doHandshake(new TextWebSocketHandler() {
			    
			    @Override
			    public void handleTextMessage(WebSocketSession session, TextMessage message) {
			        JsonParser springParser = JsonParserFactory.getJsonParser();
			        ObjectMapper objectMapper = new ObjectMapper();
			        Map<String, Object> jsonMap = springParser.parseMap(message.getPayload());
			        String action = (String)jsonMap.get("action");
			        System.out.println("agent action: " + action);
			        
			        ui.access(() -> {
			        	v.add(new Label("action: "+  action));
			        });
			    }

			    @Override
			    public void afterConnectionEstablished(WebSocketSession session) {
			        System.out.println("After connection established");
			        registerAgentSession(session);
			    }
			}, new WebSocketHttpHeaders(), URI.create("ws://localhost:" + port + "/agent")).get();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        
    }
    public void registerAgentSession(WebSocketSession webSocketSession) {
    	System.out.println("registerAgentSession");
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "register");
        jsonMap.put("agentid", agentid);
        jsonMap.put("serverport", port);
        sendMessage(webSocketSession, jsonMap);
    }
    public void sendMessage(WebSocketSession session, Map<String, Object> jsonMap) {
        System.out.println("DEBUG sendMessage session " + (session != null));
    	ObjectMapper objectMapper = new ObjectMapper();
        try {
            String message = objectMapper.writeValueAsString(jsonMap);
            session.sendMessage(new TextMessage(message));
        } catch (Exception ex) {
        	ex.printStackTrace();
        }
    }
}
