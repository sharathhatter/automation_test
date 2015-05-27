package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.AddToShoppingListAdapter;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.ArrayList;
import java.util.List;

public class ShoppingListNamesDialog extends DialogFragment {
    private List<ShoppingListName> shoppingListNames;

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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_dialog, container, false);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View view = getView();
        if (view == null || getActivity() == null) return;
        TextView txtListDialogTitle = (TextView) view.findViewById(R.id.txtListDialogTitle);
        ListView lstDialog = (ListView) view.findViewById(R.id.lstDialog);
        AddToShoppingListAdapter addToShoppingListAdapter =
                new AddToShoppingListAdapter<>(getActivity(), shoppingListNames,
                        FontHolder.getInstance(getActivity()).getFaceRobotoRegular());
        lstDialog.setAdapter(addToShoppingListAdapter);
        lstDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((ShoppingListNamesAware) getTargetFragment()).createNewShoppingList();
                } else {
                    List<ShoppingListName> selectedShoppingList = new ArrayList<>();
                    // Since 0 shows "Create New List" hence doing -1
                    selectedShoppingList.add(shoppingListNames.get(position - 1));
                    ((ShoppingListNamesAware) getTargetFragment()).addToShoppingList(selectedShoppingList);
                }
                getDialog().dismiss();
            }
        });
        txtListDialogTitle.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoRegular());
        txtListDialogTitle.setText(getActivity().getString(R.string.selectShoppingList));
    }
}
