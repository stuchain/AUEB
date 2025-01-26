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
    public Handler searchHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
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
        setContentView(R.layout.activity_main);
        areaIn = findViewById(R.id.area);
        startDateIn = findViewById(R.id.startDate);
        endDateIn = findViewById(R.id.endDate);
        guestsIn = findViewById(R.id.guests);
        priceIn = findViewById(R.id.price);
        starsIn = findViewById(R.id.stars);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String area = areaIn.getText().toString().isEmpty() ? null: areaIn.getText().toString();                            // get area
                String startDate = startDateIn.getText().toString().isEmpty() ? null: startDateIn.getText().toString();             // get start date
                String endDate = endDateIn.getText().toString().isEmpty() ? null: endDateIn.getText().toString();                   // get end date
                double stars = starsIn.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(starsIn.getText().toString());     // get stars
                int guests = guestsIn.getText().toString().isEmpty() ? 0 : Integer.parseInt(guestsIn.getText().toString());         // get guests
                double price = priceIn.getText().toString().isEmpty() ? 0.0 : Double.parseDouble(priceIn.getText().toString());     // get price
                new Dummy(searchHandler, "search",  area, startDate, endDate, guests,
                        price, stars).start();
                Log.d("Debug", "Thread started");
            }
        });
    }
}