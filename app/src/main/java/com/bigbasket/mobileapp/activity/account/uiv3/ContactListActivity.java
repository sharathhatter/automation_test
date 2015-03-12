package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.account.ContactListAdapter;
import com.bigbasket.mobileapp.interfaces.ContactNumberAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;


public class ContactListActivity extends BackButtonActivity implements ContactNumberAware {

    private ArrayList<String> selectedContactNo = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Select Contacts");
        getMobileNumber();
    }

    private void getMobileNumber() {
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{
                        ContactsContract.CommonDataKinds.Phone._ID,
                        //ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        //ContactsContract.CommonDataKinds.Photo.PHOTO
                },
                "LENGTH(" + ContactsContract.CommonDataKinds.Phone.NUMBER + ")>=10",
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        );
        if (cursor != null && (cursor.moveToFirst() || cursor.getCount() != 0))
            renderContactListActivity(cursor);

    }

    private void renderContactListActivity(final Cursor cursor) {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);

        final LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.contact_search_list, null);
        final ContactListAdapter contactListAdapter = new ContactListAdapter<>(this, cursor);
        EditText editTxtSearch = (EditText) view.findViewById(R.id.editTxtSearch);

        editTxtSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                System.out.println("Text [" + s + "]");
                contactListAdapter.getFilter().filter(s.toString());
                //contactListAdapter.notifyDataSetChanged();
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        contactListAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            StringBuilder buffer = null;
            String[] args = null;

            @Override
            public Cursor runQuery(CharSequence constraint) {
                constraint = constraint.toString().trim();
                buffer = new StringBuilder();
                buffer.append("UPPER(").append(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME).append(") GLOB ?");
                buffer.append(" AND ");
                buffer.append("LENGTH(").append(ContactsContract.CommonDataKinds.Phone.NUMBER).append(")>=10");
                args = new String[]{constraint.toString().toUpperCase() + "*"};

                return getContentResolver().query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        new String[]{
                                ContactsContract.CommonDataKinds.Phone._ID,
                                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER,
                                ContactsContract.CommonDataKinds.Phone.TYPE,
                        },
                        buffer == null ? null : buffer.toString(),
                        args,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
                );
            }
        });

        ListView phoneListView = (ListView) view.findViewById(R.id.listWithFixedFooter);

        Button btnSelectNumber = (Button) view.findViewById(R.id.btnListFooter);
        btnSelectNumber.setTypeface(faceRobotoRegular);
        btnSelectNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                data.putExtra(Constants.CONTACT_SELECTED, selectedContactNo);
                setResult(NavigationCodes.CONTACT_NUMBER_SELECTED, data);
                getCurrentActivity().finish();
            }
        });
        base.addView(view);

        phoneListView.setDividerHeight(1);

        phoneListView.setAdapter(contactListAdapter);
    }

    public void onContactNumberSelected(String mobNumber) {
        selectedContactNo.add(mobNumber);
    }

    public void onContactNumberNotSelected(String mobNumber) {
        if (selectedContactNo.contains(mobNumber))
            selectedContactNo.remove(mobNumber);
    }

    public ArrayList<String> getSelectedContacts() {
        return selectedContactNo;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.CONTACT_REFERRAL_SCREEN;
    }
}
