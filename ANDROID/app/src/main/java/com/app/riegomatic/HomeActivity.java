package com.app.riegomatic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;
public class HomeActivity extends AppCompatActivity implements Contract.ViewMVP {

    private TextView textStatus;
    private Contract.PresenterMVP presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textStatus = findViewById(R.id.status);
        Button btn_back = findViewById(R.id.back);
        Button btn_watering = findViewById(R.id.watering);

        btn_back.setOnClickListener(btnListener);
        btn_watering.setOnClickListener(btnListener);
        
        presenter = new Presenter(this);

        // btn_back.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View view) {
        //         Intent intent2 = new Intent(view.getContext(), MainActivity.class);
        //         startActivityForResult(intent2,0);
        //     }
        // });
    }

    @SuppressLint("Range")
    @Override
    public void setString(String string) {
        textStatus.setText(string);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            Intent intent;
            switch(view.getId()) {
                case R.id.watering:
                    presenter.onButtonClick();
                    break;
                default:
                    throw new IllegalStateException("Unexpexted value" + view.getId());
            }
        }
    };
}