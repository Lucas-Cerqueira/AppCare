package com.lf.appcare;


public class PatientUser extends AppCareUser
{
    String caregiverUid;
    public PatientUser()
    {
        super();
    }

    public PatientUser(String uid, String email, String firstName, String userType)
    {
        super (uid, email, firstName, userType);
        caregiverUid = "";
    }

    public void setCaregiverUid(String caregiverUid) {
        this.caregiverUid = caregiverUid;
    }

    public String getCaregiverUid() {
        return caregiverUid;
    }
}
