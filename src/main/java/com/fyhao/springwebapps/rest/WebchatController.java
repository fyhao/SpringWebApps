package com.fyhao.springwebapps.rest;

import com.fyhao.springwebapps.service.MessagingService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value="webchat")
public class WebchatController {
    static Logger logger = LoggerFactory.getLogger(WebchatController.class);

    @Autowired
    MessagingService messagingService;
    @RequestMapping("/")
	public @ResponseBody String greeting() {
        logger.info("Greeting");
		return "Hello, World 1";
    }
    @RequestMapping("/createconversation")
	public @ResponseBody String createconversation(@RequestParam String email) {
        logger.info("createconversation");
        long id = messagingService.createConversation(email);
		return id + "";
    }
    @RequestMapping("/sendmessage")
	public @ResponseBody String sendmessage(@RequestParam long id, @RequestParam String input) {
        logger.info("sendmessage");
        messagingService.sendTextMessage(id, input);
		return "0";
    }
    @RequestMapping("/getmessagecount")
	public @ResponseBody int getmessagecount(@RequestParam long id) {
        logger.info("getmessagecount");
        return messagingService.getMessageCount(id);
    }
    @RequestMapping("/findcontext")
	public @ResponseBody String findcontext(@RequestParam long id, @RequestParam String key) {
        logger.info("findcontext");
        return messagingService.findContext(id, key);
    }
    @RequestMapping("/findchannel")
	public @ResponseBody String findchannel(@RequestParam long id) {
        logger.info("findchannel");
        return messagingService.findChannel(id);
    }
    @RequestMapping("/getcontactscount")
	public @ResponseBody long getcontactscount() {
        logger.info("getcontactscount");
        return messagingService.getContactsCount();
    }
}