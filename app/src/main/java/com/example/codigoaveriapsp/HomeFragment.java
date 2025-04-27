package com.example.codigoaveriapsp;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements View.OnClickListener {

    private FirebaseDatabase db;
    private DatabaseReference ref;
    private TextView welcomeText;
    private FirebaseAuth mAuth;
    private Map<String, RecyclerView> modelRecyclerViews = new HashMap<>();
    private Map<String, ModelAdapter> modelAdapters = new HashMap<>();
    private static final String TAG = "HomeFragment";

    // Lista de modelos que queremos mostrar
    private final String[] modelosDestacados = {"Audi", "BMW", "Mercedes", "Volkswagen"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize Firebase
        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        ref = db.getReference("codigos_averia");
        mAuth = FirebaseAuth.getInstance();

        // Initialize welcome text with user info
        welcomeText = view.findViewById(R.id.welcome_text);
        setWelcomeMessage();

        // Find the container where we'll add RecyclerViews for each model
        LinearLayout modelsContainer = view.findViewById(R.id.models_container);

        // Create sections for our featured models
        for (String modelo : modelosDestacados) {
            createModelSection(modelsContainer, modelo);
            loadModelData(modelo);
        }

        return view;
    }

    private void setWelcomeMessage() {
        // Get time of day to customize welcome message
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);

        String greeting;
        if (hourOfDay < 12) {
            greeting = "Buenos días";
        } else if (hourOfDay < 20) {
            greeting = "Buenas tardes";
        } else {
            greeting = "Buenas noches";
        }

        // Get user email for personalized greeting
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "";
        String username = "";

        if (userEmail != null && !userEmail.isEmpty()) {
            username = userEmail.split("@")[0]; // Extract username from email
            username = username.substring(0, 1).toUpperCase() + username.substring(1); // Capitalize first letter
        }

        welcomeText.setText(greeting + ", " + username + "\nBienvenido a la App de Códigos OBD2");
    }

    private void createModelSection(LinearLayout container, String model) {
        // Inflate the model section layout
        View modelSection = getLayoutInflater().inflate(R.layout.model_section, container, false);

        // Set the model title
        TextView modelTitle = modelSection.findViewById(R.id.model_title);
        modelTitle.setText(model);

        // Add model image if available
        ImageView modelImage = modelSection.findViewById(R.id.model_image);
        int resourceId = getResources().getIdentifier(
                "logo_" + model.toLowerCase(), "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            modelImage.setImageResource(resourceId);
            modelImage.setVisibility(View.VISIBLE);
        } else {
            modelImage.setVisibility(View.GONE);
        }

        // Configure the RecyclerView for this model
        RecyclerView recyclerView = modelSection.findViewById(R.id.model_recycler_view);

        // Use Grid Layout with 2 columns
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // Create adapter for this model
        ModelAdapter adapter = new ModelAdapter();
        recyclerView.setAdapter(adapter);

        // Store references to recycler view and adapter
        modelRecyclerViews.put(model, recyclerView);
        modelAdapters.put(model, adapter);

        // Add "Ver Todos" button
        Button verTodosBtn = modelSection.findViewById(R.id.btn_ver_todos);
        verTodosBtn.setOnClickListener(v -> {
            // Navigate to CodigosFragment with model filter
            Bundle args = new Bundle();
            args.putString("modelo", model);

            CodigosFragment codigosFragment = new CodigosFragment();
            codigosFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, codigosFragment)
                    .addToBackStack(null)
                    .commit();
        });

        // Add the section to the container
        container.addView(modelSection);
    }

    private void loadModelData(String model) {
        // Query codes for this specific model - limit to 8 to show in grid (2x4)
        Query modelQuery = ref.orderByChild("modelo").equalTo(model).limitToFirst(8);

        modelQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<CodigoAveria> codigosList = new ArrayList<>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    CodigoAveria codigo = snapshot.getValue(CodigoAveria.class);
                    if (codigo != null) {
                        codigosList.add(codigo);
                    }
                }

                // If no data for this model, add placeholder
                if (codigosList.isEmpty()) {
                    // Check if we can add sample data
                    addSampleCodigosForModel(model, codigosList);
                }

                // Sort codes by code number
                Collections.sort(codigosList, (o1, o2) -> o1.getCodigo().compareTo(o2.getCodigo()));

                // Update the adapter with data
                if (modelAdapters.containsKey(model)) {
                    ModelAdapter adapter = modelAdapters.get(model);
                    adapter.setCodigos(codigosList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading data for model " + model + ": " + databaseError.getMessage());

                // If error, add sample data
                List<CodigoAveria> codigosList = new ArrayList<>();
                addSampleCodigosForModel(model, codigosList);

                // Update adapter with sample data
                if (modelAdapters.containsKey(model)) {
                    ModelAdapter adapter = modelAdapters.get(model);
                    adapter.setCodigos(codigosList);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    // Método para agregar códigos de muestra en caso de que no haya datos
    private void addSampleCodigosForModel(String model, List<CodigoAveria> codigosList) {
        Map<String, List<SampleCode>> sampleCodes = new HashMap<>();

        // Códigos de muestra para cada marca
        sampleCodes.put("Audi", Arrays.asList(
                new SampleCode("P0300", "Fallos de encendido múltiple"),
                new SampleCode("P0011", "Posición árbol de levas - Banco 1"),
                new SampleCode("P0171", "Sistema demasiado pobre - Banco 1"),
                new SampleCode("P2015", "Posición colector admisión"),
                new SampleCode("P0299", "Presión turbo baja"),
                new SampleCode("P0087", "Presión combustible baja"),
                new SampleCode("P0234", "Sobrealimentación turbo"),
                new SampleCode("P0303", "Fallo de encendido cilindro 3")
        ));

        sampleCodes.put("BMW", Arrays.asList(
                new SampleCode("P0128", "Temperatura refrigerante baja"),
                new SampleCode("P0174", "Sistema demasiado pobre - Banco 2"),
                new SampleCode("P0496", "Purga EVAP alta"),
                new SampleCode("P0444", "Circuito válvula EVAP"),
                new SampleCode("P0171", "Sistema demasiado pobre - Banco 1"),
                new SampleCode("P0340", "Sensor posición árbol de levas"),
                new SampleCode("P0335", "Sensor posición cigüeñal"),
                new SampleCode("P0420", "Eficiencia catalizador baja")
        ));

        sampleCodes.put("Mercedes", Arrays.asList(
                new SampleCode("P0170", "Ajuste combustible - Banco 1"),
                new SampleCode("P0300", "Fallos de encendido múltiple"),
                new SampleCode("P0446", "Sistema EVAP"),
                new SampleCode("P0410", "Sistema aire secundario"),
                new SampleCode("P0135", "Calentador sensor O2"),
                new SampleCode("P0715", "Sensor velocidad entrada"),
                new SampleCode("P0455", "Fuga sistema EVAP"),
                new SampleCode("P0442", "Fuga pequeña sistema EVAP")
        ));

        sampleCodes.put("Volkswagen", Arrays.asList(
                new SampleCode("P0411", "Flujo incorrecto aire secundario"),
                new SampleCode("P0171", "Sistema demasiado pobre - Banco 1"),
                new SampleCode("P0300", "Fallos de encendido múltiple"),
                new SampleCode("P0322", "Sin señal revoluciones motor"),
                new SampleCode("P0341", "Rango/rendimiento sensor árbol levas"),
                new SampleCode("P2015", "Posición colector admisión"),
                new SampleCode("P0299", "Presión turbo baja"),
                new SampleCode("P0605", "Error ROM módulo control")
        ));

        // Agregar códigos de muestra para el modelo actual
        if (sampleCodes.containsKey(model)) {
            for (SampleCode sampleCode : sampleCodes.get(model)) {
                CodigoAveria codigo = new CodigoAveria();
                codigo.setCodigo(sampleCode.code);
                codigo.setDescripcion(sampleCode.description);
                codigo.setModelo(model);
                codigosList.add(codigo);
            }
        }
    }

    // Clase auxiliar para los códigos de muestra
    private static class SampleCode {
        String code;
        String description;

        SampleCode(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }

    // Adapter for model-specific RecyclerViews
    private class ModelAdapter extends RecyclerView.Adapter<ModelAdapter.CodigoViewHolder> {
        private List<CodigoAveria> codigos = new ArrayList<>();

        public void setCodigos(List<CodigoAveria> codigos) {
            this.codigos = codigos;
        }

        @NonNull
        @Override
        public CodigoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.codigo_car_item, parent, false);
            return new CodigoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull CodigoViewHolder holder, int position) {
            CodigoAveria codigo = codigos.get(position);

            // Set código text with colored background based on tipo
            holder.codigoText.setText(codigo.getCodigo());

            // Color the code by type (P, B, C, U)
            if (codigo.getCodigo() != null && !codigo.getCodigo().isEmpty()) {
                char firstChar = codigo.getCodigo().charAt(0);
                int colorRes;

                switch (firstChar) {
                    case 'P':
                        colorRes = R.color.codigo_p_background;
                        break;
                    case 'B':
                        colorRes = R.color.codigo_b_background;
                        break;
                    case 'C':
                        colorRes = R.color.codigo_c_background;
                        break;
                    case 'U':
                        colorRes = R.color.codigo_u_background;
                        break;
                    default:
                        colorRes = R.color.codigo_default_background;
                }

                holder.codigoText.setBackgroundTintList(ColorStateList.valueOf(
                        ContextCompat.getColor(holder.itemView.getContext(), colorRes)));
            }

            holder.descripcionText.setText(codigo.getDescripcion());

            // Set click listener to show details
            holder.itemView.setOnClickListener(v -> mostrarDetalles(codigo));
        }

        @Override
        public int getItemCount() {
            return codigos.size();
        }

        class CodigoViewHolder extends RecyclerView.ViewHolder {
            TextView codigoText;
            TextView descripcionText;

            public CodigoViewHolder(@NonNull View itemView) {
                super(itemView);
                codigoText = itemView.findViewById(R.id.codigo_text);
                descripcionText = itemView.findViewById(R.id.descripcion_text);
            }
        }
    }

    @Override
    public void onClick(View v) {
        // Handle clicks on items in any recycler view
        for (Map.Entry<String, RecyclerView> entry : modelRecyclerViews.entrySet()) {
            RecyclerView recyclerView = entry.getValue();
            View child = recyclerView.findContainingItemView(v);
            if (child != null) {
                int position = recyclerView.getChildAdapterPosition(child);
                ModelAdapter adapter = modelAdapters.get(entry.getKey());
                if (position != RecyclerView.NO_POSITION) {
                    CodigoAveria codigo = adapter.codigos.get(position);
                    mostrarDetalles(codigo);
                    return;
                }
            }
        }
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
        DatabaseReference historialRef = db.getReference("historial");

        // Verificar si ya existe en el historial
        historialRef.orderByChild("codigo").equalTo(codigoAveria.getCodigo()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    // Si no existe, lo agregamos al historial
                    historialRef.push().setValue(codigoAveria);
                    Toast.makeText(getContext(), "Código añadido al historial: " + codigoAveria.getCodigo(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error al acceder al historial: " + databaseError.getMessage());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}