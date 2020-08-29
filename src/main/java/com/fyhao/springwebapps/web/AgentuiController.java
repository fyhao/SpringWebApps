package com.fyhao.springwebapps.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="agentui")
public class AgentuiController {    
    @RequestMapping("/login")
    public String login(){
        return "agentui/login";
    }
    @RequestMapping("/home")
    public String home(){
        return "agentui/home";
    }
}