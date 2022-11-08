package com.app.riegomatic;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
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
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

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



    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address
    private static String address = null;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textStatus = findViewById(R.id.status);
        humidity = findViewById(R.id.humidity);
        water = findViewById(R.id.water);
        Button btn_back = findViewById(R.id.back);
        Button btn_watering = findViewById(R.id.watering);
        Button btn_onOf= findViewById(R.id.power);


        btn_back.setOnClickListener(btnListener);
        btn_watering.setOnClickListener(btnListener);
        btn_onOf.setOnClickListener(btnListener);

        presenter = new Presenter(this);
        presenter.onSetPresenterModel();
        // bluetooth

        btAdapter = BluetoothAdapter.getDefaultAdapter();       // get Bluetooth adapter
        checkBTState();


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

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        }
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connecetion with BT device using UUID
    }

    @Override
    public void onResume() {
        super.onResume();
         Log.d(TAG, "...onResumeeeeeeeeeee.............................................................");

        address = "98:D3:61:F9:39:A5";

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                // return;
            }
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                //insert code to deal with this
            }
        }
        presenter.onConectar(btSocket);
        sensorManager.registerListener(lightEventListener, lightSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }

        sensorManager.unregisterListener(lightEventListener);
    }

    //Checks that the Android device Bluetooth is available and prompts to be turned on if off
    private void checkBTState() {

        if (btAdapter == null) {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    // return;
                }
                startActivityForResult(enableBtIntent, 1);
            }
        }
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