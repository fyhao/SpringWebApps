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
    
    @RequestMapping("/getagenttaskscount")
	public @ResponseBody long getagenttaskscount(String agentid) {
        logger.info("getagenttaskscount " + agentid);
		return taskService.getAgentTasksCount(agentid);
    }
}