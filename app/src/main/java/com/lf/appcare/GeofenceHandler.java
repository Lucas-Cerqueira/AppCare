package com.lf.appcare;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class GeofenceHandler
{
    public static void removeGeofence (final Context context)
    {
        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(context);
        mGeofencingClient.removeGeofences(getGeofencePendingIntent(context))
            .addOnSuccessListener(new OnSuccessListener<Void>()
            {
                @Override
                public void onSuccess(Void aVoid)
                {
                    NotificationScheduler.showNotification(context, MainActivityPatient.class, context.getString(R.string.remove_geofence_success_title), context.getString(R.string.remove_geofence_success_body));
                }
            })
            .addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                   //Toast.makeText(context, "Error removing geofence", Toast.LENGTH_SHORT).show();
                    //NotificationScheduler.showNotification(context, MainActivityPatient.class, "Error removing geofence", "Error removing geofence");
                }
            }
        );
    }

    public static void createGeofence (final Context context, LatLng center, float radius)
    {
        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(context);
        try
        {
            mGeofencingClient.addGeofences(getGeofencingRequest("GEOFENCE_ID", center, radius),
                    getGeofencePendingIntent(context))
                    .addOnSuccessListener(new OnSuccessListener<Void>()
                    {
                        @Override
                        public void onSuccess(Void aVoid)
                        {
                            NotificationScheduler.showNotification(context, MainActivityPatient.class, context.getString(R.string.add_geofence_success_title), context.getString(R.string.add_geofence_success_body));
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent1);
                            //NotificationScheduler.showNotification(context, MainActivityPatient.class, "Error adding geofence", "Error adding geofence");
                            System.out.println("Error adding geofence");
                            System.out.println(e.toString());
                        }
                    });
        }
        catch (SecurityException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static PendingIntent getGeofencePendingIntent(Context context)
    {
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // adding or removing the geofence.
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static GeofencingRequest getGeofencingRequest (String geofence_id, LatLng center, float radius)
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(geofence_id)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(center.latitude, center.longitude, radius)
                // ** Notification Responsiveness default is to 0 ms. It is the best-effort notification
                // ** responsiveness of the geofence. It might adjust this value internally without
                // ** your control to save power when needed.
                //.setNotificationResponsiveness(2000) // 2 seconds
                //.setNotificationResponsiveness(1000) // 1 second
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        builder.addGeofence(geofence);
        return builder.build();
    }

    public static void removeGeofence (final Context context, final boolean showNotification)
    {
        GeofencingClient mGeofencingClient = LocationServices.getGeofencingClient(context);
        mGeofencingClient.removeGeofences(getGeofencePendingIntent(context))
                .addOnSuccessListener(new OnSuccessListener<Void>()
                {
                    @Override
                    public void onSuccess(Void aVoid)
                    {
                        if (showNotification)
                            NotificationScheduler.showNotification(context, MainActivityPatient.class, "Geofence removed", "Geofence removed");
                    }
                })
                .addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        if (showNotification)
                            NotificationScheduler.showNotification(context, MainActivityPatient.class, "Error removing geofence", "Error removing geofence");
                    }
                }
                );
    }
}
