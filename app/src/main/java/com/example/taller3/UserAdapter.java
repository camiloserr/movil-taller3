package com.example.taller3;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.example.taller3.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class UserAdapter extends BaseAdapter {

    private Context context;
    private List<User> users;

    public UserAdapter(Context context, List<User> users) {
        this.context = context;
        this.users = users;
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
        String imageUri = "https://picsum.photos/200?random=" + i;
        Picasso.with(context).load(imageUri).transform(new CircleTransform()).into(imagen);
        /*********************************************************************************/

        return view;

    }
}
