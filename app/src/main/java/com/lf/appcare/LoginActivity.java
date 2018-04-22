package com.lf.appcare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
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

import java.util.HashMap;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;
    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private DatabaseReference db;
    private ProgressBar progressBar;
    private Button btnSignup, btnLogin, btnReset;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        if (auth.getCurrentUser() != null)
        {
            FirebaseUser user = auth.getCurrentUser();
            String userUid = user.getUid();
            DatabaseReference ref = db.child("users").child(userUid);
            ref.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    AppCareUser user = dataSnapshot.getValue(AppCareUser.class);
                    if (user == null)
                        return;

                    String userType = user.getUserType();

                    System.out.println("User type: " + userType +
                            "\nUser name: " + user.getFirstName() +
                            "\nUID: " + user.getUid());

                    Intent intent;
                    if (userType.equals(AppCareUser.PATIENT))
                    {
                        System.out.println("Entrou patient");
                        intent = new Intent(LoginActivity.this, MainActivityPatient.class);
                    }
                    else if (userType.equals(AppCareUser.CAREGIVER))
                    {
                        System.out.println("Entrou caregiver");
                        intent = new Intent(LoginActivity.this, MainActivityCaregiver.class);
                    }
                    else
                    {
                        intent = null;
                        System.out.println("Invalid user type: " + userType);
                    }
                    if (intent != null)
                    {
                        startActivity(intent);
                        finish();
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }

        // set the view now
        setContentView(R.layout.activity_login);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        inputEmail =  findViewById(R.id.email);
        inputPassword =  findViewById(R.id.password);
        progressBar =  findViewById(R.id.progressBar);
        btnSignup =  findViewById(R.id.btn_signup);
        btnLogin =  findViewById(R.id.btn_login);
        btnReset =  findViewById(R.id.btn_reset_password);

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = inputEmail.getText().toString();
                final String password = inputPassword.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                //authenticate user
                auth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                progressBar.setVisibility(View.GONE);
                                if (!task.isSuccessful())
                                {
                                    // there was an error
                                    if (password.length() < 6)
                                    {
                                        inputPassword.setError(getString(R.string.minimum_password));
                                    }
                                    else
                                    {
                                        Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                                    }
                                }
                                else
                                {
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    String userUid = user.getUid();
                                    System.out.println ("Current user: " + user.getEmail() + "\n UID: " + userUid);
                                    DatabaseReference ref = db.child("users").child(userUid);
                                    ref.addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot)
                                        {
                                            AppCareUser user = dataSnapshot.getValue(AppCareUser.class);
                                            String userType = user.getUserType();

                                            System.out.println("User type: " + userType +
                                                                "\nUser name: " + user.getFirstName() +
                                                                "\nUID: " + user.getUid());

                                            Intent intent;
                                            if (userType.equals(AppCareUser.PATIENT))
                                            {
                                                System.out.println("Entrou patient");
                                                intent = new Intent(LoginActivity.this, MainActivityPatient.class);
                                            }
                                            else if (userType.equals(AppCareUser.CAREGIVER))
                                            {
                                                System.out.println("Entrou caregiver");
                                                intent = new Intent(LoginActivity.this, MainActivityCaregiver.class);
                                            }
                                            else
                                            {
                                                intent = null;
                                                System.out.println("Invalid user type: " + userType);
                                            }
                                            if (intent != null)
                                            {
                                                startActivity(intent);
                                                finish();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError)
                                        {
                                            System.out.println("The read failed: " + databaseError.getCode());
                                        }
                                    });
                                }
                            }
                        });
            }
        });
    }
}

