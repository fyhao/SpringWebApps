package com.fyhao.springwebapps.ui;

import java.util.HashMap;
import java.util.Map;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.SkillDto;
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

@Route(value = "ui/manageskill", layout = MainView.class)
public class ManageSkillView extends Div {

    AgentProfileService agentProfileService;
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    Button addButton = new Button();
    Div listView = new Div();
    Div formDiv = new Div();

    public ManageSkillView(AgentProfileService agentProfileService) {
        this.agentProfileService = agentProfileService;
        refreshList();
        addButton.setText("Add");
        addButton.addClickListener(e -> {
            showForm("add");
        });
        add(addButton);
        add(formDiv);
        add(listView);
    }
    Map<String, TextField> inputMap = new HashMap<String, TextField>();

    void showForm(String action) {
        formDiv.setVisible(true);
        listView.setVisible(false);
        formDiv.removeAll();
        if (!action.equals("add") && !action.equals("edit"))
            return;
        formDiv.add(createTextField("skillname", "Skill Name"));
        Button submitButton = new Button();
        submitButton.setText("Submit");
        submitButton.addClickListener(e -> {
            String skillName = inputMap.get("skillname").getValue();
            // validation
            if(skillName.length() <= 3) {
                Notification.show("Skill Name cannot be less than 3 characters");
                return;
            }
            if(action.equals("add")) {
                SkillDto dto = new SkillDto();
                dto.setName(skillName);
                agentProfileService.createSkillProfile(dto);
                Notification.show("New skill is created");
                showForm("");
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
        Iterable<Skill> skills = agentProfileService.getAllSkills();
        listView.removeAll();
        listView.setVisible(true);
        formDiv.setVisible(false);
        for(Skill skill : skills) {
            Div itemDiv = new Div();
            Label label = new Label();
            label.setText(skill.getName());
            itemDiv.add(label);
            Button deleteButton = new Button();
            itemDiv.add(deleteButton);
            deleteButton.getStyle().set("text-align","right");
            deleteButton.setText("Delete");
            deleteButton.addClickListener(e -> {
                SkillDto dto = new SkillDto();
                dto.setName(skill.getName());
                agentProfileService.removeSkillProfile(dto);
                refreshList();
            });
            listView.add(itemDiv);
        }
    }
}