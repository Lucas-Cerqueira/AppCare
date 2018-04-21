package com.lf.appcare;

public class AppCareUser
{
    private String uid;
    private String firstName;
    private String userType;

    private AppCareUser() {}

    AppCareUser (String uid, String firstName, String userType)
    {
        this.uid = uid;
        this.firstName = firstName;
        this.userType = userType;
    }

    public String getUid()
    {
        return uid;
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

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
