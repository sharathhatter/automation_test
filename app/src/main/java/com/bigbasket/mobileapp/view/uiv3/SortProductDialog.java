package com.bigbasket.mobileapp.view.uiv3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.interfaces.SortAware;
import com.bigbasket.mobileapp.model.product.Option;

import java.util.List;

public class SortProductDialog extends DialogFragment {
    private List<Option> sortOptions;
    private String sortedOn;
    private Fragment fragment;

    public SortProductDialog(Fragment fragment, String sortedOn, List<Option> sortOptions) {
        this.fragment = fragment;
        this.sortedOn = sortedOn;
        this.sortOptions = sortOptions;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        if (fragment != null) {
            String[] sortOptionsArray = new String[sortOptions.size()];
            int checkedIdx = 0;
            for (int i = 0; i < sortOptions.size(); i++) {
                Option option = sortOptions.get(i);
                sortOptionsArray[i] = option.getSortName();
                if (option.getSortSlug().equals(sortedOn)) {
                    checkedIdx = i;
                }
            }
            builder.setTitle(R.string.sortBy)
                    .setSingleChoiceItems(sortOptionsArray, checkedIdx, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Option option = sortOptions.get(which);
                            if (!option.getSortName().equals(sortedOn)) {
                                ((SortAware) fragment).setSortedOn(option.getSortSlug());
                                ((ProductListAwareFragment) fragment).loadProducts();
                                dialog.dismiss();
                            }
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        }
        return builder.create();
    }
}
