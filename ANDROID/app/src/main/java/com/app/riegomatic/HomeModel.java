package com.app.riegomatic;

import android.content.Context;
import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.concurrent.Executor;

public class HomeModel implements Contract.ModelMVP {

//    public ConexionBluetooth mConexionBluetooth;
    public ThreadAsynctask mConexionBluetooth;
    Handler bluetoothIn;
    final int handlerState = 0;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @SuppressLint("HandlerLeak")
    public HomeModel(Handler bluetoothIn, Context contexto) {
        //nuevo
        mConexionBluetooth = new ThreadAsynctask(bluetoothIn, contexto);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public int checkBTStatehome(Context contexto) {
        return mConexionBluetooth.crear(contexto);
    }

    @Override
    public void sendMessage(OnSendToPresenter presenter) {
        presenter.onFinished("Regando");
    }

    @Override
    public void recibirMensaje(int bytes, String readMessage) {
        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
    }

    @Override
    public void res(Context contexto, Handler bluetoothIn) {
        //creo un hilo para conectar el bluetooth y posteriormente encargarme de la recepcion y el envio de mensajes..
        mConexionBluetooth.execute();
    }

    @Override
    public void escribirArduino(String senal) {
        //TODO falta resolver este metodo
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    public void encenderBluetooth(Context contexto) {
        mConexionBluetooth.encenderBluetooth(contexto);
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void desconectarBluetooth() throws IOException {
        mConexionBluetooth.desconectarBluetooth();
    }
}

