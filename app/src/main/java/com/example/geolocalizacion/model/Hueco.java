package com.example.geolocalizacion.model;

public class Hueco {

    private String userCreator;
    private String id;
    private double latitud;
    private double longitud;
    private boolean verificado;

    public Hueco() {
    }

    public Hueco(String id, String userCreator, double latitud, double longitud, boolean verificado) {
        this.id = id;
        this.userCreator = userCreator;
        this.latitud = latitud;
        this.longitud = longitud;
        this.verificado = verificado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserCreator() {
        return userCreator;
    }

    public void setUserCreator(String userCreator) {
        this.userCreator = userCreator;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }
}
