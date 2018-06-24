package com.lf.appcare;

import android.Manifest;
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
import android.widget.EditText;
import android.widget.ProgressBar;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivityCaregiver extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private Button signOut, patientList, reminderList, geofenceList;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth auth;
    private DatabaseReference db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_caregiver);

        // Location permission for the geofencing feature
        getLocationPermission ();

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));
        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
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
                    startActivity(new Intent(MainActivityCaregiver.this, StartupActivity.class));
                    finish();
                }
//                else
//                {
//                    String userUid = user.getUid();
//                    DatabaseReference ref = db.child("users").child(userUid);
//                    ref.addListenerForSingleValueEvent(new ValueEventListener()
//                    {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot)
//                        {
//                            CaregiverUser user = dataSnapshot.getValue(CaregiverUser.class);
//                            if (user == null)
//                                return;
//
//                            String userType = user.getUserType();
//
//                            System.out.println("User type: " + userType +
//                                    "\nUser name: " + user.getFirstName() +
//                                    "\nUID: " + user.getUid());
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError)
//                        {
//                            System.out.println("The read failed: " + databaseError.getCode());
//                        }
//                    });
//                }
            }
        };

        patientList = findViewById(R.id.connectToPatientButton);
        reminderList = findViewById(R.id.remindersButton);
        geofenceList = findViewById(R.id.geofencesButton);
        signOut =  findViewById(R.id.sign_out);

        patientList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivityCaregiver.this, PatientListActivity.class));
                finish();
            }
        });

        reminderList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivityCaregiver.this, ReminderListCaregiverActivity.class));
                finish();
            }
        });

        geofenceList.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(MainActivityCaregiver.this, GeofenceListActivity.class));
                finish();
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AuthUI.getInstance().signOut(MainActivityCaregiver.this);
            }
        });

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