package com.example.gyms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private String userName;
    private String userEmail;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = auth.getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fondoPrincipal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        cargarUsuario();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Verificar si la app tiene permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        // Inicializar el RecyclerView
        RecyclerView recyclerView = findViewById(R.id.gym_recycler_view); // Asegúrate de tener el RecyclerView en tu layout

        // Crear y asignar el LayoutManager
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        // Crear la lista de gimnasios
        List<Gym> listaGyms = new ArrayList<>();

        if (currentUser != null) {
            userEmail = currentUser.getEmail();  // Obtener el email del usuario
            Log.d("UserEmail", "Correo del usuario: " + userEmail);  // Verifica el email en Logcat

            // Crear el adaptador y pasar el correo electrónico
            GymAdapter adapter = new GymAdapter(listaGyms, userEmail);  // Pasar el email al adaptador
            recyclerView.setAdapter(adapter);
        } else {
            // Cuando no hay usuario logueado
            Toast.makeText(this, "No se ha iniciado sesión", Toast.LENGTH_SHORT).show();
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

    // Método para obtener la ubicación actual
    private void obtenerUbicacion() {
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
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            // Por si quieres probar en cualquier otra ubicación
                            /*double Latitude =;
                            double Longitude =;
                            LatLng fakeLocation = new LatLng(Latitude , Longitude);
                            obtenerGimnasiosCercanos(fakeLocation);*/

                            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                            obtenerGimnasiosCercanos(currentLocation);
                        } else {
                            Toast.makeText(MainActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Método para obtener gimnasios cercanos utilizando la API de Google Places
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

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray results = response.getJSONArray("results");
                            ArrayList<Gym> gimnasios = new ArrayList<>();

                            for (int i = 0; i < results.length(); i++) {
                                JSONObject gym = results.getJSONObject(i);
                                String name = gym.getString("name");
                                String address = gym.getString("vicinity");
                                double lat = gym.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                                double lng = gym.getJSONObject("geometry").getJSONObject("location").getDouble("lng");

                                double distance = calcularDistanciaKM(location.latitude, location.longitude, lat, lng);

                                Gym gimnasio = new Gym(name, lat, lng, address, distance);
                                gimnasios.add(gimnasio);
                            }

                            // Ordenar y mostrar gimnasios
                            Collections.sort(gimnasios, Comparator.comparingDouble(Gym::getDistance));
                            mostrarGimnasios(gimnasios);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });

        Volley.newRequestQueue(this).add(request);
    }

    // Método para mostrar los gimnasios en la UI
    private void mostrarGimnasios(ArrayList<Gym> gimnasios) {
        RecyclerView recyclerView = findViewById(R.id.gym_recycler_view);
        GymAdapter adapter = new GymAdapter(gimnasios, userEmail);
        recyclerView.setAdapter(adapter);
    }

    // Método para manejar la respuesta de la solicitud de permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Si el permiso es otorgado, obtener la ubicación
                obtenerUbicacion();
            } else {
                // Si el permiso no es otorgado, manejar el caso
                Toast.makeText(this, "Se necesitan permisos de ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private double calcularDistanciaKM(double lat1, double lng1, double lat2, double lng2) {
        Location location1 = new Location("start");
        location1.setLatitude(lat1);
        location1.setLongitude(lng1);

        Location location2 = new Location("end");
        location2.setLatitude(lat2);
        location2.setLongitude(lng2);

        double distanciaEnMetros = location1.distanceTo(location2);
        return distanciaEnMetros / 1000;
    }

    public void changeToMain(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(MainActivity.this, MainActivity.class));
    }

    public void changeToRadar(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(MainActivity.this, RadarActivity.class));
    }

    public void changeToFavorites(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(MainActivity.this, FavsActivity.class));
    }

    public void changeToLogin(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
}