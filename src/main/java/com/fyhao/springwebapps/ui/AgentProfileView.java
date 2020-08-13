package com.fyhao.springwebapps.ui;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.service.AgentProfileService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import org.springframework.beans.factory.annotation.Autowired;
@Route(value = "ui/agentprofile", layout = MainView.class)
public class AgentProfileView extends Div {

    AgentProfileService agentProfileService;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    Button addButton = new Button();
    Div listView = new Div();
    public AgentProfileView(AgentProfileService agentProfileService) {
        this.agentProfileService = agentProfileService;
        addButton.setText("Add new agent 1");
        addButton.addClickListener(e -> {
            AgentProfileDto dto = new AgentProfileDto();
            int cnt = (int)agentProfileService.getAgentCount();
            dto.setName("agent" + (cnt + 1));
            agentProfileService.createAgentProfile(dto);
            refreshList();
        });
        add(addButton);
        add(listView);
        refreshList();
    }
    
    void refreshList() {
        Iterable<Agent> agents = agentProfileService.getAllAgents();
        listView.removeAll();
        for(Agent agent : agents) {
            Label label = new Label();
            label.setText(agent.getName());
            listView.add(label);
        }
    }
}