package com.bigbasket.mobileapp.activity.product;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.BBCheckedListAdapter;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FilterActivity extends BackButtonActivity {

    private ArrayList<FilteredOn> mFilteredOns;
    private FilterTextWatcher mFilterTextWatcher;
    private String mCurrentlySelectedFilter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.viewToolbarSeparator).setVisibility(View.GONE);
        }
        setTitle(getString(R.string.filter));

        mFilteredOns = getIntent().getParcelableArrayListExtra(Constants.FILTERED_ON);

        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setTypeface(faceRobotoRegular);

        btnClear.setOnClickListener(new OnClearFilterListener());

        Button btnApply = (Button) findViewById(R.id.btnApply);
        btnApply.setTypeface(faceRobotoRegular);
        btnApply.setOnClickListener(new OnApplyFilterListener());
        refreshFilters();
    }

    private void refreshFilters() {
        ArrayList<FilterOptionCategory> filterOptionCategories =
                getIntent().getParcelableArrayListExtra(Constants.FILTER_OPTIONS);
        renderFilterOptions(filterOptionCategories);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_filter_layout;
    }

    private void renderFilterOptions(final ArrayList<FilterOptionCategory> filterOptionCategories) {
        ListView lstFilterName = (ListView) findViewById(R.id.lstFilterName);

        final FilterNameAdapter filterNameAdapter = new FilterNameAdapter(filterOptionCategories);
        if (filterOptionCategories != null && filterOptionCategories.size() > 0) {
            FilterOptionCategory filterOptionCategory = filterOptionCategories.get(0);
            if (filterOptionCategory != null) {
                mCurrentlySelectedFilter = filterOptionCategory.getFilterSlug();
                lstFilterName.setAdapter(filterNameAdapter);
                renderFilterItems(filterOptionCategory);
                lstFilterName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FilterOptionCategory filterOptionCategory = filterOptionCategories.get(position);
                        mCurrentlySelectedFilter = filterOptionCategory.getFilterSlug();
                        filterNameAdapter.notifyDataSetChanged();
                        renderFilterItems(filterOptionCategory);
                    }
                });
            }
        }
    }

    private void renderFilterItems(final FilterOptionCategory filterOptionCategory) {
        final ListView lstFilterItems = (ListView) findViewById(R.id.lstFilterItems);
        EditText editTextFilter = (EditText) findViewById(R.id.editTextFilter);
        List<FilterOptionItem> filterOptionItems = filterOptionCategory.getFilterOptionItems();
        BBCheckedListAdapter<FilterOptionItem> mFilterByAdapter = new BBCheckedListAdapter<>
                (getCurrentActivity(), R.layout.uiv3_filter_item_layout,
                        filterOptionItems, R.color.uiv3_primary_text_color, faceRobotoMedium);

        editTextFilter.setTypeface(faceRobotoRegular);
        editTextFilter.setHint(getString(R.string.search) + " " +
                filterOptionCategory.getFilterName());
        if (filterOptionItems.size() < 15) {
            editTextFilter.setVisibility(View.GONE);
            if (mFilterTextWatcher != null) {
                editTextFilter.removeTextChangedListener(mFilterTextWatcher);
                mFilterTextWatcher = null;
            }
        } else {
            editTextFilter.setVisibility(View.VISIBLE);
            if (mFilterTextWatcher == null) {
                mFilterTextWatcher = new FilterTextWatcher(mFilterByAdapter);
                editTextFilter.addTextChangedListener(mFilterTextWatcher);
            }
        }

        lstFilterItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lstFilterItems.setAdapter(mFilterByAdapter);
        for (int i = 0; i < filterOptionItems.size(); i++) {
            FilterOptionItem filterOptionItem = filterOptionItems.get(i);
            if (mFilteredOns == null) {
                // RESET Button has been clicked or has no filters
                filterOptionItem.setSelected(false);
            }
            if (filterOptionItem.isSelected()) {
                lstFilterItems.setItemChecked(i, true);
            }
        }
        lstFilterItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int itemPosition, long id) {
                if (itemPosition != ListView.INVALID_POSITION) {
                    FilterOptionItem filterOptionItem = (FilterOptionItem)
                            lstFilterItems.getAdapter().getItem(itemPosition);
                    CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                    boolean isChecked;
                    isChecked = checkedTextView.isChecked();
                    onFilterOptionClick(filterOptionCategory, filterOptionItem,
                            isChecked);
                }
            }
        });
    }

    private void onFilterOptionClick(FilterOptionCategory filterOptionCategory,
                                     FilterOptionItem filterOptionItem, boolean isChecked) {
        filterOptionItem.setSelected(isChecked);
        FilteredOn filteredOn = FilteredOn.getFilteredOn(mFilteredOns,
                filterOptionCategory.getFilterSlug());
        if (filteredOn == null) {
            filteredOn = new FilteredOn(filterOptionCategory.getFilterSlug());
            if (mFilteredOns == null) {
                mFilteredOns = new ArrayList<>();
            }
            mFilteredOns.add(filteredOn);
        }
        ArrayList<String> filterValues = filteredOn.getFilterValues();
        if (filterValues == null) {
            filterValues = new ArrayList<>();
            filteredOn.setFilterValues(filterValues);
        }

        if (isChecked) {
            if (!filterValues.contains(filterOptionItem.getFilterValueSlug())) {
                filterValues.add(filterOptionItem.getFilterValueSlug());
            }
        } else {
            if (filterValues.contains(filterOptionItem.getFilterValueSlug())) {
                filterValues.remove(filterOptionItem.getFilterValueSlug());
            }
        }
    }

    private static class FilterTextWatcher implements TextWatcher {

        private BBCheckedListAdapter<FilterOptionItem> mFilterByAdapter;

        public FilterTextWatcher(BBCheckedListAdapter<FilterOptionItem> mFilterByAdapter) {
            this.mFilterByAdapter = mFilterByAdapter;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String text = s.toString().trim();
            if (TextUtils.isEmpty(text)) {
                mFilterByAdapter.getFilter().filter(null);
            } else {
                mFilterByAdapter.getFilter().filter(text.toLowerCase(Locale.getDefault()));
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }

    private class OnApplyFilterListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent data = new Intent();
            data.putExtra(Constants.FILTERED_ON, mFilteredOns);
            setResult(NavigationCodes.FILTER_APPLIED, data);
            finish();
        }
    }

    private class OnClearFilterListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (mFilteredOns != null) {
                mFilteredOns = null;
            }
            refreshFilters();
        }
    }

    private class FilterNameAdapter extends BaseAdapter {

        private static final int VIEW_TYPE_NORMAL = 0;
        private static final int VIEW_TYPE_SELECTED = 1;

        private ArrayList<FilterOptionCategory> filterOptionCategories;

        public FilterNameAdapter(ArrayList<FilterOptionCategory> filterOptionCategories) {
            this.filterOptionCategories = filterOptionCategories;
        }

        @Override
        public int getCount() {
            return filterOptionCategories.size();
        }

        @Override
        public Object getItem(int position) {
            return filterOptionCategories.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FilterOptionCategory filterOptionCategory = filterOptionCategories.get(position);
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.uiv3_filter_name_layout, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            TextView textFilterName = viewHolder.getTxtFilterName();
            if (getItemViewType(position) == VIEW_TYPE_SELECTED) {
                textFilterName.setTypeface(faceRobotoMedium);
                convertView.setBackgroundColor(Color.WHITE);
            } else {
                textFilterName.setTypeface(faceRobotoMedium);
                convertView.setBackgroundColor(Color.TRANSPARENT);
            }
            textFilterName.setText(filterOptionCategory.getFilterName());

            TextView txtAppliedFilterCount = viewHolder.getTxtAppliedFilterCount();
            txtAppliedFilterCount.setTypeface(faceRobotoLight);

            FilteredOn filteredOn = FilteredOn.getFilteredOn(mFilteredOns, filterOptionCategory.getFilterSlug());
            if (filteredOn != null && filteredOn.getFilterValues() != null &&
                    filteredOn.getFilterValues().size() > 0) {
                txtAppliedFilterCount.setText(String.valueOf(filteredOn.getFilterValues().size()));
                txtAppliedFilterCount.setVisibility(View.VISIBLE);
            } else {
                txtAppliedFilterCount.setVisibility(View.GONE);
            }
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            if (filterOptionCategories.get(position).getFilterSlug().equals(mCurrentlySelectedFilter)) {
                return VIEW_TYPE_SELECTED;
            }
            return VIEW_TYPE_NORMAL;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        private class ViewHolder {
            private View itemView;
            private TextView txtFilterName;
            private TextView txtAppliedFilterCount;

            public ViewHolder(View itemView) {
                this.itemView = itemView;
            }

            public TextView getTxtFilterName() {
                if (txtFilterName == null) {
                    txtFilterName = (TextView) itemView.findViewById(R.id.txtFilterName);
                    txtFilterName.setTypeface(faceRobotoMedium);
                }
                return txtFilterName;
            }

            public TextView getTxtAppliedFilterCount() {
                if (txtAppliedFilterCount == null) {
                    txtAppliedFilterCount = (TextView) itemView.findViewById(R.id.txtAppliedFilterCount);
                    txtAppliedFilterCount.setTypeface(faceRobotoLight);
                }
                return txtAppliedFilterCount;
            }
        }
    }
}
