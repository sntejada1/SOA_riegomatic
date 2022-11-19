package com.app.riegomatic;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import android.os.Handler;
import java.util.logging.LogRecord;

public class Presenter implements Contract.ModelMVP.OnSendToPresenter, Contract.PresenterMVP{

    private Contract.ViewMVP homeView;
    private final Contract.ModelMVP model;
    private Context contexto;
    private Handler bluetoohIn;
    final int handlerState = 0;
    private String readMessage;
    private static final String TAG = "Presenter";


    public Presenter(Contract.ViewMVP homeView, Context contexto) { // constructor
        this.homeView = homeView;
        this.bluetoohIn = mensajeHandler();
        this.model = new HomeModel(this.bluetoohIn);
        this.contexto = contexto;
    }

    private Handler mensajeHandler() {
        @SuppressLint("HandlerLeak") Handler bt = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {          //if message is what we want
                    readMessage = (String) msg.obj;
                    Log.d(TAG, "...ESTADOOOOOOOOOOOOOOOOOOOOOO............................................................." + msg.toString());
                    int endOfLineIndex = readMessage.indexOf("a");
                    if (endOfLineIndex > 0 && readMessage.indexOf("k") != 0 ) {

                        if (readMessage.charAt(0) != '#')
                        {
                            String humedad = readMessage.substring(0, endOfLineIndex);
                            String distancia = readMessage.substring(endOfLineIndex+1);
                            actualizarHumedad(humedad);
                            homeView.setHumedad(humedad);
                            actualizarDistancia(distancia);
                        }
                    } else if(readMessage.length() > 2){
                        // presenter.actualizarEstado(readMessage.substring(1));
                    }
                }
            }
        };

        return bt;
    }

    @Override
    public void onFinished(String string) {
        this.homeView.setString(string);
    }



    public void actualizarCampos(String string) {
        this.homeView.setString(string);
    }

    public void actualizarHumedad(String string) {
        this.homeView.setHumedad(string);
    }

    public void actualizarEstado(String string) {
        this.homeView.setEstado(string);
    }


    public void actualizarDistancia(String string) {
        string = string.replaceAll("\\s+","");
        try {
            int distancia = Integer.parseInt(string.toString());
            // this.homeView.setWater(string.toString());
            if(distancia < 20) {
                this.homeView.setWater("CORRECTA");
            } else {
                this.homeView.setWater("BAJA");
            }
        } catch (NumberFormatException e) {
            return;
        }

    }
    
    @Override
    public void onButtonClick() {
        this.model.sendMessage(this);
        // this.model.watering();
    }

    @Override
    public void onRecibirMesaje(int bytes, String readMessage) {
        this.model.recibirMensaje(bytes, readMessage);
    }

    @Override
    public void onConectar(BluetoothSocket btSocket) {
        this.model.conectar(btSocket);
    }

    @Override
    public void onConectar() {
        this.model.conectar();
    }

    @Override
    public void onDestroy() {
        this.homeView = null;
    }

    @Override
    public void checkBtStateHome(){
        this.model.checkBTStatehome(contexto);
    };

    @Override
    public void res(){
        this.model.res(contexto,bluetoohIn);
    };
    @Override
    public void conectar(){
        this.model.conectar();
    };

    @Override
    public void watering() {
        this.model.escribirArduino("2");
    }
    public void arduinoOnOf() {
        this.model.escribirArduino("1");
    }

    public Contract.ViewMVP getActitivy() {
        return this.homeView;
    }
}
