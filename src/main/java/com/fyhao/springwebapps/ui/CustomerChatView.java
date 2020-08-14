package com.fyhao.springwebapps.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.Route;

@Route(value = "ui/customerchat", layout = MainView.class)
public class CustomerChatView extends Div  implements AfterNavigationObserver{
	public CustomerChatView() {
		
	}
	@Override
	public void afterNavigation(AfterNavigationEvent event) {
		
	}
}
