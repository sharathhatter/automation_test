package com.bigbasket.mobileapp.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.Contacts;
import android.provider.Contacts.ContactMethods;
import android.provider.Contacts.People;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.Filterable;
import android.widget.TextView;

public class MultipleEmailAutoComplete extends AutoCompleteTextView implements AdapterView.OnItemClickListener {

    private String previous = "";
    private String separator = " ";

    public MultipleEmailAutoComplete(final Context context, final AttributeSet attrs,
                                     final int defStyle) {
        super(context, attrs, defStyle);
        this.setThreshold(0);
        this.setUpContacts();
        setOnItemClickListener(this);
    }

    public MultipleEmailAutoComplete(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.setThreshold(0);
        this.setUpContacts();
        setOnItemClickListener(this);
    }

    public MultipleEmailAutoComplete(final Context context) {
        super(context);
        this.setThreshold(0);
        this.setUpContacts();
        setOnItemClickListener(this);
    }


    /**
     * This method filters out the existing text till the separator and launched
     * the filtering process again
     */
    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        String filterText = text.toString().trim();
        previous = filterText.substring(0,
                filterText.lastIndexOf(getSeparator()) + 1);
        filterText = filterText.substring(filterText.lastIndexOf(getSeparator()) + 1);
        if (!TextUtils.isEmpty(filterText)) {
            super.performFiltering(filterText, keyCode);
        }
    }

    /**
     * After a selection, capture the new value and append to the existing text
     */
    @Override
    protected void replaceText(final CharSequence text) {
        super.replaceText(previous + text + getSeparator());
    }

    public String getSeparator() {
        return separator;
    }

    private void setUpContacts() {
        ContactListAdapter adapter = new ContactListAdapter(getContext(), null);
        setAdapter(adapter);
    }

    @SuppressWarnings("nls")
    public static class ContactListAdapter extends CursorAdapter implements Filterable {
        public ContactListAdapter(Context context, Cursor c) {
            super(context, c);
            mContent = context.getContentResolver();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ?
                    android.R.layout.simple_dropdown_item_1line : android.R.layout.simple_list_item_1;
            final TextView view = (TextView) inflater.inflate(
                    layout, parent, false);
            view.setText(convertToString(getCursor()));
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ((TextView) view).setText(convertToString(cursor));
        }

        @Override
        public String convertToString(Cursor cursor) {
            //return cursor.getString(1) == null ? "" : cursor.getString(1) + " <" + cursor.getString(2) +">";
            return cursor.getString(1);
        }

        @Override
        public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
            if (getFilterQueryProvider() != null) {
                return getFilterQueryProvider().runQuery(constraint);
            }

            StringBuilder buffer = null;
            String[] args = null;
            if (constraint != null) {
                constraint = constraint.toString().trim();
                buffer = new StringBuilder();
                buffer.append("UPPER(").append(People.NAME).append(") GLOB ?"); //ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                buffer.append(" OR ");
                buffer.append("UPPER(").append(ContactMethods.DATA).append(") GLOB ?");
                args = new String[]{constraint.toString().toUpperCase() + "*",
                        constraint.toString().toUpperCase() + "*"};
            }

            return mContent.query(Contacts.ContactMethods.CONTENT_EMAIL_URI,
                    PEOPLE_PROJECTION, buffer == null ? null : buffer.toString(), args,
                    null); //todo remove deprecated method
        }

        private final ContentResolver mContent;
    }

    private static final String[] PEOPLE_PROJECTION = new String[]{
            People._ID,
            ContactMethods.DATA
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    }

}