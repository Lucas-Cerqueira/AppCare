package com.lf.appcare;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.Arrays;
import java.util.List;

public class StartupActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

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
                    {
                        LaunchLogin();
                        return;
                    }

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
            LaunchLogin();
        }


    }

    private void LaunchLogin()
    {
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.GoogleBuilder().build(),
                new AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build()
                );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        .build(),
                RC_SIGN_IN);
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
            System.out.println("Invalid user type: " + userType);
            return;
        }
        MyFirebaseInstanceIDService.sendRegistrationToServer(FirebaseInstanceId.getInstance().getToken());
        if (user != null)
        {
            // Update notification token
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (response == null)
            {
                finish();
                return;
            }

            if (resultCode == RESULT_OK)
            {
                String userUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                        .child("users").child(userUid);
                ref.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        // If it is a new user
                        if (!dataSnapshot.exists())
                        {
                            startActivity (new Intent(StartupActivity.this, CompleteSignupActivity.class));
                            finish();
                            return;
                        }

                        AppCareUser user = dataSnapshot.getValue(AppCareUser.class);

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
                if (response == null)
                {
                    Toast.makeText(getApplicationContext(), "Sign in cancelled",
                            Toast.LENGTH_SHORT)
                            .show();
                    LaunchLogin();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK)
                {
                    Toast.makeText(getApplicationContext(), "Check your internet connection",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Sign in erro. Try again",
                        Toast.LENGTH_SHORT)
                        .show();
                System.out.println("Sign-in error: " + response.getError());
                LaunchLogin();
            }
        }
    }

//    @Override
//    public void onBackPressed()
//    {
//        if (backEnabled)
//            super.onBackPressed();
//    }
}
