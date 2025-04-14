package com.example.codigoaveriapsp;

import android.annotation.SuppressLint;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;


public class FirebaseAdaptador extends FirebaseRecyclerAdapter<CodigoAveria, FirebaseAdaptador.MiContenedor> {
    ///Variables
    private View.OnClickListener escuchador; // Listener para clics en elementos
    private int posicionSeleccionada;

    public FirebaseAdaptador(@NonNull FirebaseRecyclerOptions<CodigoAveria> options, View.OnClickListener escuchador) {
        super(options);
        this.escuchador = escuchador;
    }
    ///Posicion para la edicion/eliminacion
    public void setPosicionSeleccionada(int posicion) {
        this.posicionSeleccionada = posicion;
    }

    public int getPosicionSeleccionada() {
        return posicionSeleccionada;
    }

    ///Configurar adaptador
    @Override
    protected void onBindViewHolder(@NonNull MiContenedor holder, @SuppressLint("RecyclerView") int position, @NonNull CodigoAveria modelo) {
        holder.tvCodigo.setText(modelo.getCodigo());
        holder.tvDescripcion.setText("Descripción: " + modelo.getDescripcion());
        holder.tvSolucion.setText("Solución: " + modelo.getSolucion());

        //Guardamos la posición en el ViewHolder | No usar deprecated
        //holder.position = position;

        //Asignar el listener de clics a cada elemento
        holder.itemView.setOnClickListener(v -> escuchador.onClick(v));

        //Configurar el listener para el menú contextual
        holder.itemView.setOnLongClickListener(v -> {
            setPosicionSeleccionada(holder.getBindingAdapterPosition());
            return false;
        });
    }

    @NonNull
    @Override
    public MiContenedor onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new MiContenedor(vista);
    }
    ///ViewHolder para reciclar vista
    public static class MiContenedor extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
        TextView tvCodigo, tvDescripcion, tvSolucion;
        int position;

        public MiContenedor(View itemView) {
            super(itemView);
            tvCodigo = itemView.findViewById(R.id.codigo);
            tvDescripcion = itemView.findViewById(R.id.descripcion);
            tvSolucion = itemView.findViewById(R.id.solucion);

            // Asociar el menú contextual al ViewHolder
            itemView.setOnCreateContextMenuListener(this);
        }
        ///infla menu contextual
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            MenuInflater inflater = new MenuInflater(view.getContext());
            inflater.inflate(R.menu.menu_contextual, contextMenu);
        }


    }
}


