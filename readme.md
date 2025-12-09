Here is a comprehensive README file draft explaining the four common server-to-client communication techniques implemented in your Spring Boot project.

-----

# 🚀 Real-Time Communication Methods in Spring Boot

This project demonstrates four distinct techniques for achieving server-to-client communication, ranging from simple **Short Polling** to efficient **WebSockets**.

## Table of Contents

1.  [Short Polling](https://www.google.com/search?q=%231-short-polling)
2.  [Long Polling](https://www.google.com/search?q=%232-long-polling)
3.  [Server-Sent Events (SSE)](https://www.google.com/search?q=%233-server-sent-events-sse)
4.  [WebSockets](https://www.google.com/search?q=%234-websockets)
5.  [Key Components Explained](https://www.google.com/search?q=%235-key-components-explained)

-----

## 1\. Short Polling

Short polling is the **simplest but least efficient** method. The client repeatedly asks the server for new data at short, fixed intervals.

### ⚙️ How It Works

* The client sends a standard **HTTP GET request**.
* The server processes the request **immediately** and returns any available data, **closing the connection**.
* The client waits for the defined interval (e.g., 2 seconds) and repeats the process.

### 📋 Code (`ShortPollingController.java`)

```java
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
```

### ▶️ Run Command

Use the `watch` command to simulate a client polling every 2 seconds:

```bash
watch -n2 curl localhost:8080/api/polling/data
```

**Observation:** You will see the counter increment every 2 seconds, but the request overhead is high.

-----

## 2\. Long Polling

Long polling is an improvement over short polling. The server keeps the connection open until new data is available or a timeout occurs.

### ⚙️ How It Works

1.  The client sends an **HTTP GET request**.
2.  The server holds the request thread open using an asynchronous container like `DeferredResult`.
3.  When a **new event arrives** (e.g., via the `/publish` endpoint), the server completes the pending `DeferredResult`, sending the response to the client.
4.  The client immediately receives the data, closes the connection, and then **immediately opens a new connection** to wait for the next event.

### 📋 Code (`LongPollingController.java`)

```java
@RestController
@RequestMapping("/api/longpoll")
public class LongPollingController {

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    @GetMapping("/events")
    public DeferredResult<String> getEvent() {
        DeferredResult<String> output = new DeferredResult<>(10000L); // 10s timeout
        
        // This thread blocks until queue.take() gets an item
        new Thread(() -> { 
            try {
                String event = queue.take(); 
                output.setResult(event);
            } catch (Exception ignored) {}
        }).start();

        return output; // Spring releases the main thread immediately
    }

    @PostMapping("/publish")
    public void publish(@RequestBody String message) {
        queue.add(message); // Releases the thread waiting on queue.take()
    }
}
```

### ▶️ Run Commands

**Step 1: Start the Listener (Client)**

```bash
curl localhost:8080/api/longpoll/events
# This command will hang, waiting for a result...
```

**Step 2: Publish an Event (Server Push)**

Open a separate terminal and send data:

```bash
curl -X POST -H "Content-Type: text/plain" -d "Hello from publisher" localhost:8080/api/longpoll/publish
```

**Observation:** The first terminal will immediately receive the message and close. To continue listening, you would have to run the `curl` command again.

-----

## 3\. Server-Sent Events (SSE)

SSE enables **one-way, continuous data flow** from the server to the client over a single, long-lived HTTP connection. It's ideal for pushing updates (e.g., stock tickers, feed updates).

### ⚙️ How It Works

1.  The client sends a single **HTTP GET request**.
2.  The server responds with the special header `Content-Type: text/event-stream` and **keeps the connection open indefinitely**.
3.  The server pushes data in a formatted way (`data: payload\n\n`) whenever it wants.
4.  Browsers provide the built-in `EventSource` object, which **automatically handles reconnection** if the connection drops.

### 📋 Code (`SseController.java`)

```java
@RestController
public class SseController {

    @GetMapping(path = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(val -> "Tick: " + val);
    }
}
```

*The use of **`Flux`** (from Project Reactor) is key here, as it represents an asynchronous stream of 0 to N items, perfectly mapping to the SSE stream.*

### ▶️ Run Command

Run the command to open and listen to the stream:

```bash
curl -H "Accept: text/event-stream" localhost:8080/sse
```

**Observation:** You will see a new "Tick" message appear every second until you manually stop the command ($\mathbf{Ctrl + C}$).

-----

## 4\. WebSockets

WebSockets provide a **true bi-directional, full-duplex communication channel** over a single, persistent TCP connection. It starts as an HTTP connection and then upgrades to the WebSocket protocol via a handshake.

### ⚙️ How It Works

1.  **Handshake:** The client sends an HTTP request with an `Upgrade` header. If the server agrees, the connection is upgraded.
2.  **Bi-directional Channel:** Data can flow freely and efficiently from client to server and vice-versa at any time without polling or closing the connection.
3.  **STOMP:** Spring often uses **STOMP (Simple Text Oriented Message Protocol)** over WebSockets to provide message routing capabilities, making it easier to send messages to specific users or topics (`/topic`).

### 📋 Code

| Component | Purpose |
| :--- | :--- |
| `WebSocketConfig.java` | Configures the message broker and the `/ws` endpoint for the WebSocket handshake. |
| `WsController.java` | Uses `@MessageMapping` and `@SendTo` to define handlers for incoming and outgoing messages. |

### ▶️ Run Steps

1.  Ensure your Spring Boot application is running.
2.  Open your browser and navigate to:
    ```
    http://localhost:8080/ws-test.html
    ```
3.  In the browser, click the **"Send Hello"** button.
4.  **Observation:** The client will send the message to `/app/hello`, and the controller will process it and send the response back to the subscribed topic `/topic/greetings`, which the client immediately receives as an alert.

-----

## 5\. Key Components Explained

| Component | Technology | Description |
| :--- | :--- | :--- |
| **`AtomicInteger`** | **Java Utilities** | Used in Short Polling to ensure thread-safe, atomic operations (incrementing) on a single integer counter, preventing concurrency issues. |
| **`BlockingQueue`** | **Java Utilities** | Used in Long Polling to safely transfer data between the publisher thread (which calls `/publish`) and the listener thread (which waits for `/events`). The `take()` method **blocks** the listener thread until an item is available. |
| **`DeferredResult`** | **Spring MVC** | Used in Long Polling to **release the Tomcat worker thread** back to the pool immediately while keeping the HTTP connection open. The request remains *deferred* until the result is set by another thread. |
| **`Flux<T>`** | **Project Reactor** | Used in SSE. A **Reactive Streams Publisher** that emits 0 to N items asynchronously. It's the perfect representation of an ongoing, continuous data stream. |
| **`@MessageMapping`**| **Spring WebSocket** | Maps an incoming STOMP message (e.g., from the client's `/app/hello` command) to a controller method. |
| **`@SendTo`** | **Spring WebSocket** | Specifies the STOMP destination (the topic) where the return value of the controller method should be broadcast. |