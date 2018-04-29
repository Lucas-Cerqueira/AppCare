package com.lf.appcare;


public class PatientUser extends AppCareUser
{
    public PatientUser()
    {
        super();
    }

    public PatientUser(String uid, String email, String firstName, String userType)
    {
        super (uid, email, firstName, userType);
    }
}
