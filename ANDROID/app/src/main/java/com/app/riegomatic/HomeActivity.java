package com.app.riegomatic;

import androidx.appcompat.app.AppCompatActivity;



import android.content.Context;

import android.util.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.os.Handler;



import java.util.UUID;


import android.bluetooth.BluetoothAdapter;

import android.bluetooth.BluetoothSocket;


//sensor


import androidx.appcompat.app.AppCompatDelegate;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class HomeActivity extends AppCompatActivity implements Contract.ViewMVP {

    private TextView textStatus;
    private TextView humidity;
    private TextView water;
    private Contract.PresenterMVP presenter;
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    //private ConnectedThread mConnectedThread;
    private static final String TAG = "HomeActivity";

    //sensor

    public static final float dark_mode_sensibility_level = 0.25f;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private SensorEventListener lightEventListener;
    private float valueMax;
    private Context contexto;



    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        contexto = this;

        textStatus = findViewById(R.id.status);
        humidity = findViewById(R.id.humidity);
        water = findViewById(R.id.water);
        Button btn_back = findViewById(R.id.back);
        Button btn_watering = findViewById(R.id.watering);
        Button btn_onOf= findViewById(R.id.power);


        btn_back.setOnClickListener(btnListener);
        btn_watering.setOnClickListener(btnListener);
        btn_onOf.setOnClickListener(btnListener);

        presenter = new Presenter(this,contexto);
        //presenter.onSetPresenterModel();
        // bluetooth
        presenter.checkBtStateHome();
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

                if (value < range)
                    getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                else getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {

            }
        };

        // Button back = findViewById(R.id.back);
        // back.setOnClickListener(new View.OnClickListener() {
        //     @Override
        //     public void onClick(View view) {
        //         Intent intent2 = new Intent(view.getContext(), MainActivity.class);
        //         startActivityForResult(intent2,0);
        //     }
        // });





    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "...onResumeeeeeeeeeee.............................................................");
        presenter.res();
        //presenter.onConectar();
        //sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        //btSocket.close();

        sensorManager.unregisterListener(lightEventListener);
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
                case R.id.back:

                        Intent intent2 = new Intent(view.getContext(), MainActivity.class);
                        startActivityForResult(intent2,0);
                    break;
                default:
                    throw new IllegalStateException("Unexpexted value" + view.getId());
            }
        }
    };
}