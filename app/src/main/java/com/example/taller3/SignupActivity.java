package com.example.taller3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText etName, etLastName, etEmail, etPass1, etPass2, etID;
    private Button btnRegister;
    private FirebaseAuth mAuth;
    private static final String TAG = "Signup";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etName = findViewById(R.id.editTextTextPersonLastName);
        etLastName = findViewById(R.id.editTextTextPersonLastName);
        etEmail = findViewById(R.id.editTextTextEmailAddressRegister);
        etPass1 = findViewById(R.id.editTextTextPasswordRegister);
        etPass2 = findViewById(R.id.editTextTextConfirm);
        etID = findViewById(R.id.editTextIDRegisterID);
        btnRegister = findViewById(R.id.buttonSignUp);

        mAuth = FirebaseAuth.getInstance();

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


    private void updateUI(FirebaseUser user) {
        if(user!=null) {
            Intent i = new Intent(SignupActivity.this, MenuActivity.class);
            startActivity(i);
        }
    }
}