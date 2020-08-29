package com.fyhao.springwebapps.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="test")
public class WelcomeController {    
    @RequestMapping("/welcome")
    public String loginMessage(){
    	System.out.println("jsp");
        return "welcome";
    }
}