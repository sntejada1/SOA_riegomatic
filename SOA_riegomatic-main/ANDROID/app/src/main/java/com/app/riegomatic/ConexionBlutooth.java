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
import java.nio.ByteBuffer;
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
    private final String MAC_address = "98:D3:61:F9:39:A5";
    private final int tamMsj = 10;
    private final int arg2 = -1;
    private final int handlerState = 0;
    private final int desconectado = 0;
    private final int conectado = 1;

    // el ejemplo que mando profe
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


    public ConexionBlutooth(Handler bluetoothIn, Context contexto) {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btSocket = null;
        this.bluetoothIn = bluetoothIn;
        this.contexto2 = contexto;
    }

    public ConexionBlutooth() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        btSocket = null;
        this.bluetoothIn = null;
        this.contexto2 = null;
    }

    public int checkBtState(Context contexto) {
        if (btAdapter == null) {
            Toast.makeText(contexto, "El dispositivo no soporta bluetooth", Toast.LENGTH_LONG).show();
            return desconectado;
        } else {
            if (btAdapter.isEnabled()) {
                return conectado;
            }
            return desconectado;
        }
    }

    public BluetoothSocket createBluetoothSocket(BluetoothDevice device, Context contexto) throws IOException {
        if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            //solicitar permiso
            checkPermissions(contexto);
        }
        //creates secure outgoing connecetion with BT device using UUID
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public int conectarBluetooth(Context contexto) {
        BluetoothDevice device = btAdapter.getRemoteDevice(MAC_address);

        try {
            //creo el socket entre disp android y hc06..
            btSocket = createBluetoothSocket(device, contexto);
        } catch (IOException e) {
            Toast.makeText(contexto, "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        try {
            // establezco la conexion del socket.
            if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                checkPermissions(contexto);
            }
            btSocket.connect();
        } catch (IOException e) {
            try {
                desconectarBluetooth();
            } catch (IOException e2) {
                e2.printStackTrace();
                return desconectado;
                //insert code to deal with this
            }
            e.printStackTrace();
            return desconectado;
        }
        this.establecerInputOutput(btSocket);
        return conectado;
    }


    public void establecerInputOutput(BluetoothSocket socket) {
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void write(String input) {
        byte[] msgBuffer = input.getBytes(); //converts entered String into bytes
        try {
            //write bytes over BT connection via outstream
            mmOutStream.write(msgBuffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        if (this.conectarBluetooth(this.contexto2) == conectado) {

            this.write("x");
            byte[] buffer = new byte[256];
            int bytes;
            int i = 0;
            String mensaje2 = "-2"; // para mostrar los botones si se conecto
            bluetoothIn.obtainMessage(handlerState, tamMsj, arg2, mensaje2).sendToTarget();
            //ciclamos para recibir mensajes..
            while (true) {
                try {
                    i++;
                    bytes = mmInStream.read(buffer);
                    String readMessage2 = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, arg2, readMessage2).sendToTarget();
                } catch (IOException e) {
                    String mensaje = "-1";
                    bluetoothIn.obtainMessage(handlerState, tamMsj, arg2, mensaje).sendToTarget();
                    try {
                        this.desconectarBluetooth();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
            }
        } else {
            String mensaje = "-1";
            bluetoothIn.obtainMessage(handlerState, tamMsj, arg2, mensaje).sendToTarget();
            try {
                this.desconectarBluetooth();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean checkPermissions(Context contexto) {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();

        //Se chequea si la version de Android es menor a la 6
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(contexto, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) contexto, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), 2);
            return false;
        }
        return true;
    }

    public void encenderBluetooth(Context contexto) {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        if (ActivityCompat.checkSelfPermission(contexto, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            //solicitar permisos
            checkPermissions(contexto);
        }
        contexto.startActivity(enableBtIntent);
    }

    public void desconectarBluetooth() throws IOException {
        try {
            if (btSocket != null)
                btSocket.close();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }


}
