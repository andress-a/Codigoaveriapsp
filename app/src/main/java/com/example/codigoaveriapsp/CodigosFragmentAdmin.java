package com.example.codigoaveriapsp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class CodigosFragmentAdmin extends Fragment implements View.OnClickListener {

    RecyclerView vistaRecycler;
    FirebaseAdaptador adaptador;
    FirebaseDatabase db;
    DatabaseReference ref, usuariosRef;
    SearchView searchView;
    FloatingActionButton fabAddCodigo;
    FirebaseAuth mAuth;
    String usuarioActualId;
    private boolean isAdmin = false;
    private static final String TAG = "CodigosFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_codigos, container, false);

        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        ref = db.getReference("codigos_averia");
        usuariosRef = db.getReference("usuarios");
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            usuarioActualId = mAuth.getCurrentUser().getUid();
            Log.d(TAG, "Usuario autenticado: " + usuarioActualId);
            checkUserRole(); // Verificar si es admin
        } else {
            Toast.makeText(getContext(), "Debes iniciar sesión para usar la aplicación", Toast.LENGTH_SHORT).show();
        }

        vistaRecycler = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.sView);
        fabAddCodigo = view.findViewById(R.id.fabAddCodigo);
        vistaRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        FirebaseRecyclerOptions<CodigoAveria> options = new FirebaseRecyclerOptions.Builder<CodigoAveria>()
                .setQuery(ref, CodigoAveria.class)
                .build();

        adaptador = new FirebaseAdaptador(options, this);
        vistaRecycler.setAdapter(adaptador);
        adaptador.startListening();

        setupSearchView();
        setupFloatingActionButton();

        return view;
    }

    private void checkUserRole() {
        if (usuarioActualId != null) {
            usuariosRef.child(usuarioActualId).child("rol").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String rol = snapshot.getValue(String.class);
                    isAdmin = "admin".equals(rol);

                    // Mostrar u ocultar el FAB según el rol
                    if (fabAddCodigo != null) {
                        fabAddCodigo.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
                    }

                    Log.d(TAG, "Usuario es admin: " + isAdmin);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e(TAG, "Error al verificar rol: " + error.getMessage());
                    // Por defecto, ocultar el FAB si hay error
                    if (fabAddCodigo != null) {
                        fabAddCodigo.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void setupFloatingActionButton() {
        fabAddCodigo.setOnClickListener(v -> {
            if (isAdmin) {
                mostrarDialogoAgregarCodigo();
            } else {
                Toast.makeText(getContext(), "Solo los administradores pueden añadir códigos", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoAgregarCodigo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_agregar_codigo, null);

        EditText etCodigo = dialogView.findViewById(R.id.etCodigo);
        EditText etDescripcion = dialogView.findViewById(R.id.etDescripcion);
        EditText etMarca = dialogView.findViewById(R.id.etMarca);
        EditText etModelo = dialogView.findViewById(R.id.etModelo);
        EditText etSolucion = dialogView.findViewById(R.id.etSolucion);

        builder.setView(dialogView)
                .setTitle("Añadir Nuevo Código de Avería")
                .setPositiveButton("Guardar", null) // Lo configuramos después para evitar que se cierre automáticamente
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnGuardar.setOnClickListener(v -> {
                String codigo = etCodigo.getText().toString().trim();
                String descripcion = etDescripcion.getText().toString().trim();
                String marca = etMarca.getText().toString().trim();
                String modelo = etModelo.getText().toString().trim();
                String solucion = etSolucion.getText().toString().trim();

                if (validarCampos(codigo, descripcion, marca, modelo, solucion)) {
                    verificarYGuardarCodigo(codigo, descripcion, marca, modelo, solucion, dialog);
                }
            });
        });

        dialog.show();
    }

    private boolean validarCampos(String codigo, String descripcion, String marca, String modelo, String solucion) {
        if (codigo.isEmpty()) {
            Toast.makeText(getContext(), "El código no puede estar vacío", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (descripcion.isEmpty()) {
            Toast.makeText(getContext(), "La descripción no puede estar vacía", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (marca.isEmpty()) {
            Toast.makeText(getContext(), "La marca no puede estar vacía", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (modelo.isEmpty()) {
            Toast.makeText(getContext(), "El modelo no puede estar vacío", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (solucion.isEmpty()) {
            Toast.makeText(getContext(), "La solución no puede estar vacía", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void verificarYGuardarCodigo(String codigo, String descripcion, String marca, String modelo, String solucion, AlertDialog dialog) {
        // Verificar si el código ya existe
        ref.orderByChild("codigo").equalTo(codigo).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Toast.makeText(getContext(), "Este código ya existe", Toast.LENGTH_LONG).show();
                } else {
                    // El código no existe, proceder a guardarlo
                    guardarNuevoCodigo(codigo, descripcion, marca, modelo, solucion, dialog);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al verificar código existente: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Error al verificar el código", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarNuevoCodigo(String codigo, String descripcion, String marca, String modelo, String solucion, AlertDialog dialog) {
        // Crear el objeto CodigoAveria
        CodigoAveria nuevoCodigoAveria = new CodigoAveria();
        nuevoCodigoAveria.setCodigo(codigo);
        nuevoCodigoAveria.setDescripcion(descripcion);
        nuevoCodigoAveria.setMarca(marca);
        nuevoCodigoAveria.setModelo(modelo);
        nuevoCodigoAveria.setSolucion(solucion);

        // Usar el código como clave para evitar duplicados
        ref.child(codigo).setValue(nuevoCodigoAveria)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Código de avería añadido correctamente", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Nuevo código añadido: " + codigo);
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al añadir el código: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Error al añadir código", e);
                });
    }

    private void setupSearchView() {
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

    private void buscar(String texto, boolean guardarHistorial) {
        Query consulta;

        if (texto.isEmpty()) {
            // Si el texto está vacío, mostrar todos los códigos
            consulta = ref;
        } else {
            // Consulta para obtener los códigos que coincidan con el texto ingresado
            consulta = ref.orderByChild("codigo")
                    .startAt(texto)
                    .endAt(texto + "\uf8ff");
        }

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


