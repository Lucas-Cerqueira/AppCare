package com.lf.appcare;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class CaregiverUser extends AppCareUser
{
    private List<PatientUser> patientList;

    public CaregiverUser()
    {
        super();
        this.patientList = new ArrayList<>();
    }

    public CaregiverUser (String uid, String email, String firstName, String userType, PatientUser connectedUser)
    {
        super (uid, email, firstName, userType);
        this.patientList = new ArrayList<>();
        System.out.println("CAREGIVER USER CREATED");
    }

    public CaregiverUser (String uid, String email, String firstName, String userType)
    {
        this(uid, email, firstName, userType, null);
    }

    public List<PatientUser> getPatientList()
    {
        return patientList;
    }

    public int AddPatient (PatientUser patient)
    {
        System.out.println("Adding user");
        if (patient == null)
            return -1;

        // Patient already connected to caregiver
        if (FindPatient(patient) != -1)
        {
            System.out.println("Patient already connected");
            return 1;
        }

        patientList.add(patient);
        System.out.println("User added. List size: " + patientList.size());

        DatabaseReference db = FirebaseDatabase.getInstance().getReference();

        // Create new patient-caregiver connection to notify the patient
        db.child("patientConnections").child(patient.getUid()).setValue(this.getUid());

        // Update caregiver user in the DB
        db.child("users").child(this.getUid()).child("patientList").setValue(patientList);

        // Update patient user in the DB
        db.child("users").child(patient.getUid()).child("caregiverUid").setValue(this.getUid());

        return 0;
    }

    public int RemovePatient (PatientUser patient)
    {
        if (patient == null)
            return -1;

        int index = FindPatient(patient);
        // Patient not connected to caregiver
        if (index == -1)
        {
            System.out.println("Patient not found");
            return 1;
        }

        patientList.remove(index);
        // Update caregiver user in the DB
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        db.child("users").child(this.getUid()).child("patientList").setValue(patientList);

        // Remove patient-caregiver connection from the DB to notify the patient
        db = FirebaseDatabase.getInstance().getReference();
        db.child("patientConnections").child(patient.getUid()).removeValue();

        // Remove caregiver UID from patient user in the DB
        db.child("users").child(patient.getUid()).child("caregiverUid").setValue("");
        return 0;
    }

    private int FindPatient (PatientUser patient)
    {
        int i = 0;
        for (PatientUser user: patientList)
        {
            if (user.getUid().equals(patient.getUid()))
                return i;
            i++;
        }
        return -1;
    }

    public PatientUser FindPatientByEmail (String email)
    {
        for (PatientUser patient: patientList)
        {
            if (patient.getEmail().equals(email))
            {
                return patient;
            }
        }
        return null;
    }
}
