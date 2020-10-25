package com.example.geolocalizacion.model;

import android.graphics.Color;
import android.widget.Toast;

import com.example.geolocalizacion.MapsActivity;
import com.example.geolocalizacion.comm.Actions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.asin;
import static java.lang.Math.sqrt;

public class Creator {

    private GoogleMap mMap;
    private Actions admin;
    private String username;
    MapsActivity mapsActivity;

    public Creator(GoogleMap mMap, Actions admin, String username, MapsActivity mapsActivity){
        this.mMap = mMap;
        this.admin = admin;
        this.username = username;
        this.mapsActivity = mapsActivity;
    }

    public HuecoView createHuecoDraw(Hueco hueco){
        Hueco h = hueco;

        LatLng latLng = new LatLng(h.getLatitud(), h.getLongitud());
        int color;
        if(h.isVerificado()){
            color = Color.RED;
        }else{
            color = Color.GREEN;
        }

        CircleOptions circleOptions = new CircleOptions().fillColor(color).center(latLng).radius(10);
        Circle circle = this.mMap.addCircle(circleOptions);

        HuecoView newHueco = new HuecoView(hueco,circle, this.admin);

        return newHueco;
    }

    public boolean updateHueco(HuecoView hueco, Hueco newHueco){

        boolean change = false;

        Hueco h = hueco.getHueco();
        Hueco nh = newHueco;

        if(h.getLatitud() != nh.getLatitud() || h.getLongitud() != nh.getLongitud()){
            hueco.getView().setCenter(new LatLng(nh.getLongitud(), nh.getLongitud()));
            h.setLatitud(nh.getLatitud());
            h.setLongitud(nh.getLongitud());
            change = true;
        }

        if(h.isVerificado() != nh.isVerificado()){
            int color;
            if(nh.isVerificado()){
                color = Color.RED;
            }else{
                color = Color.GREEN;
            }
            hueco.getView().setFillColor(color);
            nh.setVerificado(nh.isVerificado());
            change = true;
        }

        return change;

    }

    public UserView createUserDraw(User user){
        User u = user;

        LatLng latLng = new LatLng(u.getLatitud(), u.getLongitud());

        MarkerOptions markerOptions = new MarkerOptions().position(new LatLng(u.getLatitud(), u.getLongitud())).title(user.getUsername());

        if(user.getUsername().toLowerCase().equals(this.username.toLowerCase())){

        }else{
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }

        Marker marker =  this.mMap.addMarker(markerOptions);

        UserView newUser = new UserView(marker,user);

        if(u.getUsername().toLowerCase().equals(this.username.toLowerCase())){

            double radio = 0.0001;

            Polygon guia = this.mMap.addPolygon(new PolygonOptions().
                    add(new LatLng(user.getLatitud() + (radio*4), user.getLongitud())).
                    add(new LatLng(user.getLatitud() - (radio/2), user.getLongitud()- radio)).
                    add(new LatLng(user.getLatitud()- (radio/2), user.getLongitud() + radio))
            );
            newUser.setGuia(guia);
        }





        return newUser;
    }

    public void updateUser(UserView user, User newUser){

        User u = user.getUser();
        User nu = newUser;

        if(u.getLatitud() != nu.getLatitud() || u.getLongitud() != nu.getLongitud()){
            user.getView().setPosition(new LatLng(nu.getLongitud(), nu.getLongitud()));
            u.setLatitud(nu.getLatitud());
            u.setLongitud(nu.getLongitud());
            this.updateGuia(user);
        }



    }


    public boolean updateUser(UserView user, double latitud, double longitud){

        User u = user.getUser();
        boolean changePosition = false;

        if(u.getLatitud() != latitud || u.getLongitud() != longitud){
            user.getView().setPosition(new LatLng(latitud, longitud));
            u.setLatitud(latitud);
            u.setLongitud(longitud);
            this.admin.updateUser(user);
            changePosition = true;

            this.updateGuia(user);
        }



        return changePosition;
    }

    private void updateGuia(UserView u){
        double latitud = u.getUser().getLatitud();
        double longitud = u.getUser().getLongitud();

        if(u.getUser().getUsername().toLowerCase().equals(this.username.toLowerCase())){

            if(u.getGuia() != null && this.mapsActivity.getHuecoCercano() == null){
                double radio = 0.0001;
                Polygon guia = u.getGuia();
                List<LatLng> puntos = new ArrayList<>();
                puntos.add(new LatLng(latitud + (radio*4), longitud));
                puntos.add(new LatLng(latitud - (radio/2), longitud- radio)) ;
                puntos.add(new LatLng(latitud- (radio/2), longitud + radio));
                guia.setPoints(puntos);



            }else if(u.getGuia() != null){

                double refLatitud = this.mapsActivity.getHuecoCercano().getHueco().getLatitud();
                double refLongitud = this.mapsActivity.getHuecoCercano().getHueco().getLongitud();

                double angulo = this.angulo(new LatLng(u.getUser().getLatitud(), u.getUser().getLongitud()), new LatLng(refLatitud, refLongitud));
                this.mapsActivity.runOnUiThread(()->{
                    Toast.makeText(this.mapsActivity, "Angulo: " + Math.cos(Math.toDegrees(angulo)), Toast.LENGTH_SHORT).show();
                });

                double radio = 0.0001;
                Polygon guia = u.getGuia();
                List<LatLng> puntos = new ArrayList<>();
                double x1 = latitud + ((radio* Math.cos(Math.toDegrees(angulo))*4));
                double y1 = longitud  + ((radio* Math.sin(Math.toDegrees(angulo))*4));
                puntos.add(new LatLng(x1 ,y1));
                puntos.add(new LatLng(latitud - (radio/2), longitud- radio)) ;
                puntos.add(new LatLng(latitud- (radio/2), longitud + radio));
                guia.setPoints(puntos);

            }else {


            }
        }
    }



    private double rad2Deg(float x){
        return (x/(2*Math.PI)) * 360;
    }
    private double  cruz(LatLng A,LatLng B){
        return A.latitude*B.longitude - A.longitude*B.latitude;
    }
    private double  punto(LatLng A,LatLng B){
        return A.latitude*B.latitude + A.longitude*B.longitude;
    }
    private double  magnitud(LatLng A){
        return sqrt(A.latitude*A.latitude + A.longitude*A.longitude);
    }

    public double angulo(LatLng A,LatLng B){

        double c = cruz(A,B);
        double p = punto(A,B);
        double ma = magnitud(A);
        double mb = magnitud(B);
        double ab = ma*mb;
        if(c >= 0){
            if(p >= 0){
                return asin(c/ab);
            }else{
                return Math.PI - asin(c/ab);
            }
        }else{
            if(p < 0){
                return Math.PI - asin(c/ab);
            }else{
                return 2*Math.PI + asin(c/ab);
            }
        }
    }


}
