package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.taller3.model.User;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etLastName, etEmail, etPass1, etPass2, etID;
    private Button btnRegister, btnAlmacenamiento;
    private ImageView imagePicked;
    private FirebaseAuth mAuth;
    private static final String TAG = "Signup";
    private LatLng currentLocation = new LatLng(4,-72);
    private static int ALMACENAMIENTO = 2;
    private static final int IMAGE_PICKER_REQUEST = 3;
    private StorageReference mStorageRef;
    private Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.editTextTextPersonName);
        etLastName = findViewById(R.id.editTextTextPersonLastName);
        etEmail = findViewById(R.id.editTextTextEmailAddressRegister);
        etPass1 = findViewById(R.id.editTextTextPasswordRegister);
        etPass2 = findViewById(R.id.editTextTextConfirm);
        etID = findViewById(R.id.editTextIDRegisterID);
        btnRegister = findViewById(R.id.buttonSignUp);
        btnAlmacenamiento = findViewById(R.id.imgSignUpBtn);
        imagePicked = findViewById(R.id.imgPickSignUp);
        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = etEmail.getText().toString();
                String password1 = etPass1.getText().toString();
                String password2 = etPass2.getText().toString();
                if(!password1.equals(password2)){
                    Toast.makeText(getApplicationContext(), "Las contrasenas no coinciden", Toast.LENGTH_SHORT).show();
                }
                attemptSignUp(email, password1);
            }
        });

        btnAlmacenamiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solicitarPermiso((Activity) v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE,"Necesito permisos de almacenamiento",ALMACENAMIENTO);
                usarPermiso((Activity) v.getContext(), Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        });


    }

    private void attemptSignUp(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                            DatabaseReference myRef = database.getReference("users");

                            // TODO: agregar la foto a storage y cambiar currentLocation
                            User u = new User(etID.getText().toString() , 4 ,
                                    -72, false,
                                    etName.getText().toString() , etLastName.getText().toString());
                            myRef.child(user.getUid()).setValue(u);
                            uploadImage(imageUri, user.getUid());
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SignupActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // ...
                    }

                });
    }

    private void uploadImage(Uri uri, String uid){
        if(uri != null){
         StorageReference profileRef = mStorageRef.child("users/"+uid+"/profile.jpg");
         profileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 Log.i("SigUp","Se cargo la imagen");
             }
         }).addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 Log.i("SigUp","No se cargo la imagen");
             }
         });

        }
    }
    private void updateUI(FirebaseUser user) {

        if(user!=null) {
            Intent i = new Intent(SignupActivity.this, InteresesMapActivity.class);
            startActivity(i);
        }
    }
    private void solicitarPermiso(Activity context, String permiso, String justificacion, int idPermiso){
        if (ContextCompat.checkSelfPermission(context, permiso) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permiso}, idPermiso);
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permiso)) {
                Toast.makeText(this, justificacion, Toast.LENGTH_LONG).show();
            }
        }
    }
    private  void usarPermiso(Activity context, String permiso){

        if (ContextCompat.checkSelfPermission(this, permiso) == PackageManager.PERMISSION_GRANTED) {
            if(permiso==Manifest.permission.READ_EXTERNAL_STORAGE){
                Intent pickImage = new Intent(Intent.ACTION_PICK);
                pickImage.setType("image/*");
                startActivityForResult(pickImage, IMAGE_PICKER_REQUEST);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case IMAGE_PICKER_REQUEST:
                if(resultCode == RESULT_OK){
                    imageUri = data.getData();
                    imagePicked.setImageURI(imageUri);
                }
                break;
        }
    }
}