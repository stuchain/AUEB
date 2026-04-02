# Peer-to-Peer (P2P) File Sharing System

This project implements a Java-based file-sharing network with peer coordination through a central tracker.

The system includes:
- **Tracker**: maintains user sessions and file availability metadata.
- **Peer nodes**: register, authenticate, publish available files, and request downloads.

## System Architecture

- Tracker-centric peer discovery and coordination.
- Socket-based communication between tracker and peers.
- Multithreaded request handling for concurrent peer operations.

Implementation resources:
- Detailed implementation guide: [`CN/README.md`](CN/README.md)
- Core classes: [`CN/src/Tracker.java`](CN/src/Tracker.java), [`CN/src/Peer.java`](CN/src/Peer.java), [`CN/src/PeerInfo.java`](CN/src/PeerInfo.java)

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