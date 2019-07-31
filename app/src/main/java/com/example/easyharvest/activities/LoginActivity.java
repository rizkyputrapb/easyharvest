package com.example.easyharvest.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.easyharvest.R;
import com.example.easyharvest.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText emailLogin;
    private EditText passwordLogin;
    private Button buttonLogin;
    private TextView tvRegister;
    private TextView tvForgotPassword;

    private FirebaseAuth mAuth;

    private ProgressBar progressBar;
    private FirebaseDatabase myDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        myDatabase = FirebaseDatabase.getInstance();

        emailLogin = findViewById(R.id.edit_text_email_login);
        passwordLogin = findViewById(R.id.edit_text_password_login);
        buttonLogin = findViewById(R.id.button_login);
        tvRegister = findViewById(R.id.text_view_register);
        tvForgotPassword = findViewById(R.id.text_view_forgot_password);
        progressBar = findViewById(R.id.loginProgressBar);

        buttonLogin.setOnClickListener(this);
        tvRegister.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_login:
                login();
                break;
            case R.id.text_view_register:
                Intent registerIntent = new Intent(this, RegisterActivity.class);
                startActivity(registerIntent);
                break;
            case R.id.text_view_forgot_password:
                break;
        }
    }

    private void login() {
        String email = emailLogin.getText().toString().trim();
        String password = passwordLogin.getText().toString().trim();

        if (email.isEmpty()) {
            String emptyEmailMessage = getResources().getString(R.string.email_is_required);
            emailLogin.setError(emptyEmailMessage);
            emailLogin.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            String invalidEmailMessage = getResources().getString(R.string.please_enter_the_email_correctly);
            emailLogin.setError(invalidEmailMessage);
            emailLogin.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            String emptyPasswordMessage = getResources().getString(R.string.password_is_required);
            passwordLogin.setError(emptyPasswordMessage);
            passwordLogin.requestFocus();
            return;
        }

        if (password.length() < 6) {
            String errorPasswordLength = getResources().getString(R.string.the_password_must_be_at_least_6_characters);
            passwordLogin.setError(errorPasswordLength);
            passwordLogin.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();

                            if (user != null) {
                                updateUI(user);
                            }

                        } else {
                            String errorConnection = getResources().getString(R.string.please_check_your_connection);

                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(LoginActivity.this, errorConnection, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser firebaseUser) {

        if (firebaseUser != null) {
            DatabaseReference myRef = myDatabase.getReference("users").child(firebaseUser.getUid());

            ValueEventListener listener = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    if (user != null) {
                        if (user.getRole().equalsIgnoreCase("Farmer")) {
                            progressBar.setVisibility(View.GONE);
                            //intent to homescreen as farmer
                            Log.d(LoginActivity.class.getSimpleName(), "Login as farmer");
                        } else {
                            progressBar.setVisibility(View.GONE);
                            //intent to homescreen as officer
                            Log.d(LoginActivity.class.getSimpleName(), "Login as officer");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            };

            myRef.addListenerForSingleValueEvent(listener);
        }
    }
}
