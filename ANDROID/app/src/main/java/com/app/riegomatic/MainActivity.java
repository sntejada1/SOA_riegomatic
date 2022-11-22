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

public class MainActivity extends AppCompatActivity implements Contract.ViewMVP {

    private TextView textView;
    private BluetoothAdapter btAdapter;
    public ConexionBlutooth mConexionBluetooth; // solo se va a utilizar para saber el estado del bt(encendido/apago) y prenderlo en caso que sea necesario.
    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //muestro la view..

        mConexionBluetooth = new ConexionBlutooth();

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

    @Override
    public void setFlag(int valor) {

    }

    @Override
    public void mostrarBtnConectar() {

    }

    @Override
    public void ocultarBtnConectar() {

    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            Intent intent;
            switch (view.getId()) {
                case R.id.login:
                    startHome();
                    //se genera un Intent para poder lanzar la activity principal
                    //intent = new Intent(MainActivity.this, HomeActivity.class);

                    //se inicia la activity principal
                    //startActivity(intent);

                    //finish();

                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startHome() {
        if(this.checkStatusBt() == 1) {
            Intent intent;
            intent = new Intent(MainActivity.this, HomeActivity.class);

            //se inicia la activity principal
            startActivity(intent);

            finish();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private int checkStatusBt(){
        if ( mConexionBluetooth.checkBTState(this) != 1) {
            //mConexionBluetooth.encenderBluetooth(this);
            mConexionBluetooth.encernderBluetooth(this);

            return 0;
        }
        return 1 ;
    }
}