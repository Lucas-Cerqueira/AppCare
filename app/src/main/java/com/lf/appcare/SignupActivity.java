package com.lf.appcare;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUserMetadata;

import java.util.Arrays;
import java.util.List;

public class SignupActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        List<AuthUI.IdpConfig> providers = Arrays.asList(
            new AuthUI.IdpConfig.EmailBuilder().setRequireName(false).build(),
            new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setTheme(R.style.LoginTheme)
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(true)
                        .build(),
                RC_SIGN_IN);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK)
            {
                FirebaseUserMetadata metadata = FirebaseAuth.getInstance().getCurrentUser().getMetadata();
                if (metadata.getCreationTimestamp() == metadata.getLastSignInTimestamp())
                    startActivity (new Intent(SignupActivity.this, CompleteSignupActivity.class));
                else
                    startActivity (new Intent(SignupActivity.this, MainActivityPatient.class));
            }
            else
            {
                if (response == null)
                {
                    Toast.makeText(getApplicationContext(), "Sign in cancelled",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK)
                {
                    Toast.makeText(getApplicationContext(), "Check your internet connection",
                            Toast.LENGTH_SHORT)
                            .show();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Unknown error",
                        Toast.LENGTH_SHORT)
                        .show();
                System.out.println("Sign-in error: " + response.getError());
            }
        }
    }
}
