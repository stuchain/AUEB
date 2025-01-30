package ds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Reducer 
{   
    private static ServerSocket serverSocket;                           // server socker 
    private static int port = 8001;                                     // set port of reducer       
    private Map<Integer, ArrayList<RoomArray>> reduceResults;           // array with reduced results

    // Reducer constructor
    Reducer() throws IOException
    {
        reduceResults = new HashMap<>();                                // reduce results initialization
        serverSocket = new ServerSocket(port);                          // create server socket
        System.out.println("Reducer is listening on port " + port);
        while(true) {
            Socket connection = serverSocket.accept();                  // accept the incoming connections 
            new Thread(() -> {                                          // create new thread 
                try {
                    runServer(connection);                              // run server side of reducer
                } catch (IOException e) {
                    e.printStackTrace();                                // exception in thread 
                } catch (InterruptedException e) {
                    e.printStackTrace();                                // exception in thread 
                }
            }).start();
        }
    }
    // Deserialize json to results  
    private RoomArray deserializeRoomArray(String json)
    {
        Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
        .create();
        return gson.fromJson(json, RoomArray.class);
    }
    private String serializeResults(Results results)
    {
        Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
        .create();
        return gson.toJson(results);
    }
    // Method that adds the RoomResult type object to the Array
    private synchronized void reduce(RoomArray resRooms) throws InterruptedException
    {
        if(reduceResults.containsKey(resRooms.getId())) {                          
            this.reduceResults.get(resRooms.getId()).add(resRooms);        
            //notify();
        } else {
            this.reduceResults.put(resRooms.getId(), new ArrayList<>());
            this.reduceResults.get(resRooms.getId()).add(resRooms);
            //wait(); 
        }    
    }
    // Returns result from hash map
    private Results getResults(int requestId)
    {
        Results results = new Results();
        for(Integer id : reduceResults.keySet()) {
            if(id == requestId) {
                results.setId(requestId);                   // set id of results object
                results.setResults(reduceResults.get(id));  // set array with results to results object's array
            }
        }
        return results;
    }
    // Runs server side of reducer
    private void runServer(Socket connection) throws IOException, InterruptedException
    {
        BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));      // get input stream in buffered reader
        OutputStream output = connection.getOutputStream();                                                 // get output stream from reducer's socket
        String requestLine = input.readLine();                                                              // set request line 
        if (requestLine == null || requestLine.isEmpty()) {
            return;                                                                                         // kill thread running
        } else if(requestLine.startsWith("GET /getResult")) {
            handleGetResultRequest(input, output);
        } else {
            handleRequest(input, output);
        }
        consumeRemainingRequest(input);
        connection.close();                                                                                 // close connection      
        //System.out.println(results.toString());
    }
    // Handles requests from master for getting results
    private void handleGetResultRequest(BufferedReader in, OutputStream out) throws IOException
    {
        String jsonID = extractBody(in);
        int id = new Gson().fromJson(jsonID, Integer.class);
        System.out.println("\nReceived request for results with id: " + id 
                            + ", Thread : " + Thread.currentThread().threadId());
        String jsonResults = serializeResults(getResults(id));
        sendHttpResponse(out, 200, "OK", jsonResults
        , "application/json");                                          // send response for successful http request                                
    }
    // Handles incoming requests for reducer
    private void handleRequest(BufferedReader in, OutputStream out) throws IOException, InterruptedException
    {
        //Result results = new Result();
        String jsonRoomArray = extractBody(in);                                        // extract json from http request body
        RoomArray result = deserializeRoomArray(jsonRoomArray);
        reduce(result);
        sendHttpResponse(out, 200, "OK", "Reduction done"
        , "application/json");                              // send response for successful http request                        
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
    // Builds and sends the http response
    private static void sendHttpResponse(OutputStream out, int statusCode, String statusMessage, String body, String contentType) throws IOException {
        String httpResponse = String.format("HTTP/1.1 %d %s\r\nContent-Type: %s\r\nContent-Length: %d\r\n\r\n%s", statusCode, statusMessage, 
                                            contentType, body.getBytes().length, body);                     // format of http header
        //System.out.println(httpResponse);   
        out.write(httpResponse.getBytes());
    }
    // Clear buffer in case anything is left
    private static void consumeRemainingRequest(BufferedReader in) throws IOException {
        while (in.ready()) {                                                            
            in.readLine();
        }
    }    
    public static void main(String[] args) throws IOException {
        new Reducer();
    }
}