package com.fyhao.springwebapps.rest;

import com.fyhao.springwebapps.entity.Bot;
import com.fyhao.springwebapps.model.BotRepository;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("bot")
public class BotController {
    private final BotRepository botRepository;

    public BotController(BotRepository botRepository) {
        this.botRepository = botRepository;
    }

    @PostMapping("/config")
    public Bot save(@RequestBody Bot bot) {
        return botRepository.save(bot);
    }

    @GetMapping("/config")
    public Iterable<Bot> list() {
        return botRepository.findAll();
    }
}
