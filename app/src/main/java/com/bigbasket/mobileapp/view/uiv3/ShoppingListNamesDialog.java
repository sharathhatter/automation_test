package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

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
        String shopListNamesArray[] = new String[shoppingListNames.size()];
        for (int i = 0; i < shoppingListNames.size(); i++) {
            shopListNamesArray[i] = shoppingListNames.get(i).getName();
        }

        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.addItemToShopList)
                .items(shopListNamesArray)
                .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMulti() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, Integer[] which, CharSequence[] text) {
                        for (int i : which) {
                            selectedShoppingList.add(shoppingListNames.get(i));
                        }
                        ((ShoppingListNamesAware) getTargetFragment()).addToShoppingList(selectedShoppingList);
                    }
                })
                .positiveText(R.string.add)
                .negativeText(R.string.cancel)
                .build();
    }
}
