package com.bigbasket.mobileapp.view.uiv3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;

public class CreateShoppingListDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.uiv3_editable_dialog, null);
        final EditText editTextShoppingListName = (EditText) view.findViewById(R.id.editTextDialog);
        editTextShoppingListName.setHint(R.string.shoppingListNameDialogTextHint);
        builder.setView(view)
                .setTitle(R.string.createShoppingList)
                .setPositiveButton(R.string.createList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ShoppingListFragment) getTargetFragment()).createShoppingList(editTextShoppingListName.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }
}
