import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class Tracker {
    private ConcurrentHashMap<String, PeerInfo> registeredPeers; // ConcurrentHashMap
    private HashMap<String, PeerInfo> loggedUsers;
    private ServerSocket serverSocket;

    public Tracker() {
        registeredPeers = new ConcurrentHashMap<>();
        createFile();
        loggedUsers = new HashMap<>();
        try {
            serverSocket = new ServerSocket(12345);
        } catch (IOException e) {
            System.err.println("Error creating server socket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isUsernameRegistered(String userName) {
        return registeredPeers.containsKey(userName);
    }

    public synchronized void registerPeer(String userName, String password, PrintWriter out) {
        if (isUsernameRegistered(userName)) {
            out.println("Username already exists. Please choose a different one.");
            System.out.println("Username already exists. Please choose a different one.");
        } else {
            PeerInfo newPeer = new PeerInfo(userName, password);
            registeredPeers.put(userName, newPeer);
            out.println("Username available");

            System.out.println("Peer " + userName + " registered successfully.");
        }
        System.out.println();
    }

    public synchronized void login(String userName, String password, PrintWriter out) {
        if (isUsernameRegistered(userName)) {
            PeerInfo thispeer = registeredPeers.get(userName);
            String thispeerUSERNAME = thispeer.getUserName();
            String thispeerPASS = thispeer.getPassword();
            String thispeerTOKEN = thispeer.getTokenID();

            if (thispeer != null && thispeerTOKEN == null) {
                if (thispeerUSERNAME.equals(userName) && thispeerPASS.equals(password)) {
                    thispeer.login();
                    out.println("Success");
                    System.out.println("Peer " + userName + " logged in successfully.");
                    thispeerTOKEN = thispeer.getTokenID();
                    loggedUsers.put(thispeerTOKEN, thispeer);
                    out.println(thispeerTOKEN);
                } else {
                    out.println("Wrong");
                    System.out.println("Your password is wrong. Please try again.");
                }

            } else {
                System.out.println("User " + userName + " already logged in");
                out.println("User already logged in");
            }
        } else {
            System.out.println("User not found.");
            out.println("User not found.");
        }
        System.out.println();

    }

    public synchronized void getInformed(String userName, String peer_ip, int peer_port, String text, PrintWriter out) {
        StringTokenizer tokenizer = new StringTokenizer(text, "\n");

        PeerInfo thisPeer = registeredPeers.get(userName);
        if (thisPeer == null) {
            System.out.println("Peer " + userName + " not found.");
            return;
        }

        try (FileWriter writer = new FileWriter("fileDownloadList.txt", true)) {
            while (tokenizer.hasMoreTokens()) {
                String line = tokenizer.nextToken().trim(); // Αφαιρούμε τα κενά από τις άκρες της γραμμής
                if (!line.isEmpty()) { // Έλεγχος για κενή γραμμή
                    String[] files = line.split("#"); // Διαχωρίζουμε τα αρχεία με βάση το "#"
                    for (String file : files) {
                        if (!file.isEmpty()) { // Έλεγχος για κενό αρχείο
                            thisPeer.addAvailableFile(file.trim()); // Προσθέτουμε κάθε αρχείο στη λίστα
                            // Προσθήκη των δεδομένων στο αρχείο fileDownloadList.txt
                            writer.write(userName + "\n");
                            writer.write(file.trim() + "\n");
                        }
                    }
                }

            }
            thisPeer.informPeer(peer_ip, peer_port);
            System.out.println("Got informed by user " + userName);

        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();

    }

    public static void createFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter("fileDownloadList.txt"))) {
            // Γράψιμο των δεδομένων στο αρχείο
            for (int i = 1; i <= 10; i++) {
                String user = "user" + i;
                String file = "file" + i;
                writer.println(user);
                writer.println(file);
            }
            System.out.println("Created fileDownloadList.txt");
        } catch (IOException e) {
            System.err.println("An error occurred while creating the file: " + e.getMessage());
            e.printStackTrace();
        }

    }

    public synchronized void logout(String token, PrintWriter out) {
        if (loggedUsers.containsKey(token)) {
            PeerInfo peer = loggedUsers.get(token);
            if (peer != null) {
                peer.logout();
                loggedUsers.remove(token);
                out.println("Logout successful");
                System.out.println("Peer " + peer.getUserName() + " Logout successful");
            }
        } else {
            System.out.println("User is not logged in.");
        }
        System.out.println();

    }

    public synchronized PeerInfo findPeerByToken(String tokenId) {
        for (PeerInfo peer : registeredPeers.values()) {
            if (peer.getTokenID() != null && peer.getTokenID().equals(tokenId)) {
                return peer; // Επιστρέφει το PeerInfo αν το TokenId ταιριάζει
            }
        }
        return null; // Επιστρέφει null αν δεν βρεθεί PeerInfo με το συγκεκριμένο TokenId
    }

    public ArrayList<String> replyList() {
        ArrayList<String> allAvailableFiles = new ArrayList<>(); // Τελική λίστα με μοναδικά ονόματα αρχείων

        // Επιλέγουμε τα μοναδικά ονόματα αρχείων από όλους τους peer
        for (PeerInfo peer : loggedUsers.values()) {
            ArrayList<String> filesFromPeer = peer.getAvailableFiles(); // Παίρνουμε τα αρχεία από τον συγκεκριμένο peer
            for (String file : filesFromPeer) {
                if (!allAvailableFiles.contains(file)) { // Αν το αρχείο δεν υπάρχει ήδη στην τελική λίστα
                    allAvailableFiles.add(file); // Προσθέτουμε το αρχείο στην τελική λίστα
                    System.out.println("Available file: "+file);
                }
            }
        }

        return allAvailableFiles;
    }

    public void sendListToClient(ArrayList<String> allAvailableFiles, Socket clientSocket) {
        try {
            // Δημιουργήστε ένα ObjectOutputStream για την αποστολή της λίστας
            ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());

            // Στείλτε τη λίστα στον πελάτη μέσω του ObjectOutputStream
            objectOutput.writeObject(allAvailableFiles);

        } catch (IOException e) {
            System.err.println("Error sending list to client: " + e.getMessage());
            e.printStackTrace();
            System.out.println();

        }
    }

    public ArrayList<ArrayList<String>> replyDetails(String filename) {
        ArrayList<ArrayList<String>> fileDetailsList = new ArrayList<>();
        boolean fileFound = false;

        // Εξετάζουμε κάθε PeerInfo για το εάν διαθέτει το συγκεκριμένο αρχείο
        for (PeerInfo peer : registeredPeers.values()) {
            String thispeerTOKEN = peer.getTokenID();
            if (peer.getAvailableFiles().contains(filename) && thispeerTOKEN != null) {
                // Δημιουργούμε μια νέα λίστα με τα στοιχεία του αρχείου για αυτόν τον peer
                ArrayList<String> peerDetails = new ArrayList<>();
                peerDetails.add(peer.getUserName()); // Όνομα χρήστη
                peerDetails.add(peer.getIpAddress()); // Διεύθυνση IP
                peerDetails.add(Integer.toString(peer.getPort())); // Θύρα
                peerDetails.add(peer.getTokenID()); // Token ID
                peerDetails.add(Integer.toString(peer.getCountDownloads())); // Αριθμός λήψεων
                peerDetails.add(Integer.toString(peer.getCountFailures())); // Αριθμός αποτυχιών
                fileDetailsList.add(peerDetails); // Προσθέτουμε τα στοιχεία στη λίστα των λεπτομερειών του αρχείου
                fileFound = true; // Το αρχείο βρέθηκε σε τουλάχιστον έναν peer
            } else {
                System.out.println("This user is not logged in");
                System.out.println();

            }
        }

        // Αν το αρχείο δεν βρέθηκε σε κανέναν peer, προσθέτουμε αντίστοιχο μήνυμα στη
        // λίστα των λεπτομερειών
        if (!fileFound) {
            ArrayList<String> notFoundMessage = new ArrayList<>();
            notFoundMessage.add("File not found.");
            fileDetailsList.add(notFoundMessage);
        }

        return fileDetailsList; // Επιστρέφουμε τη λίστα με τις λεπτομέρειες του αρχείου ή το μήνυμα "File not
                                // found."
    }

    public void sendDetailsToClient(ArrayList<ArrayList<String>> details, Socket clientSocket) {
        try {
            // Δημιουργήστε ένα ObjectOutputStream για την αποστολή της λίστας
            ObjectOutputStream objectOutput = new ObjectOutputStream(clientSocket.getOutputStream());

            // Στείλτε τη λίστα στον πελάτη μέσω του ObjectOutputStream
            objectOutput.writeObject(details);

        } catch (IOException e) {
            System.err.println("Error sending list to client: " + e.getMessage());
            e.printStackTrace();
            System.out.println();

        }
    }

    public ConcurrentHashMap<String, PeerInfo> getRegisteredPeers() {
        return registeredPeers;
    }

    public HashMap<String, PeerInfo> getLoggedUsers() {
        return loggedUsers;
    }

    public static void main(String[] args) {
        Tracker tracker = new Tracker();

        // Έναρξη λειτουργίας του tracker για να ακούει συνδέσεις από τους peers
        tracker.startListening();
    }

    public void startListening() {
        try {
            System.out.println("Tracker is listening for connections.");

            while (true) {
                // Αποδοχή εισερχόμενης σύνδεσης
                Socket clientSocket = serverSocket.accept();
                System.out.println("Peer connected: " + clientSocket.getInetAddress());

                // Δημιουργία νέου νήματος για την εξυπηρέτηση του peer
                Thread thread = new Thread(new PeerHandler(clientSocket));
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Error accepting connection: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    public synchronized void checkActive(String userName, PrintWriter out) {
        for (PeerInfo peer : registeredPeers.values()) {
            if (peer.getUserName().equals(userName)) {
                if (peer.getTokenID() != null) {
                    out.println("Active");
                    System.out.println("User " + userName + " Active");
                } else {
                    out.println("Inactive");
                    System.out.println("User " + userName + "Inactive");
                }
                // Κλείστε το εσωτερικό if
            }

        }
        System.out.println();
    }

    public void getNotified(String userName, String success, String file, String usernameUpdate) {
        try (FileWriter writer = new FileWriter("fileDownloadList.txt", true)) {
            for (PeerInfo peer : registeredPeers.values()) {
                synchronized (registeredPeers) {
                    if (peer.getUserName().equals(userName)) {
                        if (success.startsWith("Active")) {
                            peer.incrementDownloads();
                            System.out.println(userName + " Count Downloads: " + peer.getCountDownloads());
                        } else if (success.startsWith("Inactive")) {
                            peer.incrementFailures();
                            System.out.println(userName + " Count Failures: " + peer.getCountFailures());
                        } else {
                            System.out.println("Cannot increase downloads or failures");
                        }
                        break;
                    }
                }
            }
            for (PeerInfo peer : registeredPeers.values()) {
                synchronized (registeredPeers) {
                    if (peer.getUserName().equals(usernameUpdate) && success.startsWith("Active")) {
                        if (!file.isEmpty()) {
                            peer.addAvailableFile(file.trim());
                            writer.write(usernameUpdate + "\n");
                            writer.write(file.trim() + "\n");
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("An error occurred while writing to the file: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private class PeerHandler implements Runnable {
        private Socket clientSocket;

        public PeerHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            ) {

                while (true) {
                    // Διάβασε το μήνυμα από τον peer
                    String message = in.readLine();
                    String userName;
                    String password;
                    String peer_ip;
                    String token_id1;
                    int peer_port = 0;
                    String port_convert;
                    String text;
                    String file;
                    ArrayList<String> ListToPeer;
                    ArrayList<ArrayList<String>> DetailsToPeer;
                    String usernameCheck;
                    String update;
                    String usernameUpdate;

                    // Επεξεργασία του μηνύματος από τον Peer με χρήση switch
                    switch (message) {
                        case "register":
                            userName = in.readLine();
                            password = in.readLine();
                            registerPeer(userName, password, out);
                            break;
                        case "login":
                            userName = in.readLine();
                            password = in.readLine();
                            login(userName, password, out);
                            break;
                        case "inform":
                            userName = in.readLine();
                            peer_ip = in.readLine();
                            port_convert = in.readLine();
                            try {
                                peer_port = Integer.parseInt(port_convert);
                            } catch (NumberFormatException e) {
                            }
                            text = in.readLine();
                            getInformed(userName, peer_ip, peer_port, text, out);

                            break;
                        case "list":
                            ListToPeer = replyList();
                            sendListToClient(ListToPeer, clientSocket);

                            break;
                        case "details":
                            file = in.readLine();
                            DetailsToPeer = replyDetails(file);
                            sendDetailsToClient(DetailsToPeer, clientSocket);
                            break;
                        case "checkActive":
                            usernameCheck = in.readLine();
                            checkActive(usernameCheck, out);
                            break;
                        case "notify":
                            usernameCheck = in.readLine();
                            update = in.readLine();
                            file = in.readLine();
                            usernameUpdate = in.readLine();
                            getNotified(usernameCheck, update, file, usernameUpdate);
                            break;
                        case "logout":
                            token_id1 = in.readLine();
                            logout(token_id1, out);
                            break;
                        case "exit":
                            System.out.println("Closing connection with peer.");
                            return; // Τερματισμός του νήματος μετά το "exit"
                        default:
                            out.println("Invalid command.");
                            break;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error handling peer request: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Error closing client socket: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}
