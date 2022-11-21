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
    public int flag = 1;
    private  int primera = 1;




    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        contexto = this;

        textStatus = findViewById(R.id.status);
        humidity = findViewById(R.id.humidity);
        water = findViewById(R.id.water);
        btn_back = findViewById(R.id.back);
        btn_watering = findViewById(R.id.watering);
        btn_onOf= findViewById(R.id.power);
        btn_conectar= findViewById(R.id.conectar);


        btn_back.setOnClickListener(btnListener);
        btn_watering.setOnClickListener(btnListener);
        btn_onOf.setOnClickListener(btnListener);
        btn_conectar.setOnClickListener(btnListener);

        presenter = new Presenter(this,contexto);



        Log.d(TAG, "...onCreate.............................................................");


        //SensorLuminosities
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        valueMax = lightSensor.getMaximumRange();

        lightEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                float value = sensorEvent.values[0];
                float range = valueMax * dark_mode_sensibility_level;
                //getSupportActionBar().setTitle("Luminosidad : " + value);
                Log.d(TAG, "...EJECUTO SENSOR.............................................................");
                if (value < range)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "...onResumeeeeeeeeeee.............................................................");
        sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
            if( presenter.checkBtStateHome() == 1) {
                btn_conectar.setVisibility(View.GONE);
                btn_watering.setVisibility(View.VISIBLE);
                btn_onOf.setVisibility(View.VISIBLE);
                presenter.res();
            } else if(primera == 1) {
                presenter.encenderBluetooth();
            } else {
                btn_conectar.setVisibility(View.VISIBLE);
                btn_watering.setVisibility(View.GONE);
                btn_onOf.setVisibility(View.GONE);
            }

        }

    @Override
    public void onPause() {
        super.onPause();
        primera++;
        Log.d(TAG, "...onPauseeeeeee.............................................................");
        //btSocket.close();
        sensorManager.unregisterListener(lightEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        primera = 1;
        Log.d(TAG, "Paso al estado onSotp............................................");
        try {
            presenter.pause();
        } catch (IOException e) {
            Log.d(TAG, "...exeption en el onPause.............................................................");
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Paso al estado Destroyed............................................");


    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "STARRRT............................................");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "RESTAAAAART............................................");
    }



    // fin bluetooh

    @SuppressLint("Range")
    @Override
    public void setString(String string) {
        humidity.setText(string);

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

    private View.OnClickListener btnListener = new View.OnClickListener() {
        @SuppressLint("NonConstantResourceId")
        @Override
        public void onClick(View view) {
            Intent intent;
            switch(view.getId()) {
                case R.id.watering:
                    presenter.watering();
                    break;
                case R.id.power:
                    presenter.arduinoOnOf();
                    break;
                case R.id.conectar:
                    presenter.encenderBluetooth();
                    break;
                case R.id.back:

                        Intent intent2 = new Intent(view.getContext(), MainActivity.class);
                        startActivityForResult(intent2,0);
                    break;
                default:
                    throw new IllegalStateException("Unexpexted value" + view.getId());
            }
        }
    };
    public void setFlag(int valor){
        this.flag = valor;
    }
}