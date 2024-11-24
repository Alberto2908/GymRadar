package com.example.gyms;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GymAdapter extends RecyclerView.Adapter<GymAdapter.GymViewHolder> {

    private final List<Gym> gimnasios;
    private String userEmail;  // Variable para almacenar el email del usuario

    public GymAdapter(List<Gym> gimnasios, String userEmail) {
        this.gimnasios = gimnasios;
        this.userEmail = userEmail;  // Asignar el email del usuario
    }

    @Override
    public GymViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gym_item, parent, false);
        return new GymViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GymViewHolder holder, int position) {
        Gym gimnasio = gimnasios.get(position);
        holder.nameTextView.setText(gimnasio.getName());
        holder.addressTextView.setText(gimnasio.getAddress());
        holder.distanceTextView.setText(gimnasio.getDistanceKm());
        holder.favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarGimnasioEnFirestore(v, gimnasio);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gimnasios.size();
    }

    // Método para guardar en Firestore
    private void guardarGimnasioEnFirestore(View view, Gym gimnasio) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (userEmail != null) {  // Verificar que userEmail no es nulo
            // Crear un mapa con los datos del gimnasio y el email del usuario
            Map<String, Object> gymData = new HashMap<>();
            gymData.put("name", gimnasio.getName());
            gymData.put("address", gimnasio.getAddress());
            gymData.put("latitude", gimnasio.getLat());
            gymData.put("longitude", gimnasio.getLng());
            gymData.put("userEmail", userEmail); // Añadir el email del usuario

            db.collection("gimnasiosFavs").add(gymData)
                    .addOnSuccessListener(documentReference ->
                            Toast.makeText(view.getContext(), "Gimnasio guardado en favoritos.", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(view.getContext(), "Error al guardar en favoritos.", Toast.LENGTH_SHORT).show());
        }
    }

    public static class GymViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView addressTextView;
        public TextView distanceTextView;
        public ImageButton favButton;

        public GymViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.gymName);
            addressTextView = itemView.findViewById(R.id.gymAddress);
            distanceTextView = itemView.findViewById(R.id.gymDistance);
            favButton = itemView.findViewById(R.id.favButton);
        }
    }
}