package com.lf.appcare;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.ArrayList;
import java.util.Map;


public class GeolocationActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status> {

    protected ArrayList<Geofence> mGeofenceList;
    protected GoogleApiClient mGoogleApiClient;
    private Button mAddGeofenceButton;

    private GeofencingClient mGeofencingClient;

    private final float geofenceRadius = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geolocation);

        mGeofencingClient = LocationServices.getGeofencingClient(this);

        mAddGeofenceButton = findViewById(R.id.add_geofences_button);
        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Kick off the request to build GoogleApiClient.
        buildGoogleApiClient();
    }

    public void onResult(Status status)
    {
        if (status.isSuccess())
        {
            Toast.makeText(
                    this,
                    "Geofences Added",
                    Toast.LENGTH_SHORT
            ).show();
        }
        else
        {
            // Get the status code for the error and log it using a user-friendly message.
//            String errorMessage = GeofenceErrorMessages.getErrorString(this,
//                    status.getStatusCode());
            Toast.makeText(
                    this,
                    "Error adding geofence",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private PendingIntent getGeofencePendingIntent() {
        Intent intent = new Intent(this, GeofenceTransitionIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling addgeoFences()
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void addGeofenceButtonHandler(View view)
    {
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        try
        {
//            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
//                    .addOnSuccessListener(this, new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid)
//                        {
//                            System.out.println("Geofence added");
//                        }
//                    })
//                    .addOnFailureListener(this, new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e)
//                        {
//                            System.out.println("Geofence add failed");
//                        }
//                    });

//            LocationServices.GeofencingApi.addGeofence(
//                    mGoogleApiClient,
//                    // The GeofenceRequest object.
//                    getGeofencingRequest(),
//                    // A pending intent that that is reused when calling removeGeofences(). This
//                    // pending intent is used to generate an intent when a matched geofence
//                    // transition is observed.
//                    getGeofencePendingIntent()
//            ).setResultCallback(this); // Result processed in onResult().
        } catch (SecurityException securityException)
        {
            // Catch exception generated if the app does not use ACCESS_FINE_LOCATION permission.
            System.out.println("Missing permission ACCESS_FINE_LOCATION");
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the {@code #addApi} method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mGoogleApiClient.isConnecting() || !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Do something with result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
    }

    private GeofencingRequest getGeofencingRequest(float latitude, float longitude, float radius) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofence(new Geofence.Builder()
                // Set the request ID of the geofence. This is a string to identify this
                // geofence.
                .setRequestId("GEOFENCE")

                // Set the circular region of this geofence.
                .setCircularRegion(
                        latitude,
                        longitude,
                        radius
                )

                // Set the expiration duration of the geofence. This geofence gets automatically
                // removed after this period of time.
                //.setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)

                // Set the transition types of interest. Alerts are only generated for these
                // transition. We track entry and exit transitions in this sample.
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)

                // Create the geofence.
                .build());
        return builder.build();
    }

    private void test_sendNotification(String notificationDetails)
    {


//        // Create an explicit content Intent that starts the main Activity.
//        Intent notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
//
//        // Construct a task stack.
//        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
//
//        // Add the main Activity to the task stack as the parent.
//        stackBuilder.addParentStack(MainActivity.class);
//
//        // Push the content Intent onto the stack.
//        stackBuilder.addNextIntent(notificationIntent);
//
//        // Get a PendingIntent containing the entire back stack.
//        PendingIntent notificationPendingIntent =
//                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
//
//        // Get a notification builder that's compatible with platform versions >= 4
//        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
    }
}