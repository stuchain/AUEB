package com.example.frontend;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    EditText areaIn;
    EditText startDateIn;
    EditText endDateIn;
    EditText guestsIn;
    EditText priceIn;
    EditText starsIn;
    Button submit;
    public Handler searchHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() { //Handler (searchHandler) attached to the main (UI) thread.
        @Override
        public boolean handleMessage(@NonNull Message msg) { // handles messages, if error message then it displays the toast, else starts results activity
                String jsonResults = msg.getData().getString("Results");
                if(jsonResults.startsWith("{\"message\"")) {                  // if json string is null
                    Toast.makeText(MainActivity.this, jsonResults, Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), ResultsActivity.class);
                    intent.putExtra("Results", jsonResults);
                    Log.d("DEBUG", intent.getExtras().toString());
                    startActivity(intent);
                }
            return false;
        }
    });
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // main activity layout
        areaIn = findViewById(R.id.area);
        startDateIn = findViewById(R.id.startDate);
        endDateIn = findViewById(R.id.endDate);
        guestsIn = findViewById(R.id.guests);
        priceIn = findViewById(R.id.price);
        starsIn = findViewById(R.id.stars);
        submit = findViewById(R.id.submit); //Retrieves each input field and the submit button from the layout by their IDs.

        submit.setOnClickListener(new View.OnClickListener() { //Sets an OnClickListener on the submit button.
            @Override
            public void onClick(View v) { ///Uses a ternary operator to assign a default (null or 0/0.0) if the field is empty.
                String area = areaIn.getText().toString().isEmpty() ? null: areaIn.getText().toString();                            // get area
                String startDate = startDateIn.getText().toString().isEmpty() ? null: startDateIn.getText().toString();             // get start date
                String endDate = endDateIn.getText().toString().isEmpty() ? null: endDateIn.getText().toString();                   // get end date
                double stars = starsIn.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(starsIn.getText().toString());     // get stars
                int guests = guestsIn.getText().toString().isEmpty() ? 0 : Integer.parseInt(guestsIn.getText().toString());         // get guests
                double price = priceIn.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(priceIn.getText().toString());     // get price
                new Dummy(searchHandler, "search",  area, startDate, endDate, guests, //Creates and starts a new instance of the Dummy thread, passing the searchHandler, the action "search", and the collected parameters.
                        price, stars).start();
                Log.d("Debug", "Thread started");
            }
        });
    }
}