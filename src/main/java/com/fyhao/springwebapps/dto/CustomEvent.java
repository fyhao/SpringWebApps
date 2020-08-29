package com.fyhao.springwebapps.dto;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationEvent;

public class CustomEvent extends ApplicationEvent {
	String action;
	Map<String, Object> prop = new HashMap<String, Object>();
	
	public CustomEvent(String action, Map<String, Object> prop, Object source) {
		super(source);
		this.action = action;
		this.prop = prop;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, Object> getProp() {
		return prop;
	}

	public void setProp(Map<String, Object> prop) {
		this.prop = prop;
	}
	
}
