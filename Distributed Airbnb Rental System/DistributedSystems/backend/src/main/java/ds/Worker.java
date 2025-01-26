// Worker Class
// Communication protocol is based on HTTP 
package ds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class Worker
{
    private ArrayList<Room> rooms;                                                      // rooms array
    private String[] propertyNames = {"area", "dateRange", "guests", "price", "stars"}; // array with common properties of Room and Filter 
    private static ServerSocket serverSocket;                                           // server socket 
    private static String hostname = "localhost";                                       // host name       
    private static int reducerPort = 8001;
    // Default constructor
    Worker(int port) throws IOException
    {                              
        this.rooms = new ArrayList<>();                                     // rooms array initialization
        serverSocket = new ServerSocket(port);                              // create socket
        System.out.println("Worker is listening on port " + port);
        while(true) {
            Socket connection = serverSocket.accept();                      // accepting incoming connection
            new Thread(() -> {
                try {
                    runServer(connection);                                  // open server side of worker
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();    
        }
    }
    // Constructor
    Worker()
    {
        this.rooms = new ArrayList<>();
    }
    // Adds room in rooms array
    private void addRoom(Room room)
    {
        synchronized(room) {
            rooms.add(room);
        }   
    }
    // Deserializes json file to a filter object
    private Filter deserializeFilter(String json)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .create();
        return gson.fromJson(json, Filter.class);
    }
    // Deserializes json file to a room object
    private Room deserializeRoom(String json)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .create();
        return gson.fromJson(json, Room.class);
    } 
    // Serialize results of worker's to Json 
    private String serializeResults(RoomArray resRooms)
    {
        Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
        .create();
        String jsonResults = gson.toJson(resRooms);
        return jsonResults;
    }
    // Deserializes json to request object 
    private Request deserializeRequest(String json)
    {
        return new Gson().fromJson(json, Request.class);
    }
    // Deserializes json to review object
    private static Review deserializeReview(String jsonString)
    {
        return new Gson().fromJson(jsonString, Review.class);

    }
    // Deserializes json file to booking object
    private Booking deserializeBooking(String json)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .create();
        return gson.fromJson(json, Booking.class);
    }
    // Checking if worker has a room according to the incoming filter
    // and returns array with indexes of rooms found
    private ArrayList<Integer> hasRoom(Filter filter) throws InterruptedException
    {   // Double flag 
        boolean result = false;            
        ArrayList<Integer> indexes = new ArrayList<>();                    // array to collect room indexes of rooms found
        if (filter.numNonZero() == 0)                // if Filter object has only null or 0 values on properties
        {
            return indexes;
        }
        for (Room room: rooms) {
            result = false;                         // for each room initialize result value to false
            if (filter == null && room == null) {   // both objects are null
                return indexes;
            }
            if (filter == null || room == null) {   // filter is null and room is not or the opposite
                return indexes;
            }
            if (room.numNonZero() == 0)             // if Room object has only null or 0 values on properties
            {
                return indexes;
            }    
            // Iterate through 5 common properties of Filter and Room object
            // and checks if properties are the same in each iteration
            for (String propertyName : propertyNames) {
                try {
                    Field fieldF = filter.getClass().getDeclaredField(propertyName);
                    Field fieldR = room.getClass().getDeclaredField(propertyName);
                    fieldF.setAccessible(true);
                    fieldR.setAccessible(true);
                    Object valueF = fieldF.get(filter);
                    Object valueR = fieldR.get(room);
                    if(valueF != null && valueF.getClass() == DateRange.class) {
                        if(((DateRange) valueF).getStartDate() == null || ((DateRange) valueF).getEndDate() == null) {    // special check for DateRanges to ignore if null
                            continue;
                        }
                    }
                    // Both values are not null or 0, compare them
                    if (valueF != null && valueR != null && !valueF.equals(0) && !valueR.equals(0) && 
                        !valueF.equals(0.0) && !valueR.equals(0.0)) {
                        if (valueF.equals(valueR)) {
                            result = true;          // properties are equal
                        } else {
                            result = false;
                            break;
                        }
                    } else if(valueF == null || valueR == null || valueF.equals(0) || valueR.equals(0) || 
                            valueF.equals(0.0) || valueR.equals(0.0)) {
                        continue;   // if some property of Room object or Filter object is null or 0
                    }
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            if(result) {
                indexes.add(rooms.indexOf(room));       // add index of iterated room to array
            } 
        }
        return indexes;
    }
    // Returns an array of rooms according to given filters and passes the filters' id to rooms
    private RoomArray map(ArrayList<Integer> indx,  Filter filter) throws InterruptedException
    {          
        Room resultRoom;                                            // room result instance
        RoomArray results = new RoomArray();                      // initialize array
        if (!indx.isEmpty()) {                                      // if worker has a room with the given filter
            for(Integer index : indx) {                             // for room index in indexes array
                resultRoom = rooms.get(index).copy();               // copy room
                results.addRoom(resultRoom);                        // add copy of room in the results array
                results.setId(filter.getId());                      // set id of the selected room equal to filter's id        
            }
        } else {
            results.setId(filter.getId());                          // set id of the selected room equal to filter's id                    
            return results;
        }
        return results;
    }
    // Set the room not available and does the booking
    private synchronized boolean book(Booking tuple)
    {
        boolean booked = false;  
        boolean ok = false; 
        ArrayList<DateRange> booking ;                
        for(Room room : rooms) {    
            if(room.getRoomName().equals(tuple.getRoomName()) ) {     // if room name from method's aruments is current room's name and room is available 
                if(tuple.getDate().isWithinRange(room.getDateRange())){
                    booking = room.getBookings(); 
                    if(booking.isEmpty()){
                        ok = true ;
                    }else{
                        for (DateRange b : booking){
                            if(tuple.getDate().conflictOfBooking(b) || b.isWithinRange(tuple.getDate())){ 
                                ok = false;
                                break;
                            }else {
                                ok =true ;
                            }                                                        
                        } 
                    }
                    if(ok){
                        room.addBooking(tuple.getDate());
                        room.setAvailable(false);
                        //System.out.println(room);
                        booked = true ;
                    }  
                }else{
                    booked = false ; //should've benen an exception
                }
            } 
        }
        return booked;   
    }            
    // Checks if a room with a matching manager id is booked
    // and returns indexes array with idexes of rooms found
    private ArrayList<Integer> isBooked(int managerID)
    {
        ArrayList<Integer> indexes = new ArrayList<>();                             // array to collect room indexes of rooms
        for(Room room : rooms) {                                                    // for each room in rooms array
            if(managerID == room.getManagerID() && room.getAvailable() == false) {  // if the two IDs match and current room is booked
                indexes.add(rooms.indexOf(room));                                   // add to indexes array the index of current room
            }
        }
        return indexes;     
    }

    // Checks if a room with a date range within the given date range is booked 
    // and returns indexes array with idexes of rooms found
    private ArrayList<Integer> isBookedDateRange(DateRange dateRange)
    {
        ArrayList<Integer> indexes = new ArrayList<>();                             // array to collect room indexes of rooms
        for(Room room: rooms) {                                                     // for each room in rooms array
            if(room.getAvailable() == false                                         // if current room is booked and 
                && room.getDateRange().isWithinRange(dateRange)) {                  // room's date range is within the given date range
                indexes.add(rooms.indexOf(room));                                   // add to indexes array the index of current room
            }
        }
        return indexes;
    }
    // Returns an array of rooms according to given date range and passes the request's id to array
    private RoomArray resultBookings(ArrayList<Integer> indx, Request request) 
    {
        Room resultRoom;                                            // room result instance
        RoomArray results = new RoomArray();                      // initialize array
        if (!indx.isEmpty()) {                                      // if indexes array isn't empty
            for(Integer index : indx) {                             // for room index in indexes array
                resultRoom = rooms.get(index).copy();               // copy room
                results.addRoom(resultRoom);                        // add copy of room in the results array
                results.setId(request.getId());                     // set id of the selected room equal to request's id        
            }
        } else {
            results.setId(request.getId());                     // set id of the selected room equal to request's id        
            return results;
        }
        return results;
    }
    // Gives reviews
    private synchronized boolean giveReview(Review review) {
        boolean roomFound = false;
        if(review.getReview() < 0 || review.getReview() > 5) {                  // check if review is out of bounds
            return roomFound;
        }
        for (Room room : rooms) {
            if (room.getRoomName().equals(review.getRoomForReview())) {         // find the room which has the given name
                int numberof = room.getReviews() + 1;                           // increase no of reviews
                double starof = room.getStars();                                // get the existing avg. of stars
                double sum = starof * room.getReviews();                        // compute the summary of all reviews so you can compute the next average
                room.setReviews(numberof);                                      // set the reviews +1
                room.setStars((sum + review.getReview()) / numberof);           // set the new avg. of stars
                roomFound = true;                                               // room is found so turn the roomFound to true
            }
        }
        return roomFound;                                                       // return the status of finding the room
    }
    // Opens worker's server side
    private void runServer(Socket connection) throws IOException, InterruptedException
    {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));      // get input stream in buffered reader
        OutputStream output = connection.getOutputStream();                                                 // get output stream from master's socket
        String requestLine = input.readLine();                                                              // set request line 
        if (requestLine == null || requestLine.isEmpty()) {
            Thread.interrupted();  // kill thread
        }
        // Header check
        if (requestLine.startsWith("POST /newRoom")) {               
            handleNewRoomRequest(input, output);
        } else if (requestLine.startsWith("POST /searchRoom")) {
            handleSearchRoomRequest(input, output);
        } else if(requestLine.startsWith("POST /bookRoom")) {
            handleBookRoomRequest(input, output);
        } else if(requestLine.startsWith("GET /getBooking")) {
            handleGetBookRequest(input, output);
        } else if(requestLine.startsWith("GET /getAreaBooking")) {
            handleAreaBookRequest(input, output);
        } else if(requestLine.startsWith("POST /giveReview")) {
            handleNewReviewRequest(input, output); 
        } else {
            sendNotImplementedResponse(output);
        }
        consumeRemainingRequest(input);
        connection.close();                                                                                 // close socket    
	}
    // Handles requests for new room insertion
    private void handleNewRoomRequest(BufferedReader in, OutputStream out) throws IOException {
        String jsonRoom = extractBody(in);                                                                  // extract json from request body
        Room room = deserializeRoom(jsonRoom);                                                              // create filter object from json input file
        System.out.println("\n" + room.toString());
        addRoom(room);                                                                                      // add room to array
        sendHttpResponse(out, 200, "OK", "{\"message\":\" " +room.getRoomName() +" added\"}",
                             "application/json");                                               // send response for succesfull http request
    }
    // Handles requests for searching a room
    private void handleSearchRoomRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException {
        String jsonFilter = extractBody(in);                                                                // extract json from request body
        Filter filter = deserializeFilter(jsonFilter);                                                      // create filter object from json input file
        System.out.println("\nReceived request "+ filter.getId() + " with filter: \n" 
                                +  filter.toString()  
                                + ", Thread: " + Thread.currentThread().threadId());
        RoomArray resultRooms = map(hasRoom(filter), filter);                                              // get array with results for reducer
        String jsonResults = serializeResults(resultRooms);                                                 // serialize results to json
        Thread reduce = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(hostname, reducerPort);                              // open socket to worker's port
            } catch (IOException e) {
                e.printStackTrace();
            }                               
            PrintWriter outputReducer = null;
            try {
                outputReducer = new PrintWriter(socket.getOutputStream(), true);       // set output
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader inputReducer = null;                          // buffer for inputs from worker
            try {
                inputReducer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }                    
            if(!resultRooms.getRooms().isEmpty()) {
                sendReducerRequest(outputReducer, jsonResults);     // send request to reducer 
            } else {
                try {
                    sendHttpResponse(out, 404, "Not Found", "No such room"
                    , "application/json");
                } catch (IOException e) {
                    e.printStackTrace();
                }    
            }
            // Read the response
            String responseBody = null;                                
            try {
                responseBody = extractBody(inputReducer);                // extract json with results 
                if(responseBody.equals("Reduction done")){
                    sendHttpResponse(out, 200, "OK", "Reduction done"
                    , "application/json");                              // send response for successful http request                                                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                consumeRemainingRequest(inputReducer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        reduce.start();
        reduce.join();
    }
    // Handles requests for booking
    private void handleBookRoomRequest(BufferedReader in, OutputStream out) throws IOException {
        String jsonBooking = extractBody(in);                                                                            // extract json with booking from request body
        Booking booking = deserializeBooking(jsonBooking);                                                              // create booking object from json 
        System.out.println("\nReceived request for booking: " + booking.getRoomName() + "\n");       
        if(book(booking)) {                                                                                              // book room by name and check if it is booked
            sendHttpResponse(out, 200, "OK", 
                            "{\"message\":\" " + booking.getRoomName() + " booked\"}",
                "application/json");                                                                         // send response for successful http request 
        } else {
            sendHttpResponse(out, 409, "Conflict", 
                            "{\"message\":\" " + booking.getRoomName() + " already booked\"}", 
                "application/json");                                                                        // send response for unsuccessful http request
        }
    }
    // Handles requests for given reviews
    private void handleNewReviewRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException {
        String jsonReview = extractBody(in);                                                                                // extract json from request body
        Review review = deserializeReview(jsonReview);                                                                      // create review object from json input file
        System.out.println("\nReceived rating request: " + review.toString());
        boolean done = giveReview(review);                                                                                  // get array with results for reducer
        if (done) { 
            sendHttpResponse(out, 200, "OK", "{\"message\":\"Review added\"}"
                        , "application/json");                                              // send response for succesfull http request
        }else{
            sendHttpResponse(out, 404, "Not Found", "{\"message\":\"Review out of bounds!\"}", 
            "application/json"); 
        }
    }    
    // Handles requests for getting bookings
    private void handleGetBookRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException 
    {
        String jsonRequest = extractBody(in);                                                   // extract json with managerID from http request body
        Request request = deserializeRequest(jsonRequest);                                      // create a request object with manager id from json
        int managerID = request.getManagerID();                                                 // get manager ID from request object
        System.out.println("\n" + "Received request "+ request.getId() 
                                + " with manager ID: "+ managerID 
                                + ", Thread: " + Thread.currentThread().threadId());
        RoomArray bookings = resultBookings(isBooked(managerID), request);             // get array with bookings(booked rooms) for manager
        String jsonResults = serializeResults(bookings);                                // serialize results with bookings to json
        Thread reduce = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(hostname, reducerPort);                              // open socket to worker's port
            } catch (IOException e) {
                e.printStackTrace();
            }                               
            PrintWriter outputReducer = null;
            try {
                outputReducer = new PrintWriter(socket.getOutputStream(), true);       // set output
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader inputReducer = null;                          // buffer for inputs from worker
            try {
                inputReducer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }                    
            if(!bookings.getRooms().isEmpty()) {                    // if bookings are not empty
                sendReducerRequest(outputReducer, jsonResults);     // send request to reducer 
            } else {
                try {
                    sendHttpResponse(out, 404, "Not Found", "No bookings found"
                    , "application/json");
                } catch (IOException e) {
                    e.printStackTrace();
                }    
            }
            // Read the response
            String responseBody = null;                                
            try {
                responseBody = extractBody(inputReducer);                // extract json with results 
                if(responseBody.equals("Reduction done")){
                    sendHttpResponse(out, 200, "OK", "Reduction done"
                    , "application/json");                              // send response for successful http request                                                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                consumeRemainingRequest(inputReducer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        reduce.start();
        reduce.join();
    }
    // Handles requests for getting bookings by area in given date range
    private void handleAreaBookRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException
    {
        String jsonRoom = extractBody(in);                                              // extract json of room object with given date range from http request body
        Room room = deserializeRoom(jsonRoom);                                          // create a room object with given date range from json
        System.out.println("\nReceived request: " + room.getId() + " with:"  
                            + " \n" + "Date range: " + room.getDateRange() 
                            + ", Thread: " + Thread.currentThread().threadId());
        RoomArray bookings = resultBookings(isBookedDateRange(room.getDateRange()), room);
        String jsonResults = serializeResults(bookings);                                // serialize results with bookings to json
        Thread reduce = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(hostname, reducerPort);                              // open socket to worker's port
            } catch (IOException e) {
                e.printStackTrace();
            }                               
            PrintWriter outputReducer = null;
            try {
                outputReducer = new PrintWriter(socket.getOutputStream(), true);       // set output
            } catch (IOException e) {
                e.printStackTrace();
            }
            BufferedReader inputReducer = null;                          // buffer for inputs from worker
            try {
                inputReducer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }                    
            if(!bookings.getRooms().isEmpty()) {                    // if bookings are not empty
                sendReducerRequest(outputReducer, jsonResults);     // send request to reducer 
            } else {
                try {
                    sendHttpResponse(out, 404, "Not Found", "No bookings found"
                    , "application/json");
                } catch (IOException e) {
                    e.printStackTrace();
                }    
            }
            // Read the response
            String responseBody = null;                                
            try {
                responseBody = extractBody(inputReducer);                // extract json with results 
                if(responseBody.equals("Reduction done")){
                    sendHttpResponse(out, 200, "OK", "Reduction done"
                    , "application/json");                              // send response for successful http request                                                    
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                consumeRemainingRequest(inputReducer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        reduce.start();
        reduce.join();
    }
    // Sends request to reducer
    private void sendReducerRequest(PrintWriter out, String jsonBody)
    {
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    // Sends error message when the server is incapable of performing the request
    private static void sendNotImplementedResponse(OutputStream out) throws IOException {
        sendHttpResponse(out, 501, "Not Implemented", "", "text/plain");
    }

    // Builds and sends the http response
    private static void sendHttpResponse(OutputStream out, int statusCode, String statusMessage, String body, String contentType) throws IOException {
        String httpResponse = String.format("HTTP/1.1 %d %s\r\nContent-Type: %s\r\nContent-Length: %d\r\n\r\n%s", statusCode, statusMessage, 
                                            contentType, body.getBytes().length, body);                     // format of http header
        //System.out.println(httpResponse);   
        out.write(httpResponse.getBytes());
    }
    // Consume the remainig request
    private static void consumeRemainingRequest(BufferedReader in) throws IOException {
        while (in.ready()) {
            in.readLine();
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
    public static void main(String[] args) throws IOException, InterruptedException{
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter port");
        new Worker(sc.nextInt());
        sc.close(); 
        /* 
        Room room1 = new Room(12,"Luxury Suite", 2, 200.0, 5,
                                "Downtown", 100, "luxury_suite.jpg", "2024-04-03", "2024-04-06", false);
        Room room2 = new Room(12,"Cozy Cabin", 4, 150.0, 4,
                                "Mountains", 80, "cozy_cabin.jpg", "2024-04-02", "2024-04-05", false);
        
        Worker worker = new Worker();
        worker.addRoom(room1);
        worker.addRoom(room2);
        DateRange dr = new DateRange();
        dr.setStartDate("2024-04-03");
        dr.setEndDate("2024-04-06");
        System.out.println(worker.isBookedDateRange(dr));
        */
    }
}