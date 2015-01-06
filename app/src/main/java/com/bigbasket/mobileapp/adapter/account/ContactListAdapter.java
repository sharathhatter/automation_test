package com.bigbasket.mobileapp.adapter.account;

/**
 * Created by jugal on 23/12/14.
 */

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ContactNumberAware;

import java.util.ArrayList;

public class ContactListAdapter extends BaseAdapter{

    private Context context;
    ArrayList<String> arrayListContactNumber;
    private ArrayList<String> arrayListContactName;
    private ContactNumberAware contactNumberAware;

    public ContactListAdapter(Context context, ArrayList<String> arrayListContactNumber,
                              ArrayList<String> arrayListContactName, ContactNumberAware contactNumberAware) {
        this.context = context;
        this.arrayListContactNumber = arrayListContactNumber;
        this.arrayListContactName = arrayListContactName;
        this.contactNumberAware = contactNumberAware;
    }

    @Override
    public int getCount() {
        return arrayListContactNumber.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayListContactNumber.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ContactRowHolder rowHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.uiv3_contact_list, null);
            rowHolder = new ContactRowHolder(convertView);
            convertView.setTag(rowHolder);
        } else {
            rowHolder = (ContactRowHolder) convertView.getTag();
        }

        TextView txtContactName = rowHolder.getTxtContactName();
        txtContactName.setText(arrayListContactName.get(position));

        TextView txtContactNumber = rowHolder.getTxtContactNumber();
        txtContactNumber.setText(arrayListContactName.get(position));

        CheckBox checkBox = rowHolder.getCheckbox();
        if(checkBox.isChecked())
            checkBox.setChecked(true);
        else
            checkBox.setChecked(false);

        convertView.setId(position);

        return convertView;
    }


    private class ContactRowHolder implements View.OnClickListener {
        private View base;
        private TextView txtContactName;
        private TextView txtContactNumber;
        private CheckBox checkbox;

        private ContactRowHolder(View base) {
            this.base = base;
        }

        public TextView getTxtContactName() {
            if (txtContactName == null) {
                txtContactName = (TextView) base.findViewById(R.id.txtContactName);
            }
            return txtContactName;
        }

        public TextView getTxtContactNumber() {
            if (txtContactNumber == null) {
                txtContactNumber = (TextView) base.findViewById(R.id.txtContactNumber);
            }
            return txtContactNumber;
        }

        public CheckBox getCheckbox() {
            if (checkbox == null) {
                checkbox = (CheckBox) base.findViewById(R.id.checkbox);
            }
            return checkbox;
        }

        @Override
        public void onClick(View view) {
            String contactNumber = arrayListContactNumber.get(view.getId());
            contactNumberAware.onContactNumberSelected(contactNumber);
        }
    }
}
