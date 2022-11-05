package com.app.riegomatic;
import android.bluetooth.BluetoothSocket;
public class Presenter implements Contract.ModelMVP.OnSendToPresenter, Contract.PresenterMVP{

    private Contract.ViewMVP homeView;
    private final Contract.ModelMVP model;

    public Presenter(Contract.ViewMVP homeView) { // constructor
        this.homeView = homeView;
        this.model = new HomeModel();
    }

    @Override
    public void onFinished(String string) {
        this.homeView.setString(string);
    }

    @Override
    public void actualizarCampos(String string) {
        this.homeView.setString(string);
    }
    @Override
    public void actualizarHumedad(String string) {
        this.homeView.setHumedad(string);
    }
    @Override
    public void actualizarDistancia(String string) {
        string = string.replaceAll("\\s+","");
        int distancia = Integer.parseInt(string.toString());
        // this.homeView.setWater(string.toString());
        if(distancia < 20) {
            this.homeView.setWater("CORRECTA");
        } else {
            this.homeView.setWater("BAJA");
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
    public void onDestroy() {
        this.homeView = null;
    }

    @Override
    public void onSetPresenterModel() {
        this.model.setPresenterModel(this);
    }

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
