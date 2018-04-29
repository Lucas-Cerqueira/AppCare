package com.lf.appcare;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AddPatientActivity extends AppCompatActivity
{
    private FirebaseAuth auth;
    private DatabaseReference db;

    Button searchButton, cancelButton;

    Iterable<DataSnapshot> usersSnapshot = new ArrayList<>();
    List<String> emailList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_patient);

        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();

        System.out.println("DB REFERENCE SET");

        if (auth.getCurrentUser() != null)
        {
            DatabaseReference ref = db.child("users");
            ref.addListenerForSingleValueEvent(new ValueEventListener()
            {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot)
                {
                    System.out.println("READING FROM DB");
                    usersSnapshot = dataSnapshot.getChildren();
                    for (DataSnapshot userSnapshot: usersSnapshot)
                    {
                        AppCareUser user = userSnapshot.getValue(AppCareUser.class);
                        if (user.getUserType().equals(AppCareUser.PATIENT)) { emailList.add (user.getEmail()); }
                    }
                    System.out.println("Email list: " + Arrays.toString(emailList.toArray()));
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
            startActivity(new Intent(AddPatientActivity.this, LoginActivity.class));
            finish();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, emailList);
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.patientEmail);
        textView.setAdapter(adapter);


        searchButton = findViewById(R.id.searchButton);
        searchButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(AddPatientActivity.this, PatientListActivity.class));
                finish();
            }
        });

        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(AddPatientActivity.this, PatientListActivity.class));
                finish();
            }
        });

    }

}
