package com.app.riegomatic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.content.BroadcastReceiver;
import android.content.Context;

import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.os.Handler;


import java.io.IOException;
import java.util.UUID;


import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothSocket;


//sensor


import androidx.appcompat.app.AppCompatDelegate;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class HomeActivity extends AppCompatActivity implements Contract.ViewMVP {

    private TextView textStatus;
    private TextView humidity;
    private TextView water;
    private Button btn_back ;
    private Button btn_watering ;
    private Button btn_onOf;
    private Button btn_conectar;

    private Contract.PresenterMVP presenter;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    private static final String TAG = "HomeActivity";

    //sensor
    public static final float dark_mode_sensibility_level = 0.25f;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightEventListener;
    private float valueMax;
    private Context contexto;
    private final int conectado = 1;

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            // Filtramos por la accion. Nos interesa detectar BluetoothAdapter.ACTION_STATE_CHANGED
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                final int estado = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                if(estado == BluetoothAdapter.STATE_OFF) {
                    btn_back.performClick();
                }
            }
        }
    };

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); //muestro la view
        contexto = this;
        presenter = new Presenter(this,contexto);

        //asigno los views a su al tipo de objeto correspondiente..
        textStatus = findViewById(R.id.status);
        humidity = findViewById(R.id.humidity);
        water = findViewById(R.id.water);
        btn_back = findViewById(R.id.back);
        btn_watering = findViewById(R.id.watering);
        btn_onOf= findViewById(R.id.power);
        btn_conectar= findViewById(R.id.conectar);

        //registro los listeners..
        btn_back.setOnClickListener(btnListener);
        btn_watering.setOnClickListener(btnListener);
        btn_onOf.setOnClickListener(btnListener);
        btn_conectar.setOnClickListener(btnListener);

        //SensorLuminosities
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        valueMax = lightSensor.getMaximumRange();
        lightEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float value = sensorEvent.values[0];
                float range = valueMax * dark_mode_sensibility_level;
                if (value < range)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
                //TODO Accuracy won't change.
            }
        };
    }

    private void registrarEventosBluetooth()
    {
        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar los distintos eventos que queremos recibir
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        this.registerReceiver(bReceiver, filtro);
    }



    @Override
    public void onResume() {
        super.onResume();
        //registro el listener para el sensor de luminocidad..
        sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
        registrarEventosBluetooth();
        if( presenter.checkBtStateHome() == conectado) {
            btn_conectar.setVisibility(View.GONE);
            btn_watering.setVisibility(View.VISIBLE);
            btn_onOf.setVisibility(View.VISIBLE);
            presenter.conectarBluetooth();
        } else {
            btn_conectar.setVisibility(View.VISIBLE);
            btn_watering.setVisibility(View.GONE);
            btn_onOf.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        sensorManager.unregisterListener(lightEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            presenter.desconectarBluetooth();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void setHumedad(String string) {
        humidity.setText(string);
    }

    @Override
    public void setWater(String string) {
        water.setText(string);
    }

    @Override
    public void setEstado(String string) {
        textStatus.setText(string);
    }

    @Override
    public void mostrarBtnConectar(){
        btn_conectar.setVisibility(View.VISIBLE);
        btn_watering.setVisibility(View.GONE);
        btn_onOf.setVisibility(View.GONE);
    }

    @Override
    public void ocultarBtnConectar(){
        btn_conectar.setVisibility(View.GONE);
        btn_watering.setVisibility(View.VISIBLE);
        btn_onOf.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            switch(view.getId()) {
                case R.id.watering:
                    presenter.watering();
                    break;
                case R.id.power:
                    presenter.arduinoOnOf();
                    break;
                case R.id.conectar:
                    //mostrar los botones
                    setEstado("CONECTANDO");
                    presenter.conectarBluetooth();
                    break;
                case R.id.back:
                    Intent intent2 = new Intent(view.getContext(), MainActivity.class);
                    startActivity(intent2);
                    break;
                default:
                    throw new IllegalStateException("Unexpexted value" + view.getId());
            }
        }
    };
}