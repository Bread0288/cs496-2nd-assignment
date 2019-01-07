package com.example.myapplication.chat;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.myapplication.MainActivity;
import com.example.myapplication.R;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;

import java.util.ArrayList;
import java.util.List;

public class ChatBoxAdapter extends RecyclerView.Adapter<ChatBoxAdapter.MyViewHolder> {
    private List<Message> MessageList;
    private Context _context;
    View itemView;


    public  class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView username;
        public TextView message;
        public ImageView image;

        public TextView ProfilePhone;
        public TextView ProfileName;

        public MyViewHolder(View view) {
            super(view);
            username = itemView.findViewById(R.id.name);
            message = itemView.findViewById(R.id.message);
            image = itemView.findViewById(R.id.uploadedimage);;
        }
    }

    public ChatBoxAdapter(Context context, List<Message>MessagesList) {
        this.MessageList = MessagesList;
        _context=context;
    }

        @Override
        public int getItemCount() {
        return MessageList.size();
    }
    @Override
    public ChatBoxAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.e("adapter",viewType+"");
        if(viewType==1){
            itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item_right, parent, false);
        }
        else{
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_item_left, parent, false);
        }
        return new ChatBoxAdapter.MyViewHolder(itemView);
    }
        //메세지 클릭하면 user 프로필 띄우기


    @Override
    public int getItemViewType(int position) {
        if(MainActivity.NAME.equals(MessageList.get(position).getName())){
            return 1;
        }
        else{
            return 0;
        }
    }

    @Override
    public void onBindViewHolder(final ChatBoxAdapter.MyViewHolder holder, final int position) {
        final Message m = MessageList.get(position);
        Bitmap decodedImage = null;
            holder.username.setText(m.getName());
            if(m.getImage()=="null"){
                holder.message.setText(m.getMessage() );
                holder.message.setVisibility(View.VISIBLE);
            }
            else {
                byte[] decodedString = Base64.decode(m.getImage(), Base64.DEFAULT);
                decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                decodedImage = resizeBitmap(decodedImage);
                holder.image.setImageBitmap(decodedImage);
                holder.message.setVisibility(View.INVISIBLE);
                holder.image.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {


                    }
                });
            }
                holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final View user_profile = LayoutInflater.from(_context)
                            .inflate(R.layout.user_profile, null);
                    holder.ProfileName = user_profile.findViewById(R.id.profilename);
                    holder.ProfilePhone = user_profile.findViewById(R.id.profilephone);
                    holder.ProfileName.setText(m.getName());
                    holder.ProfilePhone.setText(m.getPhonenumber());


                    new MaterialStyledDialog.Builder(_context)
                            .setIcon(R.drawable.ic_phonenumber)
                            .setTitle("PROFILE")
                            .setCustomView(user_profile)
                            .setNegativeText("닫기")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    dialog.dismiss();
                                }
                            })
                            .setPositiveText("주소록에 추가")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                    ArrayList<ContentProviderOperation> list = new ArrayList<>();
                                    try{
                                        list.add(
                                                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                                                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                                                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                                                        .build()
                                        );

                                        list.add(
                                                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                                                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                                                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, m.getName())   //이름

                                                        .build()
                                        );

                                        list.add(
                                                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                                                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                                                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, m.getPhonenumber())           //전화번호
                                                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE  , ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)   //번호타입(Type_Mobile : 모바일)

                                                        .build()
                                        );


                                        user_profile.getContext().getContentResolver().applyBatch(ContactsContract.AUTHORITY, list);  //주소록추가
                                        list.clear();   //리스트 초기화
                                    }catch(RemoteException e){
                                        e.printStackTrace();
                                    }catch(OperationApplicationException e){
                                        e.printStackTrace();
                                    }

                                    Toast.makeText(_context, "Contact added", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            }).show();

                }
            });
    }

    static public Bitmap resizeBitmap(Bitmap original) {

        int resizeWidth = 400;

        double aspectRatio = (double) original.getHeight() / (double) original.getWidth();
        int targetHeight = (int) (resizeWidth * aspectRatio);
        Bitmap result = Bitmap.createScaledBitmap(original, resizeWidth, targetHeight, false);
        if (result != original) {
            original.recycle();
        }
        return result;
    }

}