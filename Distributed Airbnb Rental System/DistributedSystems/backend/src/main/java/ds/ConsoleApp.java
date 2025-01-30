package ds;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.eclipse.collections.api.bag.MutableBag;
import org.eclipse.collections.impl.factory.Bags;

public class ConsoleApp extends Thread {
    private Scanner value1;                             // scanner for start date                
    private Scanner value2;                             // scanner for end date 
    private Scanner inp;                                // scanner for input
    private String endDate;                             // start date instance
    private String startDate;                           // end date instance
    private String finalJSONString;                     // finale json string instance
    private int managerID;                              // manager's personal ID instance
    private Room room;                                  // room object instance
    private String regex = "\\d{4}-\\d{2}-\\d{2}";      // regular expression to match the format YYYY-MM-DD
    private static String hostname = "localhost";       // hostname adress
    private static int port = 8000;                     // port
    private static String input;                        // input instance
    private static ArrayList<Results> arrayResults;     // array of results objects for printing
    // Constructor for add room function 
    ConsoleApp(Room room) throws IOException
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
            .create();
        finalJSONString= gson.toJson(room);
        //System.out.println(finalJSONString);
    }
    // Constructor for get bookings function
    ConsoleApp(int managerID)
    {
        this.managerID = managerID;
    }
    // Constructor for get bookings by area function
    ConsoleApp(String start , String end)
    {
        this.room = new Room();                 // initialize room instance
        this.room.setDateRange(start, end);     // set starting and ending date of room        
    }
    // Default constructor for running menu
    ConsoleApp() throws InterruptedException
    {
        runMenu();
    }
    // Welcomes the user manager
    public void welcome() {
        System.out.println("+---------------------------------+");
        System.out.println("|  You've logged in as a Manager  |");
        System.out.println("+---------------------------------+");

    }
    // Prints menu
    public void menu() {
        System.out.println("Please select a number and press enter to proceed. If you want to exit press 0 and enter to proceed.");
        System.out.println(" 1) Insert house information ");
        System.out.println(" 2) Get your bookings ");
        System.out.println(" 3) Get bookings by area");
        System.out.println(" 0) Exit");
    }
    // Run menu
    public void runMenu() throws InterruptedException {

        welcome();
        while (true) {
            menu();
            int choice = getInput();
            performAction(choice);
        }

    }
    // Get user input
    public int getInput() {
        inp = new Scanner(System.in); // initialize scanner
        int choice = -1;
        while (choice < 0 || choice > 3) { // user input number out of bounds
            try {
                System.out.println("Enter your selection: ");
                choice = Integer.parseInt(inp.nextLine()); // user input
            } catch (NumberFormatException e) {
                System.out.println("Invalid selection! Please try again.");
            }
        }
        return choice;
    }

    public void performAction(int choice) throws InterruptedException {
        switch (choice) {
            case 0:
                System.out.println("Thank you for using HouseBooking!");
                System.exit(0);
                break;
            case 1:
                System.out.println("Enter the path of your JSON file: ");
                String filePath = inp.nextLine();
                try {
                    String jsonString = readFileToString(filePath);                             // convert file path of json to string
                    if (jsonString != null) {
                        Room room = deserializeRoom(jsonString);                                // create room object from given json
                        System.out.println("Enter your starting date");
                        // Check if the user input matches the desired format  
                        while(true) {
                            value1 = new Scanner(System.in);
                            startDate = value1.nextLine();                                      // read starting date
                            if (Pattern.matches(regex, startDate)) {                            // check if regular expression of date format matches user's input
                                break;
                            } else {
                                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format!");
                            }                                                                        
                        }  
                        System.out.println("Enter your ending date");
                        while (true) {
                            value2 = new Scanner(System.in);                                   //read ending date 
                            endDate = value2.nextLine();                            
                            if (Pattern.matches(regex, endDate)) {                             // check if regular expression of date format matches user's input
                                room.setDateRange(startDate ,endDate);                         // set date range of room object
                                break;
                            } else {
                                System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format!");
                            }                                                                        
                        }
                        finalJSONString = serializeRoom(room);                                 // serialize room with changes to json                                                  
                        System.out.println("Final JSON File Content:\n" + finalJSONString);
                        input = "add room";
                        this.run();
                        System.out.println("Your room is now available for booking!\n");
                    } else {
                        System.out.println("Failed to read the JSON file.");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JsonSyntaxException e) {
                    System.err.println("Error parsing JSON: " + e.getMessage());
                } 
                break;
            case 2:
                System.out.println("Type your peronal ID and press enter to proceed");
                managerID = Integer.parseInt(inp.nextLine());
                input = "get booking";
                this.run();                                                                   // call run of current thread
                Collections.sort(arrayResults);
                for(Results res : arrayResults) {
                   res.printRooms();
                }       
                break;
            case 3:
                room = new Room();                                                             // create an empty room object
                System.out.println("Provide a valid date range");
                System.out.println("Enter your starting date");
                // Check if the user input matches the desired format  
                while(true) {
                    value1 = new Scanner(System.in);
                    startDate = value1.nextLine();                                              // read starting date
                    if (Pattern.matches(regex, startDate)) {                                    // check if regular expression of date format matches user's input
                        break;
                    } else {
                        System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format!");
                    }                                                                        
                }  
                System.out.println("Enter your ending date");
                while (true) {
                    value2 = new Scanner(System.in);
                    endDate = value2.nextLine();                                               // read ending date
                    if (Pattern.matches(regex, endDate)) {                                     // check if regular expression of date format matches user's input
                        room.setDateRange(startDate ,endDate);                                 // set date range (starting - ending) of room object
                        break;
                    } else {
                        System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format!");
                    }                                                                        
                }        
                input = "area booking";
                this.run(); 
                break;
            default:
                System.out.println("An unknown error has occured!");
                break;
        }
    }
    // Info chunck
    public String readFileToString(String filePath) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
    // Add results to array of results for printing
    private synchronized void addResults(Results results)
    {
        arrayResults.add(results);
    }
    // Serialize manager's personal ID
    private String serializeManagerID(int mID)
    {
        return new Gson().toJson(mID);
    }
    // Serializes room object to a json 
    private String serializeRoom(Room room)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
            .create();
        return gson.toJson(room);
    }
    // Deserializes json to a room object
    private Room deserializeRoom(String json)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .create();
        return gson.fromJson(json, Room.class);
    }    
    // Deserializes results object from json
    private Results deserializeResults(String json)
    {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .create();
        return gson.fromJson(json, Results.class);
    }
    @Override
    public void run() 
    {
        Socket socket = null;
        try {
            socket = new Socket(hostname, port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            if (input.equals("add room")) {                     // check if input is equal to 'add room'
                sendPostNewRoomRequest(out, finalJSONString);
                // Read the response
                String responseBody = extractBody(in);
                System.out.println("\n" + responseBody);
            } else if(input.equals("get booking")) {               // check if input is equal to 'get booking'
                arrayResults = new ArrayList<>();            
                sendGetBookRequest(out);                                    // send request in order to get the bookings
                String json = extractBody(in);                              // extract json from http request body 
                if (!json.startsWith("{\"message\"")) {
                    Results results = deserializeResults(json);             // create results object from json
                    addResults(results);                    
                } else {
                    System.out.println("\n" + json + "\n");                 // read the response
                }
            } else if(input.equals("area booking")) {              // check if input is equal to 'area bookings'
                sendAreaBookRequest(out);
                String json = extractBody(in);                              // extract json from http request body 
                if (!json.startsWith("{\"message\"")) {
                    Results results = deserializeResults(json);
                    MutableBag<String> areaBookings = Bags.mutable.empty();
                    for(RoomArray result: results.getResults()) {
                        for(Room room : result.getRooms()) {
                            int bookings = room.getBookings().size();               // get size of array bookings of room object
                            areaBookings.addOccurrences(room.getArea(), bookings);  // add room's area and number of bookings in bag
                        }
                    }
                    areaBookings.forEachWithOccurrences((key, occurrences) ->       // print area: and bookings:
                    System.out.println("\n" + key + ": " +  occurrences + "\n"));               
                } else {
                    System.out.println("\n" + json + "\n");    
                }
            } else {
                System.out.println("Unknown command. Use 'getRoom' or 'newRoom'.");
                inp.close();                                                // close scanner
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();                                             // close socket
            } catch (IOException e) {
                    e.printStackTrace();
            }
        }         
    } 
    // Extracts body from http search room request
    private static String extractBody(BufferedReader in) throws IOException 
    {
        StringBuilder requestBody = new StringBuilder();                    // build the body of request into string
        String line;                                                        // represents each line of http header
        // Extract content length
        int contentLength = 0;
        while (!(line = in.readLine()).isEmpty()) {                         // while message is not empty yet
            if (line.toLowerCase().startsWith("content-length:")) {
            contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
            }
        }    
        // Read the body
        if(contentLength > 0) {
            char[] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);
            requestBody.append(new String(buffer));
        }
        return requestBody.toString();
    }
    // Send request to add a new room 
    private void sendPostNewRoomRequest(PrintWriter out, String jsonBody) {
        out.println("POST /newRoom HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    // Sends request to get manager's bookings
    private void sendGetBookRequest(PrintWriter out) {
        String jsonBody = serializeManagerID(managerID);
        out.println("GET /getBooking HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    // Sends request to get bookings by area in a given date range      
    private void sendAreaBookRequest(PrintWriter out) {
        String jsonBody = serializeRoom(room);
        out.println("GET /getAreaBooking HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    public static void main(String[] args) throws IOException, InterruptedException {
        //new ConsoleApp().start();       // start thread for console app               
        Scanner sc = new Scanner(System.in);
        System.out.println("Give input");
        String in = sc.nextLine();
        if(in.equals("add room")) {
            for(int i = 0; i < 1; i++) {
                Room room1 = new Room(7, "Luxury Suite 1", 2, 200.0, 5,
                        "Athens", 100, "luxury_suite_1", "2024-04-01", "2024-04-07", true, new ArrayList<DateRange>());
                Room room2 = new Room(7, "Deluxe Suite", 3, 250.0, 4,
                        "Athens", 90, "deluxe_suite", "2024-04-02", "2024-04-08", true, new ArrayList<DateRange>());
                Room room3 = new Room(7, "Standard Room", 2, 150.0, 3,
                        "Thessaloniki", 80, "standard_room", "2024-04-03", "2024-04-09", true, new ArrayList<DateRange>());
                Room room4 = new Room(7, "Economy Room", 1, 100.0, 2,
                        "Thessaloniki", 70, "economy_room", "2024-04-04", "2024-04-10", true, new ArrayList<DateRange>());
                Room room5 = new Room(7, "Family Room", 4, 300.0, 5,
                        "Lamia", 120, "family_room", "2024-04-05", "2024-04-11", true, new ArrayList<DateRange>());
                Room room6 = new Room(13, "Suite", 2, 180.0, 4,
                        "Lamia", 110, "suite", "2024-04-06", "2024-04-12", true, new ArrayList<DateRange>());
                Room room7 = new Room(13, "Single Room", 1, 120.0, 3,
                        "Crete", 60, "single_room", "2024-04-07", "2024-04-13", true, new ArrayList<DateRange>());
                Room room8 = new Room(13, "Double Room", 2, 220.0, 4,
                        "Crete", 80, "double_room", "2024-04-08", "2024-04-14", true, new ArrayList<DateRange>());
                Room room9 = new Room(13, "Executive Suite", 3, 280.0, 5,
                        "Larisa", 150, "executive_suite", "2024-04-09", "2024-04-15", true, new ArrayList<DateRange>());
                Room room10 = new Room(13, "Penthouse", 6, 500.0, 5,
                        "Larisa", 200, "penthouse", "2024-04-10", "2024-04-16", true, new ArrayList<DateRange>());         
                input = in;
                new ConsoleApp(room1).start();
                new ConsoleApp(room2).start();
                new ConsoleApp(room3).start();       
                new ConsoleApp(room4).start();       
                new ConsoleApp(room5).start();       
                new ConsoleApp(room6).start(); 
                new ConsoleApp(room7).start(); 
                new ConsoleApp(room8).start(); 
                new ConsoleApp(room9).start();
                new ConsoleApp(room10).start();              
            }
        } else if(in.equals("get booking")) {
            input = in;
            for(int i = 0; i < 1; i++) {
                new ConsoleApp(7).start();
                //new ConsoleApp(13).start();
                Thread.sleep(1000);
                Collections.sort(arrayResults);
                for(Results res : arrayResults) {
                   res.printRooms();
                }       
            }
        } else if(in.equals("area booking")) {
            input = in;
            for(int i = 0; i < 1; i++) {
                new ConsoleApp("2024-04-01", "2024-04-16").start();
            }         
        }
        sc.close(); 
    }
}