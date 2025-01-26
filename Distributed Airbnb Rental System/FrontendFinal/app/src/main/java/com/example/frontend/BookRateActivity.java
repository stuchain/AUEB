package com.example.frontend;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.LocalDate;

public class BookRateActivity extends AppCompatActivity
{
    ImageView roomImage;
    TextView roomInformation;
    EditText startDateIn;
    EditText endDateIn;
    Button book;
    EditText rateIn;
    Button rate;
    // Deserializes json to a room object
    private Room deserializeRoom(String json)
    {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .create();
        return gson.fromJson(json, Room.class);
    }
    // Handler for book request
    private Handler bookHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String response = msg.getData().getString("Booking");
            Toast.makeText(BookRateActivity.this, response, Toast.LENGTH_SHORT).show();
            return false;
        }
    });
    // Handler for rate request
    private Handler rateHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            String response = msg.getData().getString("Rating");
            Toast.makeText(BookRateActivity.this, response, Toast.LENGTH_SHORT).show();
            return false;
        }
    });
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_rate);
        roomImage = findViewById(R.id.roomImage);
        roomInformation = findViewById(R.id.information);
        startDateIn = findViewById(R.id.startDate);
        endDateIn = findViewById(R.id.endDate);
        book = findViewById(R.id.bookButton);
        rateIn = findViewById(R.id.inputRate);
        rate = findViewById(R.id.rateButton);

        Intent intent = getIntent();                            // get intent from Results Activity
        String json = intent.getStringExtra("RoomInfo");    // get room's json
        Log.d("DEBUG",json);
        Room room = deserializeRoom(json);                      // create room object from json
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), getImageResourceByName(room.getRoomImage()));
        roomImage.setImageBitmap(bitmap);                       // set image of room
        roomInformation.setText(room.toString());               // set text of room info

        // Button book setup
        book.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startDate = startDateIn.getText().toString();    // get start date from user input
                String endDate = endDateIn.getText().toString();        // get end date from user input
                new Dummy(bookHandler, "book", room.getRoomName(), startDate, endDate).start();
                Log.d("Debug", "Thread started");
            }
        });
        // Button rate setup
        rate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double stars = Double.parseDouble(rateIn.getText().toString());     // get rating (stars)
                new Dummy(rateHandler, "rate", stars, room.getRoomName()).start();
                Log.d("Debug", "Thread started");
            }
        });
    }
    // Returns image resource id by its name
    private int getImageResourceByName(String name) {
        return getResources().getIdentifier(name, "drawable", getPackageName());
    }
}
