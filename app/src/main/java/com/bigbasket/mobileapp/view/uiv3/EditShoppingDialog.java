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
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

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
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.uiv3_editable_dialog, null);
        final EditText editTextShoppingListName = (EditText) view.findViewById(R.id.editTextDialog);
        editTextShoppingListName.setText(shoppingListName.getName());
        editTextShoppingListName.setHint(R.string.shoppingListNameDialogTextHint);

        return UIUtil.getMaterialDialogBuilder(getActivity())
                .customView(view, false)
                .title(R.string.changeShoppingListName)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String newName = editTextShoppingListName.getText().toString();
                        if (!newName.equals(shoppingListName.getName())) {
                            ((ShoppingListFragment) getTargetFragment()).editShoppingListName(shoppingListName, newName);
                        }

                    }
                })
                .build();
    }
}
