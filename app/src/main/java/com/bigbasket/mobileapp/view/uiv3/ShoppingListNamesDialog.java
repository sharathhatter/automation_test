package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListNamesDialog extends DialogFragment {
    private List<ShoppingListName> shoppingListNames;
    private List<ShoppingListName> selectedShoppingList;

    public ShoppingListNamesDialog() {
    }

    public static ShoppingListNamesDialog newInstance(ArrayList<ShoppingListName> shoppingListNames) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.SHOPPING_LIST_NAME, shoppingListNames);
        ShoppingListNamesDialog shoppingListNamesDialog = new ShoppingListNamesDialog();
        shoppingListNamesDialog.setArguments(bundle);
        return shoppingListNamesDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.shoppingListNames = getArguments().getParcelableArrayList(Constants.SHOPPING_LIST_NAME);
        this.selectedShoppingList = new ArrayList<>();
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        String shopListNamesArray[] = new String[shoppingListNames.size()];
        boolean shopListCheckedArray[] = new boolean[shoppingListNames.size()];
        for (int i = 0; i < shoppingListNames.size(); i++) {
            shopListNamesArray[i] = shoppingListNames.get(i).getName();
            shopListCheckedArray[i] = false;
        }
        builder.setTitle(R.string.selectShoppingList)
                .setMultiChoiceItems(shopListNamesArray, shopListCheckedArray, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        if (isChecked) {
                            selectedShoppingList.add(shoppingListNames.get(which));
                        } else {
                            selectedShoppingList.remove(shoppingListNames.get(which));
                        }
                    }
                })
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ShoppingListNamesAware) getTargetFragment()).addToShoppingList(selectedShoppingList);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(new OnDialogShowListener());
        return alertDialog;
    }
}
