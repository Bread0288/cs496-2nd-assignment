package com.example.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //FacebookSdk.sdkInitialize(this.getApplicationContext());
        //activity main 레이아웃을 표시
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_contacts);


        //Main 화면의 +버튼을 누를 시 새로운 채팅방을 만들기 위해 가지고 있는 전화번호부 목록으로 넘어가는 intent 설정
        android.support.design.widget.FloatingActionButton fab = findViewById(R.id.chatplus_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Hello", "Intent Start");
                Intent contactIntent = new Intent(getApplicationContext(), ContactsActivity.class);
                startActivity(contactIntent);
            }
        });
    }

}