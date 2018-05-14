package com.lf.appcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class StartupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        //Get Firebase auth instance
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        String userUid, userType;
        if (auth.getCurrentUser() != null)
        {
            userUid = auth.getCurrentUser().getUid();
            // Get user type from preferences
            SharedPreferences myPreferences
                    = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            userType = myPreferences.getString("USERTYPE", "");
            String userUidPref = myPreferences.getString("UID", "");

            if (userUidPref.equals(userUid))
            {
                GoToMain(userType, null);
                return;
            }


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

                    GoToMain(userType, user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }
        else
        {
            Intent intent = new Intent(StartupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void GoToMain (String userType, AppCareUser user)
    {
        Intent intent;
        if (userType.equals(AppCareUser.PATIENT))
        {
            System.out.println("Entrou patient");
            intent = new Intent(StartupActivity.this, MainActivityPatient.class);
        }
        else if (userType.equals(AppCareUser.CAREGIVER))
        {
            System.out.println("Entrou caregiver");
            intent = new Intent(StartupActivity.this, MainActivityCaregiver.class);
        }
        else
        {
            intent = null;
            System.out.println("Invalid user type: " + userType);
        }
        if (intent != null)
        {
            // Update notification token
            MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());

            if (user != null)
            {
                // Store user info in shared preferences
                SharedPreferences myPreferences
                        = PreferenceManager.getDefaultSharedPreferences(StartupActivity.this);
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
    }
}
