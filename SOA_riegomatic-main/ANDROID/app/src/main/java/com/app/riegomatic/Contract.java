package com.app.riegomatic;
import android.content.Context;

import android.os.Handler;

import java.io.IOException;

public interface Contract {
    interface ViewMVP{
        void setHumedad(String string);
        void setWater(String string);
        void setEstado(String string);
        void mostrarBtnConectar();
        void ocultarBtnConectar();
    }

    interface ModelMVP{

        interface OnSendToPresenter{
        }
        void conectarBluetooth(Context Contexto, Handler bluetoothIn);
        void escribirArduino(String senal);
        int checkBTStateHome(Context contexto);
        void desconectarBluetooth() throws IOException;
        boolean statusHilo();
    }

    interface PresenterMVP{
        void onDestroy();
        void watering();
        void arduinoOnOf();
        int checkBtStateHome();
        void conectarBluetooth();
        void desconectarBluetooth() throws IOException;
    }
}
