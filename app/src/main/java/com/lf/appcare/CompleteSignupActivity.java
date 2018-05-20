package com.lf.appcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import info.hoang8f.android.segmented.SegmentedGroup;

public class CompleteSignupActivity extends AppCompatActivity {

    private Button btnSignUp;
    private FirebaseAuth auth;
    private FirebaseDatabase db;
    private SegmentedGroup segmentedGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_signup);

        //Get Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        segmentedGroup = findViewById(R.id.segmentedGroupSignUp);
        segmentedGroup.check(R.id.buttonPatientSignUp);

        btnSignUp =  findViewById(R.id.sign_up_button);
        btnSignUp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String userType;
                FirebaseUser currentUser = auth.getCurrentUser();
                int checkedButton = segmentedGroup.getCheckedRadioButtonId();
                if (checkedButton == R.id.buttonPatientSignUp)
                    userType = AppCareUser.PATIENT;
                else
                    userType = AppCareUser.CAREGIVER;

                EditText name_text = findViewById(R.id.first_name);
                String firstName = name_text.getText().toString().trim();
                String token = FirebaseInstanceId.getInstance().getToken();
                Intent intent;
                if (userType.equals(AppCareUser.PATIENT))
                {
                    PatientUser user = new PatientUser(
                            currentUser.getUid(),
                            currentUser.getEmail(),
                            firstName, userType);
                    db.getReference().child("users").child(currentUser.getUid()).setValue(user);
                    MyFirebaseInstanceIDService.sendRegistrationToServer(token);
                    intent = new Intent(CompleteSignupActivity.this, MainActivityPatient.class);
                    SharedPreferences myPreferences
                            = PreferenceManager.getDefaultSharedPreferences(CompleteSignupActivity.this);
                    SharedPreferences.Editor myEditor = myPreferences.edit();
                    myEditor.putString("UID", user.getUid());
                    myEditor.putString("NAME", user.getFirstName());
                    myEditor.putString("EMAIL", user.getEmail());
                    myEditor.putString("USERTYPE", userType);
                    myEditor.apply();
                }
                else
                {
                    CaregiverUser user = new CaregiverUser(
                            currentUser.getUid(),
                            currentUser.getEmail(),
                            firstName, userType);
                    db.getReference().child("users").child(currentUser.getUid()).setValue(user);
                    MyFirebaseInstanceIDService.sendRegistrationToServer(token);
                    intent = new Intent(CompleteSignupActivity.this, MainActivityCaregiver.class);
                    SharedPreferences myPreferences
                            = PreferenceManager.getDefaultSharedPreferences(CompleteSignupActivity.this);
                    SharedPreferences.Editor myEditor = myPreferences.edit();
                    myEditor.putString("UID", user.getUid());
                    myEditor.putString("NAME", user.getFirstName());
                    myEditor.putString("EMAIL", user.getEmail());
                    myEditor.putString("USERTYPE", userType);
                    myEditor.apply();
                }

                startActivity(intent);
                finish();
            }
        });
    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        progressBar.setVisibility(View.GONE);
//    }

    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
    }
}