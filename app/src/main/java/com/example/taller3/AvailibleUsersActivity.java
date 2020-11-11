package com.example.taller3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
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
import com.google.firebase.internal.InternalTokenProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AvailibleUsersActivity extends AppCompatActivity {

    private ListView lvUsers;
    private UserAdapter userAdapter;
    private List<User> availibleUsers;
    private List<String> keysUsers;
    private final static String  TAG = "AvailibleUsersAcativity";
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private FirebaseAuth mAuth;
    private Switch switchAB;
    private DatabaseReference refUser;
    private String logedUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availible_users);



        lvUsers = findViewById(R.id.listViewUsers);

        mAuth = FirebaseAuth.getInstance();

        availibleUsers  = new ArrayList<>();
        keysUsers = new ArrayList<>();
        userAdapter = new UserAdapter(this, availibleUsers,keysUsers);
        lvUsers.setAdapter(userAdapter);


        DatabaseReference refDB = database.getReference("users");


        refDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                availibleUsers = new ArrayList<>();
                keysUsers = new ArrayList<>();
                for (DataSnapshot userSnapshot: dataSnapshot.getChildren()) {
                    User u = userSnapshot.getValue(User.class);
                    Log.i(TAG, "onDataChange: " + u.getName() + " " + userSnapshot.getKey());
                    // si el usuario esta disponible y es diferente al usuario logeado
                    if(u.isAvailible() && (!userSnapshot.getKey().equals(logedUserID))){
                        availibleUsers.add(u);
                        keysUsers.add(userSnapshot.getKey());
                    }

                }
                userAdapter = new UserAdapter(getApplicationContext(), availibleUsers,keysUsers);
                lvUsers.setAdapter(userAdapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i(TAG, "The read failed: " + databaseError.getCode());
            }
        });

        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                User u = availibleUsers.get(i);
                Log.i(TAG, "onItemClick: " + u.getName());
                Intent followIntetn = new Intent(AvailibleUsersActivity.this, FollowLocationActivity.class);
                followIntetn.putExtra("uID" , keysUsers.get(i));
                startActivity(followIntetn);
            }
        });
        listenForChanges();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        logedUserID = currentUser.getUid();
        updateUI(currentUser);
        listenForChanges();

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
        refUser.child("availible").setValue(isChecked);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int itemClicked = item.getItemId();
        if(itemClicked == R.id.menuItemLogout){
            mAuth.signOut();
            Intent intent = new Intent(AvailibleUsersActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }else if (itemClicked == R.id.menuItemUsers){
            Toast.makeText(getApplicationContext() , "active Users!!" , Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void listenForChanges(){
        refUser = database.getReference("users").child(mAuth.getCurrentUser().getUid());

        refUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User u = dataSnapshot.getValue(User.class);
                Log.i(TAG, "onDataChange: " + u.getName());
                if(u != null) {
                    updateSwitch(u);
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

    private void updateSwitch(User u) {
        if(switchAB != null)
            switchAB.setChecked(u.isAvailible());
    }


    private void updateUI(FirebaseUser user) {

        if(user == null){
            Intent i = new Intent(AvailibleUsersActivity.this , MainActivity.class);
            startActivity(i);
        }
    }
}