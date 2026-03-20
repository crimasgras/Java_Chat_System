# Java Real-Time Chat Application

A client-server chat application I built in Java to practice socket programming, multithreading and secure authentication. The server handles multiple clients simultaneously, each on its own thread, and persists chat history to a log file.

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Swing](https://img.shields.io/badge/Swing_GUI-007396?style=for-the-badge)
![Sockets](https://img.shields.io/badge/Socket_Programming-4B0082?style=for-the-badge)

## What I built

- Implemented a multithreaded client-server architecture using Java sockets, supporting up to 10 simultaneous connections with thread-safe message broadcasting.
- Secured user authentication with SHA-256 hashing and salting, with credentials persisted to a local file and validated server-side on every login.
- Built a Swing GUI with emoji support, timestamped messages, chat commands (/list, /history, /help) and automatic chat logging.

## Project Structure

- `Server.java` — manages connections, authentication and chat logging
- `ClientHandler.java` — handles each client on a separate thread
- `Client.java` — Swing GUI and server communication

## How it works

1. Start the server — it listens on port 1234
2. Launch one or more clients — each connects, registers or logs in
3. Messages are broadcast to all connected users in real time
4. Chat history is saved automatically to `chat_log.txt`
