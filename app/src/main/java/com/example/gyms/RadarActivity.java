package com.example.gyms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class RadarActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private String userName;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_radar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fondoPrincipal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cargarUsuario();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicializar el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void cargarUsuario() {
        TextView nombreUsuario = findViewById(R.id.nombreUsuario);
        UserLogged user = ((App) getApplicationContext()).getUserLogged();
        if (user != null) {
            userName = user.getName();
            userEmail = user.getEmail();

            nombreUsuario.setText(userName); // Asignar el email al TextView
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        setupMap();
    }

    private void setupMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);

        // Obtener la última ubicación y centrar el mapa
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));
                        obtenerGimnasiosCercanos(currentLocation);
                    }
                });
    }

    private void obtenerGimnasiosCercanos(LatLng location) {
        String apiKey = "Insert ur API KEY here";
        // Url con filtro
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + location.latitude + "," + location.longitude +
                "&radius=15000&type=gym" +
                "&keyword=extreme|basic+fit|altafit|fitness+park|crossfit|go+fit|sport|sports|training|wellness|studio|club|center|exercise" +
                "&key=" + apiKey;

        // Url sin filtro
        /*String url2 = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?" +
                "location=" + location.latitude + "," + location.longitude +
                "&radius=15000&type=gym&key=" + apiKey;*/

        // Ejecutar la llamada en un hilo separado para evitar bloquear la interfaz
        new Thread(() -> {
            try {
                URL requestUrl = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                // Leer la respuesta de la API
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parsear el JSON para extraer los gimnasios
                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray results = jsonResponse.getJSONArray("results");

                // Agregar marcadores en el mapa para cada gimnasio
                runOnUiThread(() -> {
                    try {
                        for (int i = 0; i < results.length(); i++) {
                            JSONObject gym = results.getJSONObject(i);
                            String address = gym.getString("vicinity");
                            JSONObject locationObj = gym.getJSONObject("geometry").getJSONObject("location");
                            double lat = locationObj.getDouble("lat");
                            double lng = locationObj.getDouble("lng");
                            String name = gym.getString("name");


                            // Crear marcador y agregarlo al mapa
                            LatLng gymLocation = new LatLng(lat, lng);
                            mMap.addMarker(new MarkerOptions().position(gymLocation).title(name));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void changeToMain(View view) {
        startActivity(new Intent(RadarActivity.this, MainActivity.class));
    }

    public void changeToRadar(View view) {
        startActivity(new Intent(RadarActivity.this, RadarActivity.class));
    }

    public void changeToFavorites(View view) {
        startActivity(new Intent(RadarActivity.this, FavsActivity.class));
    }

    public void changeToLogin(View view) {
        startActivity(new Intent(RadarActivity.this, LoginActivity.class));
    }
}
