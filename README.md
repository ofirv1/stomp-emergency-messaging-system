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

# Containerized Development Environment

The project includes a **Dev Container configuration** that allows running the system in a preconfigured Linux environment using Docker and VS Code Dev Containers.

This container provides all required dependencies for building and running both the server and the client.

## Requirements

- Docker
- VS Code
- VS Code Dev Containers extension
---
# Build Instructions

## Build the Server

From the `server` directory:

```bash
mvn compile
```

## Build the Client

From the `client` directory:

```bash
make
```

---

# Running the System

## Start the Server

From the `server` directory, run one of the following:

### Thread-Per-Client mode

```bash
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 tpc"
```

### Reactor mode

```bash
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.stomp.StompServer" -Dexec.args="7777 reactor"
```

The server will start listening on the specified port.

---

## Run the Client

From the `client` directory:

```bash
./bin/StompWCIClient 127.0.0.1 7777
```

The client expects the server host and port as command-line arguments.

---

# Client Commands

After starting the client, enter commands through the terminal.

## Login

```text
login {host:port} {username} {password}
```

Example:

```text
login 127.0.0.1:7777 alice 1234
```

---

## Join Emergency Channel

```text
join {channel_name}
```

Example:

```text
join police
```

---

## Exit Emergency Channel

```text
exit {channel_name}
```

---

## Report Emergency Events

```text
report {events_file.json}
```

The client reads the JSON file, parses the emergency events, and sends each event to the relevant channel using STOMP `SEND` frames.

Example:

```text
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

## Generate Summary

```text
summary {channel_name} {user} {output_file}
```

Example:

```text
summary police alice summary.txt
```

This command generates a summary file containing emergency updates received from the specified user in the specified channel.

---

## Logout

```text
logout
```

The client sends a `DISCONNECT` frame and closes the connection gracefully.



---

# Concepts Demonstrated

- Client-server architecture
- Network protocol implementation
- STOMP messaging protocol
- Publish-subscribe communication model
- Multithreaded client design
- Event-driven server architecture
