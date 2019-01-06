package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.myapplication.Retrofit.IMyService;
import com.example.myapplication.Retrofit.RetrofitClient;
import com.example.myapplication.chat.ChatBoxActivity;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.rengwuxian.materialedittext.MaterialEditText;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;



public class MainActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private AccessToken accessToken;
    public static LoginManager loginManager;

    public static String ID;
    public static String NAME;
    public String PHONE;

    private final int MY_PERMISSIONS_REQUEST = 100;
    private boolean isPermissionGranted = false;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService; //보낼 data 형식 정의


    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent reset = getIntent();
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        accessToken = AccessToken.getCurrentAccessToken();

        //activity main 레이아웃을 표시

        setContentView(R.layout.activity_facebook);
    // Permission request
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_CONTACTS,
                            Manifest.permission.CALL_PHONE,
                            Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_CONTACTS,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST);
        }
        else
            isPermissionGranted = true;

        if (isPermissionGranted) {
            setContentView(R.layout.activity_facebook);
        }

        accessToken = AccessToken.getCurrentAccessToken();

        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

        if(isLoggedIn){
            ID=getAppPreferences(this, "ID");
            NAME=getAppPreferences(this, "NAME");
            Intent i = new Intent(getApplicationContext(), ChatBoxActivity.class);
            i.putExtra("id", ID);
            i.putExtra("name", NAME);
            startActivity(i);
        }

        Retrofit retrofitClient = RetrofitClient.getInstance(); //data 보내기

        iMyService = retrofitClient.create(IMyService.class);

        //facebook 로그인

        Button loginButton = (Button) findViewById(R.id.login_button);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginManager = LoginManager.getInstance();
                loginManager.logInWithReadPermissions(MainActivity.this, Arrays.asList("public_profile", "email"));
                loginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.e("result",object.toString());
                                try {
                                    loginUser(object.getString("id"), object.getString("name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,gender,birthday");
                        graphRequest.setParameters(parameters);
                        graphRequest.executeAsync();
                    }

                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.e("onError", "onError " + error.getLocalizedMessage());
                    }
                });
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //계정생성
    private void registerUser(String phonenumber, String ID, String name) {
        compositeDisposable.add(iMyService.registerUser(phonenumber, ID, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(MainActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                    }
                }));
    }

    //로그인
    private void loginUser (final String id, final String name){

        compositeDisposable.add(iMyService.loginUser(id, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        //Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                        //등록되지 않은 user이면 register 창 띄움

                        if(response.equals("\"등록된 사용자가 아닙니다.\"")) {
                            final View register_layout = LayoutInflater.from(MainActivity.this)
                                    .inflate(R.layout.register_layout, null);

                            new MaterialStyledDialog.Builder(MainActivity.this)
                                    .setIcon(R.drawable.ic_phonenumber)
                                    .setTitle("REGISTRATION")
                                    .setDescription("Please fill all fields")
                                    .setCustomView(register_layout)
                                    .setNegativeText("CANCEL")
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveText("REGISTER")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                            MaterialEditText edit_register_phonenumber = (MaterialEditText) register_layout.findViewById(R.id.edit_phonenumber);

                                            if(TextUtils.isEmpty(edit_register_phonenumber.getText().toString())){
                                                Toast.makeText(MainActivity.this, "Phone Number cannot be null or empty", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            //서버에 보내기
                                            registerUser (edit_register_phonenumber.getText().toString(), id, name);

                                            ID = id;
                                            NAME = name;
                                            setAppPreferences(MainActivity.this, "ID", id);
                                            setAppPreferences(MainActivity.this, "NAME", NAME);
                                            Intent i = new Intent(getApplicationContext(), ChatBoxActivity.class);
                                            i.putExtra("id", ID);
                                            i.putExtra("name", NAME);
                                            startActivity(i);

                                        }
                                    }).show();
                        }
                        else{
                            ID = id;
                            NAME = name;
                            setAppPreferences(MainActivity.this, "ID", ID);
                            setAppPreferences(MainActivity.this, "NAME", NAME);
                            Intent i = new Intent(getApplicationContext(), ChatBoxActivity.class);
                            i.putExtra("id", ID);
                            i.putExtra("name", NAME);
                            startActivity(i);
                        }
                    }
                }));
    }

    //로그인 유지를 위해 id와 name 정보 저장

    private void setAppPreferences(Activity context, String key, String value)
    {
        SharedPreferences pref = null;
        pref = context.getSharedPreferences("FacebookCon", 0);
        SharedPreferences.Editor prefEditor = pref.edit();
        prefEditor.putString(key, value);

        prefEditor.commit();
    }

    private String getAppPreferences(Activity context, String key)
    {
        String returnValue = null;

        SharedPreferences pref = null;
        pref = context.getSharedPreferences("FacebookCon", 0);

        returnValue = pref.getString(key, "");

        return returnValue;
    }



}