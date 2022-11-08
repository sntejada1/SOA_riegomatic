package com.app.riegomatic;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;

public class MainActivity extends AppCompatActivity implements Contract.ViewMVP {

    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button login = findViewById(R.id.login);
        
        login.setOnClickListener(btnListener);


    }

    @SuppressLint("Range")
    @Override
    public void setString(String string) {
        textView.setText(string);
    }
    public void setHumedad(String string) {
        
    }
    public void setWater(String string) {
        
    }
    public void setEstado(String string) {

    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.login:
                    //se genera un Intent para poder lanzar la activity principal
                    intent = new Intent(MainActivity.this, HomeActivity.class);

                    //Se le agrega al intent los parametros que se le quieren pasar a la activyt principal
                    //cuando se lanzado
                    intent.putExtra("textoOrigen", "HOOLA"); // Esta linea por ahora no hace falta ya que no le voy a pasar ningun parametro al nuevo activity 

                    //se inicia la activity principal
                    startActivity(intent);

                    finish();

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
        }
    };
}