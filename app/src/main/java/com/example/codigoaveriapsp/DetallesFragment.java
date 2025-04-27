package com.example.codigoaveriapsp;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DetallesFragment extends Fragment {

    private TextView tvCodigo, tvDescripcion, tvMarca, tvModelo, tvSolucion;
    private EditText etNota;
    private Button btnGuardarNota, btnVolver;
    private RecyclerView rvNotas;
    private NotasAdapter notasAdapter;
    private List<Nota> listaNotas;
    private TextView tvNoNotasMsg;

    private FirebaseDatabase db;
    private DatabaseReference notasRef;
    private FirebaseAuth mAuth;
    private String usuarioActualId;
    private CodigoAveria codigoActual;

    public static DetallesFragment newInstance(CodigoAveria codigoAveria) {
        DetallesFragment fragment = new DetallesFragment();
        Bundle args = new Bundle();
        args.putSerializable("codigoAveria", codigoAveria);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detalles, container, false);

        // Inicializar Firebase
        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        notasRef = db.getReference("notas_codigos");
        mAuth = FirebaseAuth.getInstance();

        // Verificar que hay un usuario autenticado
        if (mAuth.getCurrentUser() != null) {
            usuarioActualId = mAuth.getCurrentUser().getUid();
        } else {
            // Si no hay usuario autenticado, mostrar mensaje
            Toast.makeText(getContext(), "Debes iniciar sesión para ver y añadir notas", Toast.LENGTH_SHORT).show();
        }

        // Inicializar las vistas
        tvCodigo = view.findViewById(R.id.tvCodigo);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        tvMarca = view.findViewById(R.id.tvMarca);
        tvModelo = view.findViewById(R.id.tvModelo);
        tvSolucion = view.findViewById(R.id.tvSolucion);
        etNota = view.findViewById(R.id.etNota);
        btnGuardarNota = view.findViewById(R.id.btnGuardarNota);
        btnVolver = view.findViewById(R.id.btnVolver);
        rvNotas = view.findViewById(R.id.rvNotas);
        tvNoNotasMsg = view.findViewById(R.id.tvNoNotasMsg);

        // Configurar RecyclerView
        listaNotas = new ArrayList<>();
        notasAdapter = new NotasAdapter(listaNotas);
        rvNotas.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNotas.setAdapter(notasAdapter);

        // Obtener los datos del código de avería
        if (getArguments() != null) {
            codigoActual = (CodigoAveria) getArguments().getSerializable("codigoAveria");

            if (codigoActual != null) {
                // Mostrar la información en los TextViews
                tvCodigo.setText("Código: " + codigoActual.getCodigo());
                tvMarca.setText("Marca: " + codigoActual.getMarca());
                tvModelo.setText("Modelo: " + codigoActual.getModelo());
                tvDescripcion.setText("Descripción: " + codigoActual.getDescripcion());
                tvSolucion.setText("Solución: " + codigoActual.getSolucion());

                // Cargar las notas existentes si hay usuario autenticado
                if (usuarioActualId != null) {
                    cargarNotas(codigoActual.getCodigo());
                }
            }
        }

        // Configurar el botón para guardar notas
        btnGuardarNota.setOnClickListener(v -> {
            if (usuarioActualId != null) {
                guardarNota();
            } else {
                Toast.makeText(getContext(), "Debes iniciar sesión para añadir notas", Toast.LENGTH_SHORT).show();
            }
        });

        // Configurar el botón volver
        btnVolver.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        return view;
    }

    private void cargarNotas(String codigoAveria) {
        // Consulta para obtener las notas del usuario para este código específico
        Query consultaNotas = notasRef
                .orderByChild("usuarioId_codigoAveria")
                .equalTo(usuarioActualId + "_" + codigoAveria);

        consultaNotas.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaNotas.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Nota nota = snapshot.getValue(Nota.class);
                    if (nota != null) {
                        nota.setId(snapshot.getKey());
                        listaNotas.add(nota);
                    }
                }

                // Ordenar notas por timestamp (más recientes primero)
                Collections.sort(listaNotas, (n1, n2) -> Long.compare(n2.getTimestamp(), n1.getTimestamp()));

                notasAdapter.notifyDataSetChanged();

                // Mostrar mensaje si no hay notas
                if (listaNotas.isEmpty()) {
                    tvNoNotasMsg.setVisibility(View.VISIBLE);
                } else {
                    tvNoNotasMsg.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("DetallesFragment", "Error al cargar notas: " + databaseError.getMessage());
                Toast.makeText(getContext(), "Error al cargar notas", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void guardarNota() {
        String contenidoNota = etNota.getText().toString().trim();

        if (contenidoNota.isEmpty()) {
            Toast.makeText(getContext(), "La nota no puede estar vacía", Toast.LENGTH_SHORT).show();
            return;
        }

        if (codigoActual == null) {
            Toast.makeText(getContext(), "Error: No se pudo obtener el código de avería", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear nueva nota
        Nota nuevaNota = new Nota();
        nuevaNota.setUsuarioId(usuarioActualId);
        nuevaNota.setCodigoAveria(codigoActual.getCodigo());
        nuevaNota.setContenido(contenidoNota);
        nuevaNota.setTimestamp(System.currentTimeMillis());

        // Añadir un campo compuesto para consultas más eficientes
        nuevaNota.setUsuarioId_codigoAveria(usuarioActualId + "_" + codigoActual.getCodigo());

        // Guardar en Firebase
        DatabaseReference nuevaNotaRef = notasRef.push();
        nuevaNotaRef.setValue(nuevaNota)
                .addOnSuccessListener(aVoid -> {
                    // Limpiar el campo de texto
                    etNota.setText("");
                    Toast.makeText(getContext(), "Nota guardada correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al guardar la nota: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    Log.e("DetallesFragment", "Error al guardar nota", e);
                });
    }

    // Adaptador para las notas
    private class NotasAdapter extends RecyclerView.Adapter<NotasAdapter.NotaViewHolder> {

        private List<Nota> notas;

        public NotasAdapter(List<Nota> notas) {
            this.notas = notas;
        }

        @NonNull
        @Override
        public NotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nota, parent, false);
            return new NotaViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotaViewHolder holder, int position) {
            Nota nota = notas.get(position);
            holder.tvContenidoNota.setText(nota.getContenido());

            // Formatear la fecha
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            String fechaFormateada = sdf.format(new Date(nota.getTimestamp()));
            holder.tvFechaNota.setText(fechaFormateada);

            // Configurar botón de eliminar
            holder.btnEliminarNota.setOnClickListener(v -> eliminarNota(nota.getId(), position));
        }

        @Override
        public int getItemCount() {
            return notas.size();
        }

        class NotaViewHolder extends RecyclerView.ViewHolder {
            TextView tvContenidoNota, tvFechaNota;
            ImageButton btnEliminarNota;

            NotaViewHolder(View itemView) {
                super(itemView);
                tvContenidoNota = itemView.findViewById(R.id.tvContenidoNota);
                tvFechaNota = itemView.findViewById(R.id.tvFechaNota);
                btnEliminarNota = itemView.findViewById(R.id.btnEliminarNota);
            }
        }
    }

    private void eliminarNota(String notaId, int position) {
        // Mostrar diálogo de confirmación
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar nota")
                .setMessage("¿Estás seguro de que quieres eliminar esta nota?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    // Eliminar de Firebase
                    notasRef.child(notaId).removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // La nota ya se eliminará automáticamente de la lista
                                // gracias al ValueEventListener en cargarNotas()
                                Toast.makeText(getContext(), "Nota eliminada", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getContext(), "Error al eliminar la nota",
                                        Toast.LENGTH_SHORT).show();
                                Log.e("DetallesFragment", "Error al eliminar nota", e);
                            });
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}

