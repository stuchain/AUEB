package com.example.frontend;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;
import android.content.Intent;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class LoginActivity extends AppCompatActivity {

    TextView mylabel;
    Button myBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) { // Where the activity is created
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Sets the layout for the activity to the XML file activity_login.xml

        TextView username = (TextView) findViewById(R.id.username);
        TextView password = (TextView) findViewById(R.id.password); // Retrieves the username and password input fields from the layout by their IDs.

        mylabel = (TextView) findViewById(R.id.mylabel);
        myBtn = (Button) findViewById(R.id.loginbtn); // Label and login button from the layout

        myBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // when login button is clicked, the code inside the onClick method is executed.
                if (username.getText().toString().equals("admin") && password.getText().toString().equals("1234")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("name",username.toString());
                    startActivity(intent); //Calls startActivity(intent) to switch to MainActivity.
                    username.setText("");
                    password.setText("");
                } else {
                    Toast.makeText(LoginActivity.this, "Wrong Username or Password",Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}