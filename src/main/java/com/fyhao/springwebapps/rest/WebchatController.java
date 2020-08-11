package com.fyhao.springwebapps.rest;

import java.net.URLDecoder;

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
        return messagingService.createConversation(email);
    }
    @RequestMapping("/createconversationwithchannel")
	public @ResponseBody String createconversationwithchannel(@RequestParam String email, @RequestParam String channel) {
        logger.info("createconversationwithchannel");
        return messagingService.createConversation(email, channel);
    }
    @RequestMapping("/sendmessage")
	public @ResponseBody String sendmessage(@RequestParam String id, @RequestParam String input) {
        input = URLDecoder.decode(input);
        logger.info("sendmessage");
        messagingService.sendCustomerMessage(id, input);
		return "0";
    }
    @RequestMapping("/sendagentmessage")
	public @ResponseBody String sendagentmessage(@RequestParam String id, @RequestParam String agentname, @RequestParam String input) {
        logger.info("sendagentmessage");
        input = URLDecoder.decode(input);
        messagingService.sendAgentMessage(id, agentname, input);
		return "0";
    }
    @RequestMapping("/sendcustomerstarttyping")
	public @ResponseBody String sendcustomerstarttyping(@RequestParam String id) {
        logger.info("sendmessage");
        messagingService.sendCustomerStartTyping(id);
		return "0";
    }
    @RequestMapping("/sendcustomerstoptyping")
	public @ResponseBody String sendcustomerstoptyping(@RequestParam String id) {
        logger.info("sendmessage");
        messagingService.sendCustomerStopTyping(id);
		return "0";
    }
    @RequestMapping("/getmessagecount")
	public @ResponseBody int getmessagecount(@RequestParam String id) {
        logger.info("getmessagecount");
        return messagingService.getMessageCount(id);
    }
    @RequestMapping("/findcontext")
	public @ResponseBody String findcontext(@RequestParam String id, @RequestParam String key) {
        logger.info("findcontext");
        return messagingService.findContext(id, key);
    }
    @RequestMapping("/findchannel")
	public @ResponseBody String findchannel(@RequestParam String id) {
        logger.info("findchannel");
        return messagingService.findChannel(id);
    }
    @RequestMapping("/getcontactscount")
	public @ResponseBody long getcontactscount() {
        logger.info("getcontactscount");
        return messagingService.getContactsCount();
    }
    @RequestMapping("/getconversationendtime")
	public @ResponseBody String getconversationendtime(@RequestParam String id) {
        logger.info("getconversationendtime");
        return messagingService.getConversationEndTime(id);
    }
    @RequestMapping("/getlastmessagefromparty")
	public @ResponseBody String getlastmessagefromparty(@RequestParam String id) {
        logger.info("getlastmessagefromparty");
        return messagingService.getLastMessageFromParty(id);
    }
    @RequestMapping("/getlastmessagetoparty")
	public @ResponseBody String getlastmessagetoparty(@RequestParam String id) {
        logger.info("getlastmessagetoparty");
        return messagingService.getLastMessageToParty(id);
    }
    @RequestMapping("/getlastmessagecontent")
	public @ResponseBody String getlastmessagecontent(@RequestParam String id) {
        logger.info("getlastmessagecontent");
        return messagingService.getLastMessageContent(id);
    }
}