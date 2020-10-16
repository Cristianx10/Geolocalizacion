package com.example.geolocalizacion;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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

import android.view.View.OnClickListener;
import android.widget.Toast;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, OnClickListener, LocationListener, OnMapClickListener, OnMapLongClickListener, OnMarkerClickListener {

    private GoogleMap mMap;

    private TextView tv_mensaje;
    private ConstraintLayout cl_container_mensaje;
    private Button btn_add;

    private LocationManager manager;

    private Marker me;
    private ArrayList<Marker> markes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        markes = new ArrayList<>();

        Intent intentFromLogin = getIntent();
        Bundle bundleLogin = intentFromLogin.getExtras();

        String name = bundleLogin.getString("name");


        cl_container_mensaje = findViewById(R.id.cl_container_mensaje);
        tv_mensaje = findViewById(R.id.tv_mensaje);

        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(this);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        myRef.setValue("Hello, World!");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        manager = (LocationManager) getSystemService(LOCATION_SERVICE);

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
    }


    public void btnLocationUpdate(){


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

        if(location != null){
            updateMyLocation(location);
        }else{
            LatLng sydney = new LatLng(3, -84);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
            Toast.makeText(this, "No encontro location", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_add:

                break;
        }
    }

    public void updateMyLocation(Location location){
        LatLng myPosition = new LatLng(location.getLatitude(), location.getLongitude());
        if(me == null){
            me = mMap.addMarker(new MarkerOptions().position(myPosition).title("Yo"));
        }else{
            me.setPosition(myPosition);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myPosition, 10));
        Toast.makeText(this, "Ubicaion cambiada", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        updateMyLocation(location);

        
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(@NonNull String provider) { }

    @Override
    public void onProviderDisabled(@NonNull String provider) { }

    @Override
    public void onMapClick(LatLng latLng) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        Marker marcador =    mMap.addMarker(new MarkerOptions().position(latLng).title("Marcador").snippet("Una referencia"));
        markes.add(marcador);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(this, marker.getPosition().latitude + ", " + marker.getPosition().longitude, Toast.LENGTH_LONG).show();
        marker.showInfoWindow();
        return true;
    }
}