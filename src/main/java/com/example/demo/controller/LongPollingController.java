package com.example.demo.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@RestController
@RequestMapping("/api/longpoll")
/*A controller method returns a DeferredResult object. Spring MVC immediately calls request.startAsync() and releases the HTTP worker thread back to the pool. The DispatcherServlet exits, but the HTTP connection with the client remains open.
Asynchronous Processing: A separate, application-managed thread (e.g., from a ForkJoinPool, ExecutorService, or a message broker listener) performs the long-running task.
Result Setting: When the asynchronous task is complete, the separate thread calls deferredResult.setResult(T result) or deferredResult.setErrorResult(Object result).
Resuming Processing: Spring MVC receives this notification and dispatches the request back to the servlet container. The DispatcherServlet resumes processing using the provided result value, and the final response is sent to the client*/
public class LongPollingController {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @GetMapping("/events")
    public DeferredResult<String> getEvent() {
        DeferredResult<String> output = new DeferredResult<>(10000L);

        new Thread(() -> {
            try {
                String event = queue.take();
                output.setResult(event);
            } catch (Exception ignored) {}
        }).start();

        return output;
    }

    @PostMapping("/publish")
    public void publish(@RequestBody String message) {
        queue.add(message);
    }
}
