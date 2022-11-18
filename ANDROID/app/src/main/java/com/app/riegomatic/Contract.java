package com.app.riegomatic;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;

import android.os.Handler;

public interface Contract {
    interface ViewMVP{
        void setString(String string);
        void setHumedad(String string);
        void setWater(String string);
        void setEstado(String string);
    }

    interface ModelMVP{


        interface OnSendToPresenter{
            void onFinished(String string);
            
        }
        void sendMessage(Contract.ModelMVP.OnSendToPresenter presenter);
        void recibirMensaje(int bytes, String readMessage);
        void conectar(BluetoothSocket btSocket);
        void conectar();
        void res(Context Contexto, Handler bluetoothIn);
        void escribirArduino(String senal);
        void checkBTStatehome(Context contexto);
    }

    interface PresenterMVP{
        void onButtonClick();
        void onDestroy();
        void onRecibirMesaje(int bytes, String readMessage);
        void onConectar(BluetoothSocket btSocket);
        void onConectar();
        void watering();
        void arduinoOnOf();
        void checkBtStateHome();
        void res();
        void conectar();
    }
}
