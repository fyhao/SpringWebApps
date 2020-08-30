package com.fyhao.springwebapps.rest;

import com.fyhao.springwebapps.dto.AgentProfileDto;
import com.fyhao.springwebapps.dto.AgentSkillDto;
import com.fyhao.springwebapps.dto.SkillDto;
import com.fyhao.springwebapps.entity.Agent;
import com.fyhao.springwebapps.service.AgentProfileService;
import com.fyhao.springwebapps.service.TaskService;

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
@RequestMapping(value="task")
public class TaskController {
    static Logger logger = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    TaskService taskService;
    
    @RequestMapping(method= RequestMethod.POST, value = "/closetask", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String closetask(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController closetask " + dto.getName() + " " + dto.getTaskid());
        taskService.closeTask(dto.getName(), dto.getTaskid());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/requesttransfertoagent", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String requesttransfertoagent(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController requesttransfertoagent " + dto.getName() + " " + dto.getTaskid() + " " + dto.getTargetagentid());
        taskService.requestTransferToAgent(dto.getName(), dto.getTaskid(), dto.getTargetagentid());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/requesttransfertoskill", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String requesttransfertoskill(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController requesttransfertoskill " + dto.getName() + " " + dto.getTaskid() + " " + dto.getTargetskill());
        taskService.requestTransferToSkill(dto.getName(), dto.getTaskid(), dto.getTargetskill());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/agentstarttyping", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String agentstarttyping(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController starttyping " + dto.getName() + " " + dto.getConversationid());
        taskService.agentStartTyping(dto.getName(), dto.getConversationid());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/agentstoptyping", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String agentstoptyping(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController stoptyping " + dto.getName() + " " + dto.getConversationid());
        taskService.agentStopTyping(dto.getName(), dto.getConversationid());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/inviteconference", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String inviteconference(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController inviteconference " + dto.getName() + " " + dto.getConversationid() + " " + dto.getTargetagentid());
        taskService.inviteConference(dto.getName(), dto.getTargetagentid(), dto.getConversationid());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/acceptinvite", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String acceptinvite(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController acceptinvite " + dto.getName() + " " + dto.getConversationid());
        taskService.acceptInvite(dto.getName(), dto.getConversationid());
        return "0";
    }
    @RequestMapping(method= RequestMethod.POST, value = "/bargeinconversation", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public @ResponseBody String bargeinconversation(@RequestBody AgentProfileDto dto) {
        logger.info("TaskController bargeinconversation " + dto.getName() + " " + dto.getConversationid());
        taskService.bargeinConversation(dto.getName(), dto.getConversationid());
        return "0";
    }
    @RequestMapping("/getagenttaskscount")
	public @ResponseBody long getagenttaskscount(String agentid) {
        logger.info("getagenttaskscount " + agentid);
		return taskService.getAgentTasksCount(agentid);
    }
    @RequestMapping("/getagentactivetaskscount")
	public @ResponseBody long getagentactivetaskscount(String agentid) {
        logger.info("getagentactivetaskscount " + agentid);
		return taskService.getAgentActiveTasksCount(agentid);
    }
    
}