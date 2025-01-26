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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView username = (TextView) findViewById(R.id.username);
        TextView password = (TextView) findViewById(R.id.password);

        mylabel = (TextView) findViewById(R.id.mylabel);
        myBtn = (Button) findViewById(R.id.loginbtn);

        myBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (username.getText().toString().equals("admin") && password.getText().toString().equals("1234")) {
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("name",username.toString());
                    startActivity(intent);
                    username.setText("");
                    password.setText("");
                } else {
                    Toast.makeText(LoginActivity.this, "Wrong Username or Password",Toast.LENGTH_SHORT).show();

                }

            }
        });
    }
}