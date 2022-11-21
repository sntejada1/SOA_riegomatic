package com.app.riegomatic;


import android.content.Context;
import android.annotation.SuppressLint;
import android.os.Handler;

import java.io.IOException;

public class HomeModel implements Contract.ModelMVP {

    public ConexionBlutooth mConexionBluetooth;
    Handler bluetoothIn;
    final int handlerState = 0;

    @SuppressLint("HandlerLeak")

    public HomeModel(Handler bluetoothIn, Context contexto) {

        //nuevo
        mConexionBluetooth = new ConexionBlutooth(bluetoothIn, contexto);

    }

    @Override
    public int checkBTStatehome(Context contexto) {
        return mConexionBluetooth.crear(contexto);
    }

    @Override
    public void sendMessage(OnSendToPresenter presenter) {
        presenter.onFinished("Regando");
    }

    ;

    @Override
    public void recibirMensaje(int bytes, String readMessage) {
        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
    }


    @Override
    public void res(Context contexto, Handler bluetoothIn) {

        //if (mConexionBluetooth.res(contexto) == 1) { // configuarion para antes de iniciarlo, se conecta
            mConexionBluetooth.start();
        //}

    }

    @Override
    public void escribirArduino(String senal) {

    }

    public void encenderBluetooth(Context contexto) {
        mConexionBluetooth.encernderBluetooth(contexto);
    }

    @Override
    public void pause() throws IOException {
        mConexionBluetooth.pause();
    }
}

