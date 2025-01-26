// Dummy App for users
// Communication protocol is based on HTTP
package ds;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Dummy extends Thread {                      
   private static String hostname = "localhost";                  // hostname ip address
   private static int port = 8000;                                // port instance  
   private static String input;                                   // input instance
   private static ArrayList<Results> arrayResults;                // array with results for printing
   private Scanner sc;                                            // scanner instance
   private Filter filter;                                         // filter object instance
   private String roomNameReview;                                 // room name for review
   private double rating;                                         // rating instance
   private Review review;                                         // review object that we are going to send
   private String regex = "\\d{4}-\\d{2}-\\d{2}";                 // regular expression to match the format YYYY-MM-DD
   private Booking booking;                                       // booking instance

   // Constructor in case of sending a Filter object in a search request
   Dummy(String area, String startDate, String endDate, int guests, double price, int stars) 
   {
      this.filter = new Filter();
      this.filter.setArea(area);
      if(startDate != null && endDate != null) {         // if date ramge isn't [null, null]
         this.filter.setDate(startDate, endDate);
      }
      this.filter.setGuests(guests);
      this.filter.setPrice(price);
      this.filter.setStars(stars);
   }
   // Constuctor in case of sending a room in a book request
   Dummy(String roomName, String startDate, String endDate)
   {
      booking = new Booking();
      booking.setDate(startDate, endDate);
      booking.setRoomName(roomName);
   }
   // Constructor in case of sending a Review object in a rate review
   Dummy(double stars, String roomNameReview)
   {
      review = new Review(stars, roomNameReview);
   }
   // Default constructor in case we want to send a Filter object with a menu
   Dummy() throws InterruptedException 
   {
      this.filter = new Filter();
      runMenu();
   }
   // Print the Header of the menu
   public void header() {
      System.out.println("+----------------------------+");
      System.out.println("|   Welcome to Housebooking  |");
      System.out.println("+----------------------------+");
   }
   // Print the menu of choice when called
   public void menu() {
      System.out.println();
      System.out.println("Please select a number and press enter to proceed. If you want to exit press 0 and enter to proceed.");
      System.out.println(" 1) Area");
      System.out.println(" 2) Date Range");
      System.out.println(" 3) Number of guests");
      System.out.println(" 4) Price");
      System.out.println(" 5) Number of stars");
      System.out.println(" 6) Search");
      System.out.println(" 7) Book");
      System.out.println(" 8) Rate");      
      System.out.println(" 0) Exit");
   }
   // Run the menu 
   public void runMenu() throws InterruptedException {
      this.header();
      while(true) {
         this.menu();
         int choice = this.getInput();
         this.performAction(choice);
      }
   }
   // Get the choice and check if is eligible to continue 
   public int getInput() {
      this.sc = new Scanner(System.in);
      int choice = -1;

      while(choice < 0 || choice > 8) {
         try {
            System.out.println("Enter your selection: ");
            choice = Integer.parseInt(this.sc.nextLine());                        
         } catch (NumberFormatException var3) {
            System.out.println("Invalid selection! Please try again.");
         }
      }
      return choice;
   }
   // Perform the actions needed based on the user choice
   public void performAction(int choice) throws InterruptedException {
      switch (choice) {
         case 0:
            System.out.println("Thank you for using HouseBooking!");
            System.exit(0);
            break;
         case 1:
            System.out.println("Enter area's name");
            this.filter.setArea(this.sc.nextLine());
            break;
         case 2:
            System.out.println("Enter a starting date");
            // Check if the user input matches the desired format  
            String startDate = null;
            while(true) {
               Scanner start = new Scanner(System.in);
               startDate = start.nextLine();    
               if (Pattern.matches(regex, startDate)) {          // check if regular expression of date format matches user's input
                  break;
               } else {
                  System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format!");
               }                                                                        
            } 
            System.out.println("Enter your ending date");
            while (true) {
               Scanner end = new Scanner(System.in);
               String endDate = end.nextLine();      
               if (Pattern.matches(regex, endDate)) {            // check if regular expression of date format matches user's input
                  this.filter.setDate(startDate, endDate);
                  break;
               } else {
                  System.out.println("Invalid date format. Please enter date in YYYY-MM-DD format!");
               }                                                                        
            }            
            break;
         case 3:
            System.out.println("Enter a number of guests");
            this.filter.setGuests(Integer.parseInt(this.sc.nextLine()));      // get input and parse it to make it the right type
            break;
         case 4:
            System.out.println("Enter a price");
            this.filter.setPrice(Double.parseDouble(this.sc.nextLine()));     // get input and parse it to make it the right type           
            break;
         case 5:
            System.out.println("Enter a number of stars");
            this.filter.setStars(Integer.parseInt(this.sc.nextLine())); 
            break;                                                            // get input and parse it to make it the right type         
         case 6: 
            input = "search";   
            search();                                                          // run via search () 
            break;
         case 7:
            Scanner rn = new Scanner(System.in);
            System.out.println("Type the name of the room you wish to book");
            //roomName = rn.nextLine();                                          // get the roomName
            input = "book";                  
            book();                                                            // run via book()
            break;
         case 8:
            Scanner rn2 = new Scanner(System.in);
            System.out.println("Type the name of the room you wish to rate");
            roomNameReview = rn2.nextLine();
            System.out.println("Rate room from 0 to 5 and press enter to proceed");
            rating = Double.parseDouble(rn2.nextLine());
            review = new Review();
            review.setRoomForReview(roomNameReview);
            review.setReview(rating);
            input = "rate";
            rate();
            break ; 
         default:
            System.out.println("An unknown error has occured!");
      }
   }
   // Sends filter to master and prints results
   private void search() throws InterruptedException
   {
      this.run();                                        // create request thread and send it to master
      Thread.sleep(1000);
   }
   // Sends request for booking a room
   private void book()
   {
      this.run();
   }
   // Sends request for rating a room
   private void rate()
   {
      this.run();
   }
   // Serializes filter object to json 
   private String serializeFilter(Filter filter)
   {
      Gson gson = new GsonBuilder()
         .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
         .create();
      return gson.toJson(filter);
   }
   // Serializes booking object to json
   private String serializeBooking(Booking booking)
   {
      Gson gson = new GsonBuilder()
         .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
         .create();
      return gson.toJson(booking);
   }
   // Deserializes results object from json
   private Results deserializeResults(String json)
   {
      Gson gson = new GsonBuilder()
               .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
               .create();
      return gson.fromJson(json, Results.class);
   }
   // Add results to array of results for printing
   private synchronized void addResults(Results results)
   {
      arrayResults.add(results);
   }
   @Override
   public void run() 
   {
      Socket socket = null;
      try {
         socket = new Socket(hostname, port);                          //open connection to Master
      } catch (IOException e) {
         e.printStackTrace();
      }
      try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
         BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
      if (input.equals("search")) {                           // check if input is equal to 'search' 
         arrayResults = new ArrayList<>();
         sendSearchRoomRequest(out);                                   // send request for searching a room
         String json = extractBody(in);                                // extract json from http request body
         if(json.startsWith("{\"message\"")) {                  // if json string is null             
            System.out.println(json);                                  // print the appropriate message 
            return;
         } else {
            Results results = deserializeResults(json);
            addResults(results);
            results.printRooms();            
         }
      }  else if(input.equals("book")) {                             // check if input is equal to 'book'
         sendBookRoomRequest(out);                                            // send request for booking a room
         String responseBody = extractBody(in);                               // read response
         System.out.println("\n" + responseBody);                     
      } else if(input.equals("rate")) {                              // check if input is equal to 'rate'
         sendNewReviewRequest(out);                                           // send request for rating a room
         String responseBody = extractBody(in);                               // read response
         System.out.println("\n" + responseBody);
      }
      else {
         System.out.println("Unknown command. Use 'search' or 'book' or 'rate'.");
         sc.close();                                                // close scanner
         return;                                                    
      }
      } catch (IOException e) {
         e.printStackTrace();
      } catch (InterruptedException e) {
         e.printStackTrace();
      } finally {
         try {
            socket.close();                                          // close socket
         } catch (IOException e) {
               e.printStackTrace();
         }
      }         
   }
   // Extracts body from http search room request
   private static String extractBody(BufferedReader in) throws IOException 
   {
   StringBuilder requestBody = new StringBuilder();    // build the body of request into string
   String line;                                        // represents each line of http header
   // Extract content length
   int contentLength = 0;
      while (!(line = in.readLine()).isEmpty()) {
            if (line.toLowerCase().startsWith("content-length:")) {
               contentLength = Integer.parseInt(line.substring("content-length:".length()).trim());
            }
      }    
      // Read the body
      if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            in.read(buffer, 0, contentLength);
            requestBody.append(new String(buffer));
      }
      return requestBody.toString();
   }
   // Send search request to Master
   private void sendSearchRoomRequest(PrintWriter out) {
      String jsonBody = serializeFilter(this.filter);
      out.println("POST /searchRoom HTTP/1.1");
      out.println("Host: localhost");
      out.println("Content-Type: application/json");
      out.println("Content-Length: " + jsonBody.length());
      out.println("Connection: close");
      out.println();
      out.println(jsonBody);
   }
   // Send book request to Master
   private void sendBookRoomRequest(PrintWriter out) {
      String jsonBody = serializeBooking(booking);          // serialize booking object to json
      out.println("POST /bookRoom HTTP/1.1");
      out.println("Host: localhost");
      out.println("Content-Type: application/json");
      out.println("Content-Length: " + jsonBody.length());
      out.println("Connection: close");
      out.println();
      out.println(jsonBody);
   }
   // Send rate request to Master
   private void sendNewReviewRequest(PrintWriter out) {
      String jsonBody = new Gson().toJson(this.review);
      out.println("POST /giveReview HTTP/1.1");
      out.println("Host: localhost");
      out.println("Content-Type: application/json");
      out.println("Content-Length: " + jsonBody.length());
      out.println("Connection: close");
      out.println();
      out.println(jsonBody);
   }
   public static void main(String[] args) throws IOException, InterruptedException {
      new Dummy().start(); 
      /*   
      Scanner scan = new Scanner(System.in);
      System.out.println("Give input: [search, book, rate]");
      input = scan.nextLine();
      if(input .equals("search")) {  // Search()
         arrayResults = new ArrayList<>();
         for(int i = 0; i < 1; i++) {
            (new Dummy(null, null, null, 2, 0.0, 0)).start(); 
            (new Dummy(null, null, null, 1, 0.0, 0)).start(); 
            (new Dummy(null, null, null, 0, 0.0, 0)).start();             
         }
         Thread.sleep(1000);
         Collections.sort(arrayResults);
         for(Results res : arrayResults) {
            res.printRooms();
         }
      } else if (input.equals("book")){   // Book()
         for(int i = 0; i < 1; i++) {
            new Dummy("Double Room", "2024-04-08", "2024-04-10").start();
            new Dummy("Double Room", "2024-04-11", "2024-04-13").start();
            /* 
            new Dummy("Single Room").start();
            new Dummy("Family Room").start();
            new Dummy("Suite").start();
            new Dummy("Deluxe Suite").start();
            new Dummy("Luxury Suite 1").start();
            new Dummy("Economy Room").start();
            new Dummy("Penthouse").start();
            new Dummy("Standard Room").start();
            new Dummy("Executive Suite").start();
         }         
      } else if(input.equals("rate")) { // Rate()
            new Dummy(4.2,"Single Room").start();
            new Dummy(3.1,"Luxury Suite 1").start();
            new Dummy(2.6,"Kostas Camping").start();
      }
      scan.close();
      */
   }  
}
