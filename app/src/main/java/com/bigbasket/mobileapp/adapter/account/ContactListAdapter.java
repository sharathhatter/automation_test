package com.bigbasket.mobileapp.adapter.account;

/**
 * Created by jugal on 23/12/14.
 */

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.provider.ContactsContract;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ContactListActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ContactNumberAware;

public class ContactListAdapter<T> extends CursorAdapter{

    private T ctx;
    private Cursor contactCursor;
    private LayoutInflater inflater;

    public ContactListAdapter(T context, Cursor contactCursor) {
        super(((ActivityAware)context).getCurrentActivity(), contactCursor, false);
        this.ctx = context;
        this.contactCursor = contactCursor;
        inflater = LayoutInflater.from(((ActivityAware)context).getCurrentActivity());
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.checkbox);
        if (hasPhone.equals("1") || Boolean.parseBoolean(hasPhone)) {
            final String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if (!TextUtils.isEmpty(contactId) && !TextUtils.isEmpty(name)) {
                view.setVisibility(View.VISIBLE);
                checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            ((ContactListActivity) ctx).onContactNumberSelected(contactId);
                        } else {
                            ((ContactListActivity) ctx).onContactNumberNotSelected(contactId);
                        }
                    }
                });
                TextView txtContactName = (TextView) view.findViewById(R.id.txtContactName);
                txtContactName.setText(name);
                checkbox.setChecked(((ContactNumberAware) ctx).getSelectedContacts() != null &&
                        ((ContactNumberAware) ctx).getSelectedContacts().contains(contactId));

            }else {
                view.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        final View view = inflater.inflate(R.layout.uiv3_contact_list, parent, false);
        bindView(view, context, cursor);
        return view;
    }
}
