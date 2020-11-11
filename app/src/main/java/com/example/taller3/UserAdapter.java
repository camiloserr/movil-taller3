package com.example.taller3;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.taller3.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends BaseAdapter {

    private Context context;
    private List<User> users;
    private List<String> keys;
    public UserAdapter(Context context, List<User> users,List<String> keys) {
        this.context = context;
        this.users = users;
        this.keys = keys;
    }

    @Override
    public int getCount() {
        return users.size();
    }

    @Override
    public Object getItem(int i) {
        return users.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater)
                    context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            view = LayoutInflater.from(this.context).inflate(R.layout.user_info,viewGroup,false);
        }

        //Inflar y declarar
        TextView tvNobreUsuario;
        ImageView imagen;
        tvNobreUsuario = view.findViewById(R.id.tvNombreUserInfo);
        imagen = view.findViewById(R.id.ivUserInfo);


        //Asignar titulo
        tvNobreUsuario.setText(users.get(i).getName() + " " +users.get(i).getLastname());


        //Asignar foto

        /*****************Saca una imagen random, eso toca cambiarlo**********************/
        downloadFile(keys.get(i),imagen);
        /*********************************************************************************/
        return view;

    }

    private void downloadFile(String uid, final ImageView imagen) {

        StorageReference mStorageRef;
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://taller3-401c5.appspot.com");
        StorageReference imageRef = mStorageRef.child("users/"+uid+"/profile.jpg");
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.i("Imagen",uri.toString());
                Picasso.with(context).load(uri).transform(new CircleTransform()).into(imagen);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.i("Imagen", "No pude");
            }
        });
    }
}
