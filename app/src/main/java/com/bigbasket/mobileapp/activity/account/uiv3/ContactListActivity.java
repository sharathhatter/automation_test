package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;
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

/**
 * Created by jugal on 23/12/14.
 */
public class ContactListActivity extends BackButtonActivity implements ContactNumberAware {

    private ArrayList<String> selectedContactNumber = new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select Contacts");
        ArrayList<String> arrayListContactNumber =
                (ArrayList<String>)getIntent().getSerializableExtra("arrayListContactNumber");
        ArrayList<String> arrayListContactName = (ArrayList<String>)getIntent().getSerializableExtra("arrayListContactName");
        renderContactListActivity(arrayListContactNumber, arrayListContactName);
    }

    private void renderContactListActivity(ArrayList<String> arrayListContactNumber,
                                           ArrayList<String> arrayListContactName){
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
                data.putExtra(Constants.CONTACT_SELECTED, selectedContactNumber);
                setResult(NavigationCodes.CONTACT_NUMBET_SELECTED, data);
            }
        });
        base.addView(view);

        phoneListView.setDividerHeight(1);
        ContactListAdapter contactListAdapter = new ContactListAdapter(getCurrentActivity(),
                arrayListContactNumber, arrayListContactName, this);
        phoneListView.setAdapter(contactListAdapter);
    }

    public void onContactNumberSelected(String contactNumber){
        selectedContactNumber.add(contactNumber);
    }
}
