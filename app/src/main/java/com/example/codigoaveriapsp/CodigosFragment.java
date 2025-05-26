package com.example.codigoaveriapsp;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CodigosFragment extends Fragment implements View.OnClickListener {
    //inicializo variables esta es la parte del usuario normal(mecanico)
    RecyclerView vistaRecycler;
    FirebaseAdaptador adaptador;
    FirebaseDatabase db;
    DatabaseReference ref;
    SearchView searchView;
    FirebaseAuth mAuth;
    String usuarioActualId;
    private static final String TAG = "CodigosFragment";

    @Nullable
    @Override
    //Se llama a oncreateview para el manejo de fragmentos
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_codigos, container, false);

        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        ref = db.getReference("codigos_averia");
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            usuarioActualId = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "Usuario autenticado: " + usuarioActualId);
        } else {
            Toast.makeText(getContext(), "Debes iniciar sesión para usar la aplicación", Toast.LENGTH_SHORT).show();
        }

        vistaRecycler = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.sView);
        vistaRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<CodigoAveria> options = new FirebaseRecyclerOptions.Builder<CodigoAveria>()
                .setQuery(ref, CodigoAveria.class)
                .build();

        adaptador = new FirebaseAdaptador(options, this);
        vistaRecycler.setAdapter(adaptador);
        adaptador.startListening();

        confSView();

        return view;
    }
    //Configurar el SearchView
    private void confSView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Solo guardar en historial cuando se envía la búsqueda completa
                buscar(query, true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Realizar la búsqueda pero NO guardar en historial durante la escritura
                new Handler().postDelayed(() -> buscar(newText, false), 300); // 300 ms de retraso
                return true;
            }
        });
    }
    //Realizar la búsqueda
    private void buscar(String texto, boolean guardarHistorial) {
        // Consulta para obtener los códigos que coincidan con el texto ingresado
        Query consulta = ref.orderByChild("codigo")
                .startAt(texto)
                .endAt(texto + "\uf8ff");

        FirebaseRecyclerOptions<CodigoAveria> opciones = new FirebaseRecyclerOptions.Builder<CodigoAveria>()
                .setQuery(consulta, CodigoAveria.class)
                .build();

        adaptador.updateOptions(opciones);
        adaptador.notifyDataSetChanged();

        // Solo guardamos en el historial si se indica explícitamente
        if (guardarHistorial && !texto.isEmpty()) {
            guardarEnHistorial(texto);
        }
    }
    //Guardar en el historial
    private void guardarEnHistorial(String codigo) {
        if (usuarioActualId == null) return;

        DatabaseReference historialRef = db.getReference("usuarios").child(usuarioActualId).child("historial");

        ref.orderByChild("codigo").equalTo(codigo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        CodigoAveria codigoAveria = snapshot.getValue(CodigoAveria.class);
                        if (codigoAveria != null) {
                            historialRef.orderByChild("codigo").equalTo(codigo).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot historialSnapshot) {
                                    if (!historialSnapshot.exists()) {
                                        historialRef.push().setValue(codigoAveria);
                                        Toast.makeText(getContext(), "Código añadido al historial: " + codigo, Toast.LENGTH_SHORT).show();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e(TAG, "Error al verificar historial: " + error.getMessage());
                                }
                            });
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al buscar el código: " + error.getMessage());
            }
        });
    }

    @Override
    public void onClick(View v) {
        int position = vistaRecycler.getChildAdapterPosition(v);
        CodigoAveria seleccionado = adaptador.getItem(position);

        // Mostrar la información del código en un nuevo fragmento
        mostrarDetalles(seleccionado);
    }

    private void mostrarDetalles(CodigoAveria codigoAveria) {
        // Guardar en historial cuando se selecciona un código específico
        guardarCodigoAveria(codigoAveria);

        // Mostrar detalles
        DetallesFragment detallesFragment = DetallesFragment.newInstance(codigoAveria);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detallesFragment)
                .addToBackStack(null)
                .commit();
    }

    private void guardarCodigoAveria(CodigoAveria codigoAveria) {
        if (usuarioActualId == null) return;

        DatabaseReference historialRef = db.getReference("usuarios").child(usuarioActualId).child("historial");

        historialRef.orderByChild("codigo").equalTo(codigoAveria.getCodigo()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    historialRef.push().setValue(codigoAveria);
                    Toast.makeText(getContext(), "Código añadido al historial: " + codigoAveria.getCodigo(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error al acceder al historial: " + error.getMessage());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adaptador != null) {
            adaptador.startListening();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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


