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

    private Button btnSignUp;
    private FirebaseAuth auth;
    private FirebaseDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Get Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        btnSignUp =  findViewById(R.id.sign_up_button);
        btnSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Spinner spinner = findViewById(R.id.user_type_spinner);
                String userType = spinner.getSelectedItem().toString();
                EditText name_text = findViewById(R.id.first_name);
                String firstName = name_text.getText().toString();
                //String token = FirebaseInstanceId.getInstance().getToken();
                if (userType.equals(AppCareUser.PATIENT))
                {
                    PatientUser user = new PatientUser(
                            auth.getCurrentUser().getUid(),
                            auth.getCurrentUser().getEmail(),
                            firstName, userType);
                    db.getReference().child("users").child(auth.getCurrentUser().getUid()).setValue(user);
                    //MyFirebaseInstanceIDService.sendRegistrationToServer(token);
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
                    //MyFirebaseInstanceIDService.sendRegistrationToServer(token);
                    startActivity(new Intent(SignupActivity.this, MainActivityCaregiver.class));
                    finish();
                }
            }
        });

        Spinner spinner = findViewById(R.id.user_type_spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.user_type_array, R.layout.spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        progressBar.setVisibility(View.GONE);
//    }
}