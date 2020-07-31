package com.fyhao.springwebapps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
@RequestMapping(value="test")
public class TestController {

	static Logger logger = LoggerFactory.getLogger(TestController.class);
	@RequestMapping("/")
	public @ResponseBody String greeting() {
        logger.info("Greeting");
		return "Hello, World";
	}
}