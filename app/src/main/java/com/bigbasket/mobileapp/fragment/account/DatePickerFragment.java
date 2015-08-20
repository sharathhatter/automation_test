package com.bigbasket.mobileapp.fragment.account;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;

import com.bigbasket.mobileapp.R;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    View view;

    public DatePickerFragment() {
    }

    @SuppressLint("ValidFragment")
    public DatePickerFragment(View view) {
        this.view = view;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        EditText editTextDob = (EditText) this.view.findViewById(R.id.editTextDob);
        editTextDob.setText(month + 1 + "/" + day + "/" + year);
        // Do something with the date chosen by the user
    }
}
