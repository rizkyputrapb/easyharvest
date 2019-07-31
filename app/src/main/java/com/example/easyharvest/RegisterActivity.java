package com.example.easyharvest;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameRegister;
    private EditText emailRegister;
    private EditText passwordRegister;
    private Spinner spinnerRole;
    private Button buttonRegister;

    private String email;
    private String password;

    private ProgressBar progressBar;

    private User user;

    private FirebaseAuth mAuth;
    private FirebaseDatabase myDatabase;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        nameRegister = findViewById(R.id.edit_text_name_register);
        emailRegister = findViewById(R.id.editText_email_register);
        passwordRegister = findViewById(R.id.edit_text_password_register);
        spinnerRole = findViewById(R.id.spinner_role);
        buttonRegister = findViewById(R.id.button_register);

        progressBar = findViewById(R.id.registerProgressBar);

        setSpinner();

        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });
    }

    private void register() {
        email = emailRegister.getText().toString().trim();
        password = passwordRegister.getText().toString().trim();

        String name = nameRegister.getText().toString().trim();

        if (name.isEmpty()) {
            String emptyNameMessage = getResources().getString(R.string.please_enter_your_name_here);
            nameRegister.setError(emptyNameMessage);
            nameRegister.requestFocus();
            return;
        }

        if (email.isEmpty()) {
            String emptyEmailMessage = getResources().getString(R.string.email_is_required);
            emailRegister.setError(emptyEmailMessage);
            emailRegister.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            String invalidEmailMessage = getResources().getString(R.string.please_enter_the_email_correctly);
            emailRegister.setError(invalidEmailMessage);
            emailRegister.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            String emptyPasswordMessage = getResources().getString(R.string.password_is_required);
            passwordRegister.setError(emptyPasswordMessage);
            passwordRegister.requestFocus();
            return;
        }

        if (password.length() < 6) {
            String errorPasswordLength = getResources().getString(R.string.the_password_must_be_at_least_6_characters);
            passwordRegister.setError(errorPasswordLength);
            passwordRegister.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    String registeredSuccessfullyMessage = getResources().getString(R.string.user_registered_successfully);

                    progressBar.setVisibility(View.GONE);

                    saveDataUser();

                    Intent goToLoginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(goToLoginIntent);

                    Toast.makeText(RegisterActivity.this, registeredSuccessfullyMessage, Toast.LENGTH_SHORT).show();
                } else {
                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                        String alreadyRegisteredMessage = getResources().getString(R.string.user_already_registered);

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(RegisterActivity.this, alreadyRegisteredMessage, Toast.LENGTH_SHORT).show();
                    } else {
                        String errorConnectionMessage = getResources().getString(R.string.please_check_your_connection);

                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(RegisterActivity.this, errorConnectionMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    private void saveDataUser() {
        user = new User();
        user.setName(nameRegister.getText().toString());
        user.setEmail(emailRegister.getText().toString());
        user.setPassword(passwordRegister.getText().toString());
        user.setRole(spinnerRole.getSelectedItem().toString());

        email = emailRegister.getText().toString();
        password = passwordRegister.getText().toString();

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    FirebaseUser firebaseUser = mAuth.getCurrentUser();

                    if (firebaseUser != null) {
                        user.setId(firebaseUser.getUid());

                        String uid = firebaseUser.getUid();

                        myDatabase = FirebaseDatabase.getInstance();
                        myRef = myDatabase.getReference("users");
                        myRef.child(uid).setValue(user);
                    }
                }
            }
        });

    }

    private void setSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.roles
                , android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerRole.setAdapter(adapter);
    }
}
