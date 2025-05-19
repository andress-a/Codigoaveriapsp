package com.example.codigoaveriapsp;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class HistorialFragment extends Fragment implements View.OnClickListener {

    RecyclerView vistaRecycler;
    FirebaseAdaptador adaptador;
    DatabaseReference ref;
    FirebaseAuth mAuth;
    String usuarioActualId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historial, container, false);

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            usuarioActualId = mAuth.getCurrentUser().getUid();
            ref = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app")
                    .getReference("usuarios")
                    .child(usuarioActualId)
                    .child("historial");
        } else {
            Toast.makeText(getContext(), "Inicia sesión para ver tu historial", Toast.LENGTH_SHORT).show();
            return view;
        }
        vistaRecycler = view.findViewById(R.id.recyclerViewHistorial);
        vistaRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<CodigoAveria> options = new FirebaseRecyclerOptions.Builder<CodigoAveria>()
                .setQuery(ref, CodigoAveria.class)
                .build();

        adaptador = new FirebaseAdaptador(options, this);
        vistaRecycler.setAdapter(adaptador);
        adaptador.startListening();

        Button borrarHistorial = view.findViewById(R.id.btnBorrarHistorial);
        borrarHistorial.setOnClickListener(v -> {
            ref.removeValue().addOnSuccessListener(aVoid -> {
                Toast.makeText(getContext(), "Historial borrado", Toast.LENGTH_SHORT).show();
            });
        });

        return view;
    }

    @Override
    public void onClick(View v) {
        int pos = vistaRecycler.getChildAdapterPosition(v);
        CodigoAveria cod = adaptador.getItem(pos);

        // Mostrar los detalles del código seleccionado
        mostrarDetalles(cod);
    }

    private void mostrarDetalles(CodigoAveria codigoAveria) {
        // Crear y mostrar el fragmento de detalles
        DetallesFragment detallesFragment = DetallesFragment.newInstance(codigoAveria);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detallesFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Iniciar el adaptador cuando el fragmento esté visible
        if (adaptador != null) {
            adaptador.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Detener el adaptador cuando el fragmento ya no esté visible
        if (adaptador != null) {
            adaptador.stopListening();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (adaptador != null) {
            adaptador.stopListening();
        }
    }
}
