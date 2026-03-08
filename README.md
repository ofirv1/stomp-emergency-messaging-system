# STOMP Emergency Messaging System

A client-server emergency messaging platform based on the **STOMP (Simple Text Oriented Messaging Protocol)**.  
The system allows users to subscribe to emergency channels (e.g., fire, police, medical), report incidents, receive updates, and generate summaries of reported events.

This project was developed as part of a **Systems Programming course assignment** and demonstrates protocol implementation, multithreading, and publish-subscribe messaging architecture.

The system consists of a **Java server** and a **C++ client** communicating over TCP using STOMP frames.

---

# System Architecture

## Server (Java)

The server acts as a **central STOMP message broker** responsible for:

- managing client connections
- handling topic subscriptions
- forwarding messages to subscribed clients

The server supports two concurrency models:

**Thread-Per-Client (TPC)**  
Each client connection is handled by its own thread.

**Reactor Pattern**  
An event-driven model that handles multiple clients efficiently using non-blocking I/O.

---

## Client (C++)

The client provides the user interface and communicates with the server using STOMP frames.

Capabilities include:

- connecting to the server
- subscribing/unsubscribing to emergency channels
- reporting emergency events from JSON files
- receiving live updates
- generating summaries of events

The client runs **two threads**:

- one thread reads commands from the terminal
- one thread listens for messages from the server

---

# Technologies

- Java
- C++
- TCP sockets
- STOMP messaging protocol
- Multithreading
- Maven (server build)
- Makefile (client build)

---

# Build Instructions

## Build the Server

From the `server` directory:

```
mvn compile
```

## Build the Client

From the `client` directory:

```
make
```

---

# Running the System

## Start the Server

Thread-Per-Client mode:

```
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 tpc"
```

Reactor mode:

```
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 reactor"
```

The server will start listening on the specified port.

---

## Run the Client

From the `client` directory:

```
./bin/StompEMIClient
```

---

# Client Commands

The client receives commands through the terminal.

### Login

```
login {host:port} {username} {password}
```

Example:

```
login 127.0.0.1:7777 alice 1234
```

---

### Join Emergency Channel

```
join {channel_name}
```

Example:

```
join fire_dept
```

---

### Exit Emergency Channel

```
exit {channel_name}
```

---

### Report Emergency Events

```
report {events_file.json}
```

The client reads the JSON file, parses the emergency events, and sends each event to the relevant channel using STOMP `SEND` frames.

Example:

```
report data/events1_partial.json
```

Example event format:

```json
{
  "event_name": "Fire",
  "city": "Liberty City",
  "date_time": "1773279900",
  "description": "A gas pipe leak caused a fire at a factory.",
  "general_information": {
    "active": true,
    "forces_arrival_at_scene": true
  }
}
```

Each event is broadcast by the server to all subscribers of the corresponding channel.

---

### Generate Summary

```
summary {channel_name} {user} {output_file}
```

Example:

```
summary police alice summary.txt
```

This command generates a summary file containing all emergency updates received from the specified user in the specified channel.

---

### Logout

```
logout
```

The client sends a `DISCONNECT` frame and closes the connection gracefully after receiving a receipt from the server.

---

# Concepts Demonstrated

- Client-server architecture
- Network protocol implementation
- STOMP messaging protocol
- Publish-subscribe communication model
- Multithreaded client design
- Event-driven server architecture
