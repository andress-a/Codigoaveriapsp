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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthActivity extends AppCompatActivity {
    private static final String THEME_PREFS = "theme_preferences";
    private static final String IS_DARK_MODE = "is_dark_mode";
    private static final String TAG = "AuthActivity";
    private FirebaseAuth mAuth;
    private EditText edtEmail, edtPassword;
    private Button btnRegistrar, btnAcceder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        aplicarTemaGuardado();
        setContentView(R.layout.login_layout);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Inicializar vistas
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnAcceder = findViewById(R.id.btnAcceder);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Verificar si el usuario ya está logueado
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // Si ya está logueado, no permitir acceso a la pantalla de login, ir a MainActivity
            irAMainActivity();
        }
    }

    private void irAMainActivity() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Cierra AuthActivity para que no se pueda volver atrás con el botón de retroceso
    }

    public void click_registrar(View v) {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        // Validar campos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(AuthActivity.this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Registro exitoso, autenticación y redirección
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // Error al registrar, mostrar mensaje
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Error al registrar: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    public void click_acceder(View v) {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        // Validar campos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(AuthActivity.this, "Por favor ingresa email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Ingreso exitoso, redirigir
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // Si falla, mensaje de error
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AuthActivity.this, "Error al iniciar sesión: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            // Usuario autenticado, redirigir a la actividad principal
            irAMainActivity();
        }
    }
    /*
    // Método para cerrar sesión
    public void cerrarSesion() {
        // Desconectar al usuario de Firebase
        mAuth.signOut();

        // Redirigir a la pantalla de login (por si acaso la sesión se mantiene persistente)
        Intent intent = new Intent(AuthActivity.this, AuthActivity.class);
        startActivity(intent);
        finish();  // Cierra la actividad actual
    }

    // En caso de que quieras forzar la revalidación del token
    private void renovarToken() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    Log.d(TAG, "Token renovado: " + token);
                } else {
                    Log.w(TAG, "Error al renovar el token: ", task.getException());
                }
            });
        }
    }*/
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

