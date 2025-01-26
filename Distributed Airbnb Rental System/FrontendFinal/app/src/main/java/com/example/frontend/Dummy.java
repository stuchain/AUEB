// Dummy App for users
// Communication protocol is based on HTTP
package com.example.frontend;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

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
   private static String hostname = "192.251.248.253";                  // hostname ip address
   private static int port = 8000;                                // port instance  
   private String input;                                   // input instance
   private static ArrayList<Results> arrayResults;                // array with results for printing
   private Scanner sc;                                            // scanner instance
   private Filter filter;                                         // filter object instance
   private String roomNameReview;                                 // room name for review
   private double rating;                                         // rating instance
   private Review review;                                         // review object that we are going to send
   private String regex = "\\d{4}-\\d{2}-\\d{2}";                 // regular expression to match the format YYYY-MM-DD
   private Booking booking;                                       // booking instance
   private Handler handler;                                       // handler instance
   // Constructor in case of sending a Filter object in a search request
   Dummy(Handler handler, String input, String area, String startDate, String endDate, int guests,
         double price, double stars)
   {
      this.handler = handler;
      this.input = input;
      this.filter = new Filter();
      this.filter.setArea(area);
      if(startDate != null && endDate != null) {         // if date range isn't [null, null]
         this.filter.setDate(startDate, endDate);
      }
      this.filter.setGuests(guests);
      this.filter.setPrice(price);
      this.filter.setStars(stars);
   }
   // Constructor in case of sending a room in a book request
   Dummy(Handler handler, String input, String roomName, String startDate, String endDate)
   {
      this.handler = handler;
      this.input = input;
      booking = new Booking();
      booking.setDate(startDate, endDate);
      booking.setRoomName(roomName);
   }
   // Constructor in case of sending a Review object in a rate review
   Dummy(Handler handler, String input, double stars, String roomNameReview)
   {
      this.handler = handler;
      this.input = input;
      review = new Review(stars, roomNameReview);
   }
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
         sendSearchRoomRequest(out);                                   // send request for searching a room
         String jsonResult = extractBody(in);                                // extract json from http request body
         Message msg = new Message();
         Bundle bundle = new Bundle();
         bundle.putString("Results", jsonResult);        // wrap json to data bundle
         msg.setData(bundle);                            // set data to message
         handler.sendMessage(msg);                       // send message
      }  else if(input.equals("book")) {                    // check if input is equal to 'book'
         sendBookRoomRequest(out);                          // send request for booking a room
         String jsonBooking = extractBody(in);              // read response to booking
         Message msg = new Message();
         Bundle bundle = new Bundle();
         bundle.putString("Booking", jsonBooking);          // wrap json to data bundle
         msg.setData(bundle);                               // set data to message
         handler.sendMessage(msg);                          // send message
      } else if(input.equals("rate")) {                     // check if input is equal to 'rate'
         sendNewReviewRequest(out);                         // send request for rating a room
         String jsonRating = extractBody(in);             // read response to rating
         Message msg = new Message();
         Bundle bundle = new Bundle();
         bundle.putString("Rating", jsonRating);          // wrap json to data bundle
         msg.setData(bundle);                               // set data to message
         handler.sendMessage(msg);                          // send message
      }
      else {
         System.out.println("Unknown command. Use 'search' or 'book' or 'rate'.");
         sc.close();                                                // close scanner
         return;                                                    
      }
      } catch (IOException e) {
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
      //new Dummy().start();
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
