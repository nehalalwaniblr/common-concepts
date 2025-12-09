package com.example.demo.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class SseController {

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    /*Spring WebFlux is a reactive and non-blocking alternative to traditional Spring MVC.

Core idea:

handle I/O without blocking threads, using reactive streams

It uses:

Netty (instead of Tomcat)

Project Reactor (Mono, Flux)

non-blocking event loop*/
    public Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(val -> "Tick: " + val);
    }
}
