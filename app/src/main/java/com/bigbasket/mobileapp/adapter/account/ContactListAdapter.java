package com.bigbasket.mobileapp.adapter.account;

/**
 * Created by jugal on 23/12/14.
 */

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ContactListActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ContactNumberAware;

public class ContactListAdapter<T> extends CursorAdapter implements Filterable {

    private T ctx;
    private LayoutInflater inflater;

    public ContactListAdapter(T context, Cursor contactCursor) {
        super(((ActivityAware) context).getCurrentActivity(), contactCursor, false);
        this.ctx = context;
        inflater = LayoutInflater.from(((ActivityAware) context).getCurrentActivity());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String displayName = cursor.getString(1);
        final String number = cursor.getString(2);
        view.setVisibility(View.VISIBLE);
        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ((ContactListActivity) ctx).onContactNumberSelected(number);
                } else {
                    ((ContactListActivity) ctx).onContactNumberNotSelected(number);
                }
            }
        });
        TextView txtContactName = (TextView) view.findViewById(R.id.txtContactName);
        txtContactName.setText(displayName);
        checkbox.setChecked(((ContactNumberAware) ctx).getSelectedContacts() != null &&
                ((ContactNumberAware) ctx).getSelectedContacts().contains(number));

        TextView txtContactNumber = (TextView) view.findViewById(R.id.txtContactNumber);
        txtContactNumber.setText(number + "(" + getNumberType(cursor.getInt(3)) + ")");
    }


    private String getNumberType(int type){
        switch (type){
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                return "Home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                return "Mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                return "Work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE:
                return "Mobile";
            default:
                return "Other";
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return inflater.inflate(R.layout.uiv3_contact_list, parent, false);
    }
}
