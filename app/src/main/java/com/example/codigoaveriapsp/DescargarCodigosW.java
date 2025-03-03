package com.example.codigoaveriapsp;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

//Esta clase descargará codigos de averia desde Firebase
public class DescargarCodigosW extends Worker{

    private DatabaseReference ref;

    public DescargarCodigosW(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        //Estado: new
        ref = FirebaseDatabase.getInstance("https://codigosaveriatfg-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("codigos_averia");
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            //Estado: runnable
            Log.d("WorkManager", "Iniciando descarga de códigos de avería...");

            //Simula que obtiene datos de Firebase
            ref.get().addOnCompleteListener(tarea -> {
                if (tarea.isSuccessful()) {
                    DataSnapshot captura = tarea.getResult();
                    for (DataSnapshot datos : captura.getChildren()) {
                        String codigo = datos.child("codigo").getValue(String.class);
                        Log.d("WorkManager", "Código descargado: " + codigo);
                    }
                } else {
                    Log.e("WorkManager", "Error descargando códigos: " + tarea.getException());
                }
            });

            //Estado: Terminated
            return Result.success();

        } catch (Exception e) {
            //Estado: Blocked
            Log.e("WorkManager", "Error en la descarga", e);
            return Result.retry(); //Estado: Waiting
        }
    }
}