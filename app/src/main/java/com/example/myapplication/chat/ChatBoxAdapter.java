package com.example.myapplication.chat;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.List;

public class ChatBoxAdapter extends RecyclerView.Adapter<ChatBoxAdapter.MyViewHolder> {
    private List<Message> MessageList;
    public  class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView message;
        public MyViewHolder(View view) {
            super(view);
            username = (TextView) view.findViewById(R.id.name);
            message = (TextView) view.findViewById(R.id.message);

        }

    }

    public ChatBoxAdapter(List<Message>MessagesList) {
        this.MessageList = MessagesList;
    }
        @Override    public int getItemCount() {
        return MessageList.size();
    }
    @Override
    public ChatBoxAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new ChatBoxAdapter.MyViewHolder(itemView);
    }
        //메세지 클릭하면 user 프로필 띄우기
        @Override
        public void onBindViewHolder(final ChatBoxAdapter.MyViewHolder holder, final int position) {
            final Message m = MessageList.get(position);
            holder.username.setText(m.getName() +" : ");
            holder.message.setText(m.getMessage() );
            holder.username.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final View user_profile = LayoutInflater.from(new ChatBoxActivity())
                            .inflate(R.layout.user_profile, null);

                    new MaterialStyledDialog.Builder(new ChatBoxActivity())
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

                                    ContentResolver cr = user_profile.getContext().getContentResolver();
                                    ContentValues cv = new ContentValues();
                                    cv.put(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, m.getName());
                                    cv.put(ContactsContract.CommonDataKinds.Phone.NUMBER, m.getPhonenumber() );
                                    cv.put(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
                                    cr.insert(ContactsContract.RawContacts.CONTENT_URI, cv);
                                    Toast.makeText(new ChatBoxActivity(), "Contact added", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            }).show();

                }
            });
    }
}