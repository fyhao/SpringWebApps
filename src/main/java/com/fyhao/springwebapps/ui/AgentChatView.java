package com.fyhao.springwebapps.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.fyhao.springwebapps.entity.AgentTerminal;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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
	HorizontalLayout contextBox = new HorizontalLayout();
	void showChatView() {
		TextField agentnameTF = new TextField();
		v.add(agentnameTF);
		agentnameTF.setValue("agent1");
		Button setAgentNameBtn = new Button("Connect with this Agent Name");
		v.add(setAgentNameBtn);
		v.add(statusLabel);
		v.add(lastAction);
		v.add(contextBox);
		setAgentNameBtn.addClickListener(e -> {
			String va = agentnameTF.getValue();
			agentid = va;
			connectWS();
		});
		renderStatusButton();
		renderChatBoxGroup();
		add(v);
	}
	Registration broadcasterRegistration;

    // Creating the UI shown separately
	WebSocketClient webSocketClient;
    WebSocketSession webSocketSession;
    String agentid = "agent1";
    List<String> conversationids = new ArrayList<String>();
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
			        else if(jsonMap.get("action").equals("incomingTask")) { 
                        String a = (String)jsonMap.get("conversationid");
                        String taskid = (String)jsonMap.get("taskid");
			            Map<String,Object> context = (Map<String, Object>)jsonMap.get("context");
			            updateContextVariable(context);
                        conversationids.add(a);
                        appendNewConversation(a, taskid);
			        }
			        else if(action.equals("chatMessageReceived")) {
			        	String conversationid = (String) jsonMap.get("conversationid");
                        String content = (String) jsonMap.get("content");
                        appendMessageReply(conversationid, content);
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
    public void sendChatMessage(String conversationid, String chatMessage) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "sendChatMessage");
        jsonMap.put("conversationid", conversationid);
        jsonMap.put("agentid", agentid);
        jsonMap.put("chatMessage", chatMessage);
        sendMessage(webSocketSession, jsonMap);
    }
    public void closeTask(String agentid, String taskid) {
        Map<String, Object> jsonMap = new HashMap<String, Object>();
        jsonMap.put("action", "closeTask");
        jsonMap.put("agentid", agentid);
        jsonMap.put("taskid", taskid);
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
    
    HorizontalLayout chgroup = new HorizontalLayout();
    VerticalLayout chtab = new VerticalLayout();
    VerticalLayout charea = new VerticalLayout();
    void renderChatBoxGroup() {
        // draw tabs based on list of conversationids 
        v.add(chgroup);
        // render chatbox tabs based on conversationids
        chtab.setWidth("20%");
        charea.setWidth("80%");
        chgroup.add(chtab);
        chgroup.add(charea);
    }
    
    int buttonCount = 0;
    void appendNewConversation(String conversationid, String taskid) {
        UI ui = v.getUI().get();
        ui.access(() -> {
            Button button = new Button();
            button.setText("Chat " + (++buttonCount));
            button.getElement().setAttribute("conversationid", conversationid);
            chtab.add(button);
            button.addClickListener(e -> {
                String id = e.getSource().getElement().getAttribute("conversationid");
                showChatBox(id);
            });
            showChatBox(conversationid, taskid);
        });
    }
    Map<String, Div> conMap = new HashMap<String, Div>();
    void showChatBox(String conversationid) {
        showChatBox(conversationid, null);
    }
    void showChatBox(String conversationid, String taskid) {
        for(Div div : conMap.values()) {
            div.setVisible(false);
        }
        if(conMap.containsKey(conversationid)) {
            conMap.get(conversationid).setVisible(true);
        }
        else {
            Div div = new Div();
            div.getElement().setAttribute("conversationid", conversationid);
            if(taskid != null) {
                div.getElement().setAttribute("taskid", taskid);
            }
            conMap.put(conversationid, div);
            charea.add(div);
            renderChatBox(div);
        }
    }
    
    void renderChatBox(Div div) {
        String conversationid = div.getElement().getAttribute("conversationid");
        String taskid = div.getElement().getAttribute("taskid");
        VerticalLayout chatBox = new VerticalLayout();
        chatBox.getElement().setAttribute("isChatBox", "1");
    	div.add(chatBox);
    	TextField messageTF = new TextField();
		div.add(messageTF);
		Button sendChatBtn = new Button("Send");
		div.add(sendChatBtn);
		sendChatBtn.addClickListener( e -> {
			String message = messageTF.getValue();
			sendChatMessage(conversationid, message);
			appendSelfReply(conversationid, message);
        });
        Button closeTaskBtn = new Button("Close Task");
        div.add(closeTaskBtn);
        closeTaskBtn.addClickListener( e -> {
            closeTask(agentid, taskid);
        });
    }
    void appendSelfReply(String conversationid, String content) {
        Div div = conMap.get(conversationid);
        VerticalLayout chatBox = (VerticalLayout)div.getChildren().filter(e -> e.getElement().getAttribute("isChatBox") != null).findFirst().get();
        UI ui = chatBox.getUI().get();
    	ui.access( () -> {
    		Label label = new Label("You: " + content);
    		label.setWidthFull();
    		label.getStyle().set("text-align", "right");
    		chatBox.add(label);
    	});
    	maintainLastNChat(conversationid);
    }
    void appendMessageReply(String conversationid, String content) {
        Div div = conMap.get(conversationid);
        VerticalLayout chatBox = (VerticalLayout)div.getChildren().filter(e -> e.getElement().getAttribute("isChatBox") != null).findFirst().get();
        UI ui = chatBox.getUI().get();
    	ui.access( () -> {
    		Label label = new Label("System: " + content + "( " + conversationid + " )");
    		label.setWidthFull();
    		label.getStyle().set("text-align", "left");
    		chatBox.add(label);
    	});
    	maintainLastNChat(conversationid);
    }
    void maintainLastNChat(String conversationid) {
    	Div div = conMap.get(conversationid);
        VerticalLayout chatBox = (VerticalLayout)div.getChildren().filter(e -> e.getElement().getAttribute("isChatBox") != null).findFirst().get();
        UI ui = chatBox.getUI().get();
    	ui.access( () -> {
    		while(chatBox.getComponentCount() > 10) {
    			chatBox.remove(chatBox.getComponentAt(0));
    		}
    	});
    }
    void updateContextVariable(Map<String, Object> contexts) {
    	System.out.println("incoming task updatecontextvariable " + contexts.size());
    	UI ui = contextBox.getUI().get();
    	ui.access( () -> {
    		contextBox.removeAll();
			for(Map.Entry<String, Object> entry:contexts.entrySet()) {
        		contextBox.add(new Label(entry.getKey() + " = " + entry.getValue()));
        	}
    	});
    }
}
