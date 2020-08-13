package com.fyhao.springwebapps.ui;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.router.RouterLink;


public class MainView extends VerticalLayout implements RouterLayout {

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
        add(label);
    }

    void createMenu() {
        Div menu = new Div();
        menu.add(new RouterLink("Home", HomeView.class));
        menu.add(new RouterLink("Contact", ContactView.class));
        menu.add(new RouterLink("Agent Profile", AgentProfileView.class));
        add(menu);
    }
}