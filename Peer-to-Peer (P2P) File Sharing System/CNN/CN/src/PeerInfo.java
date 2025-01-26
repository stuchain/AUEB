import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.ArrayList;
import java.util.UUID; // Προσθήκη της κλάσης UUID

class PeerInfo {
    private String userName;
    private String password;
    private int countDownloads;
    private int countFailures;
    private boolean loggedIn;
    private ArrayList<String> availableFiles;
    private String ipAddress;
    private int port;
    private String tokenID;

    public PeerInfo(String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.countDownloads = 0;
        this.countFailures = 0;
        this.loggedIn = false;
        this.availableFiles = new ArrayList<>();
        this.ipAddress = "";
        this.port=0;
    }

    public void login() {
        this.loggedIn = true;
        this.tokenID = UUID.randomUUID().toString(); // Τυχαία δημιουργία tokenID
        System.out.println("Login successful. Username: " + userName + ", TokenID: " + tokenID); // Εκτύπωση των στοιχείων εισόδου
    }

    public void informPeer(String peer_ip,int peer_port){
        this.ipAddress = peer_ip;
        this.port = peer_port;
    }

    public void logout() {
        this.loggedIn = false;
        this.ipAddress = "";
        this.port = 0;
        this.tokenID = null;
        System.out.println("Logout successful.");
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public String getTokenID() {
        return tokenID;
    }

    public void addAvailableFile(String fileName) {
        availableFiles.add(fileName);
    }

    public ArrayList<String> getAvailableFiles() {
        return availableFiles;
    }
    public boolean fileExists(String filename) {
        return availableFiles.contains(filename);
    }
    
    public void incrementDownloads() {
        countDownloads++;
    }

    public void incrementFailures() {
        countFailures++;
    }

    public int getCountDownloads() {
        return countDownloads;
    }

    public int getCountFailures() {
        return countFailures;
    }

    public String getUserName() {
        return userName;
    }
    public String getPassword() {
        return password;
    }
}
