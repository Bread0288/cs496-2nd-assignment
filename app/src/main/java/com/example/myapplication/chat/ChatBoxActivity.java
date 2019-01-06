package com.example.myapplication.chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.ContactsActivity;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.github.nkzawa.emitter.Emitter;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ChatBoxActivity extends AppCompatActivity {
    public RecyclerView myRecylerView ;
    public List<Message> MessageList ;
    public ChatBoxAdapter chatBoxAdapter;
    public  EditText messagetxt ;
    public  Button send ;    //declare socket object
    public String Name ;
    public String ID ;
    private Socket socket;

    private static final int REQ_IMG_FILE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //implementing socket listeners
        try {
            socket = IO.socket("http://socrip4.kaist.ac.kr:380/");
            socket.connect();
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
                                // make instance of message
                                Message m = new Message(name, message, phonenumber);
                                // add the message to the messageList
                                MessageList.add(m);
                                // add the new updated list to the adapter
                                chatBoxAdapter = new ChatBoxAdapter(ChatBoxActivity.this, MessageList );
                                // notify the adapter to update the recycler view
                                chatBoxAdapter.notifyDataSetChanged();
                                //set the adapter for the recycler view
                                myRecylerView.setAdapter(chatBoxAdapter);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
            socket.emit("join", Name);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        socket.on("userdisconnect", new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
                        Log.e("load","실행됨");
                        JSONObject data = (JSONObject) args[0];
                        try {
                            //extract data from fired event
                            String name = data.getString("senderName");
                            String message = data.getString("message");
                            String phonenumber = data.getString("phonenumber");
                            // make instance of message
                            Message m = new Message(name, message, phonenumber);
                            // add the message to the messageList
                            MessageList.add(m);
                            // add the new updated list to the adapter
                            chatBoxAdapter = new ChatBoxAdapter(ChatBoxActivity.this, MessageList );
                            // notify the adapter to update the recycler view
                            chatBoxAdapter.notifyDataSetChanged();
                            //set the adapter for the recycler view
                            myRecylerView.setAdapter(chatBoxAdapter);
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
                        String data = (String) args[0];
                        Toast.makeText(ChatBoxActivity.this, data, Toast.LENGTH_SHORT).show();
                        socket.emit("load");
                    }
                });

            }
        });

        setContentView(R.layout.activity_chat_box);

        Button logout = findViewById(R.id.logout);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.loginManager.getInstance().logOut();
                Intent reset = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(reset);
                socket.disconnect();
            }
        });

        Button fab = findViewById(R.id.chatplus_button);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(getApplicationContext(), ContactsActivity.class);
                startActivity(contactIntent);
            }
        });

        Button upload = findViewById(R.id.upload_image);

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
        send = (Button)findViewById(R.id.send);        // get the nickame of the user
        Intent intent = getIntent();
        Name= intent.getStringExtra("name");       //connect you socket client to the server
        ID = intent.getStringExtra("id");


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
                socket.emit("messagedetection", Name, messagetxt.getText().toString(), ID);
                messagetxt.setText(" ");
            }
        }
        });


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();  }
}
