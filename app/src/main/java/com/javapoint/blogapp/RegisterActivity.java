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

public class RegisterActivity extends AppCompatActivity {

   private EditText email_registor;
   private EditText register_new_password;
   private EditText register_conform_password;
   private Button create_button;
   private Button already_button;

   private FirebaseAuth mAuth;

   private ProgressBar regCycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        email_registor = (EditText) findViewById(R.id.register_email);
        register_new_password = (EditText) findViewById(R.id.regiser_password);
        register_conform_password = (EditText) findViewById(R.id.register_conform_password);
        create_button = (Button) findViewById(R.id.reg_button);
        already_button = (Button) findViewById(R.id.registered_button);

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {

            sendToMain();

        }
    }

    public void alreadyClick (View view) {

        finish();

    }

    private void sendToMain() {

        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    public void onClickCreate(View view) {

        String createEmail = email_registor.getText().toString();
        String createPassword = register_new_password.getText().toString();
        String createConformPassword = register_conform_password.getText().toString();

        regCycle = (ProgressBar) findViewById(R.id.progressBarReg);

        if (!TextUtils.isEmpty(createEmail) && !TextUtils.isEmpty(createPassword) & !TextUtils.isEmpty(createConformPassword)) {

            if (createPassword.equals(createConformPassword)) {

                regCycle.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(createEmail, createPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {

                            Intent setupIntent = new Intent(RegisterActivity.this, SetupActivity.class);
                            startActivity(setupIntent);
                            finish();

                        } else {

                            String errorMessage = task.getException().getMessage();
                            Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();

                        }

                    regCycle.setVisibility(View.INVISIBLE);
                    }
                });

            } else {

                Toast.makeText(RegisterActivity.this, "The two passwords doesn't match each other.", Toast.LENGTH_SHORT).show();

            }

        }
    }
}
