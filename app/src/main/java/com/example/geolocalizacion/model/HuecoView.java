package com.example.geolocalizacion.model;

import android.graphics.Color;
import android.util.Log;

import com.example.geolocalizacion.comm.Actions;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;


public class HuecoView {

    private Hueco hueco;
    private Circle view;
    private Actions actions;

    public HuecoView(Hueco hueco, Circle view, Actions actions) {
        this.hueco = hueco;
        this.view = view;
        this.actions = actions;
        this.updateStatusVerificado();
    }

    public LatLng getCenter(){
        return this.view.getCenter();
    }

    public void setVerificado(boolean verificado){

        if(this.getHueco().isVerificado() == false) {
            this.getHueco().setVerificado(verificado);
            this.actions.confirmarHueco(this);
        }else{
            this.getHueco().setVerificado(verificado);
        }

        this.updateStatusVerificado();
    }

    private void updateStatusVerificado(){
        if(this.getHueco().isVerificado()){
            this.view.setFillColor(Color.GREEN);
        }else{
            this.view.setFillColor(Color.RED);
        }
    }

    public Hueco getHueco() {
        return hueco;
    }

    public void setHueco(Hueco hueco) {
        this.hueco = hueco;
    }

    public Circle getView() {
        return view;
    }

    public void setView(Circle view) {
        this.view = view;
    }
}
