package com.lf.appcare;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivityPatient extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference db;
    private Button signOut, createReminder, removeReminder, emergency;
    private PatientUser patient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_patient);

        // Location permission for the geofencing feature
        getLocationPermission();

        Toolbar toolbar =  findViewById(R.id.toolbar);

        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);

        //TESTE PARA O LISTENER DO BOTAO DE VOLUME
//        // Enable a receiver
//        Context context = getApplicationContext();
//        ComponentName receiver = new ComponentName(context, EmergencyReceiver.class);
//        PackageManager pm = context.getPackageManager();
//
//        pm.setComponentEnabledSetting(receiver,
//                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
//                PackageManager.DONT_KILL_APP);

        //get firebase auth instance
        auth = FirebaseAuth.getInstance();

        //get current user
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivityPatient.this, StartupActivity.class));
                    finish();
                }
            }
        };

        db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                patient = dataSnapshot.getValue(PatientUser.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        createReminder =  findViewById(R.id.menuCreateReminder);
        removeReminder =  findViewById(R.id.menuRemoveReminder);
        signOut = findViewById(R.id.menuSignOut);
        emergency = findViewById(R.id.emergencyButton);

        createReminder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                Intent intent = new Intent(getApplicationContext(), CreateReminderNameActivity.class);
                startActivity(intent);
                finish();
            }
        });

        removeReminder.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                Intent intent = new Intent(getApplicationContext(), ReminderListPatientActivity.class);
                startActivity(intent);
                finish();
            }
        });


        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance().signOut(MainActivityPatient.this);
            }
        });

        emergency.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick (View view)
            {
                db = FirebaseDatabase.getInstance().getReference();
                System.out.println("patient uid: " + patient.getUid());
                System.out.println("caregiver uid: " + patient.getCaregiverUid());
                if (!patient.getCaregiverUid().isEmpty())
                {
                    db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("emergencyType").setValue("emergencyButton");
                    Toast.makeText(getApplicationContext(), R.string.emergencyMessage, Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(getApplicationContext(), R.string.emergencyMessageError, Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onResume() {
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

    @Override
    public void onBackPressed()
    {
        //super.onBackPressed();
    }

    private void getLocationPermission ()
    {
        System.out.println("Getting location permissions");
        String [] permissions = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
        else
        {
            System.out.println("Location permissions already granted");
        }
    }
}