package com.example.geolocalizacion.model;

import com.google.android.gms.maps.model.Marker;

public class UserView {

    private Marker view;
    private User user;

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
}
