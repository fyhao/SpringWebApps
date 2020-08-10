package com.fyhao.springwebapps;

import com.fyhao.springwebapps.service.MessagingService;
import com.fyhao.springwebapps.ws.AgentSocketHandler;
import com.fyhao.springwebapps.ws.ChannelSocketHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Autowired
    ChannelSocketHandler channelSocketHandler;
    @Autowired
    AgentSocketHandler agentSocketHandler;
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(channelSocketHandler, "/channel").setAllowedOrigins("*");
        registry.addHandler(agentSocketHandler, "/agent").setAllowedOrigins("*");
	}
}
