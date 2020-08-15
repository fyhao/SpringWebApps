package com.fyhao.springwebapps.ui;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.entity.AgentTerminal;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
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
	Label statusLabel = new Label();
	Label lastAction = new Label();
	void showChatView() {
		TextField agentnameTF = new TextField();
		v.add(agentnameTF);
		Button setAgentNameBtn = new Button("Connect with this Agent Name");
		v.add(setAgentNameBtn);
		v.add(statusLabel);
		v.add(lastAction);
		setAgentNameBtn.addClickListener(e -> {
			String va = agentnameTF.getValue();
			agentid = va;
			connectWS();
		});
		renderStatusButton();
		add(v);
	}
	Registration broadcasterRegistration;

    // Creating the UI shown separately
	WebSocketClient webSocketClient;
    WebSocketSession webSocketSession;
    String agentid = "agent1";
    int port = 8080;
    
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
			        System.out.println("agent action: " + action);
			        
			        ui.access(() -> {
			        	lastAction.setText(action);
			        });
			        if(action.equals("connectionready")) {
			        	updateAgentStatusUI(AgentTerminal.NOT_READY);
			        }
			        else if(action.equals("agentStatusChanged")) {
			        	String agentid = (String)jsonMap.get("agentid");
                        String oldstatus = (String)jsonMap.get("oldstatus");
                        String newstatus = (String)jsonMap.get("newstatus");
                        updateAgentStatusUI(newstatus);
			        }
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
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();
        
        
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
    public void setAgentStatus(String status) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "setAgentStatus");
        jsonMap.put("agentid", agentid);
        jsonMap.put("status", status);
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
    Button statusButton = new Button();
    void renderStatusButton() {
    	v.add(statusButton);
    	statusButton.addClickListener(e -> {
    		setAgentStatus(targetStatus);
    	});
    }

	String targetStatus = null;
    void updateAgentStatusUI(String status) {
    	UI ui = v.getUI().get();
    	ui.access(() -> {
    		statusLabel.setText(status);
    		if(status.equals(AgentTerminal.NOT_READY)) {
    			targetStatus = AgentTerminal.READY;
    		}
    		else if(status.equals(AgentTerminal.READY)) {
    			targetStatus = AgentTerminal.NOT_READY;
    		}
			statusButton.setText(targetStatus);
    	});
    }
}
