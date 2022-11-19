package com.app.riegomatic;
import android.content.Context;

import android.os.Handler;

import java.io.IOException;

public interface Contract {
    interface ViewMVP{
        void setString(String string);
        void setHumedad(String string);
        void setWater(String string);
        void setEstado(String string);
        void setFlag(int valor);
    }

    interface ModelMVP{


        void encenderBluetooth(Context contexto);
        void pause() throws IOException;

        interface OnSendToPresenter{
            void onFinished(String string);
            
        }
        void sendMessage(Contract.ModelMVP.OnSendToPresenter presenter);
        void recibirMensaje(int bytes, String readMessage);
        void res(Context Contexto, Handler bluetoothIn);
        void escribirArduino(String senal);
        int checkBTStatehome(Context contexto);
    }

    interface PresenterMVP{
        void onDestroy();
        void watering();
        void arduinoOnOf();
        int checkBtStateHome();
        void res();
        void pause() throws IOException;
        void encenderBluetooth();
    }
}
