package com.lf.appcare;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivityCaregiver extends AppCompatActivity {

    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, remove, signOut, patientList;

    private EditText oldEmail, newEmail, password, newPassword;
    private ProgressBar progressBar;
    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_caregiver);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        setSupportActionBar(toolbar);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        // Subscribe for notifications
        FirebaseMessaging.getInstance().subscribeToTopic("android");

        //get current user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivityCaregiver.this, LoginActivity.class));
                    finish();
                }
                else
                {
                    String userUid = user.getUid();
                    DatabaseReference ref = db.child("users").child(userUid);
                    ref.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot)
                        {
                            CaregiverUser user = dataSnapshot.getValue(CaregiverUser.class);
                            if (user == null)
                                return;

                            String userType = user.getUserType();

                            System.out.println("User type: " + userType +
                                    "\nUser name: " + user.getFirstName() +
                                    "\nUID: " + user.getUid());
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {
                            System.out.println("The read failed: " + databaseError.getCode());
                        }
                    });
                }
            }
        };

//        btnChangeEmail = findViewById(R.id.change_email_button);
//        btnChangePassword = findViewById(R.id.change_password_button);
//        btnSendResetEmail = findViewById(R.id.sending_pass_reset_button);
//        btnRemoveUser =  findViewById(R.id.remove_user_button);
//        changeEmail =  findViewById(R.id.changeEmail);
//        changePassword =  findViewById(R.id.changePass);
//        sendEmail =  findViewById(R.id.send);
//        remove =  findViewById(R.id.remove);
        patientList = findViewById(R.id.connectToPatientButton);
        signOut =  findViewById(R.id.sign_out);

//        oldEmail =  findViewById(R.id.old_email);
//        newEmail =  findViewById(R.id.new_email);
//        password =  findViewById(R.id.password);
//        newPassword =  findViewById(R.id.newPassword);

//        oldEmail.setVisibility(View.GONE);
//        newEmail.setVisibility(View.GONE);
//        password.setVisibility(View.GONE);
//        newPassword.setVisibility(View.GONE);
//        changeEmail.setVisibility(View.GONE);
//        changePassword.setVisibility(View.GONE);
//        sendEmail.setVisibility(View.GONE);
//        remove.setVisibility(View.GONE);


        patientList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivityCaregiver.this, PatientListActivity.class));
                finish();
            }
        });

//        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                oldEmail.setVisibility(View.GONE);
//                newEmail.setVisibility(View.VISIBLE);
//                password.setVisibility(View.GONE);
//                newPassword.setVisibility(View.GONE);
//                changeEmail.setVisibility(View.VISIBLE);
//                changePassword.setVisibility(View.GONE);
//                sendEmail.setVisibility(View.GONE);
//                remove.setVisibility(View.GONE);
//            }
//        });
//
//        changeEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                progressBar.setVisibility(View.VISIBLE);
//                if (user != null && !newEmail.getText().toString().trim().equals("")) {
//                    user.updateEmail(newEmail.getText().toString().trim())
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Toast.makeText(MainActivityCaregiver.this, "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
//                                        signOut();
//                                        progressBar.setVisibility(View.GONE);
//                                    } else {
//                                        Toast.makeText(MainActivityCaregiver.this, "Failed to update email!", Toast.LENGTH_LONG).show();
//                                        progressBar.setVisibility(View.GONE);
//                                    }
//                                }
//                            });
//                } else if (newEmail.getText().toString().trim().equals("")) {
//                    newEmail.setError("Enter email");
//                    progressBar.setVisibility(View.GONE);
//                }
//            }
//        });
//
//        btnChangePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                oldEmail.setVisibility(View.GONE);
//                newEmail.setVisibility(View.GONE);
//                password.setVisibility(View.GONE);
//                newPassword.setVisibility(View.VISIBLE);
//                changeEmail.setVisibility(View.GONE);
//                changePassword.setVisibility(View.VISIBLE);
//                sendEmail.setVisibility(View.GONE);
//                remove.setVisibility(View.GONE);
//            }
//        });
//
//        changePassword.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                progressBar.setVisibility(View.VISIBLE);
//                if (user != null && !newPassword.getText().toString().trim().equals("")) {
//                    if (newPassword.getText().toString().trim().length() < 6) {
//                        newPassword.setError("Password too short, enter minimum 6 characters");
//                        progressBar.setVisibility(View.GONE);
//                    } else {
//                        user.updatePassword(newPassword.getText().toString().trim())
//                                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<Void> task) {
//                                        if (task.isSuccessful()) {
//                                            Toast.makeText(MainActivityCaregiver.this, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
//                                            signOut();
//                                            progressBar.setVisibility(View.GONE);
//                                        } else {
//                                            Toast.makeText(MainActivityCaregiver.this, "Failed to update password!", Toast.LENGTH_SHORT).show();
//                                            progressBar.setVisibility(View.GONE);
//                                        }
//                                    }
//                                });
//                    }
//                } else if (newPassword.getText().toString().trim().equals("")) {
//                    newPassword.setError("Enter password");
//                    progressBar.setVisibility(View.GONE);
//                }
//            }
//        });
//
//        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                oldEmail.setVisibility(View.VISIBLE);
//                newEmail.setVisibility(View.GONE);
//                password.setVisibility(View.GONE);
//                newPassword.setVisibility(View.GONE);
//                changeEmail.setVisibility(View.GONE);
//                changePassword.setVisibility(View.GONE);
//                sendEmail.setVisibility(View.VISIBLE);
//                remove.setVisibility(View.GONE);
//            }
//        });
//
//        sendEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                progressBar.setVisibility(View.VISIBLE);
//                if (!oldEmail.getText().toString().trim().equals("")) {
//                    auth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Toast.makeText(MainActivityCaregiver.this, "Reset password email is sent!", Toast.LENGTH_SHORT).show();
//                                        progressBar.setVisibility(View.GONE);
//                                    } else {
//                                        Toast.makeText(MainActivityCaregiver.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
//                                        progressBar.setVisibility(View.GONE);
//                                    }
//                                }
//                            });
//                } else {
//                    oldEmail.setError("Enter email");
//                    progressBar.setVisibility(View.GONE);
//                }
//            }
//        });
//
//        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                progressBar.setVisibility(View.VISIBLE);
//                if (user != null) {
//                    user.delete()
//                            .addOnCompleteListener(new OnCompleteListener<Void>() {
//                                @Override
//                                public void onComplete(@NonNull Task<Void> task) {
//                                    if (task.isSuccessful()) {
//                                        Toast.makeText(MainActivityCaregiver.this, "Your profile is deleted:( Create a account now!", Toast.LENGTH_SHORT).show();
//                                        startActivity(new Intent(MainActivityCaregiver.this, SignupActivity.class));
//                                        finish();
//                                        progressBar.setVisibility(View.GONE);
//                                    } else {
//                                        Toast.makeText(MainActivityCaregiver.this, "Failed to delete your account!", Toast.LENGTH_SHORT).show();
//                                        progressBar.setVisibility(View.GONE);
//                                    }
//                                }
//                            });
//                }
//            }
//        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

    }

    //sign out method
    public void signOut() {
        auth.signOut();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            auth.removeAuthStateListener(authListener);
        }
    }
}