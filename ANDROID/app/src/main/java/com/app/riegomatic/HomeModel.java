package com.app.riegomatic;

public class HomeModel implements Contract.ModelMVP{
    

    @Override
    public void sendMessage(OnSendToPresenter presenter) {
        presenter.onFinished("Regando");
    }

}
