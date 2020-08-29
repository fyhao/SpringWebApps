package com.fyhao.springwebapps.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.fyhao.springwebapps.dto.CustomEvent;

@Component
public class EventPublisher {
	@Autowired
    private ApplicationEventPublisher publisher;
     
    public void publishEvent(String action, Map<String,Object> prop) {
        CustomEvent evt = new CustomEvent(action, prop, this);
        this.publisher.publishEvent(evt);
    }
    public void publishEvent(String action) {
    	Map<String,Object> prop = new HashMap<String, Object>();
    	CustomEvent evt = new CustomEvent(action, prop, this);
    	this.publisher.publishEvent(evt);
    }
}
