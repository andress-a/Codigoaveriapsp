package com.example.codigoaveriapsp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddCodigo extends AppCompatActivity {

    private EditText etCodigo, etDescripcion, etMarca, etModelo, etSolucion;
    private Button btnGuardar;
    FirebaseDatabase db;
    DatabaseReference ref;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addcodigo);

        etCodigo = findViewById(R.id.edtCodigo);
        etDescripcion = findViewById(R.id.edtDescripcion);
        etMarca = findViewById(R.id.edtMarca);
        etModelo = findViewById(R.id.edtModelo);
        etSolucion = findViewById(R.id.edtSolucion);
        btnGuardar = findViewById(R.id.btnAgregar);

        //Referencia a Firebase
        db = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app");
        ref = db.getReference("codigos_averia");

    }

    public void clickCancelar(View view) {
            setResult(RESULT_CANCELED); // Indica que no se agregó nada
            finish();
    }
    public void clickAdd(View view) {
        guardarCodigo();
    }
    private void guardarCodigo() {
        String codigo = etCodigo.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String marca = etMarca.getText().toString().trim();
        String modelo = etModelo.getText().toString().trim();
        String solucion = etSolucion.getText().toString().trim();

        if (codigo.isEmpty() || descripcion.isEmpty() || marca.isEmpty() || modelo.isEmpty()  || solucion.isEmpty()) {
            Toast.makeText(this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        //Crear objeto y guardar en Firebase
        String id = ref.push().getKey();  //Genera un ID único
        CodigoAveria nuevoCodigo = new CodigoAveria(codigo, descripcion, marca, modelo, solucion);

        if (id != null) {
            ref.child(id).setValue(nuevoCodigo)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(AddCodigo.this, "Código agregado", Toast.LENGTH_SHORT).show();
                        //Indicar que hubo un cambio y notificar a MainActivity
                        setResult(RESULT_OK);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(AddCodigo.this, "Error al guardar", Toast.LENGTH_SHORT).show());
        }
    }
}

