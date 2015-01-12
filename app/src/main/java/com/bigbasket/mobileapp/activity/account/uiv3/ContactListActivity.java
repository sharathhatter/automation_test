package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.account.ContactListAdapter;
import com.bigbasket.mobileapp.interfaces.ContactNumberAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;


public class ContactListActivity extends BackButtonActivity implements ContactNumberAware {

    private ArrayList<String> selectedContactIds = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select Contacts");
        getMobileNumber();
    }

    private void getMobileNumber() {
        //Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        String[] projection = new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER };
        Cursor cursor = getContentResolver().query(ContactsContract.Contacts.CONTENT_URI, null, null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
        if(cursor != null && (cursor.moveToFirst() || cursor.getCount() != 0))
            renderContactListActivity(cursor);
    }

    private void renderContactListActivity(Cursor cursor){
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);

        final LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.uiv3_list_with_action, null);
        ListView phoneListView = (ListView) view.findViewById(R.id.listWithFixedFooter);

        Button btnSelectNumber = (Button) view.findViewById(R.id.btnListFooter);
        btnSelectNumber.setText("Select");
        btnSelectNumber.setTypeface(faceRobotoRegular);
        btnSelectNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data =  new Intent();
                data.putExtra(Constants.CONTACT_SELECTED, selectedContactIds);
                setResult(NavigationCodes.CONTACT_NUMBER_SELECTED, data);
                getCurrentActivity().finish();
            }
        });
        base.addView(view);

        phoneListView.setDividerHeight(1);
        ContactListAdapter contactListAdapter = new ContactListAdapter<>(this, cursor);
        phoneListView.setAdapter(contactListAdapter);
    }

    public void onContactNumberSelected(String contactId){
        selectedContactIds.add(contactId);
    }

    public void onContactNumberNotSelected(String contactNumber){
        if(selectedContactIds.contains(contactNumber))
            selectedContactIds.remove(contactNumber);
    }

    public ArrayList<String> getSelectedContacts(){
        return selectedContactIds;
    }
}
