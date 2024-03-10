# Chat Application

This chat application enables users to communicate in real-time across different chat rooms. Built with Java and leveraging Remote Method Invocation (RMI), it supports features such as message broadcasting, private messaging, and handling temporary server unavailability.

## Features

- **Real-time Messaging**: Users can send and receive messages instantly in various chat rooms.
- **Multiple Chat Rooms**: Support for multiple chat rooms, allowing users to join conversations of their interest.
- **Private Messaging**: (If implemented) Users can send private messages to other users.
- **Resilience to Server Downtime**: Mechanism to handle temporary unavailability of the `ChatServerManager`, including retrying connections and buffering messages.
- **Dynamic Room Management**: Users can create, join, and leave chat rooms dynamically.
- **User Authentication**: (If implemented) Secure login system to authenticate users before accessing the chat rooms.

