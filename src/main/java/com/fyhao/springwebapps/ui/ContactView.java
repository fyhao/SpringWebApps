package com.fyhao.springwebapps.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.ParentLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLayout;
@Route(value = "ui/contact", layout = MainView.class)
public class ContactView extends Div {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public ContactView() {
		Label label = new Label();
        label.setText("Header");
        add(label);
	}
}