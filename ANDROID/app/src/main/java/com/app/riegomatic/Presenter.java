package com.app.riegomatic;

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
    public void onButtonClick() {
        this.model.sendMessage(this);
    }

    @Override
    public void onDestroy() {
        this.homeView = null;
    }
}
