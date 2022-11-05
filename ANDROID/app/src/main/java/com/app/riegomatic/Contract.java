package com.app.riegomatic;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothSocket;
public interface Contract {
    interface ViewMVP{
        void setString(String string);
        void setHumedad(String string);
        void setWater(String string);
    }

    interface ModelMVP{
        interface OnSendToPresenter{
            void onFinished(String string);
            void actualizarCampos(String String);
            void actualizarHumedad(String String);
            void actualizarDistancia(String String);
            
        }
        void sendMessage(Contract.ModelMVP.OnSendToPresenter presenter);
        void recibirMensaje(int bytes, String readMessage);
        void conectar(BluetoothSocket btSocket);
        void setPresenterModel(Presenter presenter);
        void escribirArduino(String senal);
    }

    interface PresenterMVP{
        void onButtonClick();
        void onDestroy();
        void onRecibirMesaje(int bytes, String readMessage);
        void onConectar(BluetoothSocket btSocket);
        void onSetPresenterModel();
        void watering();
        void arduinoOnOf();
    }
}
