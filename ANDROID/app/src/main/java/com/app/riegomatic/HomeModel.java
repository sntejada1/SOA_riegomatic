package com.app.riegomatic;

import androidx.core.app.ActivityCompat;


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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;
import android.content.Intent;
import android.widget.TextView;
import android.annotation.SuppressLint;
import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.widget.Toast;

public class HomeModel extends Thread implements Contract.ModelMVP{

    public Presenter presenter;
    private InputStream mmInStream;
    private OutputStream mmOutStream;

    private TextView textStatus;
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();
    //private ConnectedThread mConnectedThread;
    private static final String TAG = "HomeModel";

     String readMessage;

    // SPP UUID service - this should work for most devices
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    @SuppressLint("HandlerLeak")

     public HomeModel() {

         bluetoothIn = new Handler() {
            @SuppressLint("SuspiciousIndentation")
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {          //if message is what we want
                 readMessage = (String) msg.obj;
                 //Log.d(TAG, "...ESTADOOOOOOOOOOOOOOOOOOOOOO............................................................." + msg.toString());
                 int endOfLineIndex = readMessage.indexOf("a");
                    if (endOfLineIndex > 0 && readMessage.indexOf("k") != 0 ) {

                        if (readMessage.charAt(0) != '#')       
                        {
                            String humedad = readMessage.substring(0, endOfLineIndex);             
                            String distancia = readMessage.substring(endOfLineIndex+1);
                            presenter.actualizarHumedad(humedad);
                            presenter.actualizarDistancia(distancia);           
                        }
                    } else if(readMessage.length() > 2){
                        presenter.actualizarEstado(readMessage.substring(1));
                    }
                }
            }
        };

     }


     //creation of the connect thread
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

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Keep looping to listen for received messages
            while (true) {
                try {
                    Log.d(TAG, "...EJECUTANDO WHILEEEEEEEE............................................................." );
                    bytes = mmInStream.read(buffer);         //read bytes from input buffer
                    String readMessage2 = new String(buffer, 0, bytes);
                    // Send the obtained bytes to the UI Activity via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage2).sendToTarget();
                    // presenter.onRecibirMesaje(bytes,readMessage);
                    // presenter.actualizarCampos("hola");
                } catch (IOException e) {
                    break;
                }
            }
        }
        //write method
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


    
    @Override
    public void sendMessage(OnSendToPresenter presenter) {
        presenter.onFinished("Regando");
    };
    @Override
    public void recibirMensaje(int bytes, String readMessage) {
       bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
    }
    @Override
    public void conectar(BluetoothSocket btSocket) {
       this.ConnectedThread(btSocket);
       this.start();
       Log.d(TAG, "...START............................................................." );
       this.write("x");
    }
    @Override
    public void setPresenterModel(Presenter presenter) {
    Log.d(TAG, "...SETEANDO PRESENTADOR EN MODELO............................................................." );
      this.presenter = presenter;
    }
    @Override
    public void escribirArduino(String senal) {
        this.write(senal);
    }
}

