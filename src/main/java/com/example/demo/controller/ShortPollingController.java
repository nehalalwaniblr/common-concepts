package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/polling")
public class ShortPollingController {

    private AtomicInteger counter = new AtomicInteger(1);

    @GetMapping("/data")
    public String getData() {
        System.out.println("req received");
        return "Value: " + counter.getAndIncrement();
    }
}