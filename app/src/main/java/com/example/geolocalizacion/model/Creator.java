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
import com.google.maps.android.SphericalUtil;

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
        if(h.isVerificado() == false){
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
            hueco.getView().setCenter(new LatLng(nh.getLatitud(), nh.getLongitud()));
            h.setLatitud(nh.getLatitud());
            h.setLongitud(nh.getLongitud());
            change = true;
        }

        if(h.isVerificado() != nh.isVerificado()){
            int color;
            if(nh.isVerificado() == false){
                color = Color.RED;
            }else{
                color = Color.GREEN;
            }
            hueco.getView().setFillColor(color);
            h.setVerificado(nh.isVerificado());
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

            double latitud = user.getLatitud();
            double longitud = user.getLongitud();

            if(this.mapsActivity.getHuecoCercano() != null){

                double refLatitud = this.mapsActivity.getHuecoCercano().getView().getCenter().latitude;
                double refLongitud = this.mapsActivity.getHuecoCercano().getView().getCenter().longitude;

                double myLatitud = marker.getPosition().latitude;
                double myLongitud = marker.getPosition().longitude;


                double refDis =  SphericalUtil.computeHeading(new LatLng(refLatitud,refLongitud ), new LatLng(myLatitud,myLongitud ));
                double angulo = this.angulo(new LatLng(myLatitud, myLongitud), new LatLng(refLatitud, refLongitud));

                double radio = 0.0001;

                List<LatLng> puntos = new ArrayList<>();

                LatLng inclinado = this.anguloInclinacion(new LatLng(0, 1), this.radianes(refDis));
                LatLng inclinado2 = this.anguloInclinacion(new LatLng(1, 1), this.grados(angulo));

                double x1 = latitud + ((inclinado2.latitude) *radio*3);
                double y1 = longitud + ((inclinado.latitude) *radio*3);
                puntos.add(new LatLng(x1 ,y1));
                puntos.add(new LatLng(latitud - (radio/2), longitud- radio)) ;
                puntos.add(new LatLng(latitud- (radio/2), longitud + radio));

                Polygon guia = this.mMap.addPolygon(new PolygonOptions().
                        add(new LatLng(x1 ,y1)).
                        add(new LatLng(latitud - (radio/2), longitud- radio)).
                        add(new LatLng(latitud- (radio/2), longitud + radio))
                );
                newUser.setGuia(guia);

            }else{

                double radio = 0.0001;
                Polygon guia = this.mMap.addPolygon(new PolygonOptions().
                        add(new LatLng(latitud + (radio*4), longitud)).
                        add(new LatLng(latitud - (radio/2), longitud- radio)).
                        add(new LatLng(latitud- (radio/2), longitud + radio))
                );
                newUser.setGuia(guia);
            }

        }





        return newUser;
    }

    public void updateUser(UserView user, User newUser){

        User u = user.getUser();
        User nu = newUser;

        if(u.getLatitud() != nu.getLatitud() || u.getLongitud() != nu.getLongitud()){
            user.getView().setPosition(new LatLng(nu.getLatitud(), nu.getLongitud()));
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

            }else if(u.getGuia() != null && this.mapsActivity.getHuecoCercano() != null){

                double refLatitud = this.mapsActivity.getHuecoCercano().getView().getCenter().latitude;
                double refLongitud = this.mapsActivity.getHuecoCercano().getView().getCenter().longitude;

                double myLatitud = u.getView().getPosition().latitude;
                double myLongitud = u.getView().getPosition().longitude;


                double refDis =  SphericalUtil.computeHeading(new LatLng(refLatitud,refLongitud ), new LatLng(myLatitud,myLongitud ));
                double angulo = this.angulo(new LatLng(myLatitud, myLongitud), new LatLng(refLatitud, refLongitud));

                double radio = 0.0001;

                Polygon guia = u.getGuia();
                List<LatLng> puntos = new ArrayList<>();

                LatLng inclinado = this.anguloInclinacion(new LatLng(0, 1), this.radianes(refDis));
                LatLng inclinado2 = this.anguloInclinacion(new LatLng(1, 1), this.grados(angulo));

                double x1 = latitud + ((inclinado2.latitude) *radio*3);
                double y1 = longitud + ((inclinado.latitude) *radio*3);
                puntos.add(new LatLng(x1 ,y1));
                puntos.add(new LatLng(latitud - (radio/2), longitud- radio)) ;
                puntos.add(new LatLng(latitud- (radio/2), longitud + radio));
                guia.setPoints(puntos);

            }else {


            }
        }
    }

    public double grados(double radianes){
        return  radianes * 180 / Math.PI;
    }

    public double radianes(double grados){
        return grados * Math.PI / 180;
    }


    public LatLng anguloInclinacion(LatLng cor, double angulo){
        double inclinado = angulo;
        double x = Math.cos(inclinado)*cor.latitude - Math.sin(inclinado)*cor.longitude;
        double y = Math.sin(inclinado)*cor.latitude + Math.cos(inclinado)*cor.longitude;

        return (new LatLng(x, y));
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