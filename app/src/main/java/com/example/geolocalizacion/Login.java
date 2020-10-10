package com.example.geolocalizacion;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.view.View.OnClickListener;

public class Login extends AppCompatActivity implements OnClickListener {


    EditText et_username;
    Button btn_entrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        et_username = findViewById(R.id.et_username);
        btn_entrar = findViewById(R.id.btn_entrar);
        btn_entrar.setOnClickListener(this);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION}, 1
        );



    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btn_entrar:

                Intent goToMaps = new Intent(this, MapsActivity.class);
                String name = et_username.getText().toString();
                goToMaps.putExtra("name", name);

                startActivity(goToMaps);

                break;
        }
    }
}