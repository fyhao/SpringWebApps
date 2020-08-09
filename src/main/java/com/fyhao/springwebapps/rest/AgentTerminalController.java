package com.fyhao.springwebapps.rest;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.service.AgentTerminalService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
@RequestMapping(value="agentterminal")
public class AgentTerminalController {
    static Logger logger = LoggerFactory.getLogger(AgentTerminalController.class);

    @Autowired
    AgentTerminalService agentTerminalService;
    
    @RequestMapping(method= RequestMethod.POST, value = "/registeragent", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String registeragent(@RequestBody AgentProfileDto dto) {
        logger.info("AgentTerminalController registeragent " + dto.getName());
        agentTerminalService.registerAgent(dto.getName());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/unregisteragent", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String unregisteragent(@RequestBody AgentProfileDto dto) {
        logger.info("AgentTerminalController unregisteragent " + dto.getName());
        agentTerminalService.unregisterAgent(dto.getName());
        return "0";
    }
    @RequestMapping("/getagentterminalscount")
	public @ResponseBody long getagentterminalscount() {
        logger.info("getagentterminalscount");
		return agentTerminalService.getAgentTerminalsCount();
    }
}