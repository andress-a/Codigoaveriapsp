package com.example.codigoaveriapsp;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    ///Variables
    RecyclerView vistaRecycler;
    FirebaseAdaptador adaptador;
    FirebaseDatabase db;
    DatabaseReference ref;
    SearchView searchView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ///Inicializacion de variables
        vistaRecycler = findViewById(R.id.recyclerView);
        searchView = findViewById(R.id.sView);
        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        ref = db.getReference("codigos_averia");

        //Log para depurar
        Log.d("FIREBASE_CONFIG", "URL de base de datos: " + db.getReference().toString());

        //Configurar RecyclerView
        vistaRecycler.setLayoutManager(new LinearLayoutManager(this));

        //Configurar FirebaseRecyclerOptions
        FirebaseRecyclerOptions<CodigoAveria> options =
                new FirebaseRecyclerOptions.Builder<CodigoAveria>()
                        .setQuery(ref, CodigoAveria.class)
                        .build();

        //Configurar adaptador con opciones y listener
        adaptador = new FirebaseAdaptador(options, this);
        vistaRecycler.setAdapter(adaptador);

        //Configurar SearchView
        setupSearchView();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                realizarBusqueda(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                realizarBusqueda(newText);
                return false;
            }
        });
    }

    private void realizarBusqueda(String textoConsulta) {
        Query consultaFiltrada;

        if (textoConsulta.isEmpty()) {
            //Si no hay texto de búsqueda, mostrar todos los resultados
            consultaFiltrada = ref;
        } else {
            //Buscar por código
            consultaFiltrada = ref.orderByChild("codigo")
                    .startAt(textoConsulta)
                    .endAt(textoConsulta + "\uf8ff");  // Técnica para búsqueda "contiene"
        }

        //Actualizar opciones del adaptador con la nueva consulta
        FirebaseRecyclerOptions<CodigoAveria> nuevasOpciones =
                new FirebaseRecyclerOptions.Builder<CodigoAveria>()
                        .setQuery(consultaFiltrada, CodigoAveria.class)
                        .build();

        //Detener escucha actual
        adaptador.stopListening();

        //Actualizar adaptador con nuevas opciones
        adaptador = new FirebaseAdaptador(nuevasOpciones, this);
        vistaRecycler.setAdapter(adaptador);

        //Iniciar escucha con nuevas opciones
        adaptador.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        adaptador.startListening(); // Empieza a escuchar cambios en Firebase
    }

    @Override
    protected void onStop() {
        super.onStop();
        adaptador.stopListening(); // Deja de escuchar cambios en Firebase
    }

    @Override
    public void onClick(View v) {
        //Obtener posicion
        int position = vistaRecycler.getChildAdapterPosition(v);
        CodigoAveria codigoAveria = adaptador.getItem(position);
        Toast.makeText(this, "Seleccionado: " + codigoAveria.getCodigo(), Toast.LENGTH_SHORT).show();
    }

    //// Menu ////
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addAveria) {
            Intent intent = new Intent(MainActivity.this, AddCodigo.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    ///manejar onactivityresult para que el boton cancelar no falle
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // Se agregó un código, recargar datos
                adaptador.notifyDataSetChanged();
            } else if (resultCode == RESULT_CANCELED) {
                // Se canceló sin cambios, no hacer nada
            }
        }
    }

    ///preiene que falle recyclerview
    @Override
    protected void onResume() {
        super.onResume();
        if (adaptador != null) {
            adaptador.notifyDataSetChanged();
        }
    }

    ///Implementación menu contextual
    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        int position = adaptador.getPosicionSeleccionada();

        if (item.getItemId() == R.id.eliminar_item) {
            // Obtenemos la referencia del elemento en Firebase
            DatabaseReference itemRef = adaptador.getRef(position);

            // Confirmamos la eliminación
            new AlertDialog.Builder(this)
                    .setTitle("Eliminar código")
                    .setMessage("¿Estás seguro de que quieres eliminar este código?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Eliminamos el elemento de Firebase
                        itemRef.removeValue()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(MainActivity.this, "Código eliminado correctamente", Toast.LENGTH_SHORT).show();
                                    // No necesitamos notificar cambios ya que FirebaseRecyclerAdapter lo hace automáticamente
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(MainActivity.this, "Error al eliminar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();

            return true;
        }

        return super.onContextItemSelected(item);
    }



}
