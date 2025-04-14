package com.example.codigoaveriapsp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;

public class DetallesFragment extends Fragment {

    private TextView tvCodigo, tvDescripcion, tvSolucion;
    // Ya no necesitamos la referencia a Firebase para añadir al historial
    // porque esto se hará exclusivamente desde CodigosFragment

    public static DetallesFragment newInstance(CodigoAveria codigoAveria) {
        DetallesFragment fragment = new DetallesFragment();
        Bundle args = new Bundle();
        args.putSerializable("codigoAveria", codigoAveria);  // Pasar el objeto al fragmento
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_detalles, container, false);

        // Inicializar las vistas
        tvCodigo = view.findViewById(R.id.tvCodigo);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        tvSolucion = view.findViewById(R.id.tvSolucion);

        // Obtener los datos del código de avería
        if (getArguments() != null) {
            CodigoAveria codigoAveria = (CodigoAveria) getArguments().getSerializable("codigoAveria");

            if (codigoAveria != null) {
                // Mostrar la información en los TextViews
                tvCodigo.setText("Código: " + codigoAveria.getCodigo());
                tvDescripcion.setText("Descripción: " + codigoAveria.getDescripcion());
                tvSolucion.setText("Solución: " + codigoAveria.getSolucion());

                // Removemos la funcionalidad de guardar en el historial
                // para evitar duplicados
            }
        }

        return view;
    }
}

