package com.app.riegomatic;

public interface Contract {
    interface ViewMVP{
        void setString(String string);
    }

    interface ModelMVP{
        interface OnSendToPresenter{
            void onFinished(String string);
        }
        void sendMessage(Contract.ModelMVP.OnSendToPresenter presenter);
    }

    interface PresenterMVP{
        void onButtonClick();
        void onDestroy();
    }
}
