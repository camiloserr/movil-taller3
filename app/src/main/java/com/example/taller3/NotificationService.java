package com.example.taller3;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.taller3.model.User;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class NotificationService extends IntentService {

    private static final String TAG = "Servicio";
    private static final String CHANNEL_ID = "servicio_notificaciones_taller_3" ;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref;
    private Map<String, Boolean> mapa = new HashMap<>();
    private int notificationId = 001;
    private String myKey;


    public NotificationService() {
        super("Servicio de notificaciones");
        Log.i(TAG, "Constructor del servicio ");

    }


    public void start() {
        Log.i(TAG, "en el start ");

        ref = database.getReference("users");

        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String prevChildKey) {

                User u = dataSnapshot.getValue(User.class);
                Log.i(TAG, "adding " + u.getName() + " to hashmap");
                mapa.put(dataSnapshot.getKey(), u.isAvailible());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String prevChildKey) {
                User changedUser = dataSnapshot.getValue(User.class);
                Log.i(TAG, "onChildChanged: ");
                if(!mapa.get(dataSnapshot.getKey()) && changedUser.isAvailible() && (!dataSnapshot.getKey().equals(myKey))) {
                    // muestra notificacion
                    Log.i(TAG, "Mostrar notificacion de " + changedUser.getName());

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID);
                    mBuilder.setSmallIcon(R.drawable.icon);
                    mBuilder.setContentTitle("Alguien está disponible");
                    mBuilder.setContentText(changedUser.getName() + " "+changedUser.getLastname() + " está disponible! ");
                    mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);


                    //TODO: llevar a la actividad de seguimiento
                    Intent intent = new Intent(getApplicationContext(), AvailibleUsersActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
                    mBuilder.setContentIntent(pendingIntent);
                    mBuilder.setAutoCancel(true); //Remueve la notificación cuando se toca

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
                    // notificationId es un entero unico definido para cada notificacion que se lanza
                    notificationManager.notify(notificationId, mBuilder.build());
                    notificationId++;
                }
                mapa.put(dataSnapshot.getKey(), changedUser.isAvailible());

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.i(TAG, "onChildRemoved: ");
            }
            

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String prevChildKey) {
                Log.i(TAG, "onChildMoved: ");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "onCancelled: ");
            }
        });
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "channel";
            String description = "channel description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            //IMPORTANCE_MAX MUESTRA LA NOTIFICACIÓN ANIMADA
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    @Override
    protected void onHandleIntent(@NonNull Intent intent) {
        myKey = intent.getStringExtra("myKey");
        createNotificationChannel();
        start();
    }
}
