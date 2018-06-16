package com.lf.appcare;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import com.google.android.gms.tasks.Task;

import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private boolean mLocationPermissionsGranted = false;
    private SeekBar radiusSlide;
    private TextView textRadius;
    private float radius;
    private LatLng currentPosition;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final float DEFAULT_RADIUS = 50f;

    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize with default value
        radius = DEFAULT_RADIUS;

        radiusSlide = findViewById(R.id.radiusSlide);
        textRadius = findViewById(R.id.textRadius);

        radiusSlide.setProgress (Math.round(radius));
        textRadius.setText("Radius: " + radiusSlide.getProgress() + "m");

        radiusSlide.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                textRadius.setText("Radius: " + radiusSlide.getProgress() + "m");
                radius = radiusSlide.getProgress();
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

        getLocationPermission();
    }

    private void initMap()
    {
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        System.out.println("onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted)
        {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);

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
                    .fillColor(0x8038F75E) // First 2 hexas are the alpha
                    .strokeWidth(5));
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
                        Toast.makeText(MapsActivity.this, "Unable to get current location", Toast.LENGTH_SHORT).show();
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//
//        // Add a marker in Sydney and move the camera
//        LatLng marker = new LatLng(0, 0);
//        mMap.setMyLocationEnabled(true);
//        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//       // mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//        Circle circle = mMap.addCircle(new CircleOptions()
//                        .center(marker)
//                        .radius(50));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
//    }
}
