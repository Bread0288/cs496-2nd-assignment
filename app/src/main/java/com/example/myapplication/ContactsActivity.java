package com.example.myapplication;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

public class ContactsActivity extends FontActivity implements ProfileListAdapter.EventListener{
    public ArrayList<Profile> _profiles_data;
    public ArrayList<Profile> _profiles_show;
    private ProfileListAdapter adapter;
    private EditText listSearch;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        _profiles_data = new ArrayList();
        _profiles_show = new ArrayList();
        adapter = new ProfileListAdapter(this, this, R.layout.profileview, _profiles_show);
        ListView listView = findViewById(R.id.numberlist);
        listView.setAdapter(adapter);
        final EditText listSearch = (EditText) findViewById(R.id.numlistSearch);
        listSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
                public void afterTextChanged(Editable s) {
                String text = listSearch.getText().toString();
                search(text);
            }
        });
    }

    public void reloadContacts() {
        _profiles_data.clear();
        _profiles_show.clear();
        fetchContacts();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        reloadContacts();
    }

    public void fetchContacts() {

        Uri CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;
        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;

        Uri PhoneCONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String Phone_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Uri EmailCONTENT_URI =  ContactsContract.CommonDataKinds.Email.CONTENT_URI;
        String EmailCONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
        String DATA = ContactsContract.CommonDataKinds.Email.DATA;

        ContentResolver contentResolver = this.getContentResolver();

        Cursor cursor = contentResolver.query(CONTENT_URI, null,null, null, null);

        // Loop for every contact in the phone
        if (cursor.getCount() > 0) {

            while (cursor.moveToNext()) {

                String contact_id = cursor.getString(cursor.getColumnIndex( _ID ));
                String name = cursor.getString(cursor.getColumnIndex( DISPLAY_NAME ));
                String phoneNumber = "";
                String email = "";

                int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex( HAS_PHONE_NUMBER )));

                if (hasPhoneNumber > 0) {
                    // Query and loop for every phone number of the contact
                    Cursor phoneCursor = contentResolver.query(PhoneCONTENT_URI, null, Phone_CONTACT_ID + " = ?", new String[] { contact_id }, null);

                    while (phoneCursor.moveToNext()) {
                        phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(NUMBER));
                    }

                    phoneCursor.close();

                    // Query and loop for every email of the contact
                    Cursor emailCursor = contentResolver.query(EmailCONTENT_URI,null, EmailCONTACT_ID+ " = ?", new String[] { contact_id }, null);

                    while (emailCursor.moveToNext()) {
                        email = emailCursor.getString(emailCursor.getColumnIndex(DATA));
                    }

                    emailCursor.close();
                }

                int lookupKeyIndex = cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY);
                String currentLookupKey = cursor.getString(lookupKeyIndex);
                int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
                long currentId = cursor.getLong(idIndex);
                Uri contactUri = ContactsContract.Contacts.getLookupUri(currentId, currentLookupKey);

                // Modify the phone number string to look great!
                if (phoneNumber.contains("010") && !phoneNumber.contains("-")) {
                    phoneNumber = new StringBuilder(phoneNumber).insert(3, "-").toString();
                    phoneNumber = new StringBuilder(phoneNumber).insert(phoneNumber.length()-4, "-").toString();
                }

                Profile profile = new Profile(name, phoneNumber, email, contactUri);
                _profiles_data.add(profile);
            }
        }
        Collections.sort(_profiles_data, new CompareProfile());
        _profiles_show.addAll(_profiles_data);
    }

    public void search(String targetText) {
        _profiles_show.clear();
        targetText = targetText.toLowerCase();

        if (targetText.length() == 0) {
            _profiles_show.addAll(_profiles_data);
        }
        else {
            for (int i = 0 ; i < _profiles_data.size() ; i++) {
                if (_profiles_data.get(i).getName().toLowerCase().contains(targetText) ||
                        _profiles_data.get(i).getPhone().toLowerCase().replace("-","").contains(targetText) ||
                        _profiles_data.get(i).getEmail().toLowerCase().contains(targetText) ) {

                    _profiles_show.add(_profiles_data.get(i));
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
