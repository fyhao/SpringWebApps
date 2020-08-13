package com.fyhao.springwebapps.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;

@Route(value = "ui/home", layout = MainView.class)
public class HomeView extends Div {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public HomeView() {
		Label label = new Label();
        label.setText("home");
        add(label);
	}
}