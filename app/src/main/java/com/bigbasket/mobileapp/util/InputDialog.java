package com.bigbasket.mobileapp.util;

import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;

public class InputDialog<T> {
    private T context;

    @StringRes
    private int positiveBtnTxt;

    @StringRes
    private int negativeBtnTxt;

    @StringRes
    private int title;

    @StringRes
    private int hint;

    private int inputType;

    private String text;

    public InputDialog(T context, @StringRes int positiveBtnTxt, @StringRes int negativeBtnTxt,
                       @StringRes int title, @StringRes int hint) {
        this.context = context;
        this.positiveBtnTxt = positiveBtnTxt;
        this.negativeBtnTxt = negativeBtnTxt;
        this.title = title;
        this.hint = hint;
        this.inputType = InputType.TYPE_NULL;
    }

    public InputDialog(T context, @StringRes int positiveBtnTxt, @StringRes int negativeBtnTxt,
                       @StringRes int title, @StringRes int hint, String text) {
        this(context, positiveBtnTxt, negativeBtnTxt, title, hint);
        this.text = text;
    }

    public InputDialog(T context, @StringRes int positiveBtnTxt, @StringRes int negativeBtnTxt,
                       @StringRes int title, @StringRes int hint, int inputType) {
        this(context, positiveBtnTxt, negativeBtnTxt, title, hint);
        this.inputType = inputType;
    }

    public void show() {
        final BaseActivity activity = ((ActivityAware) context).getCurrentActivity();
        View base = activity.getLayoutInflater().inflate(R.layout.uiv3_editable_dialog, null);
        final EditText editTextDialog = (EditText) base.findViewById(R.id.editTextDialog);
        editTextDialog.setHint(hint);
        if (inputType != InputType.TYPE_NULL) {
            editTextDialog.setInputType(inputType);
        }
        if (!TextUtils.isEmpty(text)) {
            editTextDialog.setText(text);
        }
        editTextDialog.setTypeface(FontHolder.getInstance(activity).getFaceRobotoRegular());

        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(title)
                .setView(base)
                .setPositiveButton(positiveBtnTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseActivity.hideKeyboard(activity, editTextDialog);
                        onPositiveButtonClicked(editTextDialog.getText() != null ?
                                editTextDialog.getText().toString().trim() : "");
                    }
                })
                .setNegativeButton(negativeBtnTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BaseActivity.hideKeyboard(activity, editTextDialog);
                        onNegativeButtonClicked();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new OnDialogShowListener());
        alertDialog.show();
    }

    public void onPositiveButtonClicked(String inputText) {

    }

    public void onNegativeButtonClicked() {

    }
}
