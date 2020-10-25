package com.example.geolocalizacion.comm;

import android.annotation.SuppressLint;
import android.util.Log;

import com.example.geolocalizacion.model.Hueco;
import com.example.geolocalizacion.model.HuecoView;
import com.example.geolocalizacion.model.User;
import com.example.geolocalizacion.model.UserView;
import com.example.geolocalizacion.observer.OnReadMapObject;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;


public class Actions {

    private HTTPSWebUtilDomi https;
    private Gson gson;

    public static String URL_PROYECT = "https://geolocalizacion-292101.firebaseio.com/";

    private OnReadMapObject observer;

    private boolean isAliveReadHuecos;
    private boolean isAliveReadUsers;

    //METODO DE SUSCRIPCION AL EVENTO


    public Actions() {
        https = new HTTPSWebUtilDomi();
        gson = new Gson();
        this.isAliveReadHuecos = true;
        this.isAliveReadUsers = true;
    }


    public void onStartReadHuecos(){
        Thread hilo = new Thread(()->{

            while (isAliveReadHuecos){
                try {

                    this.readUsuariosDatabase();
                    this.readHuecosDatabase();

                    Thread.sleep(10000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        hilo.start();
    }

    //Se pide el usuario. Si es nulo es porque no existe y se crea. Si ya existia no se crea
    public void registerUserIfNotExists(String nameUser) {
        new Thread(
                ()->{
                        String url = URL_PROYECT + "/users/" + nameUser + ".json";
                        String response = https.GETrequest(url);

                        //SI EL USUARIO NO EXISTE, LO CREAMOS
                        if (response.equals("null")) {
                            String uid = UUID.randomUUID().toString();
                            User usuario = new User(uid, nameUser, 0, 0);

                            https.PUTrequest(url, gson.toJson(usuario));

                            if(this.observer !=null){
                                this.observer.onLoginUser(usuario);
                            }

                        } else {
                            User usuario = gson.fromJson(response, User.class);

                            if(this.observer !=null){
                                this.observer.onLoginUser(usuario);
                            }
                        }

                }
        ).start();
    }


    public void createHueco(Hueco hueco) {
        new Thread(
                () -> {

                    String url = URL_PROYECT + "/huecos/" + hueco.getId() + ".json";

                    https.PUTrequest(url, gson.toJson(hueco));

                }).start();
    }

    @SuppressLint("NewApi")
    public void readHuecosDatabase() {

        new Thread(() -> {
            String url = URL_PROYECT + "/huecos.json";
            String response = https.GETrequest(url);
            Type type = new TypeToken<HashMap<String, Hueco>>() {
            }.getType();
            HashMap<String, Hueco> huecos = new HashMap<>();
            if (response.equals("null")) {
                if (this.observer != null) {
                    this.observer.onReadHuecos(huecos);
                }
            }else{
               huecos = gson.fromJson(response, type);
                if (this.observer != null) {
                    this.observer.onReadHuecos(huecos);
                }
            }

        }).start();
    }


    @SuppressLint("NewApi")
    public void readUsuariosDatabase() {

        new Thread(() -> {
            String url = URL_PROYECT + "/users.json";
            String response = https.GETrequest(url);
            if (response.equals("null")) {
            }else{
                Type type = new TypeToken<HashMap<String, User>>() {
                }.getType();
                HashMap<String, User> users = gson.fromJson(response, type);

                if (this.observer != null) {
                    this.observer.onReadUsers(users);
                }
            }

        }).start();
    }

    public void confirmarHueco(HuecoView hueco){
        new Thread(
                ()->{
                    String url = URL_PROYECT + "/huecos/" + hueco.getHueco().getId() + ".json";
                    https.PUTrequest(url, gson.toJson(hueco.getHueco()));
                }
        ).start();
    }

    public void updateUser(UserView user){
        new Thread(
                ()->{
                    String url = URL_PROYECT + "/users/" + user.getUser().getUsername() + ".json";
                    https.PUTrequest(url, gson.toJson(user.getUser()));
                }
        ).start();
    }

    public void setObserver(OnReadMapObject observer) {
        this.observer = observer;
    }
}
