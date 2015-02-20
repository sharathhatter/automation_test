package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.util.UIUtil;

public class CreateShoppingListDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.uiv3_editable_dialog, null);
        final EditText editTextShoppingListName = (EditText) view.findViewById(R.id.editTextDialog);
        editTextShoppingListName.setHint(R.string.shoppingListNameDialogTextHint);
        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.createShoppingList)
                .customView(view, false)
                .positiveText(R.string.createList)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        ((ShoppingListFragment) getTargetFragment()).
                                createShoppingList(editTextShoppingListName.getText().toString().trim());
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .build();
    }
}
