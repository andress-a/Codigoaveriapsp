package com.example.codigoaveriapsp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;


public class MainActivity extends AppCompatActivity {
    private static final String THEME_PREFS = "theme_preferences";
    private static final String IS_DARK_MODE = "is_dark_mode";
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Aplicar el tema antes de inflar el layout
        aplicarTemaGuardado();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Cargar fragmento por defecto
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new HomeFragment())
                .commit();

        // Listener de navegación
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            }
            else if (item.getItemId() == R.id.nav_codigos) {
                selectedFragment = new CodigosFragment();
            }
            else if (item.getItemId() == R.id.nav_historial) {
                selectedFragment = new HistorialFragment();
            } else if (item.getItemId() == R.id.nav_perfil) {
                selectedFragment = new PerfilFragment();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment)
                    .commit();

            return true;
        });
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

