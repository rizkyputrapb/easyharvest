package com.example.easyharvest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    private FirebaseDatabase myDatabase;
    private DatabaseReference myRef;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

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
        String password = emailLogin.getText().toString().trim();

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

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(View.GONE);

                    final FirebaseUser user = mAuth.getCurrentUser();

                    if (user != null) {
                        myRef.addValueEventListener(new ValueEventListener() {

                            String loggedInSuccessfullyMessage = getResources().getString(R.string.logged_in_succesfully);


                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                User data = dataSnapshot.getValue(User.class);

                                if (data.getRole().equalsIgnoreCase("Farmer") ||
                                        data.getRole().equalsIgnoreCase("Petani")) {
                                    //intent ke halaman awal petani
                                    Toast.makeText(LoginActivity.this, loggedInSuccessfullyMessage, Toast.LENGTH_SHORT).show();
                                } else {
                                    //intent ke halaman awal petugas
                                    Toast.makeText(LoginActivity.this, loggedInSuccessfullyMessage, Toast.LENGTH_SHORT).show();
                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                } else {
                    String errorConnection = getResources().getString(R.string.please_check_your_connection);

                    progressBar.setVisibility(View.GONE);

                    Toast.makeText(LoginActivity.this, errorConnection, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
