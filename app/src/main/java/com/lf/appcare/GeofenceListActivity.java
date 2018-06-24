package com.lf.appcare;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.collect.Lists;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GeofenceListActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private DatabaseReference db;

    private ListView geofenceListView;
    private List<DataSnapshot> geofencesSnapshot = new ArrayList<>();
    private List<Geofence> geofenceList = new ArrayList<>();
    private List<String> patientUids = new ArrayList<>();
    private Map<String,String> patientNames = new HashMap<>();

    private FirebaseUser user;
    private ArrayAdapter<Geofence> arrayAdapterGeofence;
    String caregiverUid;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_geofence_list);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.title_activity_geofence_list));
//        toolbar.setTitleTextColor(android.graphics.Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        if (auth.getCurrentUser() == null)
        {
            startActivity(new Intent(GeofenceListActivity.this, StartupActivity.class));
            finish();
        }

        geofenceListView = findViewById(R.id.geofenceListView);
        geofenceListView.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(final AdapterView<?> parent, View view, final int position, long id)
            {
                Geofence geofence = geofenceList.get(position);
                Intent intent = new Intent(GeofenceListActivity.this, MapsActivity.class);
                intent.putExtra("mode", "edit");
                intent.putExtra("patientUid", patientUids.get(position));
                intent.putExtra("lat", geofence.getLat());
                intent.putExtra("lng", geofence.getLng());
                intent.putExtra("radius", geofence.getRadius());
                startActivity(intent);
//                final PatientUser patient = patientList.get(position);
//                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
//                builder.setMessage(getString(R.string.remove_patient_message, patient.getFirstName()))
//                        .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id)
//                            {
//                                int response = caregiver.RemovePatient(patient);
//                                Toast.makeText(getApplicationContext(), R.string.patient_removed,
//                                        Toast.LENGTH_SHORT)
//                                        .show();
//                            }
//                        })
//                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
//                        {
//                            public void onClick(DialogInterface dialog, int id)
//                            {
//                            }
//                        });
//                // Create the AlertDialog object
//                AlertDialog dialog = builder.create();
//                dialog.show();
//
//                // Configure the buttons
//                Button posButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
//                Button negButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
//                posButton.setTextColor(getResources().getColor(R.color.colorPrimary));
//                negButton.setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        user = auth.getCurrentUser();
        caregiverUid = user.getUid();
        DatabaseReference ref = db.child("geofences").child(caregiverUid);
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                // Read all the caregiver stored geofences
                geofenceList = new ArrayList<>();
                patientUids = new ArrayList<>();
                geofencesSnapshot = Lists.newArrayList (dataSnapshot.getChildren().iterator());

                for (DataSnapshot geofenceSnapshot : geofencesSnapshot)
                {
                    Geofence geofence = geofenceSnapshot.getValue(Geofence.class);
                    geofenceList.add(geofence);
                    patientUids.add(geofenceSnapshot.getKey());
                }
                GetPatientNames(patientUids);
                //ListReminders();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        // "Add patient" button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(GeofenceListActivity.this, MapsActivity.class);
                intent.putExtra("mode", "create");
                startActivity(intent);
                finish();
            }
        });
    }

    private void GetPatientNames(final List<String> patientUids)
    {
        if (!patientUids.isEmpty())
        {
            DatabaseReference ref = db.child("users");
            ref.addListenerForSingleValueEvent(new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    List<DataSnapshot> usersSnapshot = Lists.newArrayList(dataSnapshot.getChildren().iterator());
                    for (DataSnapshot userSnapshot : usersSnapshot)
                    {
                        AppCareUser user = userSnapshot.getValue(AppCareUser.class);
                        String userUid = user.getUid();
                        if (user.getUserType().equals(AppCareUser.PATIENT) && patientUids.contains(userUid))
                        {
                            patientNames.put(userUid, user.getFirstName());
                            System.out.println("New patient found: " + user.getFirstName());
                        }
                    }

                    ListGeofences();
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {
                    System.out.println("The read failed: " + databaseError.getCode());
                }
            });
        }
        // If there are no patient uids
        // empty the hashmap
        else
        {
            patientNames = new HashMap<>();
            ListGeofences();
        }
    }

    private void ListGeofences ()
    {
        if (geofenceList == null)
        {
            System.out.println("NOT GEOFENCELIST REFERENCE");
            geofenceListView.setVisibility(View.GONE);
        }
        else if (geofenceList.isEmpty())
        {
            System.out.println("EMPTY GEOFENCE LIST");
            geofenceListView.setVisibility(View.GONE);
        }
        else
        {
            System.out.println("THERE ARE " + geofenceList.size() + " GEOFENCES");
            geofenceListView.setVisibility(View.VISIBLE);
            arrayAdapterGeofence = new ArrayAdapter<Geofence>(this, android.R.layout.simple_list_item_2, android.R.id.text1, geofenceList) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent)
                {
                    View view = super.getView(position, convertView, parent);
                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);

                    //text1.setText(geofenceList.get(position).getFirstName());
                    text1.setText(patientNames.get(patientUids.get(position)));
                    text2.setText(R.string.click_to_see_map);
                    return view;
                }
            };
            geofenceListView.setAdapter(arrayAdapterGeofence);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home)
        {
            startActivity(new Intent(getApplicationContext(), MainActivityCaregiver.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(getApplicationContext(), MainActivityCaregiver.class));
        finish();
    }
}
