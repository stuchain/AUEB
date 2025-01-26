package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;
import java.util.ArrayList;

public class ResultsActivity extends AppCompatActivity
{
    ListView listView;      // list view instance
    // Deserializes results object from json
    private Results deserializeResults(String json)
    {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .create();
        return gson.fromJson(json, Results.class);
    }
    // Serializes room object to json
    private String serializeRoom(Room room)
    {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .create();
        return gson.toJson(room);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);
        Log.d("DEBUG", "I am in!");
        Intent intent = getIntent();                                // get intent object from main activity
        Log.d("DEBUG", intent.getExtras().toString());
        String json = intent.getExtras().getString("Results");  // get json from intent object
        Results results = deserializeResults(json);                 // get results from json

        listView = (ListView)findViewById(R.id.listview);           // find list view in xml
        ArrayList<Room> roomsResult = new ArrayList<>();            // create an array for rooms
        for(RoomArray array : results.getResults()) {               // iterate through results and
            for(Room room: array.getRooms()) {                      // get the rooms
                roomsResult.add(room);
                Log.d("DEBUG", room.getRoomName());
            }
        }
        RoomsAdapter adapter = new RoomsAdapter(getLayoutInflater(), roomsResult);    // create and set adapter
        listView.setAdapter(adapter);                               // set adapter of listview

        // Change to BookRateActivity when click on an element of listView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Room room = (Room) parent.getItemAtPosition(position);  // get room object from list
                String json = serializeRoom(room);                      // create json with room
                Intent intent = new Intent(getApplicationContext(), BookRateActivity.class);    // setup intent object
                intent.putExtra("RoomInfo", json);
                startActivity(intent);
            }
        });
    }
}