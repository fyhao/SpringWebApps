package com.fyhao.springwebapps.rest;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.service.AgentProfileService;

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
@RequestMapping(value="agentprofile")
public class AgentprofileController {
    static Logger logger = LoggerFactory.getLogger(AgentprofileController.class);

    @Autowired
    AgentProfileService agentProfileService;
    @RequestMapping("/")
	public @ResponseBody String greeting() {
        logger.info("Greeting");
		return "Hello, World 1";
    }

    @RequestMapping(method= RequestMethod.POST, value = "/createagentprofile", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String createagentprofile(@RequestBody AgentProfileDto agent) {
        logger.info("AgentprofileController createagentprofile " + agent.getName());
        agentProfileService.createAgentProfile(agent);
        return "0";
    }

    @RequestMapping("/getagentcount")
	public @ResponseBody long getagentcount() {
        logger.info("getagentcount");
		return agentProfileService.getAgentCount();
    }
}