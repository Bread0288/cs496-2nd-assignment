package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
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
    public String ID;
    public String NAME;
    public String PHONE;
    private Button btn;
    private EditText nickname;
    public static final String NICKNAME = "usernickname";

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
        //FacebookSdk.sdkInitialize(this.getApplicationContext());
        //activity main 레이아웃을 표시

        setContentView(R.layout.activity_facebook);

        Retrofit retrofitClient = RetrofitClient.getInstance(); //data 보내기

        iMyService = retrofitClient.create(IMyService.class);


        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
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
                Log.e("LoginErr",error.toString());
            }
        });


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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

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

    private void loginUser (final String id, final String name){

        compositeDisposable.add(iMyService.loginUser(id, name)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
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
                                        }
                                    }).show();
                        }
                        else{
                            ID=id;
                            NAME=name;
                            Intent i = new Intent(getApplicationContext(), ChatBoxActivity.class);
                            i.putExtra(id,name);
                            startActivity(i);
                        }
                    }
                }));
    }

}