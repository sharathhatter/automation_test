package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

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
        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.deleteQuestion)
                .content(R.string.deleteShoppingListText)
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        ((ShoppingListFragment) getTargetFragment()).deleteShoppingList(shoppingListName);
                    }
                })
                .build();
    }
}
