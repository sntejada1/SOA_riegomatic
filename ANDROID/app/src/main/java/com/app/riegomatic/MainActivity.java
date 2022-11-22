package com.app.riegomatic;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    public ConexionBlutooth mConexionBluetooth; // solo se va a utilizar para saber el estado del bt(encendido/apago) y prenderlo en caso que sea necesario.
    private final int estaConectado = 1;
    private final int on = 1;
    private final int off = 0;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //muestro la view..
        mConexionBluetooth = new ConexionBlutooth();

        Button login = findViewById(R.id.login);
        login.setOnClickListener(btnListener);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.login:
                    startHome();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startHome() {
        if(this.checkStatusBt() == estaConectado) {
            Intent intent;
            intent = new Intent(MainActivity.this, HomeActivity.class);
            //se inicia la activity principal
            startActivity(intent);
            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private int checkStatusBt(){
        if ( mConexionBluetooth.checkBtState(this) != estaConectado) {
            mConexionBluetooth.encenderBluetooth(this);
            return off;
        }
        return on;
    }
}