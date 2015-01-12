package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jugal on 6/1/15.
 */
public class BaseReferralDialog extends DialogFragment {

    private BaseActivity baseActivity;
    private Typeface faceRobotoRegular;

    public BaseReferralDialog() {
    }

    public BaseReferralDialog(BaseActivity baseActivity, Typeface faceRobotoRegular) {
        this.baseActivity = baseActivity;
        this.faceRobotoRegular = faceRobotoRegular;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.referral_dialog, container, false);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view != null) {
            TextView txtDialogTitle = (TextView) view.findViewById(R.id.txtDialogTitle);
            txtDialogTitle.setTypeface(faceRobotoRegular, Typeface.BOLD);
            final EditText editTextListEmailAddress = (EditText) view.findViewById(R.id.editTextListEmailAddress);
            editTextListEmailAddress.setTypeface(faceRobotoRegular);
            editTextListEmailAddress.requestFocus();
            BaseActivity.showKeyboard(editTextListEmailAddress);

            final EditText editTextMessage = (EditText) view.findViewById(R.id.editTextMessage);
            editTextMessage.setTypeface(faceRobotoRegular);

            final TextView txtSubmit = (TextView) view.findViewById(R.id.txtSubmit);
            txtSubmit.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        txtSubmit.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        txtSubmit.setBackgroundColor(getResources().getColor(R.color.white));
                        String emailCommaSeparatedString = editTextListEmailAddress.getText().toString().trim();
                        if(emailCommaSeparatedString.length()>0){
                            boolean isValidEmail = true;
                            String [] strings = emailCommaSeparatedString.split(",");
                            for(String email: strings){
                                if(!UIUtil.isValidEmail(email)) {
                                    baseActivity.reportFormInputFieldError(editTextListEmailAddress, getString(R.string.invalid_email));
                                    isValidEmail = false;
                                    break;
                                }
                            }
                            if(isValidEmail) {
                                sendEmailList(emailCommaSeparatedString, editTextMessage.getText().toString());
                                BaseActivity.hideKeyboard(baseActivity,editTextListEmailAddress);
                                if (getDialog().isShowing())
                                    getDialog().dismiss();
                            }
                        }else {
                            baseActivity.reportFormInputFieldError(editTextListEmailAddress, getString(R.string.error_field_required));
                        }

                    }
                    return true;
                }
            });


            final TextView txtCancel = (TextView) view.findViewById(R.id.txtCancel);
            txtCancel.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        txtCancel.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        txtCancel.setBackgroundColor(getResources().getColor(R.color.white));
                        if (getDialog().isShowing())
                            getDialog().dismiss();
                    }
                    return true;
                }
            });

        }
    }

    public void sendEmailList(String emailList, String message) {
    }
}
