package com.example.taller3;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.example.taller3.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MenuActivity extends AppCompatActivity {

    private Button logout;
    private FirebaseAuth mAuth;
    private Switch switchAB;
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final static String TAG = "menu activity";
    private DatabaseReference ref;
    private User myUser;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        logout = findViewById(R.id.buttonLogOutMenu);
        mAuth = FirebaseAuth.getInstance();

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                updateUI(null);
            }
        });


        ref = database.getReference("users").child(mAuth.getCurrentUser().getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                Log.i(TAG, "onDataChange: " + u.getName() + " " + u.getLastname());
                myUser = u;
                updateSwitch();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        });

    }

    private void updateSwitch() {
        switchAB.setChecked(myUser.isAvailible());
    }


    private void updateUI(FirebaseUser user) {

        if(user == null){
            Intent i = new Intent(MenuActivity.this , MainActivity.class);
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
            Intent intent = new Intent(MenuActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (itemClicked == R.id.menuItemUsers){
            Toast.makeText(getApplicationContext() , "active Users!!" , Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }
}