package com.example.codigoaveriapsp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class PerfilFragment extends Fragment {

    FirebaseUser usuario;
    private static final String THEME_PREFS = "theme_preferences";
    private static final String IS_DARK_MODE = "is_dark_mode";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        usuario = FirebaseAuth.getInstance().getCurrentUser();

        TextView correo = view.findViewById(R.id.tvCorreo);
        Button cerrarSesion = view.findViewById(R.id.btnCerrarSesion);
        SwitchCompat switchTema = view.findViewById(R.id.switchTema);

        if (usuario != null) {
            correo.setText("Correo: " + usuario.getEmail());
        }

        cerrarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getContext(), AuthActivity.class));
                requireActivity().finish();
            }
        });

        //Configurar el switch del tema
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean(IS_DARK_MODE, true); // Por defecto modo oscuro

        //Establecer el estado del switch basado en la preferencia guardada
        switchTema.setChecked(isDarkMode);

        //Cambiar el tema
        switchTema.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Guardar preferencia
                sharedPreferences.edit().putBoolean(IS_DARK_MODE, isChecked).apply();

                //Aplicar tema
                aplicarTema(isChecked);
            }
        });

        return view;
    }

    private void aplicarTema(boolean isDarkMode) {
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        //aplicar los cambios
        requireActivity().recreate();
    }
}

