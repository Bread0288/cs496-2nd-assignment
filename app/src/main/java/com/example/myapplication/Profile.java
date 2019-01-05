package com.example.myapplication;

import android.net.Uri;

import java.util.Comparator;

public class Profile {
    private String _name;
    private String _phone;
    private String _email;
    private Uri _contactUri;

    public Profile(String name, String phone, String email, Uri contactUri) {
        _name = name;
        _phone = phone;
        _email = email;
        _contactUri = contactUri;
    }

    public String getName() {
        return _name;
    }

    public String getPhone() { return _phone; }

    public String getEmail() {
        return _email;
    }

    public Uri getContactUri() { return _contactUri; }
}

class CompareProfile implements Comparator<Profile> {
    @Override
    public int compare(Profile a, Profile b) {
        return a.getName().compareTo(b.getName());
    }
}
