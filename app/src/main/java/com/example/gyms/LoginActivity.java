package com.example.gyms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText emailLogin;
    private EditText passLogin;
    private Button btnLogin;
    private String userName; // Variable global
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailLogin = findViewById(R.id.emailReg);
        passLogin = findViewById(R.id.passReg);
        btnLogin = findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(view -> login());
    }

    private void login() {
        String email = emailLogin.getText().toString().trim();
        String password = passLogin.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor, completa todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Buscar el nombre del usuario en Firestore
        buscarNombre(email, success -> {
            if (success) {
                // Iniciar sesión solo si se encuentra el usuario
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                // Guardar usuario logueado en la aplicación
                                UserLogged user = new UserLogged(userName, email);
                                ((App) getApplicationContext()).setUserLogged(user);

                                Toast.makeText(this, "Inicio de sesión exitoso.", Toast.LENGTH_SHORT).show();

                                // Cambiar a MainActivity
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            } else {
                                Toast.makeText(this, "El correo o contraseña son incorrectos.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Error al iniciar sesión.", Toast.LENGTH_SHORT).show();
                        });
            } else {
                Toast.makeText(this, "Usuario no encontrado en Firestore.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void buscarNombre(String email, OnNameCheckCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        userName = document.getString("name"); // Guardar nombre en variable global

                        callback.onCheckCompleted(true);
                    } else {
                        callback.onCheckCompleted(false); // Usuario no encontrado
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onCheckCompleted(false); // Error en la consulta
                });
    }

    // Interfaz para manejar el resultado de buscarNombre
    public interface OnNameCheckCallback {
        void onCheckCompleted(boolean success);
    }

    public void changeToMain(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }

    public void changeToRadar(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(LoginActivity.this, RadarActivity.class));
    }

    public void changeToFavorites(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(LoginActivity.this, FavsActivity.class));
    }

    public void changeToLogin(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(LoginActivity.this, LoginActivity.class));
    }

    public void changeToRegister(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
    }
}