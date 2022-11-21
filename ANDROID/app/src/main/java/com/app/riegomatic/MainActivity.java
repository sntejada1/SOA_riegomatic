package com.app.riegomatic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //muestro la view..
        Button login = findViewById(R.id.login);
        login.setOnClickListener(btnListener);
    }

    @SuppressLint("Range")
    public void setString(String string) {
        textView.setText(string);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            Intent intent;
            //Intent para poder lanzar la activity principal
            intent = new Intent(MainActivity.this, HomeActivity.class);

            startActivity(intent); //se inicia la activity principal
            finish();
        }
    };
}