package com.example.myapplication.chat;

import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.ContactsActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ChatBoxActivity extends AppCompatActivity  {
    public RecyclerView myRecylerView ;
    public List<Message> MessageList ;
    public ChatBoxAdapter chatBoxAdapter;
    public  EditText messagetxt ;
    public  ImageButton send ;    //declare socket object
    public String Name ;
    public String ID ;
    private Socket socket;

    private static final int REQ_IMG_FILE = 1;
    private Uri filePath;
    private Bitmap bitmap;



    String selectedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //implementing socket listeners
        try {

            //Intent 값들 받아오기
            Intent intent = getIntent();
            Name= intent.getStringExtra("name");
            ID = intent.getStringExtra("id");

            //socket을 서버에 연결
            socket = IO.socket("http://socrip4.kaist.ac.kr:380/");
            socket.connect();
            socket.emit("join", Name);
            Log.e("join","방출됨");

            socket.on("load", new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("load","실행됨");
                            JSONObject data = (JSONObject) args[0];
                            try {
                                //extract data from fired event
                                String name = data.getString("senderName");
                                String message = data.getString("message");
                                String phonenumber = data.getString("phonenumber");
                                String encodedImage = data.getString("image");
                                Message m;
                                // make instance of message
                                if (name.equals(MainActivity.NAME)){
                                    m = new Message(name, message, phonenumber, encodedImage);
                                }
                                else{
                                    m = new Message(name, message, phonenumber, encodedImage);
                                }
                                // make instance of message
                                // add the message to the messageList
                                MessageList.add(m);
                                // add the new updated list to the adapter
                                chatBoxAdapter = new ChatBoxAdapter(ChatBoxActivity.this, MessageList);
                                // notify the adapter to update the recycler view
                                chatBoxAdapter.notifyDataSetChanged();

                                //set the adapter for the recycler view
                                myRecylerView.setAdapter(chatBoxAdapter);
                                myRecylerView.smoothScrollToPosition(myRecylerView.getAdapter().getItemCount());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("userdisconnect","실행됨");
                        String data = (String) args[0];
                        Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_SHORT).show();
                    }

                });

            }
        });

        socket.on("message", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("message","실행됨");
                        JSONObject data = (JSONObject) args[0];
                        try {
                            //extract data from fired event
                            String name = data.getString("senderName");
                            String message = data.getString("message");
                            String phonenumber = data.getString("phonenumber");
                            String encodedImage = data.getString("image");
                            Message m;
                            // make instance of message
                            if (name.equals(MainActivity.NAME)){
                            m = new Message(name, message, phonenumber, encodedImage);}
                            else{m = new Message(name, message, phonenumber, encodedImage);}
                            // make instance of message
                            // add the message to the messageList
                            MessageList.add(m);
                            // add the new updated list to the adapter
                            chatBoxAdapter = new ChatBoxAdapter(ChatBoxActivity.this, MessageList);
                            // notify the adapter to update the recycler view
                            chatBoxAdapter.notifyDataSetChanged();
                            //set the adapter for the recycler view
                            myRecylerView.setAdapter(chatBoxAdapter);
                            myRecylerView.smoothScrollToPosition(myRecylerView.getAdapter().getItemCount());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        socket.on("userjoinedthechat", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("userjoinedthechat","실행됨");
                        String data = (String) args[0];
                        Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_SHORT).show();
                        //Log.e("load","방출됨");
                        //socket.emit("load");
                    }
                });
            }
        });

        setContentView(R.layout.activity_chat_box);

        ImageButton logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.loginManager.getInstance().logOut();
                Intent reset = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(reset);
                socket.disconnect();
            }
        });

        ImageButton fab = findViewById(R.id.chatplus_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(getApplicationContext(), ContactsActivity.class);
                startActivity(contactIntent);
            }
        });

        ImageButton upload = findViewById(R.id.upload_image);

        upload.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                if(Build.VERSION.SDK_INT >= 18)
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setType("image/*");
                startActivityForResult(intent, REQ_IMG_FILE);
            }
        });

        messagetxt = (EditText) findViewById(R.id.message) ;

        send = (ImageButton)findViewById(R.id.send);        // get the nickame of the user

        MessageList = new ArrayList<>();
        myRecylerView = (RecyclerView) findViewById(R.id.messagelist);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        myRecylerView.setLayoutManager(mLayoutManager);
        myRecylerView.setItemAnimator(new DefaultItemAnimator());

        //message send action
        send.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //retrieve the nickname and the message content and fire the event messagedetection
            if(!messagetxt.getText().toString().isEmpty()){
                socket.emit("messagedetection", Name, messagetxt.getText().toString(), null, ID);
                messagetxt.setText(" ");
            }
        }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_IMG_FILE && data.getData() !=null){
            filePath = data.getData();
            try {
                InputStream imageStream = getContentResolver().openInputStream(filePath);
                Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                selectedImage.compress(Bitmap.CompressFormat.JPEG,100,baos);
                byte[] b = baos.toByteArray();
                String encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                Log.e("encoding", encodedImage);
                socket.emit("messagedetection", Name, null, encodedImage, ID);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }



}
