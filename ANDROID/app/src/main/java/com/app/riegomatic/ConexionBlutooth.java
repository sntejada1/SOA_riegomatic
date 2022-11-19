package com.app.riegomatic;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class ConexionBlutooth extends Thread {

    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler bluetoothIn;

    public ConexionBlutooth(Handler bluetoothIn){
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btSocket = null;
        this.bluetoothIn = bluetoothIn;

    }
    public int crear(Context contexto) {
        checkBTState(contexto);

        return 1;
    }

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device, Context contexto) throws IOException {

        if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
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

    // lo que estaba en el on resumen
    public void res(Context cotexto, Handler bluetoothIn) {
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
                //insert code to deal with this
            }
        }
        //presenter.onConectar(btSocket);
        this.ConnectedThread(btSocket);
        // this.start(); esto lo inicia el modelo de home
        //Log.d(TAG, "...START............................................................." );
        //this.write("x");
    }


    public void checkBTState(Context contexto) {

        if (btAdapter == null) {
            Toast.makeText(contexto, "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (btAdapter.isEnabled()) {
            } else {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // que hacer si no tengo permisos
                }
                contexto.startActivity(enableBtIntent);


            }
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
                // presenter.onRecibirMesaje(bytes,readMessage);
                // presenter.actualizarCampos("hola");
            } catch (IOException e) {
                break;
            }
        }
    }
}
