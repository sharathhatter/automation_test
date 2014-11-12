package com.bigbasket.mobileapp.view.uiv3;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.ExpandableListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.FilterByAdapter;
import com.bigbasket.mobileapp.fragment.base.ProductListAwareFragment;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FilterProductDialog extends DialogFragment {

    private ArrayList<FilterOptionCategory> filterOptionCategories;
    private Map<String, Set<String>> filteredOn;

    public static FilterProductDialog newInstance(ArrayList<FilterOptionCategory> filterOptionCategories,
                                                  Map<String, Set<String>> filteredOn) {
        FilterProductDialog filterProductDialog = new FilterProductDialog();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.FILTER_OPTIONS, filterOptionCategories);

        ArrayList<String> filteredOnMapKeyList = new ArrayList<>();
        ArrayList<Set<String>> filteredOnMapValueList = new ArrayList<>();
        for (Map.Entry<String, Set<String>> entrySet : filteredOn.entrySet()) {
            filteredOnMapKeyList.add(entrySet.getKey());
            filteredOnMapValueList.add(entrySet.getValue());
        }
        bundle.putStringArrayList(Constants.FILTER_NAME, filteredOnMapKeyList);
        bundle.putSerializable(Constants.FILTER_VALUES, filteredOnMapValueList);
        filterProductDialog.setArguments(bundle);
        return filterProductDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        filterOptionCategories = getArguments().getParcelableArrayList(Constants.FILTER_OPTIONS);
        ArrayList<String> filteredOnMapKeyList;
        ArrayList<Set<String>> filteredOnMapValueList;
        filteredOnMapKeyList = getArguments().getStringArrayList(Constants.FILTER_NAME);
        filteredOnMapValueList = (ArrayList<Set<String>>) getArguments().getSerializable(Constants.FILTER_VALUES);
        filteredOn = new HashMap<>();
        for (int i = 0; i < filteredOnMapKeyList.size(); i++) {
            filteredOn.put(filteredOnMapKeyList.get(i), filteredOnMapValueList.get(i));
        }
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
                        ((ProductListAwareFragment) getTargetFragment()).loadProducts();
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
                        ((ProductListAwareFragment) getTargetFragment()).loadProducts();
                    }
                });
        return builder.create();
    }
}
