package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.databinding.ActivityLoginBinding;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding loginBinding;
    FirebaseAuth auth=FirebaseAuth.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loginBinding =ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(loginBinding.getRoot());

        FirebaseUser user=auth.getCurrentUser();
        if(user !=null){

            Intent intent=new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        }

        loginBinding.buttonLogin.setOnClickListener(v->{
            String email=loginBinding.editTextEmailLogin.getText().toString().trim();
            String password=loginBinding.editTextPasswordLogin.getText().toString().trim();

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Please Enter your email and password", Toast.LENGTH_SHORT).show();
            }
            else {
                auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task->{
                   if(task.isSuccessful()){
                       Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                       startActivity(intent);
                       finish();
                   }
                   else{

                   }
                    Toast.makeText(this,task.getException().getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                });

            }
        });

        loginBinding.textViewSignUp.setOnClickListener(v->{
                Intent intent=new Intent (this,SignupActivity.class);
                startActivity(intent);
        });

    }
}