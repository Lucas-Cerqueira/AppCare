package com.lf.appcare;

public class AppCareUser
{
    static final String PATIENT = "Patient";
    static final String CAREGIVER = "Caregiver";

    private String uid;
    private String email;
    private String firstName;
    private String userType;

    public AppCareUser() {}

    public AppCareUser (String uid, String email, String firstName, String userType)
    {
        System.out.println("APPCARE USER CONSTRUCTOR");
        this.uid = uid;
        this.email = email;
        this.firstName = firstName;
        this.userType = userType;
    }

    public String getUid()
    {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getUserType()
    {
        return userType;
    }

    public void setUserType(String userType)
    {
        this.userType = userType;
    }
}
