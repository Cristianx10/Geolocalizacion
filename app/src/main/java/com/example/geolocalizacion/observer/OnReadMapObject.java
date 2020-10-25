package com.example.geolocalizacion.observer;

import com.example.geolocalizacion.model.Hueco;
import com.example.geolocalizacion.model.User;

import java.util.ArrayList;
import java.util.HashMap;

public interface OnReadMapObject {

    public void onLoginUser(User user);

    public void onReadHuecos(HashMap<String,Hueco> huecos);

    public void onReadUsers(HashMap<String, User> users);
}
