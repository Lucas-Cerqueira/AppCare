package com.lf.appcare;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.provider.ContactsContract;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>
{

    protected GoogleApiClient mGoogleApiClient;

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionsGranted = false;
    private SeekBar radiusSlide;
    private TextView textRadius;
    //private Button createGeofenceButton;
    private float radius;
    private LatLng currentPosition;
    private GeofencingClient mGeofencingClient;
    private PendingIntent pendingIntent;
    private CaregiverUser caregiverUser;

    private String mode;
    private String patientUid;
    private LatLng savedCircleCenter;
    private float savedCircleRadius;

    private Map<String,String> patientEmailsUids = new HashMap<>();
    private AutoCompleteTextView patientEmailText;
    private ArrayAdapter<String> adapterEmail;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final float DEFAULT_RADIUS = 50f;

    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        final String userUid = auth.getCurrentUser().getUid();

        // Mode can que equal to:
        // 'create': When creating a geofence
        // 'edit':  When editing a geofence
        // 'view': When viewing a position on the map
        mode = getIntent().getStringExtra("mode");
        patientUid = getIntent().getStringExtra("patientUid");

        patientEmailText = findViewById(R.id.patientEmailText);
        adapterEmail = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<String>());
        if (mode.equals("create"))
        {
            // Autocomplete text field for the patient's email
            patientEmailText.setThreshold(1);
            patientEmailText.setAdapter(adapterEmail);
        }
        else
        {
            ViewGroup layout = (ViewGroup) patientEmailText.getParent();
            if (layout != null)
                layout.removeView(patientEmailText);
            //patientEmailText.setVisibility(View.INVISIBLE);
            savedCircleCenter = new LatLng(getIntent().getDoubleExtra("lat", 0), getIntent().getDoubleExtra("lng", 0));
            savedCircleRadius = getIntent().getFloatExtra("radius", 0);
        }


        DatabaseReference ref = db.child("users").child(userUid);
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                ArrayList<String> emailList = new ArrayList<>();
                patientEmailsUids = new HashMap<>();
                caregiverUser = dataSnapshot.getValue(CaregiverUser.class);
                if (caregiverUser == null || patientUid != null)
                    return;

                for (PatientUser patient: caregiverUser.getPatientList())
                {
                    patientEmailsUids.put(patient.getEmail(), patient.getUid());
                }
                if (patientEmailsUids.isEmpty())
                    patientEmailText.setHint(getString(R.string.empty_patient_list_hint));
                else
                    patientEmailText.setHint(getString(R.string.patient_email));

                for (String email: patientEmailsUids.keySet())
                {
                    System.out.println("Email: " + email);
                    emailList.add(email);
                }

                adapterEmail.clear();
                for (String email: emailList)
                {
                    System.out.println("Email list: " + email);
                }
                adapterEmail.addAll(emailList);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        if (patientUid == null)
            // Initialize with default value
            radius = DEFAULT_RADIUS;
        else
            radius = getIntent().getFloatExtra("radius", 0);

        radiusSlide = findViewById(R.id.radiusSlide);
        textRadius = findViewById(R.id.textRadius);

        radiusSlide.setProgress (Math.round(radius));
        textRadius.setText(getString(R.string.radius_seekbar_text, radius));

        if (mode.equals("view"))
        {
            ViewGroup layout = (ViewGroup) radiusSlide.getParent();
            if (layout != null)
                layout.removeView(radiusSlide);
            layout = (ViewGroup) textRadius.getParent();
            if (layout != null)
                layout.removeView(textRadius);
        }

        radiusSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                textRadius.setText(getString(R.string.radius_seekbar_text, radius));
                radius = radiusSlide.getProgress();
                if (circle == null)
                    createCircle(currentPosition, radius);
                else
                    createCircle(circle.getCenter(), radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {

            }
        });

        Button leftButton = findViewById(R.id.leftButton);
        // Cancel
        if (mode.equals("create"))
            leftButton.setText(R.string.cancel_geofence_button);
        // Remove geofence
        else if (mode.equals("edit"))
            leftButton.setText(R.string.remove_geofence_button);
        // View position
        else
            leftButton.setText(R.string.close_geofence_button);

        leftButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // Cancel or Close
                if (mode.equals("create") || mode.equals("view"))
                {
                    startActivity(new Intent(MapsActivity.this, GeofenceListActivity.class));
                    finish();
                }
                // Remove geofence
                else
                {
                    // REMOVE GEOFENCE ON CLIENT
                    patientEmailText.setVisibility(View.INVISIBLE);
                    DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                    db.child("geofences").child(userUid).child(patientUid).removeValue();
                    startActivity(new Intent(MapsActivity.this, GeofenceListActivity.class));
                    finish();
                }
            }
        });

        Button rightButton = findViewById(R.id.rightButton);
        // Create geofence
        if (mode.equals("create"))
            rightButton.setText(R.string.create_geofence_button);
        // Save changes
        else if (mode.equals(("edit")))
            rightButton.setText(R.string.save_geofence_button);
        // View position
        else
        {
            ViewGroup layout = (ViewGroup) rightButton.getParent();
            if (layout != null)
                layout.removeView(rightButton);
        }

        rightButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // SEND GEOFENCE TO CLIENT
                if (patientUid == null)
                {
                    patientUid = patientEmailsUids.get(patientEmailText.getText().toString());
                    if (patientUid == null)
                    {
                        Toast.makeText(getApplicationContext(), R.string.error_no_patient, Toast.LENGTH_SHORT).show();
                    }
                }

                // DEBUG
                //GeofenceHandler.createGeofence(MapsActivity.this, circle.getCenter(), radius);
                //

                com.lf.appcare.Geofence geofence = new com.lf.appcare.Geofence( circle.getCenter().latitude,
                                                                                circle.getCenter().longitude,
                                                                                radius);
                DatabaseReference db = FirebaseDatabase.getInstance().getReference();
                db.child("geofences").child(caregiverUser.getUid()).child(patientUid).setValue(geofence);
                Intent intent = new Intent(getApplicationContext(), GeofenceListActivity.class);
                startActivity(intent);
                finish();
            }
        });

        getLocationPermission();
    }

    private PendingIntent getGeofencePendingIntent()
    {
        if (pendingIntent != null)
        {
            return pendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
    }

    private GeofencingRequest getGeofencingRequest (String geofence_id, LatLng center, float radius)
    {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        Geofence geofence = new Geofence.Builder()
                .setRequestId(geofence_id)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setCircularRegion(center.latitude, center.longitude, radius)
                .setNotificationResponsiveness(1000) // 1 second
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
        builder.addGeofence(geofence);
        return builder.build();
    }

    private void initMap()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    public void onResult(Status status)
    {
        if (status.isSuccess())
        {
            Toast.makeText(
                    this,
                    R.string.add_geofence_success,
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
                    R.string.add_geofence_error,
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        System.out.println("onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted && mode.equals("create"))
        {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Click on map listener
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    // Creating a marker
                    MarkerOptions markerOptions = new MarkerOptions();

                    // Setting the position for the marker
                    markerOptions.position(latLng);
                    markerOptions.draggable(false);

                    // Clears the previously touched position
                    mMap.clear();
                    circle = null;

                    // Animating to the touched position
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                    // Placing a marker on the touched position
                    mMap.addMarker(markerOptions);

                    createCircle (latLng, radius);
                }
            });
        }
        else
        {
            moveCamera(savedCircleCenter, DEFAULT_ZOOM);

            // Creating a marker
            MarkerOptions markerOptions = new MarkerOptions();

            // Setting the position for the marker
            markerOptions.position(savedCircleCenter);
            markerOptions.draggable(false);

            // Setting the title for the marker.
            if (mode.equals("view"))
                markerOptions.title(getString(R.string.last_position));

            mMap.addMarker(markerOptions);
            createCircle (savedCircleCenter, savedCircleRadius);

            // Click on map listener
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {

                    // Creating a marker
                    MarkerOptions markerOptions = new MarkerOptions();

                    // Setting the position for the marker
                    markerOptions.position(latLng);

                    // Setting the title for the marker.
                    // This will be displayed on taping the marker
                    //markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                    // Clears the previously touched position
                    mMap.clear();
                    circle = null;

                    // Animating to the touched position
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                    // Placing a marker on the touched position
                    mMap.addMarker(markerOptions);

                    createCircle (latLng, radius);
                }
            });
        }
    }

    private void createCircle(LatLng latLng, float radius)
    {
        if (circle == null)
        {
            circle = mMap.addCircle(new CircleOptions()
                    .center(latLng)
                    .radius(radius)
                    .fillColor(0x90ff7251) // First 2 hexas are the alpha
                    .strokeWidth(2));
        }
        else
        {
            circle.setCenter (latLng);
            circle.setRadius (radius);
        }
    }

    private void getDeviceLocation()
    {
        if (!mLocationPermissionsGranted)
        {
            getLocationPermission();
            return;
        }

        System.out.println("Getting device current location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try
        {
            final Task location = mFusedLocationProviderClient.getLastLocation();
            location.addOnCompleteListener(new OnCompleteListener()
            {
                @Override
                public void onComplete(@NonNull Task task)
                {
                    if (task.isSuccessful())
                    {
                        System.out.println("onComplete: found location");
                        if (task.getResult() != null)
                        {
                            Location currentLocation = (Location) task.getResult();
                            currentPosition = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                            moveCamera(currentPosition, DEFAULT_ZOOM);
                            // Creating a marker
                            MarkerOptions markerOptions = new MarkerOptions();
                            // Setting the position for the marker
                            markerOptions.position(currentPosition);
                            // Setting the title for the marker.
                            // This will be displayed on taping the marker
                            //markerOptions.title(coord.latitude + " : " + coord.longitude);
                            mMap.addMarker(markerOptions);
                            createCircle (currentPosition, radius);
                        }
                        else
                        {
                            System.out.println("onComplete: current location is null");
                        }
                    }
                    else
                    {
                        System.out.println("onComplete: current location is null");
                        Toast.makeText(MapsActivity.this, R.string.get_location_error, Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
        catch (SecurityException e){
            System.out.println("getDeviceLocation: SecurityException: " + e.getMessage() );
        }
        catch (NullPointerException e){
            System.out.println("getDeviceLocation: NullPointerException: " + e.getMessage() );
        }
    }

    private void moveCamera (LatLng latLng, float zoom)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getLocationPermission ()
    {
        System.out.println("Getting location permissions");
        String [] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            mLocationPermissionsGranted = true;
            initMap();
        }
        else
        {
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        System.out.println("onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

       if (requestCode == LOCATION_PERMISSION_REQUEST_CODE)
        {
            if(grantResults.length > 0)
            {
                for(int i = 0; i < grantResults.length; i++)
                {
                    if(grantResults[i] != PackageManager.PERMISSION_GRANTED)
                    {
                        mLocationPermissionsGranted = false;
                        System.out.println("onRequestPermissionsResult: permission failed");
                        return;
                    }
                }
                System.out.println("onRequestPermissionsResult: permission granted");
                mLocationPermissionsGranted = true;
                //initialize our map
                initMap();
            }
        }
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
    public void onConnected(Bundle connectionHint)
    {
        System.out.println("Google API connected");
    }

    @Override
    public void onConnectionFailed(ConnectionResult result)
    {
        // Do something with result.getErrorCode());
        System.out.println("Google API connection failed - " + result.getErrorMessage());
        System.out.println("Error code: " + result.getErrorCode());
    }

    @Override
    public void onConnectionSuspended(int cause)
    {
        System.out.println("Google API connection suspended - " + cause);
        mGoogleApiClient.connect();
    }
}
