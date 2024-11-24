package com.example.gyms;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    EditText nombre;
    EditText emailRegistrar;
    EditText passRegistrar;
    EditText repPassRegistrar;
    Button btnRegistro;
    FirebaseAuth mAuth;
    FirebaseFirestore mFirestore;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.fondoPrincipal), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        nombre = findViewById(R.id.nombreReg);
        emailRegistrar = findViewById(R.id.emailReg);
        passRegistrar = findViewById(R.id.passReg);
        repPassRegistrar = findViewById(R.id.repPassReg);
        btnRegistro = findViewById(R.id.btnLogin);
        mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();

        btnRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        String name = nombre.getText().toString();
        String email = emailRegistrar.getText().toString();
        String password = passRegistrar.getText().toString();
        String confirmPassword = repPassRegistrar.getText().toString();

        if (!name.isEmpty() && !email.isEmpty() && !password.isEmpty() && !confirmPassword.isEmpty()) {
            if (isEmailValid(email)) {
                if (password.equals(confirmPassword)) {
                    if (password.length() >= 6) {
                        createUser(name, email, password);
                    } else {
                        Toast.makeText(RegisterActivity.this, "La contraseña debe tener al menos 6 caractéres.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Las contraseñas no coinciden.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(RegisterActivity.this, "El email no es válido.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(RegisterActivity.this, "Rellena todos los campos para continuar.", Toast.LENGTH_SHORT).show();
        }
    }

    public void createUser(String name, String email, String password) {
        Map<String, Object> values = new HashMap<>();
        values.put("name", nombre.getText().toString());
        values.put("email", emailRegistrar.getText().toString());
        values.put("password", passRegistrar.getText().toString());

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String id = mAuth.getCurrentUser().getUid();
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", name);
                    map.put("email", email);
                    map.put("password", password);
                    mFirestore.collection("users").document(id).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Intent i = new Intent(RegisterActivity.this, LoginActivity.class);
                                startActivity(i);
                                Toast.makeText(RegisterActivity.this, "El usuario se ha registrado correctamente.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "No se pudo registrar el usuario. Intente de nuevo.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@[\\w\\.-]+\\.[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    public void changeToMain(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
    }

    public void changeToRadar(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(RegisterActivity.this, RadarActivity.class));
    }

    public void changeToFavorites(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(RegisterActivity.this, FavsActivity.class));
    }

    public void changeToLogin(View view) { // Función para cambiar de pantalla
        startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
    }
}