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
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;

public class EditShoppingDialog extends DialogFragment {

    private ShoppingListName shoppingListName;

    public static EditShoppingDialog newInstance(ShoppingListName shoppingListName) {
        EditShoppingDialog editShoppingDialog = new EditShoppingDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SHOPPING_LIST_NAME, shoppingListName);
        editShoppingDialog.setArguments(bundle);
        return editShoppingDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.shoppingListName = getArguments().getParcelable(Constants.SHOPPING_LIST_NAME);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.uiv3_edit_shopping_list_name, null);
        final EditText editTextShoppingListName = (EditText) view.findViewById(R.id.editTextShoppingListName);
        editTextShoppingListName.setText(shoppingListName.getName());
        builder.setView(view)
                .setPositiveButton(R.string.change, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = editTextShoppingListName.getText().toString();
                        if (!newName.equals(shoppingListName.getName())) {
                            ((ShoppingListFragment) getTargetFragment()).editShoppingListName(shoppingListName, newName);
                        }
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
