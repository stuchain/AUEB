import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

public class Peer {
    private static final String TRACKER_ADDRESS = "localhost";
    private static final int TRACKER_PORT = 12345; // Variable to store known port of TRACKER
    private static String token_id; // Variable to store token_id
    private static String peer_ip; // Variable to store peer's IP address
    private static int peer_port; // Variable to store peer's port number
    private static Socket trackerSocket; // Socket for connecting to the tracker
    private static BufferedReader userInput;
    private static String username; // Variable to store username
    private static ArrayList<String> fileList = new ArrayList<>(); // List to store available files

    public static void main(String[] args) {
        System.out.println("Peer started.");

        try {
            // Εύρεση διαθέσιμου random port και δημιουργία server socket
            peer_port = findAvailablePort();
            ServerSocket serverSocket = new ServerSocket(peer_port);
            System.out.println("Server socket started on port: " + peer_port);
            InetAddress localHost = InetAddress.getLocalHost();
            peer_ip = localHost.getHostAddress();
             // Connect στον tracker
            trackerSocket = new Socket(TRACKER_ADDRESS, TRACKER_PORT);
            System.out.println("Connected to tracker: " + trackerSocket.getInetAddress());
            System.out.println(); // Space για καλύτερη κατανομή χώρου στο console
            // Παίρνω την επιλογή του χρήστη
            userInput = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                showMenu();
                String action = userInput.readLine();
                System.out.println();
                switch (action) {
                    case "1":
                        register();
                        break;
                    case "2":
                        login();
                        break;
                    case "3":
                        inform();
                        break;
                    case "4":
                        list();
                        break;
                    case "5":
                        details();
                        break;
                    case "6":
                        logout();
                        break;
                    case "7":
                        closeConnection();
                        serverSocket.close();
                        return;

                    default:
                        System.out.println("Invalid option. Please try again.");
                        break;
                }
            }
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void showMenu() {
        System.out.println("Select an option:");
        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. Inform");
        System.out.println("4. List");
        System.out.println("5. Details");
        System.out.println("6. Logout");
        System.out.println("7. Exit connection with tracker");

    }

    private static void register() {
        try {
            PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
            out.println("register"); // message to tracker to start register process

            System.out.println("Enter username:");
            username = userInput.readLine();
            out.println(username);
            System.out.println("Enter password:");
            String password = userInput.readLine();
            out.println(password);

            @SuppressWarnings("resource")
            String response = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream())).readLine(); //Reader to read incoming response from the registration attempt

            if (response.equals("Username available")) {
                createFile(); // create shared directory file of the peer with 3 random files from fileDownloadList
                System.out.println("User " + username + " registered.");
            } else {
                System.out.println("Username already exists. Please try a different username.");
            }
        } catch (IOException e) {
            System.err.println("Error registering peer: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void login() {
        try {
            if (token_id != null) {
                System.out.println("You are already logged in. Please logout first.");
                return;
            }

            PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
            out.println("login");

            System.out.println("Enter username:");
            username = userInput.readLine();
            out.println(username);

            System.out.println("Enter password:");
            String password = userInput.readLine();
            out.println(password);

            BufferedReader in = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
            String loginResponse = in.readLine();
            if (loginResponse.startsWith("Success")) {
                token_id = in.readLine(); // Receive and store token_id
                System.out.println("Login successful. Received token_id: " + token_id);
            } else if (loginResponse.startsWith("Wrong")) {
                System.out.println("Your password is wrong. Please try again.");
            } else if (loginResponse.startsWith("User already logged in")) {
                System.out.println("Login failed: " + loginResponse);
            } else {
                System.out.println("Error logging in");

            }
        } catch (IOException e) {
            System.err.println("Error logging in: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void createFile() {
        try {
            // Δημιουργία του φακέλου Shared_directory με το όνομα του χρήστη
            File sharedDirectory = new File("Shared_directory/" + username);
            if (!sharedDirectory.exists()) {
                if (sharedDirectory.mkdirs()) {
                    System.out.println("Created directory: " + sharedDirectory.getPath());
                } else {
                    throw new IOException("Failed to create directory: " + sharedDirectory.getPath());
                }
            } else {
                System.out.println("Directory already exists: " + sharedDirectory.getPath());
            }
    
            // Δημιουργία του αρχείου μέσα στον φάκελο
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(sharedDirectory.getPath() + "/" + username + ".txt"));
            fileWriter.close();
            System.out.println("Created file for peer: " + username + ".txt.");
    
            // Ανοίγουμε το αρχείο fileDownloadList.txt
            BufferedReader br = new BufferedReader(new FileReader("fileDownloadList.txt"));
    
            // Δημιουργούμε μια λίστα με τους μονούς αριθμούς γραμμών
            List<Integer> oddLines = new ArrayList<>();
            String line;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                lineNumber++;
                if (lineNumber % 2 == 0) {
                    oddLines.add(lineNumber);
                }
            }
    
            // Επιλέγουμε τυχαία 3 μονούς αριθμούς γραμμών
            Collections.shuffle(oddLines);
            List<Integer> selectedLines = oddLines.subList(0, 3);
    
            // Διαβάζουμε το αρχείο ξανά και περνάμε τις επιλεγμένες γραμμές στο αρχείο του χρήστη
            br.close();
            br = new BufferedReader(new FileReader("fileDownloadList.txt"));
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(sharedDirectory.getPath() + "/" + username + ".txt", true))) {
                lineNumber = 0;
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (selectedLines.contains(lineNumber)) {
                        bw.write(line); // Προσθέτουμε το όνομα χρήστη στην αρχή της γραμμής
                        bw.newLine();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            br.close();
            System.out.println("Added lines from fileDownloadList.txt to " + username + ".txt.");
        } catch (IOException e) {
            System.err.println("Error creating file for peer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private static void inform() {
        try {
            if (token_id == null) {
                System.out.println("You are logged out. Please login to inform.");
                return;
            } else {
                PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
                // Στείλε τις πληροφορίες του peer στον Tracker
                out.println("inform");
                out.println(username);
                out.println(peer_ip);
                out.println(peer_port);
                // Στείλε τα ονόματα των διαθέσιμων αρχείων ως ένα text 
                String fileContents = readContentsOfFile(username);
                String separator = "#"; // Χαρακτήρας διαχωρισμού
                out.println(fileContents.replaceAll("\n", separator)); // Στείλτε το fileContents με χαρακτήρα διαχωρισμού
                                                                    

                System.out.println("Informed tracker about peer's current content and communication info.");
            }

        } catch (IOException e) {
            System.err.println("Error informing tracker: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static String readContentsOfFile(String username) { // Διάβασε τα ονόματα αρχείων και βάλε διαχωριστικό #
        StringBuilder contents = new StringBuilder();
        String filename = "Shared_directory/" + username + "/" + username + ".txt";
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (!firstLine) {
                    contents.append("#");
                } else {
                    firstLine = false;
                }
                contents.append(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            e.printStackTrace();
            System.out.println();
        }
        return contents.toString();
    }

    @SuppressWarnings("unchecked")
    private static void list() { // Δέξου τα διαθέσιμα αρχεία
        try {

            if (token_id == null) {
                System.out.println("You are logged out. Please login to inform.");
                return;
            } else {
                PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
                out.println("list");

                ObjectInputStream objectInput = new ObjectInputStream(trackerSocket.getInputStream());

                fileList = (ArrayList<String>) objectInput.readObject();

                System.out.println("List of available files from tracker:");
            }
            for (String file : fileList) {
                System.out.println(file);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error listing files: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static Map<String, Long> executionTimesMap = new HashMap<>();

    private static void details() { // Πάρε τις πληροφορίες για το συγκεκριμένο αρχείο
        try {
            PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
            out.println("details");

            System.out.println("Enter filename:");
            String filename = userInput.readLine();
            out.println(filename);

            ObjectInputStream objectInput = new ObjectInputStream(trackerSocket.getInputStream());
            Object peerList = objectInput.readObject();

            @SuppressWarnings("unchecked")
            ArrayList<ArrayList<String>> peerInfoList = (ArrayList<ArrayList<String>>) peerList;
            System.out.println();
            if (peerList instanceof ArrayList) {
                if (peerInfoList.isEmpty()) {
                    System.out.println("No details available for file: " + filename);
                } else {
                    System.out.println("Details of owners of " + filename + ":"); // πληροφορίες για κάθε owner του αρχείου
                    System.out.println();
                    for (ArrayList<String> peerInfo : peerInfoList) {
                        System.out.println("Peer Info:");
                        System.out.println("Username: " + peerInfo.get(0));
                        System.out.println("IP Address: " + peerInfo.get(1));
                        System.out.println("Port: " + peerInfo.get(2));
                        System.out.println("Token Id: " + peerInfo.get(3));
                        System.out.println("Count Downloads: " + peerInfo.get(4));
                        System.out.println("Count Failures: " + peerInfo.get(5));
                  
                        // Καλέστε τη μέθοδο checkActive και κρατάμε τον χρόνο απόκρισης

                        if (peerInfo.size() >= 3) {
                            String username = peerInfo.get(0);
                            System.out.println("Checking if user " + username + " is active");
                            long startTime = System.nanoTime();
                            String checkMessage = checkActive(username);
                            if (checkMessage.startsWith("Active")) {
                                System.out.println("Check Active response time:");
                                long endTime = System.nanoTime();
                                long executionTime = endTime - startTime;
                                System.out.println("Start time: " + startTime);
                                System.out.println("End time: " + endTime);
                                System.out.println("Total time: " + executionTime);
                                executionTimesMap.put(username, executionTime);
                            }

                        }
                        System.out.println();
                    }
                }
            } else {
                System.out.println((String) peerList); // Print failure message
            }
            @SuppressWarnings("resource")
            Scanner scanner = new Scanner(System.in);
            System.out.println("Do you want to download the file? (1 for yes, 2 for no)");

            // Ανάγνωση της απάντησης του χρήστη
            int answer = scanner.nextInt();
            System.out.println();
            // Έλεγχος της απάντησης
            if (answer == 1) {
                System.out.println("Download process started for file: " + filename);
                // Κλήση της μεθόδου simpleDownload
                simpleDownload(executionTimesMap, peerInfoList, filename);
            } else if (answer == 2) {
                // Επιλογή όχι download
                System.out.println("No download selected. Exiting...");
            } else {
                // Μήνυμα λάθους εάν η απάντηση δεν είναι ούτε 1 ούτε 2
                System.out.println("Invalid choice. Exiting...");
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error getting file details: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Υπολογισμός του σκορ για έναν χρήστη
    private static double calculateScore(long executionTime, int countDownloads, int countFailures) {
        return executionTime * (Math.pow(0.75, countDownloads) * Math.pow(1.25, countFailures));
    }

    private static void simpleDownload(Map<String, Long> executionTimesMap, ArrayList<ArrayList<String>> peerInfoList,
            String file) {
        // Φτιάχνουμε ένα tree map για να αποθηκεύσουμε τα σκορ σε αύξουσα σειρά
        TreeMap<Double, String> sortedScores = new TreeMap<>();

        // Προσθέτουμε
        for (Map.Entry<String, Long> entry : executionTimesMap.entrySet()) {
            String testusername = entry.getKey();
            long executionTime = entry.getValue();
            int countDownloads = 0;
            int countFailures = 0;

            // Βρίσκουμε countDownloads και countFailures για κάθε χρήστη
            for (ArrayList<String> innerArray : peerInfoList) {
                if (innerArray.contains(testusername) && innerArray.size() >= 5) {
                    countDownloads = Integer.parseInt(innerArray.get(4));
                    countFailures = Integer.parseInt(innerArray.get(5));
                    break;
                }
            }

            double score = calculateScore(executionTime, countDownloads, countFailures);
            System.out.println("User " + testusername + " has score: " + score);
            sortedScores.put(score, testusername);
            System.out.println();
        }
        boolean flag = false;
    
        while (!sortedScores.isEmpty() || !flag) {
            if (sortedScores.isEmpty()) {

                break;
            }
            // Παίρνουμε τον χρήστη με το μικρότερο σκόρ
            double minScore = sortedScores.firstKey();
            String minUsername = sortedScores.get(minScore);
            double firstScore = sortedScores.firstKey();
            String firstUsername = sortedScores.get(firstScore);

            // Ελέγχουμε εαν είμαστε εμέις ο πρώτος χρήστης
            if (firstUsername.equals(minUsername)) {
                for (ArrayList<String> innerArray : peerInfoList) {
                    if (innerArray.contains(minUsername) && innerArray.size() >= 4) {
                        String userTokenId = innerArray.get(3);
                        if (userTokenId != null && userTokenId.equals(token_id)) {
                            sortedScores.remove(firstScore); // Μας διαγράφουμε απο την λίστα
                            System.out
                                    .println("This user was the current user. Removed current user from sortedScores.");
                            System.out.println();
                            minScore = sortedScores.firstKey();
                            minUsername = sortedScores.get(minScore);
                            firstScore = sortedScores.firstKey();
                            firstUsername = sortedScores.get(firstScore);
                            break;
                        }
                    }

                }
            }
            if (!sortedScores.isEmpty()) {
                // ελέχουμε εάν ο πρώτος χρήστης είναι active
                String status = checkActive(minUsername);
                if (status.startsWith("Inactive")) {
                    System.out.println("Failed to download file");
                    System.out.println("User " + minUsername + " is inactive. Trying the next one.");
                    // καλόυμε την notify για να ενημερωθεί ο tracker οτι είχαμε ανεπιτυχές download
                    notify(minUsername, "Inactive", file, username);
                    sortedScores.remove(minScore); // Δεν είναι active, τον διαγράφουμε
                } else if (status.startsWith("Active")) {

                    // Παίρνουμε το ip και το port ωστε να συνδεθούμε για να κατεβάσουμε το αρχείο
                    String peerIP = null;
                    int peerPort = -1;
                    for (ArrayList<String> innerArray : peerInfoList) {
                        if (innerArray.contains(minUsername)) {
                            peerIP = innerArray.get(1);
                            peerPort = Integer.parseInt(innerArray.get(2));
                            break;
                        }
                    }

                    // Connect με τον χρήστη που έχει το διαθέσιμο αρχείο
                    if (peerIP != null && peerPort != -1) {
                        try (Socket peerSocket = new Socket(peerIP, peerPort)) {
                            System.out.println("Connected to peer " + minUsername + " at " + peerIP + " : " + peerPort);
                            // καλόυμε την update για να ενημερώσει το shared_directory μας
                            update(username, file);
                            // καλόυμε την notify για να ενημερωθεί ο tracker οτι έγινε επιτυχές download
                            notify(minUsername, "Active", file, username);
                            System.out.println("File " + file + " successfully downloaded");
                            System.out.println();
                            ImageIcon icon = new ImageIcon("photo.jpg");
                            SwingUtilities.invokeLater(() -> new PhotoConfirmationDialog(icon)); // photo για επιτυχές download
                            return; 
                        } catch (IOException e) {
                            System.err.println("Error connecting to peer: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("Invalid or missing IP/port information for the selected peer.");
                    }
                    flag = true;
                } else {
                    System.out.println("Error from checkActive");
                }
            }
            System.out.println();

        }

        // If no active users found
        System.out.println("No active users found.");
    }

    private static String notify(String username, String update, String file, String username1) {
        try {

            if (token_id == null) {
                System.out.println("You are logged out. Please login to notify.");
                return "You are logged out";
            } else {
                PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
                out.println("notify");
                out.println(username);
                out.println(update);
                out.println(file);
                out.println(username1);

            }
            System.out.println();
            return "";

        } catch (IOException e) {
            System.err.println("Error informing tracker: " + e.getMessage());
            e.printStackTrace();
            return "Error";
        }

    }

    private static void update(String username, String content) {
        String filePath = "Shared_directory/" + username + "/" + username + ".txt";
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(filePath, true));
            fileWriter.write(content);
            fileWriter.newLine();
            fileWriter.close();
            System.out.println("Added " + content + " to " + username + ".txt.");
        } catch (IOException e) {
            System.err.println("Error updating file for peer: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    private static String checkActive(String username) {
        try {

            if (token_id == null) {
                System.out.println("You are logged out. Please login to checkActive.");
                System.out.println();
                return "You are logged out";

            } else {
                PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
                out.println("checkActive");
                out.println(username);
                BufferedReader in = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
                String response = in.readLine();

                if (response.startsWith("Active")) {

                    System.out.println("Peer " + username + " Active");
                    return "Active";
                } else if (response.startsWith("Inactive")) {
                    System.out.println("Peer " + username + " Inctive");
                    return "Inactive";
                } else {
                    System.out.println("Error");
                    return "error";
                }

            }

        }

        catch (IOException e) {
            System.err.println("Error informing tracker: " + e.getMessage());
            e.printStackTrace();
            return "Error";
        }
    }

    private static void logout() {
        try {
            if (token_id != null) {
                PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
                out.println("logout");
                out.println(token_id);

                BufferedReader in = new BufferedReader(new InputStreamReader(trackerSocket.getInputStream()));
                String trackerResponse = in.readLine();
                if (trackerResponse.equals("Logout successful")) {
                    System.out.println("Logout successful.");
                    token_id = null;
                } else {
                    System.out.println("Logout failed: " + trackerResponse);
                }
            } else {
                System.out.println("You are not logged in.");
            }
        } catch (IOException e) {
            System.err.println("Error logging out: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println();
    }

    private static void closeConnection() {
        try {
            PrintWriter out = new PrintWriter(trackerSocket.getOutputStream(), true);
            out.println("exit"); // message to tracker to star register process
            trackerSocket.close();
            System.out.println("Closed connection to tracker.");
        } catch (IOException e) {
            System.err.println("Error closing connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static int findAvailablePort() {
        int initialPort = 49152; // Αρχική τιμή για την αναζήτηση θύρας
        int maxPort = 65535; // Μέγιστο επιτρεπτό port

        // Δοκιμάζουμε κάθε port μέχρι να βρούμε μια διαθέσιμη θύρα
        for (int port = initialPort; port <= maxPort; port++) {
            if (isPortAvailable(port)) {
                return port;
            }
        }

        // Αν δεν βρεθεί διαθέσιμη θύρα, επιστρέφουμε -1
        return -1;
    }

    private static boolean isPortAvailable(int port) {
        try {
            // Δημιουργούμε ένα socket στη συγκεκριμένη θύρα
            ServerSocket ss = new ServerSocket(port);
            ss.close(); // Κλείνουμε το socket αμέσως μετά το άνοιγμα
            return true; // Η θύρα είναι διαθέσιμη
        } catch (Exception e) {
            return false; // Η θύρα είναι σε χρήση
        }
    }
}
