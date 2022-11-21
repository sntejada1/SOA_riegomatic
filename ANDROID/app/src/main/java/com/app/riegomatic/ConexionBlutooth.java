package com.app.riegomatic;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

public class ConexionBlutooth extends Thread {

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler bluetoothIn;
    private Context contexto2;

    // el ejemplo que mando profe (no funciono)
    String[] permissions = new String[]{
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE};


    public ConexionBlutooth(Handler bluetoothIn, Context cotexto){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btSocket = null;
        this.bluetoothIn = bluetoothIn;
        this.contexto2 = cotexto;

    }
    public int crear(Context contexto) {
        return checkBTState(contexto);

    }

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device, Context contexto) throws IOException {


        // lo que estaba en el on resumen
        if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            //solicitar permiso bluetooth_connect

        }
        //creates secure outgoing connecetion with BT device using UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);

    }


    public int res(Context cotexto) {
        String address = "98:D3:61:F9:39:A5";

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device, cotexto);
        } catch (IOException e) {
            Toast.makeText(cotexto, "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establish the Bluetooth socket connection.
        try {
            if (ActivityCompat.checkSelfPermission(cotexto, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // lo que tengo que hacer si no tengo permisos
            }
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
                return 0;
                //insert code to deal with this
            }
        }
        this.ConnectedThread(btSocket);
        return 1;
    }


    public int checkBTState(Context contexto) {


        if (btAdapter == null) {
            Toast.makeText(contexto, "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
            return 0;
        } else {
            if (btAdapter.isEnabled()) {
                return 1;
            }
            return 0;
        }
    }

    public void ConnectedThread(BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void write(String input) {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {
            //if you cannot write, close the application
            //Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
            // finish();

        }
    }


    public void run() {
        Log.d(TAG, "...RUUUUUNS.............................................................");
        if (this.res(this.contexto2) == 1 ) {
            Log.d(TAG, "...RUNNNNN DENTRO IF.............................................................");
            this.write("x");
            byte[] buffer = new byte[256];
            int bytes;
            int i = 0;
            // Keep looping to listen for received messages
            while (true) {
                try {
                    Log.d(TAG, "...EJECUTANDO WHILEEEEEEEE............................................................." + i);
                    i++;
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage2 = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(0, bytes, -1, readMessage2).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }
    }




    private  boolean checkPermissions(Context contexto) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        //Se chequea si la version de Android es menor a la 6
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String p:permissions) {
            result = ContextCompat.checkSelfPermission(contexto,p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) contexto, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),2 );
            return false;
        }
        return true;
    }

    public void encernderBluetooth(Context contexto){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            //solicitar permisos
        }
        contexto.startActivity(enableBtIntent);
    }

    public void pause() throws IOException {
        try
        {
            if(btSocket != null){
                btSocket.close();
            }
        } catch (IOException e2) {

        }

    }


}
