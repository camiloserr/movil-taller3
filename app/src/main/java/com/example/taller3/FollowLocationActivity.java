package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.taller3.model.User;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.taller3.model.Interes;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FollowLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final int FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    private GoogleMap mMap;
    private List<Interes> intereses;
    private LatLng currentLocation;
    private Geocoder mGeocoder;
    private Marker myMarker;

    private Button logout;
    private FirebaseAuth mAuth;
    private Switch switchAB;
    private FirebaseDatabase database;
    private final static String TAG = "menu activity";
    private DatabaseReference ref;
    private User myUser;
    private Toolbar mToolbar;
    private boolean firstTime = true;
    private String userID;
    private Marker marker;

    private static final String PATH_CURRENT_LOCATION = "currentLocation/";
    private static final String PATH_LOCATION = "locationsArray/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        intereses = new ArrayList<>();
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        updateUI(mAuth.getCurrentUser());
        setContentView(R.layout.activity_intereses_map);
        database = FirebaseDatabase.getInstance();
        mGeocoder = new Geocoder(getBaseContext());

        userID = getIntent().getStringExtra("uID");

        follow();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();
        upDateCurrentPosition();
        solicitarPermiso(this, Manifest.permission.ACCESS_FINE_LOCATION, "Necesito permiso para localización", FINE_LOCATION);
        usarPermiso();
        listenForChanges();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        solicitarPermiso(this, Manifest.permission.ACCESS_FINE_LOCATION, "Necesito permiso para localización", FINE_LOCATION);

        // Debo usar permiso en este callback
        solicitarPermiso(this, Manifest.permission.ACCESS_FINE_LOCATION, "Necesito permiso para localización", FINE_LOCATION);
        usarPermiso();
        upDateCurrentPosition();


    }
    public void pintarUsuario(User u){//Creo los markers y los pongo en el mapa
        Log.i("PintarCositos",""+intereses.size());
        LatLng ll = new LatLng(u.getLat(),u.getLon());
        if(marker != null){
            marker.remove();
        }
        marker = mMap.addMarker(new MarkerOptions().position(ll).title(u.getName() + " " + u.getLastname()).alpha(0.8f).
                icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
    }


    public void follow(){
        DatabaseReference refDB = database.getReference("users").child(userID);
        refDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                pintarUsuario(u);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        });

    }

    private void solicitarPermiso(Activity context, String permiso, String justificacion, int idPermiso) {
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permiso}, idPermiso);
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permiso)) {
                Toast.makeText(this, justificacion, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
        }
    }
    protected LocationRequest createLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); //tasa de refresco en milisegundos
        locationRequest.setFastestInterval(5000); //máxima tasa de refresco
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    public void upDateCurrentPosition(){
        mLocationCallback = new LocationCallback(){
            public void onLocationResult(LocationResult locationResult){
                Location location = locationResult.getLastLocation();
                if(location != null){
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if(mMap != null){
                        if(myMarker!=null){
                            myMarker.remove();
                        }
                        updatePositionFirebase(location.getLatitude(),location.getLongitude());
                        myMarker = mMap.addMarker(new MarkerOptions().position(currentLocation).title(geoCoderSearch(currentLocation)).snippet("Ubicación Actual").alpha(0.8f)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

                        if(firstTime) {
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                            firstTime = false;
                        }
                    }
                }
            }
        };
    }

    private void updatePositionFirebase(final Double latitude, final Double longitud){
        ref = database.getReference("users").child(mAuth.getCurrentUser().getUid());
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                if(u != null && u.isAvailible()) {
                    myUser = u;
                    myUser.setLon(longitud);
                    myUser.setLat(latitude);
                    ref.setValue(myUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        });
    }
    private String geoCoderSearch(LatLng latlng){
        String address = "";
        try{
            List<Address> res = mGeocoder.getFromLocation(latlng.latitude, latlng.longitude, 2);
            if(res != null && res.size() > 0){
                address = res.get(0).getAddressLine(0);
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return address;
    }

    public void usarPermiso(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            checkSettings();
        }
    }
    public void checkSettings(){
        LocationSettingsRequest.Builder builder = new
                LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates(); //Todas las condiciones para recibir localizaciones
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode){
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try{
                            ResolvableApiException resolvable = (ResolvableApiException) e;
                            resolvable.startResolutionForResult(FollowLocationActivity.this,REQUEST_CHECK_SETTINGS);
                        }catch (IntentSender.SendIntentException sendEx){
                            Log.e("Actividad Mapis: ",sendEx.getMessage());
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                if (resultCode == RESULT_OK) {
                    startLocationUpdates(); //Se encendió la localización!!!
                } else {
                    Toast.makeText(this,
                            "Sin acceso a localización, hardware deshabilitado!",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case FINE_LOCATION:{
                usarPermiso();
            }
        }
    }@Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }
    private void updateSwitch() {
        if(switchAB != null)
            switchAB.setChecked(myUser.isAvailible());
    }


    private void updateUI(FirebaseUser user) {

        if(user == null){
            Intent i = new Intent(FollowLocationActivity.this , MainActivity.class);
            startActivity(i);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        MenuItem switchOnOffItem = menu.findItem(R.id.menuItemAvailible);
        switchOnOffItem.setActionView(R.layout.switch_layout);

        switchAB = switchOnOffItem.getActionView().findViewById(R.id.mySwitch);
        switchAB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                updateDB(isChecked);
            }
        });

        return true;
    }

    private void updateDB(boolean isChecked) {
        ref.child("availible").setValue(isChecked);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemClicked = item.getItemId();
        if(itemClicked == R.id.menuItemLogout){
            mAuth.signOut();
            Intent intent = new Intent(FollowLocationActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (itemClicked == R.id.menuItemUsers){
            Intent i = new Intent(FollowLocationActivity.this, AvailibleUsersActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }
    private void listenForChanges(){
        ref = database.getReference("users").child(mAuth.getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                if(u != null) {
                    myUser = u;
                    updateSwitch();
                }
                else{
                    Log.i(TAG, "onDataChange: U is null");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        });
    }
}