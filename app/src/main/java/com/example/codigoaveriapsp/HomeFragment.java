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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private FirebaseDatabase db;
    private DatabaseReference ref;
    private TextView welcomeText;
    private FirebaseAuth mAuth;
    private Map<String, RecyclerView> modelRecyclerViews = new HashMap<>();
    private Map<String, ModelAdapter> modelAdapters = new HashMap<>();
    private static final String TAG = "HomeFragment";

    // Marcas destacadas que sí existen en Firebase
    private final String[] modelosDestacados = {"Ford", "Opel", "Citroën", "Mercedes"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        ref = db.getReference("codigos_averia");
        mAuth = FirebaseAuth.getInstance();

        welcomeText = view.findViewById(R.id.welcome_text);
        setWelcomeMessage();

        LinearLayout modelsContainer = view.findViewById(R.id.models_container);

        for (String modelo : modelosDestacados) {
            createModelSection(modelsContainer, modelo);
            loadModelData(modelo);
        }

        return view;
    }

    private void setWelcomeMessage() {
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

        FirebaseUser currentUser = mAuth.getCurrentUser();
        String userEmail = currentUser != null ? currentUser.getEmail() : "";
        String username = "";

        if (userEmail != null && !userEmail.isEmpty()) {
            username = userEmail.split("@")[0];
            username = username.substring(0, 1).toUpperCase() + username.substring(1);
        }

        welcomeText.setText(greeting + ", " + username + "\nBienvenido a la App de Códigos OBD2");
    }

    private void createModelSection(LinearLayout container, String model) {
        View modelSection = getLayoutInflater().inflate(R.layout.model_section, container, false);

        TextView modelTitle = modelSection.findViewById(R.id.model_title);
        modelTitle.setText(model);

        ImageView modelImage = modelSection.findViewById(R.id.model_image);
        int resourceId = getResources().getIdentifier(
                "logo_" + model.toLowerCase(), "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            modelImage.setImageResource(resourceId);
            modelImage.setVisibility(View.VISIBLE);
        } else {
            modelImage.setVisibility(View.GONE);
        }

        RecyclerView recyclerView = modelSection.findViewById(R.id.model_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        ModelAdapter adapter = new ModelAdapter();
        recyclerView.setAdapter(adapter);

        modelRecyclerViews.put(model, recyclerView);
        modelAdapters.put(model, adapter);

        Button verTodosBtn = modelSection.findViewById(R.id.btn_ver_todos);
        verTodosBtn.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("marca", model);

            CodigosFragment codigosFragment = new CodigosFragment();
            codigosFragment.setArguments(args);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, codigosFragment)
                    .addToBackStack(null)
                    .commit();
        });

        container.addView(modelSection);
    }

    private void loadModelData(String model) {
        // Filtrar por "marca" correctamente
        Query modelQuery = ref.orderByChild("marca").equalTo(model).limitToFirst(8);

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

                // Actualizar adapter solo si hay datos
                if (modelAdapters.containsKey(model)) {
                    ModelAdapter adapter = modelAdapters.get(model);
                    adapter.setCodigos(codigosList);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error loading data for model " + model + ": " + databaseError.getMessage());
            }
        });
    }

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

            holder.codigoText.setText(codigo.getCodigo());

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

    private void mostrarDetalles(CodigoAveria codigoAveria) {
        DetallesFragment detallesFragment = DetallesFragment.newInstance(codigoAveria);
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detallesFragment)
                .addToBackStack(null)
                .commit();
    }
}
