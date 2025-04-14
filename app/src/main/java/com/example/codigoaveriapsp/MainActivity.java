package com.example.codigoaveriapsp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.Constraints;
import androidx.work.WorkManager;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


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
                .replace(R.id.fragment_container, new CodigosFragment())
                .commit();

        // Listener de navegación
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_codigos) {
                selectedFragment = new CodigosFragment();
            } else if (item.getItemId() == R.id.nav_historial) {
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

