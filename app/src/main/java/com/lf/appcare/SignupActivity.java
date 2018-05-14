package com.lf.appcare;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class SignupActivity extends AppCompatActivity {

    private EditText inputEmail, inputPassword;
    private Button btnSignIn, btnSignUp, btnResetPassword;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText password = (EditText) findViewById( R.id.password);
        password.setTypeface( Typeface.DEFAULT );

        //Get Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        btnSignIn =  findViewById(R.id.sign_in_button);
        btnSignUp =  findViewById(R.id.sign_up_button);
        inputEmail =  findViewById(R.id.email);
        inputPassword =  findViewById(R.id.password);
        progressBar =  findViewById(R.id.progressBar);



        btnSignIn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        btnSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {

                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), "Enter email address!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(), "Enter password!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);
                //create user
                auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the auth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Spinner spinner = (Spinner) findViewById(R.id.user_type_spinner);
                                    String userType = spinner.getSelectedItem().toString();
                                    EditText name_text = (EditText) findViewById(R.id.first_name);
                                    String firstName = name_text.getText().toString();
                                    String token = FirebaseInstanceId.getInstance().getToken();
                                    if (userType.equals(AppCareUser.PATIENT))
                                    {
                                        PatientUser user = new PatientUser(
                                                auth.getCurrentUser().getUid(),
                                                auth.getCurrentUser().getEmail(),
                                                firstName, userType);
                                        db.getReference().child("users").child(auth.getCurrentUser().getUid()).setValue(user);
                                        MyFirebaseInstanceIDService.sendRegistrationToServer(token);
                                        startActivity(new Intent(SignupActivity.this, MainActivityPatient.class));
                                        finish();
                                    }
                                    else
                                    {
                                        CaregiverUser user = new CaregiverUser(
                                                auth.getCurrentUser().getUid(),
                                                auth.getCurrentUser().getEmail(),
                                                firstName, userType);
                                        db.getReference().child("users").child(auth.getCurrentUser().getUid()).setValue(user);
                                        MyFirebaseInstanceIDService.sendRegistrationToServer(token);
                                        startActivity(new Intent(SignupActivity.this, MainActivityCaregiver.class));
                                        finish();
                                    }
                                }
                            }
                        });

            }
        });

//        Spinner spinner = (Spinner) findViewById(R.id.user_type_spinner);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//                R.array.user_type_array, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        spinner.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }
}