package com.example.myapplication.chat;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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


    public  class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView lusername;
        public TextView lmessage;
        public ImageView limage;
        public TextView rusername;
        public TextView rmessage;
        public ImageView rimage;
        public TextView ProfileName;
        public TextView ProfilePhone;

        public MyViewHolder(View view) {
            super(view);
            lusername = (TextView) view.findViewById(R.id.lname);
            lmessage = (TextView) view.findViewById(R.id.lmessage);
            limage = (ImageView) view.findViewById(R.id.luploadedimage);
            rusername = (TextView) view.findViewById(R.id.rname);
            rmessage = (TextView) view.findViewById(R.id.rmessage);
            rimage = (ImageView) view.findViewById(R.id.ruploadedimage);
        }
    }

    public ChatBoxAdapter(Context context, List<Message>MessagesList) {
        this.MessageList = MessagesList;
        _context=context;
    }
        @Override    public int getItemCount() {
        return MessageList.size();
    }
    @Override
    public ChatBoxAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.chat_item, parent, false);
        return new ChatBoxAdapter.MyViewHolder(itemView);
    }
        //메세지 클릭하면 user 프로필 띄우기
        @Override
        public void onBindViewHolder(final ChatBoxAdapter.MyViewHolder holder, final int position) {
            final Message m = MessageList.get(position);
            Bitmap decodedImage = null;
            TextView username;
            TextView message;
            ImageView image;

            if(m.getName().equals(MainActivity.NAME)){
                Log.e("right",MainActivity.NAME + m.getName());
                holder.limage.setVisibility(View.INVISIBLE);
                holder.lusername.setVisibility(View.INVISIBLE);
                holder.lmessage.setVisibility(View.INVISIBLE);
                holder.limage.setScaleY(0);
                holder.lusername.setScaleY(0);
                holder.lmessage.setScaleY(0);
                holder.lusername.setVisibility(View.INVISIBLE);
                holder.lmessage.setVisibility(View.INVISIBLE);
                holder.rimage.setVisibility(View.VISIBLE);
                holder.rusername.setVisibility(View.VISIBLE);
                holder.rmessage.setVisibility(View.VISIBLE);
                username = holder.rusername;
                message = holder.rmessage;
                image = holder.rimage;
            }
            else{
                Log.e("left",MainActivity.NAME + m.getName());
                holder.limage.setVisibility(View.VISIBLE);
                holder.lusername.setVisibility(View.VISIBLE);
                holder.lmessage.setVisibility(View.VISIBLE);
                holder.rimage.setVisibility(View.INVISIBLE);
                holder.rusername.setVisibility(View.INVISIBLE);
                holder.rmessage.setVisibility(View.INVISIBLE);
                username = holder.lusername;
                message = holder.lmessage;
                image = holder.limage;
            }

            username.setText(m.getName());
            if(m.getImage()==null){
                message.setText(m.getMessage() );
                message.setVisibility(View.VISIBLE);
                image.setVisibility(View.INVISIBLE);
            }
            else {
                byte[] decodedString = Base64.decode(m.getImage(), Base64.DEFAULT);
                decodedImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                image.setImageBitmap(decodedImage);
                message.setVisibility(View.INVISIBLE);
                image.setVisibility(View.VISIBLE);
            }

            username.setOnClickListener(new View.OnClickListener() {
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

}