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
    private TextView bienvenidatxt;
    private FirebaseAuth mAuth;
    private Map<String, RecyclerView> modelRecyclerViews = new HashMap<>();
    private Map<String, ModeloAdapter> modelAdapters = new HashMap<>();
    private static final String TAG = "HomeFragment";

    //marcas que tengo en FireBase
    private final String[] modelosDestacados = {"Ford", "Opel", "Citroën", "Mercedes"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        ref = db.getReference("codigos_averia");
        mAuth = FirebaseAuth.getInstance();

        bienvenidatxt = view.findViewById(R.id.welcome_text);
        setBienvenida();

        LinearLayout modelsContainer = view.findViewById(R.id.models_container);

        for (String modelo : modelosDestacados) {
            seccionModelo(modelsContainer, modelo);
            cargarDatos(modelo);
        }

        return view;
    }

    private void setBienvenida() {
        Calendar calendar = Calendar.getInstance();
        int hora = calendar.get(Calendar.HOUR_OF_DAY);

        String saludo;
        if (hora < 12) {
            saludo = "Buenos días";
        } else if (hora < 20) {
            saludo = "Buenas tardes";
        } else {
            saludo = "Buenas noches";
        }

        FirebaseUser usuarioActual = mAuth.getCurrentUser();
        String userEmail = usuarioActual != null ? usuarioActual.getEmail() : "";
        String username = "";

        if (userEmail != null && !userEmail.isEmpty()) {
            username = userEmail.split("@")[0];
            username = username.substring(0, 1).toUpperCase() + username.substring(1);
        }

        bienvenidatxt.setText(saludo + ", " + username + "\nBienvenido a la App de Códigos OBD2");
    }

    private void seccionModelo(LinearLayout container, String model) {
        View seccionModelo = getLayoutInflater().inflate(R.layout.model_section, container, false);

        TextView tituloModelo = seccionModelo.findViewById(R.id.model_title);
        tituloModelo.setText(model);

        ImageView imagenModelo = seccionModelo.findViewById(R.id.model_image);
        int resourceId = getResources().getIdentifier(
                "logo_" + model.toLowerCase(), "drawable", requireActivity().getPackageName());

        if (resourceId != 0) {
            imagenModelo.setImageResource(resourceId);
            imagenModelo.setVisibility(View.VISIBLE);
        } else {
            imagenModelo.setVisibility(View.GONE);
        }

        RecyclerView recyclerView = seccionModelo.findViewById(R.id.model_recycler_view);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        ModeloAdapter adapter = new ModeloAdapter();
        recyclerView.setAdapter(adapter);

        modelRecyclerViews.put(model, recyclerView);
        modelAdapters.put(model, adapter);

        Button verTodosBtn = seccionModelo.findViewById(R.id.btn_ver_todos);
        verTodosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putString("marca", model);

                CodigosFragment codigosFragment = new CodigosFragment();
                codigosFragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, codigosFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        container.addView(seccionModelo);
    }

    private void cargarDatos(String model) {
    //Filtrar por marcar
        Query querymod = ref.orderByChild("marca").equalTo(model).limitToFirst(8);

        querymod.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    ModeloAdapter adapter = modelAdapters.get(model);
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

    private class ModeloAdapter extends RecyclerView.Adapter<ModeloAdapter.CodigoViewHolder> {
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
                        colorRes = R.color.codigo_p;
                        break;
                    case 'B':
                        colorRes = R.color.codigo_b;
                        break;
                    case 'C':
                        colorRes = R.color.codigo_c;
                        break;
                    case 'U':
                        colorRes = R.color.codigo_u;
                        break;
                    default:
                        colorRes = R.color.codigo_defecto;
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
