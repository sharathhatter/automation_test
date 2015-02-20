package com.bigbasket.mobileapp.view.uiv3;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.InfiniteProductListAware;
import com.bigbasket.mobileapp.interfaces.SortAware;
import com.bigbasket.mobileapp.model.product.Option;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class SortProductDialog extends DialogFragment {
    private ArrayList<Option> sortOptions;
    private String sortedOn;

    public SortProductDialog() {
    }

    public static SortProductDialog newInstance(String sortedOn, ArrayList<Option> sortOptions) {
        SortProductDialog sortProductDialog = new SortProductDialog();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SORT_ON, sortedOn);
        bundle.putParcelableArrayList(Constants.PRODUCT_SORT_OPTION, sortOptions);
        sortProductDialog.setArguments(bundle);
        return sortProductDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sortOptions = getArguments().getParcelableArrayList(Constants.PRODUCT_SORT_OPTION);
        this.sortedOn = getArguments().getString(Constants.SORT_ON);
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String[] sortOptionsArray = new String[sortOptions.size()];
        int checkedIdx = 0;
        for (int i = 0; i < sortOptions.size(); i++) {
            Option option = sortOptions.get(i);
            sortOptionsArray[i] = option.getSortName();
            if (option.getSortSlug().equals(sortedOn)) {
                checkedIdx = i;
            }
        }

        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.sortBy)
                .positiveText(R.string.sort)
                .negativeText(R.string.cancel)
                .items(sortOptionsArray)
                .itemsCallbackSingleChoice(checkedIdx, new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence text) {
                        Option option = sortOptions.get(which);
                        if (!option.getSortName().equals(sortedOn)) {
                            ((SortAware) getTargetFragment()).setSortedOn(option.getSortSlug());
                            ((InfiniteProductListAware) getTargetFragment()).loadProducts();
                        }
                    }
                })
                .build();
    }
}
