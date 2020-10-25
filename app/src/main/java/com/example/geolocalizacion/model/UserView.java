package com.example.geolocalizacion.model;

import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polygon;

public class UserView {

    private Marker view;
    private User user;
    private Polygon guia;

    public UserView(){

    }

    public UserView(Marker view, User user) {
        this.view = view;
        this.user = user;
    }

    public Marker getView() {
        return view;
    }

    public void setView(Marker view) {
        this.view = view;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Polygon getGuia() {
        return guia;
    }

    public void setGuia(Polygon guia) {
        this.guia = guia;
    }
}
