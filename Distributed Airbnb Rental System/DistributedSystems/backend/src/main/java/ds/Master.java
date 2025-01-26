// Master class
// Communication protocol is based on HTTP 
package ds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;

public class Master
{
    public static WorkerConfig workerConfig;        // workers configuration instance
    private static ServerSocket serverSocket;       // server socker 
    private static int port = 8000;                 // set port of master
    private static String hostnameW = "172.20.10.2";   // host name of workers
    private static String hostnameR = "172.20.10.14";   // host name of reducer
    private static int requestID = 0;               // id for request
    private static int reducerPort = 8001;
    // Master constructor
    Master() throws IOException
    {
        serverSocket = new ServerSocket(port);                          // create server socket
        System.out.println("Master is listening on port " + port);
        while(true) {
            Socket connection = serverSocket.accept();                  // accept the incoming connections 
            new Thread(() -> {                                          // create new thread 
                try {
                    runServer(connection);                              // run server side of master
                } catch (IOException e) {
                    e.printStackTrace();                                // exception in thread 
                } catch (InterruptedException e) {
                    e.printStackTrace();                                // exception in thread 
                }
            }).start();
        }
    }
    // Give unique number in order in the next request
    private synchronized int getUniqueNumber() {
        return requestID++;                                           
    }
    // Sets unique id to request 
    private void setUniqueID(Request request)
    {
        request.setId(getUniqueNumber());
    }
    // Hash function 
    private static int hashFunc(String roomName, int numOfWorkers) {
        int hashCode = roomName.hashCode();                         // hashing room's name
        int absHashCode = Math.abs(hashCode);                       // give absolute value
        int nodeID = absHashCode % 3;                               // node id inside boundries
        nodeID++;                                                   // starting from 1 to number of nodes
        return nodeID;
    }
    // Serializers, Deserializes
    // Deserializes json to a filter object
    private Filter deserializeFilter(String json)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .create();
        return gson.fromJson(json, Filter.class);
    }
    // Deserializes json to a room object
    private Room deserializeRoom(String json)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .create();
        return gson.fromJson(json, Room.class);
    }    
    // Serializes room object to json  
    private String serializeRoom(Room room)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
            .create();
        return gson.toJson(room);
    }    
    // Serializes filter object to json 
    private String serializeFilter(Filter filter)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
            .create();
        return gson.toJson(filter);
    }
    // Deserializes json file to an integer (manager ID)
    private int deserializeManagerID(String json)
    {
        return new Gson().fromJson(json, Integer.class);
    }
    // Deserializes json file to booking object
    private Booking deserializeBooking(String json)
    {
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
            .create();
        return gson.fromJson(json, Booking.class);
    }
    // Serializes request object to json 
    private String serializeRequest(Request request)
    {
        return new Gson().toJson(request);
    }
    // Opens master's server side
    private void runServer(Socket connection) throws IOException, InterruptedException
    {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));      // get input stream in buffered reader
        OutputStream output = connection.getOutputStream();                                                 // get output stream from master's socket
        String requestLine = input.readLine();                                                              // set request line 
        if (requestLine == null || requestLine.isEmpty()) {
            return;                                                                                         // kill thread running
        }
        // Header check
        if (requestLine.startsWith("POST /newRoom")) {               
            handleNewRoomRequest(input, output);
        } else if (requestLine.startsWith("POST /searchRoom")) {
            handleSearchRoomRequest(input, output);
            //clientOutput = output;
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
        connection.close();                                                                                 // close connection        
    }
    // Returns decided port of worker by hash function
    private static int getPort(int wID)
    {
        int resultPort = 0;
        for(Worker worker : workerConfig.workers) {     // for worker in workers array
            //System.out.println("WorkerID: " + worker.workerID);
            if(worker.workerID == wID) {           // if ids match
                resultPort = worker.port;                      // return port
            } else {
                //System.out.println("NO ID FOUND");
            }
        }
        return resultPort;   
    }
    // Handles requests for new room insertion
    private void handleNewRoomRequest(BufferedReader in, OutputStream out) throws IOException {
        String jsonRoom = extractBody(in);                                              // extract json from request body
        Room room = deserializeRoom(jsonRoom);                                          // get room object from json
        System.out.println("\n" + "Received request for adding room: " 
                                +  room.getRoomName()  
                                + ", Thread: " + Thread.currentThread().threadId());
        // Client side of master                                    
        int workerID = hashFunc(room.getRoomName(), workerConfig.nofWorkers);           // hash room name and get worker id to send request  
        int workerPort = getPort(workerID);                                             // get port of selected worker
        new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(hostnameW, workerPort);                              // open socket to worker's port
            } catch (IOException e) {
                e.printStackTrace();
            }                               
            PrintWriter output = null;
            try {
                output = new PrintWriter(socket.getOutputStream(), true);       // set output
            } catch (IOException e) {
                e.printStackTrace();
            } 
            try (BufferedReader inputWorker = new BufferedReader
                                        (new InputStreamReader(socket.getInputStream()))) {
                sendNewRoomRequest(output, jsonRoom);                                   // send request
                // Read the response
                String responseLine = extractBody(inputWorker);
                System.out.println("\n" + responseLine);                                   // print response message
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();                                                             // close socket
            } catch (IOException e) {
                e.printStackTrace();
            }                                                                              
        }).start();   
        sendHttpResponse(out, 200, "OK",                        // send response for succesfull http request
        "{\"message\":\" " + room.getRoomName() + " added\"}"
           , "application/json");                          
    }
    // Handles requests for searching room
    private void handleSearchRoomRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException {
        String jsonFilter = extractBody(in);                                        // extract json from http request body
        Filter filter = deserializeFilter(jsonFilter);                              // create filter object from json 
        setUniqueID(filter);                                               // set a unique id to filter and reducer object
        jsonFilter = serializeFilter(filter);                                       // convert filter object back to json
        System.out.println("\n" + "Received request "+ filter.getId() + " with filter: \n" 
                                +  filter.toString()  
                                + ", Thread: " + Thread.currentThread().threadId());
        // Client side of master                            
        final String json = jsonFilter;                                     // make it final because of try/catch
        Flag reduction = new Flag();                                        // create flag object for lambda expression
        for(Worker worker : workerConfig.workers) {                         // for each worker configured
            Thread work  = new Thread(() -> {
                Socket socket = null;
                try {
                    socket = new Socket(hostnameW, worker.port);             // open socket to worker's port
                } catch (IOException e) {
                    e.printStackTrace();
                }                                 
                PrintWriter outputWorker = null;                            // set output to worker
                try {
                    outputWorker = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }                                             
                BufferedReader inputWorker = null;                          // buffer for inputs from worker
                try {
                    inputWorker = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }                    
                sendSearchRoomRequest(outputWorker,json);                   // send request to each worker
                // Read the response
                String responseBody = null;                                
                try {
                    responseBody = extractBody(inputWorker);                // extract json with results 
                    if(responseBody.equals("Reduction done")) {     // if reduction is done by reducer
                        reduction.setFlag(true);                        // set flag to true
                    }                   
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    consumeRemainingRequest(inputWorker);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();                                         // close socket
                } catch (IOException e) {
                    e.printStackTrace();
                }                                                                                
            });
            work.start();
            work.join();
        }
        if(!reduction.getFlag()) {      // if flag for reduction isn't true
            try {
                // Send response for unsuccesfull http request
                sendHttpResponse(out, 404, "Not Found", "{\"message\":\"No such room\"}"
                , "application/json");                  
            } catch (IOException e) {
                e.printStackTrace();
            }                                                    
        } else {
            String jsonID = new Gson().toJson(filter.getId());
            Socket socket = new Socket(hostnameR, reducerPort);
            PrintWriter outputReducer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inputReducer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendReducerRequest(outputReducer, jsonID);
            // Read the response
            String responseBody = extractBody(inputReducer);
            sendHttpResponse(out, 200, "OK", responseBody
            , "application/json");                              // send response for successful http request                                    
            consumeRemainingRequest(inputReducer);
            socket.close();
        }
    }
    // Handles requests for booking room
    private void handleBookRoomRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException 
    {
        String jsonBooking = extractBody(in);                                               // extract json with booking from request body
        Booking booking = deserializeBooking(jsonBooking);                                  // create booking object from json
        System.out.println("\n" + "Received book request for room: " + booking.getRoomName());
        int workerID = hashFunc(booking.getRoomName(), workerConfig.nofWorkers);                         // hash room name of booking and get worker id to send request  
        int workerPort = getPort(workerID);                                                 // get port of selected worker
        Thread book  = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(hostnameW, workerPort);                              // open socket to worker's port
            } catch (IOException e) {
                e.printStackTrace();
            }                               
            PrintWriter outputWorker = null;
            try {
                outputWorker = new PrintWriter(socket.getOutputStream(), true);       // set output
            } catch (IOException e) {
                e.printStackTrace();
            } 
            try (BufferedReader inputWorker = new BufferedReader
                                        (new InputStreamReader(socket.getInputStream()))) {
                sendBookRoomRequest(outputWorker, jsonBooking);                                   // send book request
                // Read the response
                String responseLine;
                while ((responseLine = inputWorker.readLine()) != null) {
                    if(responseLine.startsWith("HTTP/1.1 409 Conflict")) {
                        sendHttpResponse(out, 409, "Conflict", 
                        "{\"message\":\" " + booking.getRoomName() +  " already booked\"}", 
                        "application/json");                                    // send response for unsuccessful http request            
                    } else if(responseLine.startsWith("HTTP/1.1 200 OK")) {
                        sendHttpResponse(out, 200, "OK", 
                        "{\"message\":\" " + booking.getRoomName() + " booked\"}",
                        "application/json");                                    // send response for successful http request             
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }                                                                           // close socket
        });
        book.start();
        book.join();
    }
    private void handleNewReviewRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException {
        String jsonReview = extractBody(in);                                            // extract json from request body
        Review review = new Gson().fromJson(jsonReview, Review.class);         // create filter object from json 
        System.out.println("\n" + "Received request with review: \n" 
                                +  review.toString()  
                                + ", Thread: " + Thread.currentThread().threadId());
        final String json = jsonReview;     // make it final to work
        // Client side of master
        int workerID = hashFunc(review.getRoomForReview(), workerConfig.nofWorkers);           // hash room name and get worker id to send request  
        int workerPort = getPort(workerID);                                                    // for each worker configured
        Thread rev = new Thread(() -> {
            Socket socket = null;
            try {
                socket = new Socket(hostnameW, workerPort);
            } catch (IOException e) {
                e.printStackTrace();
            }                                   // open socket to worker's port
            PrintWriter output = null;
            try {
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }                                   // set output
            BufferedReader inputWorker = null;
            try {
                inputWorker = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }                                   // buffer for inputs from worker            
            sendNewReviewRequest(output,json);                                                 // send a new Review request
            // Read the response
            String responseBody = null;                                
            try {
                responseBody = extractBody(inputWorker);                // extract json with results 
                if(responseBody.equals("{\"message\":\"Review out of bounds!\"}")) {
                    sendHttpResponse(out, 404, "Not Found", "{\"message\":\"Review out of bounds!\"}", 
                    "application/json");                                    // send response for unsuccessful http request            
                } else if(responseBody.startsWith("{\"message\":\"Review added\"}")) {
                    sendHttpResponse(out, 200, "OK", "{\"message\":\"Review added\"}",
                    "application/json");                                    // send response for successful http request             
                }
            } catch (IOException e) {
                e.printStackTrace();
            }     
            System.out.println("\n" + responseBody);          
            try {
                consumeRemainingRequest(inputWorker);
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }                                                                             // close socket               
        });
        rev.start();
        rev.join();
    }
    // Handles requests for getting bookings
    private void handleGetBookRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException 
    {
        String jsonMangerID = extractBody(in);                                      // extract json with managerID from request body
        int managerID = deserializeManagerID(jsonMangerID);                         // get manager ID from json 
        Request request = new Request();                                            // create request object
        request.setManagerID(managerID);                                            // set manager ID field of request object
        setUniqueID(request);                                              // set unique ID to request and reducer object 
        String jsonRequest = serializeRequest(request);                             // create json from request object
        System.out.println("\n" + "Received request "+ request.getId() 
                                + " with manager ID: "+ managerID 
                                + ", Thread: " + Thread.currentThread().threadId());
        Flag reduction = new Flag();                                        // create flag object for lambda expression                                
        for(Worker worker : workerConfig.workers) {                                 // for each worker configured
            Thread work = new Thread(() -> { 
                Socket socket = null;
                try {
                    socket = new Socket(hostnameW, worker.port);                     // open socket to worker's port
                } catch (IOException e) {
                    e.printStackTrace();
                }                                 
                PrintWriter outputWorker = null;                                    // set output to worker
                try {
                    outputWorker = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }                                             
                BufferedReader inputWorker = null;                                  // buffer for inputs from worker
                try {
                    inputWorker = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendGetBookRequest(outputWorker, jsonRequest);                      // send request to get bookings
                // Read the response
                String responseBody = null;                                
                try {
                    responseBody = extractBody(inputWorker);                // extract json with results 
                    if(responseBody.equals("Reduction done")) {     // if reduction is done by reducer
                        reduction.setFlag(true);                        // set flag to true
                    }                   
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    consumeRemainingRequest(inputWorker);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();                                         // close socket
                } catch (IOException e) {
                    e.printStackTrace();
                }                                                                                
            });
            work.start();                                                           // start thread
            work.join();                                                            // call external thread to wait for inside thread to finish
        }
        if(!reduction.getFlag()) {      // if flag for reduction isn't true
            try {
                // Send response for unsuccesfull http request
                sendHttpResponse(out, 404, "Not Found", "{\"message\":\"No bookings found\"}"
                , "application/json");                  
            } catch (IOException e) {
                e.printStackTrace();
            }                                                    
        } else {
            String jsonID = new Gson().toJson(request.getId());
            Socket socket = new Socket(hostnameR, reducerPort);
            PrintWriter outputReducer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inputReducer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendReducerRequest(outputReducer, jsonID);
            // Read the response
            String responseBody = extractBody(inputReducer);
            sendHttpResponse(out, 200, "OK", responseBody
            , "application/json");                              // send response for successful http request                                    
            consumeRemainingRequest(inputReducer);
            socket.close();
        }
    }
    // Handles requests for getting bookings by area in given date range
    private void handleAreaBookRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException
    {
        String jsonRoom = extractBody(in);                                          // extract json of room with given data range from http request body
        Room room = deserializeRoom(jsonRoom);                                      // get room object from json 
        setUniqueID(room);                                                          // set unique ID to room object
        String jsonDataRange = serializeRoom(room);                                 // create json of room with given data range from room object
        System.out.println("\nReceived request: " + room.getId() + " with:"  
                            + " \n" + "Date range: " + room.getDateRange() 
                            + ", Thread: " + Thread.currentThread().threadId());
        Flag reduction = new Flag();                                                // create flag object for lambda expression                                        
        for(Worker worker : workerConfig.workers) {                                 // for each worker configured
            Thread work = new Thread(() -> {                                        // create thread 
                Socket socket = null;
                try {
                    socket = new Socket(hostnameW, worker.port);                     // open socket to worker's port
                } catch (IOException e) {
                    e.printStackTrace();
                }                                 
                PrintWriter outputWorker = null;                                    // set output to worker
                try {
                    outputWorker = new PrintWriter(socket.getOutputStream(), true);
                } catch (IOException e) {
                    e.printStackTrace();
                }                                             
                BufferedReader inputWorker = null;                                  // buffer for inputs from worker
                try {
                    inputWorker = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sendAreaBookRequest(outputWorker, jsonDataRange);                   // send request with given date range
                // Read the response
                String responseBody = null;                                
                try {
                    responseBody = extractBody(inputWorker);                // extract json with results 
                    if(responseBody.equals("Reduction done")) {     // if reduction is done by reducer
                        reduction.setFlag(true);                        // set flag to true
                    }                   
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    consumeRemainingRequest(inputWorker);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    socket.close();                                         // close socket
                } catch (IOException e) {
                    e.printStackTrace();
                }                                                                                
            });
            work.start();
            work.join();
        }           
        if(!reduction.getFlag()) {      // if flag for reduction isn't true
            try {
                // Send response for unsuccesfull http request
                sendHttpResponse(out, 404, "Not Found", "{\"message\":\"No bookings found\"}"
                , "application/json");                  
            } catch (IOException e) {
                e.printStackTrace();
            }                                                    
        } else {
            String jsonID = new Gson().toJson(room.getId());
            Socket socket = new Socket(hostnameR, reducerPort);
            PrintWriter outputReducer = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader inputReducer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            sendReducerRequest(outputReducer, jsonID);
            // Read the response
            String responseBody = extractBody(inputReducer);
            sendHttpResponse(out, 200, "OK", responseBody
            , "application/json");                              // send response for successful http request                                    
            consumeRemainingRequest(inputReducer);
            socket.close();
        }
    }
    // Sends request to reducer
    private void sendReducerRequest(PrintWriter out, String jsonBody)
    {
        out.println("GET /getResult HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }

    // Sends http request for searching a room to worker
    private void sendSearchRoomRequest(PrintWriter out, String jsonBody) {
        out.println("POST /searchRoom HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    // Sends http request for adding a room to worker
    private void sendNewRoomRequest(PrintWriter out, String jsonBody) {
        out.println("POST /newRoom HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    // Sends http request for booking a room to worker
    private void sendBookRoomRequest(PrintWriter out, String jsonBody) {
        out.println("POST /bookRoom HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    private void sendNewReviewRequest (PrintWriter out, String jsonBody){
        out.println("POST /giveReview HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " +  jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    // Sends request to get manager's bookings
    private void sendGetBookRequest(PrintWriter out, String jsonBody) {
        out.println("GET /getBooking HTTP/1.1");
        out.println("Host: localhost");
        out.println("Content-Type: application/json");
        out.println("Content-Length: " + jsonBody.length());
        out.println("Connection: close");
        out.println();
        out.println(jsonBody);
    }
    // Sends request to get bookings by area in a given date range      
    private void sendAreaBookRequest(PrintWriter out, String jsonBody) {
        out.println("GET /getAreaBooking HTTP/1.1");
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
                                            contentType, body.getBytes().length, body); // format of http header
        //System.out.println(httpResponse);   
        out.write(httpResponse.getBytes());
    }
    // Clear buffer in case anything is left
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
            //System.out.println(line);                   // print http response
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
    // Reads filepath and creates json string
    public static String readFileToString(String filePath) throws IOException 
    {
        return new String(Files.readAllBytes(Paths.get(filePath)));
    }
    // Inner class worker
    private class Worker
    {
        private int workerID;           // ID of worker
        private String ipAddress;       // worker's ip address
        private int port;               // worker's port

        @Override
        public String toString() 
        {
            return "Worker: " + workerID + 
                    " ipAddress='" + ipAddress + '\'' +
                    ", port=" + port;
        }
    }
    // Inner class worker configuration
    private class WorkerConfig
    {
        private int nofWorkers;                 // number of workers
        private ArrayList<Worker> workers;      // array of workers

        @Override
        public String toString() {
            return "WorkerConfig: " +
                    "nofWorkers=" + nofWorkers +
                    ", workers=" + workers;
        }
    }
    public static void main(String[] args) throws IOException {
        //if(args.length < 1) {
        //    System.out.println("Usage: java Master.java <json_path>");
        //    return;
        //}
        //System.out.println(workerConfig.toString()); 
        //Scanner scan = new Scanner(System.in);
        //System.out.println("Enter path of configuration file 'workers'");
        //String filepath = readFileToString(scan.nextLine()); // read file from input and convert it to string
        String filepath = readFileToString("/Users/stu/Desktop/DistributedSystems/workers.json");
        Gson gson = new Gson();
        workerConfig = gson.fromJson(filepath, WorkerConfig.class);    // create worker config object from json
        new Master();
        //scan.close();
    }
}