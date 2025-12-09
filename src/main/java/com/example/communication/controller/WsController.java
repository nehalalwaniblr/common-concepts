package com.example.communication.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class WsController {
    /*WebSocket with STOMP is NOT req/resp (like HTTP)

It is:

one to many

publish / subscribe

event driven*/

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public String greet(String message) throws Exception {
        log.info(">>>> Received message = {}", message);
        return "Hello: " + message;
    }
}