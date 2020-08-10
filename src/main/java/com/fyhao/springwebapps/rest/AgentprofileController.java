package com.fyhao.springwebapps.rest;

import java.util.List;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.dto.SkillDto;
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

    @RequestMapping(method= RequestMethod.POST, value = "/createskillprofile", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String createskillprofile(@RequestBody SkillDto skill) {
        logger.info("AgentprofileController createskillprofile " + skill.getName());
        agentProfileService.createSkillProfile(skill);
        return "0";
    }

    @RequestMapping(method= RequestMethod.POST, value = "/assignagentskillaction", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String assignagentskillaction(@RequestBody AgentSkillDto dto) {
        logger.info("AgentprofileController assignagentskillaction " + dto.getAgent() + " " + dto.getSkill() + " " + dto.getAction());
        agentProfileService.assignAgentSkillAction(dto);
        return "0";
    }
    @RequestMapping("/getskillnamesofagent")
	public @ResponseBody List<String> getskillnamesofagent(@RequestParam String agent) {
        logger.info("getskillnamesofagent");
		return agentProfileService.getSkillNamesOfAgent(agent);
    }
    @RequestMapping("/getagentcount")
	public @ResponseBody long getagentcount() {
        logger.info("getagentcount");
		return agentProfileService.getAgentCount();
    }
    @RequestMapping("/getskillcount")
	public @ResponseBody long getskillcount() {
        logger.info("getskillcount");
		return agentProfileService.getSkillCount();
    }
    @RequestMapping("/getmaxconcurrenttaskofagent")
	public @ResponseBody long getmaxconcurrenttaskofagent(@RequestParam String agentname) {
        logger.info("getmaxconcurrenttaskofagent");
		return agentProfileService.getMaxConcurrentTaskOfAgent(agentname);
    }
    @RequestMapping("/setmaxconcurrenttaskofagent")
	public @ResponseBody long setmaxconcurrenttaskofagent(@RequestParam String agentname, @RequestParam int maxconcurrenttask) {
        logger.info("setmaxconcurrenttaskofagent");
		return agentProfileService.setMaxConcurrentTaskOfAgent(agentname, maxconcurrenttask);
    }
}