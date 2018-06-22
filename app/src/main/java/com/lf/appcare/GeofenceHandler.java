package com.lf.appcare;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
                    NotificationScheduler.showNotification(context, MainActivityPatient.class, "Geofence removed", "Geofence removed");
                }
            })
            .addOnFailureListener(new OnFailureListener()
            {
                @Override
                public void onFailure(@NonNull Exception e)
                {
                   //Toast.makeText(context, "Error removing geofence", Toast.LENGTH_SHORT).show();
                    NotificationScheduler.showNotification(context, MainActivityPatient.class, "Error removing geofence", "Error removing geofence");
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
                            NotificationScheduler.showNotification(context, MainActivityPatient.class, "Geofence added", "Geofence added");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception e)
                        {
                            NotificationScheduler.showNotification(context, MainActivityPatient.class, "Error adding geofence", "Error adding geofence");
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
        Intent intent = new Intent(context, GeofenceTransitionIntentService.class);
        return PendingIntent.getService(context, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private static GeofencingRequest getGeofencingRequest (String geofence_id, LatLng center, float radius)
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(geofence_id)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(center.latitude, center.longitude, radius)
                .setNotificationResponsiveness(2000) // 2 seconds
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
