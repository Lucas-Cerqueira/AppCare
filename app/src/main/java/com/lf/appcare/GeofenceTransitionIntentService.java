package com.lf.appcare;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseUserMetadata;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Listens for geofence transition changes.
 */
public class GeofenceTransitionIntentService extends IntentService {
    protected static final String TAG = "geofence-transition";
    private PatientUser patient;
    private int geofenceTransition;

    private double lastLat, lastLng, accuracy;

    public GeofenceTransitionIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        if (LocationResult.hasResult(intent) || LocationAvailability.hasLocationAvailability(intent))
        {
            System.out.println("Service location");
            return;
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError())
        {
            System.out.println("Geofencing event error");
            return;
        }

        // Get the transition type.
        geofenceTransition = geofencingEvent.getGeofenceTransition();
        lastLat = geofencingEvent.getTriggeringLocation().getLatitude();
        lastLng = geofencingEvent.getTriggeringLocation().getLongitude();
        accuracy = geofencingEvent.getTriggeringLocation().getAccuracy();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
        {

            DatabaseReference db = FirebaseDatabase.getInstance().getReference();
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null)
            {
                System.out.println("User is NULL");
                return;
            }
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL)
            {
                System.out.println("Dwell");
                return;
            }

            db.child("users").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    patient = dataSnapshot.getValue(PatientUser.class);
                    if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER)
                    {
                        System.out.println("Transition ENTER");
                        NotificationScheduler.showNotification(getApplicationContext(), MainActivityCaregiver.class,
                                "Transition ENTER", "Lat: " + Double.toString(lastLat) + " | Lng" + Double.toString(lastLng) + "Acc: " + Double.toString(accuracy));

                        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                        GeofenceEmergency emergency = new GeofenceEmergency("geofenceEnter", lastLat, lastLng, (float)accuracy);
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("emergencyType").setValue("geofenceEnter");
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("lat").setValue(lastLat);
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("lng").setValue(lastLng);
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("acc").setValue(accuracy);
                        db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).setValue(emergency);
                    }
                    else
                    {
                        System.out.println("Transition EXIT");
                        NotificationScheduler.showNotification(getApplicationContext(), MainActivityCaregiver.class,
                               "Transition EXIT", "Transition EXIT");
                        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                        GeofenceEmergency emergency = new GeofenceEmergency("geofenceExit", lastLat, lastLng, (float)accuracy);
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("emergencyType").setValue("geofenceExit");
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("lat").setValue(lastLat);
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("lng").setValue(lastLng);
                        //db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).child("acc").setValue(accuracy);
                        db.child("emergencyRequest").child(patient.getUid()).child(patient.getCaregiverUid()).setValue(emergency);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });




            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            String geofenceTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);
            System.out.println(geofenceTransitionDetails);
        }
        else
        {
            // Log the error.
            //Log.e(TAG, getString(R.string.geofence_transition_invalid_type, geofenceTransition));
            System.out.println(" Error on transition: " + geofenceTransition);
        }
    }

    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                //return getString(R.string.geofence_transition_entered);
                return "Transition entered";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                //return getString(R.string.geofence_transition_exited);
                return "Transition exited";
            default:
                //return getString(R.string.unknown_geofence_transition);
                return "Unknown transition";
        }
    }

    private void sendNotification(String notificationDetails)
    {
        System.out.println("Notification sent");
    }

}