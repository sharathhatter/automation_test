package com.bigbasket.mobileapp.view.uiv3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;

public class DeleteShoppingListDialog extends DialogFragment {

    private ShoppingListName shoppingListName;

    public static DeleteShoppingListDialog newInstance(ShoppingListName shoppingListName) {
        DeleteShoppingListDialog deleteShoppingListDialog = new DeleteShoppingListDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.SHOPPING_LIST_NAME, shoppingListName);
        deleteShoppingListDialog.setArguments(bundle);
        return deleteShoppingListDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.shoppingListName = getArguments().getParcelable(Constants.SHOPPING_LIST_NAME);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.deleteQuestion)
                .setMessage(R.string.deleteShoppingListText)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ShoppingListFragment) getTargetFragment()).deleteShoppingList(shoppingListName);
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
