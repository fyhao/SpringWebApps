package com.fyhao.springwebapps.ui;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;

public class UIUtil {

	public static Div createLineLabel(String text) {
        Div div = new Div();
        Label label = new Label(text);
        div.add(label);
        return div;
    }
}
