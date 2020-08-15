package com.fyhao.springwebapps.ui;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;

@Push
public class MainView extends Composite<Div> implements RouterLayout {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public MainView() {
        createHeader();
        createMenu();
    }
    
    void createHeader() {
        Label label = new Label();
        label.getStyle().set("font-size", "36px");
        label.setText("Header");
        getContent().add(label);
    }

    Div content = new Div();
    void createMenu() {
    	HorizontalLayout container = new HorizontalLayout();
    	VerticalLayout menu = new VerticalLayout();
        menu.add(createRouterLink("Home", HomeView.class));
        menu.add(createRouterLink("Contact", ContactView.class));
        menu.add(createRouterLink("Agent Profile", AgentProfileView.class));
        menu.add(createRouterLink("Manage Skill", ManageSkillView.class));
        menu.add(createRouterLink("Agent Chat", AgentChatView.class));
        menu.add(createRouterLink("Customer Chat", CustomerChatView.class));
        menu.setWidth("20%");
        content.setWidth("80%");
        container.add(menu);
        container.add(content);
        getContent().add(container);
    }
    
    RouterLink createRouterLink(String title, Class<? extends Component> clazz) {
    	RouterLink link = new RouterLink(title, clazz);
    	link.getStyle().set("margin", "5px");
    	return link;
    }
    
    public void showRouterLayoutContent(HasElement hasElement) {
    	content.removeAll();
    	content.getElement().appendChild(hasElement.getElement());
    }
}