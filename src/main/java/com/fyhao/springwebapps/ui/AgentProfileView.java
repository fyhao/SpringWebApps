package com.fyhao.springwebapps.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.entity.Skill;
import com.fyhao.springwebapps.service.AgentProfileService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@Route(value = "ui/agentprofile", layout = MainView.class)
public class AgentProfileView extends Div {

    AgentProfileService agentProfileService;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    Button addButton = new Button();
    Div listView = new Div();
    Div formDiv = new Div();
    Div manageSkillDiv = new Div();

    public AgentProfileView(AgentProfileService agentProfileService) {
        this.agentProfileService = agentProfileService;
        refreshList();
        addButton.setText("Add");
        addButton.addClickListener(e -> {
            showForm("add");
        });
        add(addButton);
        add(formDiv);
        add(listView);
        add(manageSkillDiv);
    }
    Map<String, TextField> inputMap = new HashMap<String, TextField>();

    void showForm(String action) {
        formDiv.setVisible(true);
        listView.setVisible(false);
        manageSkillDiv.setVisible(false);
        formDiv.removeAll();
        if (!action.equals("add") && !action.equals("edit"))
            return;
        formDiv.add(createTextField("agentname", "Agent Name"));
        Button submitButton = new Button();
        submitButton.setText("Submit");
        submitButton.addClickListener(e -> {
            String agentName = inputMap.get("agentname").getValue();
            // validation
            if(agentName.length() <= 3) {
                Notification.show("Agent Name cannot be less than 3 characters");
                return;
            }
            if(action.equals("add")) {
                AgentProfileDto dto = new AgentProfileDto();
                dto.setName(agentName);
                agentProfileService.createAgentProfile(dto);
                Notification.show("New agent is created");
                refreshList();
            }
        });
        formDiv.add(submitButton);
    }
    Div createTextField(String id, String title) {
        Div agentnameDiv = new Div();
        Label agentnameLabel = new Label();
        agentnameLabel.setText(title);
        TextField agentnameTF = new TextField();
        agentnameDiv.add(agentnameLabel);
        agentnameDiv.add(agentnameTF);
        inputMap.put(id, agentnameTF);
        return agentnameDiv;
    }
    
    void refreshList() {
        Iterable<Agent> agents = agentProfileService.getAllAgents();
        listView.removeAll();
        listView.setVisible(true);
        formDiv.setVisible(false);
        manageSkillDiv.setVisible(false);
        for(Agent agent : agents) {
            Div itemDiv = new Div();
            Label label = new Label();
            label.setText(agent.getName());
            itemDiv.add(label);

            Button manageSkillButton = new Button();
            manageSkillButton.setText("Manage Skill");
            manageSkillButton.addClickListener(e -> {
                showManageSkillForm(agent.getName());
            });
            itemDiv.add(manageSkillButton);

            Button deleteButton = new Button();
            itemDiv.add(deleteButton);
            deleteButton.getStyle().set("text-align","right");
            deleteButton.setText("Delete");
            deleteButton.addClickListener(e -> {
                AgentProfileDto dto = new AgentProfileDto();
                dto.setName(agent.getName());
                agentProfileService.removeAgentProfile(dto);
                refreshList();
            });
            listView.add(itemDiv);
        }
    }

    void showManageSkillForm(String agentName) {
        listView.setVisible(false);
        formDiv.setVisible(false);
        manageSkillDiv.setVisible(true);
        manageSkillDiv.removeAll();
        manageSkillDiv.add(createLineLabel("Manage skill for " + agentName));
        manageSkillDiv.add(createLineLabel("Current list of skills:"));
        
        List<String> agentSkills = getskillnamesofagent(agentName);    
        for(String a : agentSkills) {
            Div div = createLineLabel(a);
            Button unassignBtn = new Button();
            unassignBtn.setText("Unassign");
            div.add(unassignBtn);
            unassignBtn.addClickListener(e -> {
                AgentSkillDto d = new AgentSkillDto();
                d.setAction(AgentSkillDto.REMOVED_FROM_AGENT);
                d.setAgent(agentName);
                d.setSkill(a);
                assignagentskillaction(d);
                showManageSkillForm(agentName);
            });
            manageSkillDiv.add(div);
        }
        manageSkillDiv.add(createLineLabel("To assign skill: "));
        for(Skill skill : agentProfileService.getAllSkills()) {
        	if(agentSkills.contains(skill.getName())) continue;
            Div div = createLineLabel(skill.getName());
            Button assignBtn = new Button();
            assignBtn.setText("Assign");
            div.add(assignBtn);
            assignBtn.addClickListener(e -> {
                AgentSkillDto d = new AgentSkillDto();
                d.setAction(AgentSkillDto.ASSIGNED_TO_AGENT);
                d.setAgent(agentName);
                d.setSkill(skill.getName());
                assignagentskillaction(d);
                showManageSkillForm(agentName);
            });
            manageSkillDiv.add(div);
        }
        
    }
    Div createLineLabel(String text) {
        Div div = new Div();
        Label label = new Label(text);
        div.add(label);
        return div;
    }

    private List<String> getskillnamesofagent(String agent) {
        RestTemplate restTemplate = new RestTemplate();
        String port = "8080";
        String resp = restTemplate.getForObject("http://localhost:" + port + "/agentprofile/getskillnamesofagent?agent=" + agent,
                String.class);
        JsonParser springParser = JsonParserFactory.getJsonParser();
        List<Object> list = springParser.parseList(resp);
        List<String> strList = list.stream()
                           .map( Object::toString )
                           .collect( Collectors.toList() );
        return strList;
    }
    private String assignagentskillaction(AgentSkillDto dto) {
        RestTemplate restTemplate = new RestTemplate();
        String port = "8080";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        ObjectMapper objectMapper = new ObjectMapper();
        String message = null;
        try {
            message = objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
        }
        HttpEntity<String> request = new HttpEntity<String>(message, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity("http://localhost:" + port + "/agentprofile/assignagentskillaction", request,
                String.class);
        return resp.getBody();
    }
}