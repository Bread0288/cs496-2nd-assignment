package com.example.myapplication;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.EventListener;

public class ProfileListAdapter extends BaseAdapter {
    private LayoutInflater _inflater;
    private ArrayList<Profile> _profiles;
    private int _layout;
    private final Context _context;
    private EventListener _listener;

    public ProfileListAdapter(Context context, EventListener listener, int layout, ArrayList profiles) {
        _inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _profiles = profiles;
        _layout = layout;
        _context = context;
        _listener = listener;
    }

    public interface EventListener {
        void reloadContacts();
    }

    @Override
    public int getCount() {
        return _profiles.size();
    }

    @Override
    public String getItem(int pos) {
        return _profiles.get(pos).getName();
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

    @Override
    public View getView(int pos, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = _inflater.inflate(_layout, parent, false);
        }

        final Profile profile = _profiles.get(pos);

        TextView name = convertView.findViewById(R.id.name);
        name.setText(profile.getName());

        TextView phone = convertView.findViewById(R.id.phone);
        phone.setText(profile.getPhone());

        TextView email = convertView.findViewById(R.id.email);
        email.setText(profile.getEmail());

        LinearLayout make_call = convertView.findViewById(R.id.phone_layout);
        LinearLayout make_mail = convertView.findViewById(R.id.email_layout);
        TextView modify_contact = convertView.findViewById(R.id.name);
        TextView invite_contact = convertView.findViewById(R.id.inviteButton);

        make_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri phone_number = Uri.parse("tel:" + profile.getPhone());
                Intent callIntent = new Intent(Intent.ACTION_DIAL, phone_number);
                callIntent.putExtra("finishActivityOnSaveCompleted", true);
                _context.startActivity(callIntent);
            }
        });
        make_mail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("message/rfc822");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {profile.getEmail()});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "");
                emailIntent.putExtra("finishActivityOnSaveCompleted", true);
                _context.startActivity(Intent.createChooser(emailIntent, "Send email to " + profile.getName()));
            }
        });
        modify_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactIntent = new Intent(Intent.ACTION_EDIT);
                contactIntent.setDataAndType(profile.getContactUri(), ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                contactIntent.putExtra("finishActivityOnSaveCompleted", true);
                _context.startActivity(contactIntent);
            }
        });
        invite_contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog diaBox = AskOption(profile.getPhone(), profile.getName());
                diaBox.show();
            }
        });

        return convertView;
    }
    private AlertDialog AskOption(final String phone, String name) {
        final SmsManager sms = SmsManager.getDefault();
        Log.e("SMS",phone);
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(_context)
                //set message, title, and icon
                .setTitle("Invite")
                .setMessage("CHATAPP으로 "+name+"님을 초대하시겠습니까? 확인을 누르시면 메세지 앱으로 이동합니다.")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {

                        String smsNumber = phone;
                        String smsText = "CHATAPP을 설치하고 친구와 채팅을 즐기세요!\n https://github.com/Bread0288/cs496-2nd-assignment";

                        Uri uri = Uri.parse("smsto:" + smsNumber);
                        Intent intent = new Intent(Intent.ACTION_SENDTO, uri);
                        intent.putExtra("sms_body", smsText);
                        _context.startActivity(intent);

                        Log.e("SMS",phone);
                        //sms.sendTextMessage("01044685779", null, "CHATAPP을 설치하고 친구와 채팅을 즐기세요!\n https://github.com/Bread0288/cs496-2nd-assignment", null, null);
                        Log.e("SMS",phone);
                        //dialog.dismiss();
                    }

                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("SMS",phone);
                        dialog.dismiss();
                    }
                })
                .create();
        Log.e("SMS",phone);
        return myQuittingDialogBox;
    }
}
