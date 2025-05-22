package com.example.codigoaveriapsp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AuthActivity extends AppCompatActivity {
    private static final String THEME_PREFS = "theme_preferences";
    private static final String IS_DARK_MODE = "is_dark_mode";
    private static final String TAG = "AuthActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference usuariosRef;
    private EditText edtEmail, edtPassword;
    private Button btnRegistrar, btnAcceder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aplicarTemaGuardado();
        setContentView(R.layout.login_layout);

        // Firebase - USAR LA MISMA URL QUE EN LOS OTROS FRAGMENTOS
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        usuariosRef = database.getReference("usuarios");

        // Vistas
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnAcceder = findViewById(R.id.btnAcceder);
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            checkUserRole(currentUser.getUid());
        }
    }

    public void click_registrar(View v) {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar formato de email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Por favor ingresa un email válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validar longitud de contraseña
        if (password.length() < 6) {
            Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Crear el objeto usuario con estructura completa
                            Map<String, Object> userInfo = new HashMap<>();
                            userInfo.put("email", email);
                            userInfo.put("rol", "user");
                            userInfo.put("uid", user.getUid());

                            Log.d(TAG, "Intentando guardar usuario con UID: " + user.getUid());
                            Log.d(TAG, "Referencia de database: " + usuariosRef.toString());

                            // Guardar en Firebase Database
                            usuariosRef.child(user.getUid()).setValue(userInfo)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Usuario guardado correctamente en la base de datos");

                                        // Verificar que se guardó correctamente
                                        usuariosRef.child(user.getUid()).get().addOnCompleteListener(verifyTask -> {
                                            if (verifyTask.isSuccessful()) {
                                                DataSnapshot snapshot = verifyTask.getResult();
                                                if (snapshot.exists()) {
                                                    Log.d(TAG, "Verificación exitosa - Usuario existe en DB: " + snapshot.getValue());
                                                } else {
                                                    Log.w(TAG, "Verificación falló - Usuario no existe en DB");
                                                }
                                            }
                                        });

                                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                                        // Redirigir directamente ya que sabemos que es un usuario nuevo con rol "user"
                                        irAMainActivity();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Error al guardar usuario: " + e.getMessage());
                                        Toast.makeText(this, "Error al guardar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "Error al registrar: " + errorMessage);
                        Toast.makeText(this, "Error al registrar: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void click_acceder(View v) {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "Inicio de sesión exitoso para: " + user.getEmail());
                            checkUserRole(user.getUid());
                        }
                    } else {
                        String errorMessage = task.getException() != null ?
                                task.getException().getMessage() : "Error desconocido";
                        Log.e(TAG, "Error al iniciar sesión: " + errorMessage);
                        Toast.makeText(this, "Error al iniciar sesión: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkUserRole(String uid) {
        Log.d(TAG, "Verificando el rol del usuario: " + uid);

        usuariosRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "Snapshot recibido: " + snapshot.toString());
                Log.d(TAG, "Snapshot existe: " + snapshot.exists());

                if (snapshot.exists()) {
                    String rol = snapshot.child("rol").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    Log.d(TAG, "Rol encontrado: " + rol);
                    Log.d(TAG, "Email encontrado: " + email);

                    if ("admin".equals(rol)) {
                        irAMainActivityAdmin();
                    } else {
                        irAMainActivity();
                    }
                } else {
                    Log.w(TAG, "No se encontró información del usuario en la base de datos");

                    // Si es un usuario que se acaba de registrar, puede que aún no se haya sincronizado
                    // Intentar crear el nodo del usuario si no existe
                    FirebaseUser currentUser = mAuth.getCurrentUser();
                    if (currentUser != null) {
                        Map<String, Object> defaultUserInfo = new HashMap<>();
                        defaultUserInfo.put("email", currentUser.getEmail());
                        defaultUserInfo.put("rol", "user");
                        defaultUserInfo.put("uid", currentUser.getUid());

                        usuariosRef.child(uid).setValue(defaultUserInfo)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Usuario creado por defecto");
                                    irAMainActivity();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al crear usuario por defecto: " + e.getMessage());
                                    // Por defecto, redirigir como usuario normal
                                    irAMainActivity();
                                });
                    } else {
                        irAMainActivity();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al obtener el rol del usuario: " + error.getMessage());
                Toast.makeText(AuthActivity.this, "Error al obtener el rol del usuario: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void irAMainActivity() {
        Log.d(TAG, "Redirigiendo a MainActivity");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void irAMainActivityAdmin() {
        Log.d(TAG, "Redirigiendo a MainActivity (Admin)");
        Intent intent = new Intent(this, MainActivityAdmin.class);
        // Puedes agregar extras para identificar que es admin
        intent.putExtra("isAdmin", true);
        startActivity(intent);
        finish();
    }

    private void aplicarTemaGuardado() {
        SharedPreferences sharedPreferences = getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(IS_DARK_MODE, true); // Por defecto modo oscuro

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
