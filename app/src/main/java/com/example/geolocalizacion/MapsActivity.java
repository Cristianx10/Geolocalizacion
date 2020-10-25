package com.example.geolocalizacion;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.geolocalizacion.comm.Actions;
import com.example.geolocalizacion.model.Creator;
import com.example.geolocalizacion.model.Hueco;
import com.example.geolocalizacion.model.HuecoView;
import com.example.geolocalizacion.model.User;
import com.example.geolocalizacion.model.UserView;
import com.example.geolocalizacion.observer.OnReadMapObject;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.maps.android.SphericalUtil;

import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        OnClickListener,
        LocationListener,
        OnMapClickListener,
        OnMapLongClickListener,
        OnMarkerClickListener,
        OnReadMapObject {

    private GoogleMap mMap;

    private TextView tv_mensaje;
    private ConstraintLayout cl_container_mensaje;
    private Button btn_add;

    private boolean iCanConfirmateHuecos;

    private ConstraintLayout cl_notificacion;
    private Button btn_notificacion_agregar;
    private Button btn_cancelar;
    private TextView tv_coordenadas;
    private TextView tv_direccion;


    private LocationManager manager;

    private UserView me;

    private Location lastLocation;
    private double latitudActual = 0;
    private double longitudActual = 0;

    private HashMap<String, HuecoView> huecos;
    private HashMap<String, UserView> users;

    private Actions admin;
    private Creator creator;

    private String myName;

    private HuecoView huecoCercano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        this.admin = new Actions();
        this.admin.setObserver(this);

        this.users = new HashMap<>();
        this.huecos = new HashMap<>();

        Intent intentFromLogin = getIntent();
        Bundle bundleLogin = intentFromLogin.getExtras();

        this.myName =  bundleLogin.getString("name");
        this.iCanConfirmateHuecos = false;


        cl_container_mensaje = findViewById(R.id.cl_container_mensaje);
        tv_mensaje = findViewById(R.id.tv_mensaje);

        cl_notificacion = findViewById(R.id.cl_notificacion);
        tv_coordenadas = findViewById(R.id.tv_coordenadas);
        tv_direccion = findViewById(R.id.tv_direccion);

        btn_notificacion_agregar = findViewById(R.id.btn_notificacion_agregar);
        btn_notificacion_agregar.setOnClickListener(this);

        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);

        btn_cancelar = findViewById(R.id.btn_cancelar);
        btn_cancelar.setOnClickListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    public void setICanConfirmateHuecos(boolean accion){
        this.iCanConfirmateHuecos = accion;
        if(this.iCanConfirmateHuecos == true){
            this.btn_add.setText("CONFIRMAR");
        }else{
            this.btn_add.setText("+");
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mMap = googleMap;

        this.creator = new Creator(this.mMap, this.admin, this.myName, this);

        //Marker myMarcador = mMap.addMarker(new MarkerOptions().position(myPosition).title("Yo"))
        //this.me.setView(myMarcador);


        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);


        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        this.admin.registerUserIfNotExists(this.myName);

    }

    public HuecoView getHuecoCercano() {
        return huecoCercano;
    }

    public void btnLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }


    public void setInicialPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 120); //*** Agrega la petición!
            return;
        }

        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            if(this.me != null && (this.me.getUser().getLatitud() == 0 || this.me.getUser().getLongitud() == 0)){
                this.updateMyLocation(location);
            }else if(this.me != null){
                this.updateMyMakerPosition(this.me.getUser().getLatitud(),this.me.getUser().getLongitud());
            }

        } else {
            LatLng sydney = new LatLng(3, -84);
            this.updateMyMakerPosition(sydney.latitude, sydney.longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            Toast.makeText(this, "No encontro location", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_add:
                if(this.iCanConfirmateHuecos){
                    this.confirmarHueco();
                }else{
                    this.openNotificationCreate();
                }
                break;
            case R.id.btn_notificacion_agregar:
                addHueco();
                this.cl_notificacion.setVisibility(View.INVISIBLE);
                break;
            case R.id.btn_cancelar:
                this.cl_notificacion.setVisibility(View.INVISIBLE);
                break;
        }
    }

    public void confirmarHueco(){
        if(this.huecoCercano != null) {
            this.huecoCercano.setVerificado(true);
        }
    }

    public void openNotificationCreate(){
        this.cl_notificacion.setVisibility(View.VISIBLE);
        String coodenadasText = this.latitudActual + " , " + this.longitudActual;
        this.tv_coordenadas.setText(coodenadasText);

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String direccion = "No hay una dirección especifica";
        try {
            List<Address> direcciones = geocoder.getFromLocation(this.latitudActual, this.longitudActual, 1);
            for (int i = 0; i < direcciones.size(); i++){
                if(direccion == "No hay una dirección especifica"){
                    Address dir = direcciones.get(i);
                    direccion = dir.getAddressLine(0).toString();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.tv_direccion.setText(direccion);
    }

    public void addHueco() {
        String uid = UUID.randomUUID().toString();
        String name = this.myName;
        Double latitud = this.latitudActual;
        Double longitud = this.longitudActual;

        Hueco h = new Hueco(uid, name, latitud, longitud, false);
        HuecoView newHueco = this.creator.createHuecoDraw(h);

        this.huecos.put(h.getId(), newHueco);
        this.admin.createHueco(h);

        this.computedDistances();
    }


    public void updateMyLocation(Location location) {
        this.lastLocation = location;
        this.latitudActual = location.getLatitude();
        this.longitudActual = location.getLongitude();

        this.updateMyMakerPosition(this.latitudActual, this.longitudActual);
    }

    private void updateMyMakerPosition(double latitud, double longitud){
        this.updateMyMakerPositionZoom(latitud, longitud, true);
    }

    private void updateMyMakerPosition(double latitud, double longitud, boolean zoom){
        this.updateMyMakerPositionZoom(latitud, longitud, zoom);
    }

    private void updateMyMakerPositionZoom(double latitud, double longitud, boolean zoom){
        LatLng myPosition = new LatLng(latitud, longitud);

        this.latitudActual = myPosition.latitude;
        this.longitudActual = myPosition.longitude;

        if (this.me != null) {
            boolean changePosition = this.creator.updateUser(this.me, this.latitudActual, this.longitudActual);
            if(changePosition){
                this.computedDistances();
            }
        }

        if(zoom){
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 10));
        }
    }

    public void computedDistances(){

        final int DISTANCIA_MINIMA_METROS = 30;

        double metersMin = -1;
        String index = "null";

        for (HuecoView hueco: this.huecos.values()){

            //if(hueco.getHueco().isVerificado() == false){
                LatLng huecoLoc = hueco.getCenter();
                LatLng meLoc = me.getView().getPosition();

                double meters = SphericalUtil.computeDistanceBetween(huecoLoc, meLoc);

                if(metersMin == -1 || meters < metersMin){
                    metersMin = meters;
                    index = hueco.getHueco().getId();
                }
          //  }
        }

        if(index.equals("null") == false){

            if(metersMin != -1){
                this.tv_mensaje.setText("Hueco a " + Math.round(metersMin) + " M");
            }else{
                this.tv_mensaje.setText("No se pueden ubicar");
            }


            if(metersMin < DISTANCIA_MINIMA_METROS){
                this.setICanConfirmateHuecos(true);
                HuecoView huecoMasCercano = huecos.get(index);
                this.huecoCercano = huecoMasCercano;
            }else{
                this.setICanConfirmateHuecos(false);
                HuecoView huecoMasCercano = huecos.get(index);
                this.huecoCercano = huecoMasCercano;
                //this.huecoCercano = null;
            }

        }else{

            this.huecoCercano = null;
            this.setICanConfirmateHuecos(false);
            this.tv_mensaje.setText("No hay huecos");

        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateMyLocation(location);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        this.updateMyMakerPosition(latLng.latitude, latLng.longitude, false);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        //Toast.makeText(this, marker.getPosition().latitude + ", " + marker.getPosition().longitude, Toast.LENGTH_SHORT).show();

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude), 17));
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void onLoginUser(User user) {
        runOnUiThread(()->{

            UserView refUser = this.users.get(user.getId());
            if(refUser != null){
                this.creator.updateUser(refUser, user);
            }else{
                UserView newUser = this.creator.createUserDraw(user);
                this.me = newUser;
            }
            setInicialPosition();
            this.admin.onStartReadHuecos();

        });
    }

    @Override
    public void onReadHuecos(HashMap<String, Hueco> huecos) {

        runOnUiThread(() -> {

            boolean changeRamaHueco = false || huecos.size() == 0;

            for(Hueco h : huecos.values()){
                HuecoView refHueco = this.huecos.get(h.getId());

                if(refHueco != null){
                    changeRamaHueco = this.creator.updateHueco(refHueco, h);
                }else{
                    HuecoView newHueco = this.creator.createHuecoDraw(h);
                    this.huecos.put(h.getId(), newHueco);
                    changeRamaHueco = true;
                }
            }

            if(changeRamaHueco){
                Toast.makeText(this, "Actualizando posicion", Toast.LENGTH_SHORT).show();
                this.computedDistances();
            }


        });
    }

    @Override
    public void onReadUsers(HashMap<String, User> users) {
        runOnUiThread(() -> {
            for(User u : users.values()){
                UserView refUser = this.users.get(u.getId());
                if(refUser != null){
                    this.creator.updateUser(refUser, u);
                }else{
                    if(this.me.getUser().getUsername().equals(u.getUsername()) == false){
                        UserView newUser = this.creator.createUserDraw(u);
                        this.users.put(u.getId(), newUser);
                    }
                }
            }
        });
    }



}