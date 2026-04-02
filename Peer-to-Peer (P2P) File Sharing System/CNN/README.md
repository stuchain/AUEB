# Peer-to-Peer (P2P) File Sharing System

This project implements a Java-based file-sharing network with tracker-coordinated peer discovery and download orchestration.

## At a Glance
- **Architecture style:** Tracker + peer clients
- **Primary objective:** Peer registration, file advertisement, and download coordination
- **Implementation details:** [`CN/README.md`](CN/README.md)
- **Source folder:** [`CN/src`](CN/src)

## System Architecture

- **Tracker** handles registration/login state, metadata indexing, and peer lookup.
- **Peers** provide user operations (register, login, inform, list, details, check active, notify, logout).
- **Concurrency model** uses multithreaded socket handling for simultaneous clients.

## How It Works

1. Start the tracker service.
2. Start one or more peer clients.
3. Register/login peers and publish available files.
4. Query file owners and perform download workflow.
5. Logout or disconnect from the network.

For exact commands and runtime notes, see [`CN/README.md`](CN/README.md).

## Technologies Used

- [Java](https://www.oracle.com/java/technologies/downloads/)
- [Java Sockets](https://docs.oracle.com/javase/tutorial/networking/sockets/)
- [Java Concurrency / Threads](https://docs.oracle.com/javase/tutorial/essential/concurrency/)