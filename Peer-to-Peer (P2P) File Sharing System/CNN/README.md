# 🔗 Peer-to-Peer (P2P) File Sharing System  

This is a **peer-to-peer (P2P) file-sharing system** that allows users to **share files directly** with each other in a decentralized network. The system consists of two main components:  

- **Peer**: A node representing a user that can register, connect, share files, download files, and disconnect.  
- **Tracker**: A central mediator that maintains a list of registered users, manages connections, and provides information about available files.  

## ⚙️ System Architecture  

- **Tracker**: Acts as a central meeting point, managing user registrations and file availability.  
- **Peers**: Communicate with the Tracker and other peers to exchange files.  
- **Sockets & Multithreading**: Enables real-time communication and efficient handling of multiple connections.  

## 🚀 How It Works  

1. **Run the Tracker** to manage peer connections.  
2. **Start a Peer** to connect to the network and register.  
3. **Share & Download Files** by requesting data from other peers.  
4. **Disconnect** when done, ensuring smooth network operation.  

## 🖥️ Technologies Used  

- **Java**  
- **Socket Programming** (for communication)  
- **Multithreading** (for handling multiple connections)  