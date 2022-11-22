package com.app.riegomatic;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import android.os.Handler;

import java.io.IOException;

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
        this.model = new HomeModel(this.bluetoohIn,contexto);
        this.contexto = contexto;
    }

    private Handler mensajeHandler() { // accion que se realiza cuando llega un mensaje por bluetooth
        @SuppressLint("HandlerLeak") Handler bt = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {          //if message is what we want
                    actualizarEstado("CONECTADO");
                    readMessage = (String) msg.obj;
                    //Log.d(TAG, "...ESTADOOOOOOOOOOOOOOOOOOOOOO............................................................." + msg.toString());
                    int endOfLineIndex = readMessage.indexOf("a");
                    if (endOfLineIndex > 0 ) {

                        if (readMessage.charAt(0) != '#')
                        {
                            String humedad = readMessage.substring(0, endOfLineIndex);
                            String distancia = readMessage.substring(endOfLineIndex+1);
                            actualizarHumedad(humedad);
                            actualizarDistancia(distancia);
                        }
                    } else if(readMessage.equals("-1")){ // menos uno llego el mensaje de desconctado
                        //desconectar hilo
                        Log.e(TAG, "...DESCONECTO IDOLO PRESENTADOR.............................................................");
                        desconectarHilo();
                        actualizarEstado("DESCONECTADO");
                        actualizarHumedad("-");
                        actualizarDistancia("-");
                        mostrarBtnConectar();
                    } else if(readMessage.equals("-2")){
                        ocultarBtnConectar();
                    }
                }
            }
        };

        return bt;
    }


    public void actualizarHumedad(String string) {
        this.homeView.setHumedad(string);
    }

    public void actualizarEstado(String string) {
        this.homeView.setEstado(string);
    }

    public void actualizarDistancia(String string) {
        if(string == "-"){
            this.homeView.setWater(string);
        }else{
            string = string.replaceAll("\\s+", "");
            try {
                int distancia = Integer.parseInt(string);
                if (distancia < 20) {
                    this.homeView.setWater("CORRECTA");
                } else {
                    this.homeView.setWater("BAJA");
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, e.getMessage());
                return;
            }
        }
    }

    public void mostrarBtnConectar(){
        this.homeView.mostrarBtnConectar();
    }

    public void ocultarBtnConectar(){
        this.homeView.ocultarBtnConectar();
    }

    public  void desconectarHilo(){
        try {
            if( this.model.statusHilo() ) {
                this.model.desconectarBluetooth();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }







    @Override
    public void onDestroy() {
        this.homeView = null;
    }

    @Override
    public int checkBtStateHome(){
        if(this.model.checkBTStateHome(contexto) == 1) {
            //Log.d(TAG, "...111111111111111111111111111111111............................................................." );
            return 1;
        } else {
            //Log.d(TAG, "...000000000000000000000000000000............................................................." );
            return 0;
        }
        //return 1;
    };


    public void desconectarBluetooth() throws IOException {
        Log.e(TAG, "...DESDE PAUSEEEEE.............................................................");
        this.model.desconectarBluetooth();
    }


    @Override
    public void conectarBluetooth(){
        this.model.conectarBluetooth(contexto,bluetoohIn);
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
