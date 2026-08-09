package com.fyhao.springwebapps.rest;

import com.fyhao.springwebapps.dto.TwilioMessage;
import com.fyhao.springwebapps.service.TwilioService;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("twilio")
public class TwilioController {
    private final TwilioService twilioService;

    public TwilioController(TwilioService twilioService) {
        this.twilioService = twilioService;
    }

    @PostMapping(
            value = "/postmessage",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_XML_VALUE)
    public String postMessage(
            @RequestParam(name = "MessageSid", required = false) String messageSid,
            @RequestParam(name = "SmsSid", required = false) String smsSid,
            @RequestParam(name = "From") String from,
            @RequestParam(name = "To") String to,
            @RequestParam(name = "Body") String body) {
        String externalId = messageSid != null ? messageSid : smsSid;
        TwilioMessage twilioMessage = new TwilioMessage(externalId, from, to, body);
        twilioService.receiveMessage(twilioMessage);
        return "<Response/>";
    }
}
