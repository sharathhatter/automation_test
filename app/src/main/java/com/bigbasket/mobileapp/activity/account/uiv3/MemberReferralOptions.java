package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;

/**
 * Created by jugal on 23/12/14.
 */
public class MemberReferralOptions extends BackButtonActivity {

    private String text = "Jugal Joshi has referred you for https://bigbasket.com";
    private static final ArrayList<Integer> referralImageArrayList = new ArrayList<>();
    private static final ArrayList<String> referralStringArrayList = new ArrayList<>();

    static {
        referralImageArrayList.add(R.drawable.ic_action_chat);
        referralImageArrayList.add(R.drawable.whatsapp_icon);
        referralImageArrayList.add(R.drawable.com_facebook_inverse_icon);
        referralImageArrayList.add(R.drawable.ic_action_email);
        referralImageArrayList.add(R.drawable.btn_gplus_normal);
        referralImageArrayList.add(R.drawable.main_nav_login_arrow);
    }

    static {
        referralStringArrayList.add("Free SMS");
        referralStringArrayList.add("WhatsApp");
        referralStringArrayList.add("Facebook");
        referralStringArrayList.add("BigBasket Email");
        referralStringArrayList.add("Google+");
        referralStringArrayList.add("Share via Other");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Refer Friends");
        renderMemberReferralList();
    }

    private void renderMemberReferralList() {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        base.addView(contentView);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        for (int i = 0; i < referralImageArrayList.size(); i++) {
            View view = inflater.inflate(R.layout.uiv3_list_icon_and_text_row, contentView, false);
            final RelativeLayout layoutRow = (RelativeLayout) view.findViewById(R.id.layoutRow);
            layoutRow.setId(i);

            ImageView itemImg = (ImageView) view.findViewById(R.id.itemImg);
            itemImg.setImageResource(referralImageArrayList.get(i));
            TextView itemTitle = (TextView) view.findViewById(R.id.itemTitle);
            itemTitle.setText(referralStringArrayList.get(i));
            layoutRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    messageHandler(view);
                }
            });
            contentView.addView(view);
        }

    }

    private void messageHandler(View view) {
        switch (view.getId()) {
            case 0:
                sendFreeSMS();
                break;
            case 1:
                sendWhatsAppMsg(text);
                break;
            case 2:
                useFacebookReferral(text);
                break;
            case 3:
                useBBmail();
                break;
            case 4:
                useGplus(text);
                break;
            case 5:
                useOther(text);
                break;
        }
    }


    private void sendFreeSMS() {
        showProgressDialog(getString(R.string.please_wait));
        getMobileNumber();
        hideProgressDialog();
    }


    private void getMobileNumber() {
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        ArrayList<String> arrayListContactName = new ArrayList<>();
        ArrayList<String> arrayListPhoneNumber = new ArrayList<>();
        while (cursor.moveToNext()) {
            String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if ("1".equals(hasPhone) || Boolean.parseBoolean(hasPhone)) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactId, null, null);
                while (phones.moveToNext()) {
                    String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    int isType = phones.getInt(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                    final boolean isMobile =
                            isType == ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE ||
                                    isType == ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;

                    if (isMobile) {
                        arrayListContactName.add(name);
                        arrayListPhoneNumber.add(phoneNumber);
                    }

                }
                phones.close();
            }
        }
        createContactList(arrayListPhoneNumber, arrayListContactName);
    }

    private void createContactList(ArrayList<String> arrayListContactNumber,
                                   ArrayList<String> arrayListContactName) {
        Intent intent = new Intent(getCurrentActivity(), ContactListActivity.class);
        intent.putExtra("arrayListContactNumber", arrayListContactNumber);
        intent.putExtra("arrayListContactName", arrayListContactName);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (NavigationCodes.CONTACT_NUMBET_SELECTED):
                ArrayList<String> selectedContactNumber = (ArrayList<String>) data.getSerializableExtra(Constants.CONTACT_SELECTED);
                sendServerPhoneNumber(selectedContactNumber);
                break;
            default:
                super.onActivityResult(reqCode, resultCode, data);
        }
    }

    private void sendServerPhoneNumber(ArrayList<String> selectedContactNumber) {
        Toast.makeText(getApplicationContext(), "Selected contacts: " + selectedContactNumber.size(),
                Toast.LENGTH_SHORT).show();

    }

    public void sendWhatsAppMsg(String message) {
        Intent waIntent = new Intent(Intent.ACTION_SEND);
        waIntent.setType("text/plain");
        waIntent.setPackage("com.whatsapp");
        if (waIntent != null) {
            waIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(waIntent, message));
        } else {
            Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void useFacebookReferral(String message) {
        Intent fbIntent = new Intent(Intent.ACTION_SEND);
        fbIntent.setType("text/plain");
        fbIntent.setPackage("com.facebook.katana");
        if (fbIntent != null) {
            fbIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(fbIntent, message));
        } else {
            Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void useBBmail() {
        // open form for email
    }

    private void useGplus(String message) {
        Intent gplusIntent = new Intent(Intent.ACTION_SEND);
        gplusIntent.setType("text/plain");
        gplusIntent.setPackage("com.google.android.apps.plus");
        if (gplusIntent != null) {
            gplusIntent.putExtra(Intent.EXTRA_TEXT, message);
            startActivity(Intent.createChooser(gplusIntent, message));
        } else {
            Toast.makeText(this, "WhatsApp not found", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void useOther(String message) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

}
