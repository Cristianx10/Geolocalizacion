package com.example.geolocalizacion.model;

import android.graphics.Color;

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
    }

    public LatLng getCenter(){
        return this.view.getCenter();
    }

    public void confirmar(){
        this.getHueco().setVerificado(true);
        this.actions.confirmarHueco(this);
        this.view.setFillColor(Color.GREEN);
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
