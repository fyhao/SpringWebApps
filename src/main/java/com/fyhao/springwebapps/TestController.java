package com.fyhao.springwebapps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fyhao.springwebapps.entity.UserAccount;
import com.fyhao.springwebapps.model.UserAccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ResponseBody;

@RestController
@RequestMapping(value="test")
public class TestController {

    static Logger logger = LoggerFactory.getLogger(TestController.class);
    
    @Autowired
    UserAccountRepository repo;
    
	@RequestMapping("/")
	public @ResponseBody String greeting() {
        logger.info("Greeting");
		return "Hello, World 1";
    }
    @RequestMapping("/list")
	public @ResponseBody Iterable<UserAccount> list() {
        logger.info("list");
        return repo.findAll();
    }
    @RequestMapping("/add")
	public @ResponseBody String add(@RequestParam("username") String username) {
        UserAccount act = new UserAccount();
        act.setUsername(username);
        act.setEmail(username + "@gmail.com");
        repo.save(act);
		return "Added " + act.getId();
	}
}