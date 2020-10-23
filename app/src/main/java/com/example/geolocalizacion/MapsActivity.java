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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.geolocalizacion.comm.Actions;
import com.example.geolocalizacion.model.Hueco;
import com.example.geolocalizacion.model.HuecoView;
import com.example.geolocalizacion.model.User;
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
import com.google.android.gms.maps.model.Polygon;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
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

    private Marker me;
    private ArrayList<Marker> markes;

    private Location currentLocation;
    private double latitudActual = 0;
    private double longitudActual = 0;

    private ArrayList<HuecoView> huecos;
    private Actions admin;

    private String myName;

    private HuecoView huecoCercano;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        admin = new Actions();
        admin.setObserver(this);
        markes = new ArrayList<>();
        huecos = new ArrayList<>();

        Intent intentFromLogin = getIntent();
        Bundle bundleLogin = intentFromLogin.getExtras();

        String name = bundleLogin.getString("name");
        this.myName = name;
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

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

        String uid = UUID.randomUUID().toString();
        admin.registerUserIfNotExists(new User(uid, name));

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
        mMap = googleMap;


        manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);

        // Add a marker in Sydney and move the camera
        setInicialPosition();

        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);

        admin.readHuecosDatabase();
    }


    public void btnLocationUpdate() {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);


    }


    public void setInicialPosition() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 120); //*** Agrega la petición!

            return;
        }
        Location location = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (location != null) {
            updateMyLocation(location);
        } else {
            LatLng sydney = new LatLng(3, -84);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
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

            this.huecoCercano.confirmar();

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
        Circle v =  this.drawCircle(new LatLng(latitud, longitud), false);

        huecos.add(new HuecoView(h, v, this.admin));

        admin.createHueco(h);
    }

    public Circle drawCircle(LatLng latLng, boolean state){
        CircleOptions circleOptions = new CircleOptions().fillColor(Color.RED).center(latLng).radius(10);
        Circle circle = mMap.addCircle(circleOptions);
        return circle;
    }


    public void updateMyLocation(Location location) {
        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        this.latitudActual = location.getLatitude();
        this.longitudActual = location.getLongitude();
        if (me == null) {
            me = mMap.addMarker(new MarkerOptions().position(myPosition).title("Yo"));
        } else {
            me.setPosition(myPosition);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 10));

       // mMap.addCircle(new CircleOptions().center(myPosition).radius(5).fillColor(Color.RED));
    }

    public void computedDistances(){

        double metersMin = -1;
        int index = -1;

        for(int i = 0; i < huecos.size(); i++){
            HuecoView hueco = huecos.get(i);
            LatLng huecoLoc = hueco.getCenter();
            LatLng meLoc = me.getPosition();

            double meters = SphericalUtil.computeDistanceBetween(huecoLoc, meLoc);

            if(metersMin == -1 || meters < metersMin){
                metersMin = meters;
                index = i;
            }
        }

        if(index != -1){
            if(metersMin < 5){
                this.setICanConfirmateHuecos(true);
                HuecoView huecoMasCercano = huecos.get(index);
                this.huecoCercano = huecoMasCercano;
            }else{
                this.setICanConfirmateHuecos(false);
                this.huecoCercano = null;
            }
        }else{
            this.huecoCercano = null;
            this.setICanConfirmateHuecos(false);
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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marcador = mMap.addMarker(new MarkerOptions().position(latLng).title("Marcador").snippet("Una referencia"));
        markes.add(marcador);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getPosition().latitude + ", " + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
        marker.showInfoWindow();
        return true;
    }

    @Override
    public void OnReadHuecos(ArrayList<Hueco> huecos) {
        runOnUiThread(() -> {

            for (int i = 0; i < huecos.size(); i++) {
                Hueco h = huecos.get(i);
                LatLng position = new LatLng(h.getLatitud(), h.getLongitud());
                Circle viewHueco = this.drawCircle(position, h.isVerificado());
                this.huecos.add(new HuecoView(h, viewHueco, this.admin));
            }
        });
    }
}