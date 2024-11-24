package com.example.gyms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FavsActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private Location ubicacionActual;
    private RecyclerView favsRecyclerView;
    private GymAdapter favsAdapter;
    private ArrayList<Gym> listaGymsFav = new ArrayList<>();
    private String userName;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_favs);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fondoPrincipal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Verificar permisos de ubicación
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUbicacion();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }

        cargarUsuario();
        favsRecyclerView = findViewById(R.id.favsRecyclerView);
        favsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                obtenerUbicacion();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }

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
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    ubicacionActual = location;
                    cargarGimnasiosFavoritos();
                } else {
                    Toast.makeText(FavsActivity.this, "No se pudo obtener la ubicación", Toast.LENGTH_SHORT).show();
                }
            }
        });
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

    private void cargarGimnasiosFavoritos() {
        UserLogged user = ((App) getApplicationContext()).getUserLogged();
        if (user != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("gimnasiosFavs")
                    .whereEqualTo("userEmail", userEmail)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            listaGymsFav.clear();
                            for (DocumentSnapshot document : task.getResult()) {
                                String name = document.getString("name");
                                String address = document.getString("address");
                                double latitude = document.getDouble("latitude");
                                double longitude = document.getDouble("longitude");

                                double distance = calcularDistanciaKM(ubicacionActual.getLatitude(), ubicacionActual.getLongitude(), latitude, longitude);
                                listaGymsFav.add(new Gym(name, latitude, longitude, address, distance));
                            }

                            Collections.sort(listaGymsFav, Comparator.comparingDouble(Gym::getDistance));
                            mostrarGimnasiosFavoritos();
                        }
                    });
        }
    }

    private void mostrarGimnasiosFavoritos() {
        favsAdapter = new GymAdapter(listaGymsFav, ((App) getApplicationContext()).getUserLogged().getEmail());
        favsRecyclerView.setAdapter(favsAdapter);
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
        startActivity(new Intent(FavsActivity.this, MainActivity.class));
    }

    public void changeToRadar(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(FavsActivity.this, RadarActivity.class));
    }

    public void changeToFavorites(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(FavsActivity.this, FavsActivity.class));
    }

    public void changeToLogin(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(FavsActivity.this, LoginActivity.class));
    }
}