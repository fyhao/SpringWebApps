package com.fyhao.springwebapps.service;

import com.fyhao.springwebapps.dto.BotRequest;
import com.fyhao.springwebapps.dto.BotResponse;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConversationalWorkflowClient {
    private final RestTemplate restTemplate;

    public ConversationalWorkflowClient() {
        this(new RestTemplate());
    }

    ConversationalWorkflowClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public BotResponse send(String endpoint, BotRequest request) {
        return restTemplate.postForObject(endpoint, request, BotResponse.class);
    }
}
