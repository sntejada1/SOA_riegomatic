package com.app.riegomatic;


import static android.content.ContentValues.TAG;

import android.content.Context;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;

public class HomeModel implements Contract.ModelMVP {

    public ConexionBlutooth mConexionBluetooth;
    private boolean statusHilo;

    @SuppressLint("HandlerLeak")
    public HomeModel(Handler bluetoothIn, Context contexto) {
        mConexionBluetooth = new ConexionBlutooth(bluetoothIn, contexto);
    }

    @Override
    public int checkBTStateHome(Context contexto) {
        return mConexionBluetooth.checkBtState(contexto);
    }

    @Override
    public void conectarBluetooth(Context contexto, Handler bluetoothIn) {

        mConexionBluetooth = new ConexionBlutooth(bluetoothIn, contexto);
        statusHilo = true;
        mConexionBluetooth.start();
    }

    @Override
    public void escribirArduino(String senal) {
        mConexionBluetooth.write(senal);
    }

    @Override
    public void desconectarBluetooth() throws IOException {
        mConexionBluetooth.desconectarBluetooth();
        statusHilo = false;
        mConexionBluetooth.interrupt();

    }

    public boolean statusHilo(){
        return statusHilo;
    }
}

