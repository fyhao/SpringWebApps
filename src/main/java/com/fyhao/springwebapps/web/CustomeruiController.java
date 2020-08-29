package com.fyhao.springwebapps.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value="customerui")
public class CustomeruiController {
	@RequestMapping("/login")
    public String login(){
        return "customerui/login";
    }
    @RequestMapping("/home")
    public String home(){
        return "customerui/home";
    }
}
