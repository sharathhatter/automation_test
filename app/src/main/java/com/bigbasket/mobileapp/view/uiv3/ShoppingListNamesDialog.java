package com.bigbasket.mobileapp.view.uiv3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.MobileApiUrl;

import java.util.LinkedList;
import java.util.List;

public class ShoppingListNamesDialog extends DialogFragment {
    private List<ShoppingListName> shoppingListNames;
    private List<ShoppingListName> selectedShoppingList;
    private Context context;
    private BaseFragment fragment;

    public ShoppingListNamesDialog(List<ShoppingListName> shoppingListNames, Context context, BaseFragment fragment) {
        this.shoppingListNames = shoppingListNames;
        this.context = context;
        this.selectedShoppingList = new LinkedList<>();
        this.fragment = fragment;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        String shopListNamesArray[] = new String[shoppingListNames.size()];
        boolean shopListCheckedArray[] = new boolean[shoppingListNames.size()];
        for (int i = 0; i < shoppingListNames.size(); i++) {
            shopListNamesArray[i] = shoppingListNames.get(i).getName();
            shopListCheckedArray[i] = false;
        }
        builder.setTitle(R.string.addItemToShopList)
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
                        if (selectedShoppingList == null || selectedShoppingList.size() == 0) {
                            Toast.makeText(context, getString(R.string.chooseShopList), Toast.LENGTH_SHORT).show();
                        } else {
                            ShoppingListDoAddDeleteTask shoppingListDoAddDeleteTask =
                                    new ShoppingListDoAddDeleteTask(fragment,
                                            MobileApiUrl.getBaseAPIUrl() + "sl-add-item/", selectedShoppingList,
                                            ShoppingListOption.ADD_TO_LIST);
                            shoppingListDoAddDeleteTask.execute();
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
