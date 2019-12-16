package com.javapoint.blogapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private Button registerButton;

    private FirebaseAuth mAuth;

    private ProgressBar progressCycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        emailInput = (EditText) findViewById(R.id.register_email);
        passwordInput = (EditText) findViewById(R.id.register_conform_password);
        loginButton = (Button) findViewById(R.id.login_button);
        registerButton = (Button) findViewById(R.id.reg_button);

        progressCycle = (ProgressBar) findViewById(R.id.progressBarLogin);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            sendToMain();
        }
    }
    public void onRegClick (View view) {
        Intent intentReg = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intentReg);
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void onClickLogin (View view) {

        String emailLogin = emailInput.getText().toString();
        String passwordLogin = passwordInput.getText().toString();

        if (!(TextUtils.isEmpty(emailLogin) && TextUtils.isEmpty(passwordLogin))) {

            progressCycle.setVisibility(View.VISIBLE);

            mAuth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        sendToMain();

                    } else {

                        String error = task.getException().getMessage();

                        Toast.makeText(LoginActivity.this, "ERROR: " + error, Toast.LENGTH_LONG).show();

                    }
                    progressCycle.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
