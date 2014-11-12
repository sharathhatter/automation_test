package com.bigbasket.mobileapp.view.uiv3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.ExpandableListView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.FilterByAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterProductDialog extends DialogFragment {

    private List<FilterOptionCategory> filterOptionCategories;
    private Map<String, Set<String>> filteredOn;

    private Fragment fragment;

    public FilterProductDialog() {}

    @SuppressLint("ValidFragment")
    public FilterProductDialog(Fragment fragment, List<FilterOptionCategory> filterOptionCategories,
                               Map<String, Set<String>> filteredOn) {
        this.fragment = fragment;
        this.filterOptionCategories = filterOptionCategories;
        this.filteredOn = filteredOn;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ExpandableListView expandableListView = new ExpandableListView(getActivity());
        FilterByAdapter filterByAdapter = new FilterByAdapter(filterOptionCategories, filteredOn, getActivity());
        expandableListView.setAdapter(filterByAdapter);
        expandableListView.setGroupIndicator(null);
        builder.setTitle(R.string.filterOn)
                .setView(expandableListView)
                .setPositiveButton(R.string.filter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((ProductListAwareFragment) fragment).loadProducts();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNeutralButton(R.string.clear, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (Map.Entry<String, Set<String>> filteredOnOption : filteredOn.entrySet()) {
                            filteredOnOption.getValue().clear();
                        }
                        for (FilterOptionCategory filterOptionCategory : filterOptionCategories) {
                            for (FilterOptionItem filterOptionItem : filterOptionCategory.getFilterOptionItems()) {
                                filterOptionItem.setSelected(false);
                            }
                        }
                        ((ProductListAwareFragment) fragment).loadProducts();
                    }
                });
        return builder.create();
    }
}
