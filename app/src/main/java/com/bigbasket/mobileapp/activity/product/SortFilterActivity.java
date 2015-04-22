package com.bigbasket.mobileapp.activity.product;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.astuetz.PagerSlidingTabStrip;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.BBCheckedListAdapter;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.Option;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SortFilterActivity extends BackButtonActivity {

    private ArrayList<FilteredOn> mFilteredOns;
    private String mSortedOn;

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_filter_sort_layout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.sortOrFilterBy));

        Button btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setTypeface(faceRobotoRegular);

        btnClear.setOnClickListener(new OnClearFilterListener());

        Button btnApply = (Button) findViewById(R.id.btnApply);
        btnApply.setTypeface(faceRobotoRegular);
        btnApply.setOnClickListener(new OnApplySortFilterListener());

        ArrayList<FilterOptionCategory> filterOptionCategories = getIntent().getParcelableArrayListExtra(Constants.FILTER_OPTIONS);
        mFilteredOns = getIntent().getParcelableArrayListExtra(Constants.FILTERED_ON);
        ArrayList<Option> sortOpts = getIntent().getParcelableArrayListExtra(Constants.PRODUCT_SORT_OPTION);
        mSortedOn = getIntent().getStringExtra(Constants.SORT_ON);

        boolean isLargeScreen = hasSpaceToShowFilterLayout();
        setToggleBehaviour((TextView) findViewById(R.id.lblSortBy), findViewById(R.id.lstSortBy), isLargeScreen);

        setToggleBehaviour((TextView) findViewById(R.id.lblFilterBy), findViewById(R.id.layoutSwipeTabContainer),
                isLargeScreen);
        renderSortOpts(sortOpts);
        renderFilterOptions(filterOptionCategories);
    }

    private boolean hasSpaceToShowFilterLayout() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int densityDpi = metrics.densityDpi;
        int screenLayout = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return (densityDpi != DisplayMetrics.DENSITY_LOW &&
                densityDpi != DisplayMetrics.DENSITY_MEDIUM &&
                densityDpi != DisplayMetrics.DENSITY_HIGH) ||
                (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                        screenLayout == 4);
    }

    private void setToggleBehaviour(final TextView lbl, final View contentUnderLbl, final boolean expanded) {
        lbl.setTypeface(faceRobotoRegular);
        toggle(lbl, contentUnderLbl, expanded);
        lbl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean expanded = contentUnderLbl.getVisibility() == View.VISIBLE;
                toggle(lbl, contentUnderLbl, !expanded);
            }
        });
    }

    private void toggle(TextView lbl, View contentUnderLbl, boolean expanded) {
        if (expanded) {
            contentUnderLbl.setVisibility(View.VISIBLE);
            lbl.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, ContextCompat.getDrawable(this, R.drawable.crispy_arrow_down), null);
        } else {
            contentUnderLbl.setVisibility(View.GONE);
            lbl.setCompoundDrawablesWithIntrinsicBounds(null,
                    null, ContextCompat.getDrawable(this, R.drawable.crispy_arrow_right), null);
        }
    }

    private void renderFilterOptions(ArrayList<FilterOptionCategory> filterOptionCategories) {
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
        PagerSlidingTabStrip pagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.slidingTabs);
        pagerSlidingTabStrip.setBackgroundColor(getResources().getColor(R.color.white));

        if (filterOptionCategories == null || filterOptionCategories.size() == 0) {
            viewPager.setVisibility(View.GONE);
            pagerSlidingTabStrip.setVisibility(View.GONE);
            return;
        }

        FilterByPagerAdapter filterByPagerAdapter = new FilterByPagerAdapter(filterOptionCategories);
        viewPager.setAdapter(filterByPagerAdapter);

        pagerSlidingTabStrip.setViewPager(viewPager);
    }

    private void renderSortOpts(final ArrayList<Option> sortOpts) {
        TextView lblSortBy = (TextView) findViewById(R.id.lblSortBy);
        lblSortBy.setTypeface(faceRobotoRegular);
        ListView lstSortBy = (ListView) findViewById(R.id.lstSortBy);

        if (sortOpts == null || sortOpts.size() == 0) {
            lstSortBy.setVisibility(View.GONE);
            lblSortBy.setVisibility(View.GONE);
            return;
        }
        BBCheckedListAdapter<Option> sortByAdapter = new BBCheckedListAdapter<>
                (this, android.R.layout.simple_list_item_single_choice, sortOpts);
        lstSortBy.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lstSortBy.setAdapter(sortByAdapter);
        lstSortBy.setItemChecked(findCurrentSortedByPosition(sortOpts, mSortedOn), true);
        lstSortBy.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position != ListView.INVALID_POSITION) {
                    mSortedOn = sortOpts.get(position).getSortSlug();
                }
            }
        });
    }

    private int findCurrentSortedByPosition(ArrayList<Option> sortOpts, String sortedOn) {
        for (int i = 0; i < sortOpts.size(); i++) {
            Option option = sortOpts.get(i);
            if (option.getSortSlug().equals(sortedOn)) {
                return i;
            }
        }
        return 0;
    }

    private class FilterByPagerAdapter extends PagerAdapter {

        private ArrayList<FilterOptionCategory> filterOptionCategories;

        private FilterByPagerAdapter(ArrayList<FilterOptionCategory> filterOptionCategories) {
            this.filterOptionCategories = filterOptionCategories;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return filterOptionCategories.get(position).getFilterName();
        }

        @Override
        public int getCount() {
            return filterOptionCategories.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            List<FilterOptionItem> filterOptionItems = filterOptionCategories.get(position).getFilterOptionItems();
            View returnView;
            ListView lstFilterOnItems;
            final BBCheckedListAdapter<FilterOptionItem> filterByAdapter = new BBCheckedListAdapter<>
                    (getCurrentActivity(), android.R.layout.simple_list_item_multiple_choice,
                            filterOptionItems);
            if (filterOptionItems.size() < 15) {
                lstFilterOnItems = new ListView(getCurrentActivity());
                returnView = lstFilterOnItems;
            } else {
                View base = getLayoutInflater().inflate(R.layout.uiv3_filterable_listview, container, false);
                returnView = base;
                lstFilterOnItems = (ListView) base.findViewById(R.id.lstVw);
                final EditText editTextFilter = (EditText) base.findViewById(R.id.editTextFilter);
                editTextFilter.setTypeface(faceRobotoRegular);
                editTextFilter.setHint(getString(R.string.search) + " " +
                        filterOptionCategories.get(position).getFilterName());
                editTextFilter.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        String text = s.toString().trim();
                        if (TextUtils.isEmpty(text)) {
                            filterByAdapter.getFilter().filter(null);
                        } else {
                            filterByAdapter.getFilter().filter(text.toLowerCase(Locale.getDefault()));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
            }
            lstFilterOnItems.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lstFilterOnItems.setAdapter(filterByAdapter);

            int sz = filterOptionItems.size();
            for (int i = 0; i < sz; i++) {
                FilterOptionItem filterOptionItem = filterOptionItems.get(i);
                if (filterOptionItem.isSelected()) {
                    lstFilterOnItems.setItemChecked(i, true);
                }
            }
            lstFilterOnItems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int itemPosition, long id) {
                    if (itemPosition != ListView.INVALID_POSITION) {
                        FilterOptionCategory filterOptionCategory = filterOptionCategories.get(position);
                        FilterOptionItem filterOptionItem = filterOptionCategory.getFilterOptionItems().
                                get(itemPosition);
                        CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(android.R.id.text1);
                        boolean isChecked;
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                            isChecked = !checkedTextView.isChecked();
                        } else {
                            isChecked = checkedTextView.isChecked();
                        }
                        onFilterOptionClick(filterOptionCategory, filterOptionItem,
                                isChecked);
                    }
                }
            });

            container.addView(returnView);
            return returnView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    private void onFilterOptionClick(FilterOptionCategory filterOptionCategory,
                                     FilterOptionItem filterOptionItem, boolean isChecked) {
        filterOptionItem.setSelected(isChecked);
        FilteredOn filteredOn = FilteredOn.getFilteredOn(mFilteredOns,
                filterOptionCategory.getFilterSlug());
        if (filteredOn == null) {
            filteredOn = new FilteredOn(filterOptionCategory.getFilterSlug());
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

    private class OnApplySortFilterListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent data = new Intent();
            data.putExtra(Constants.SORT_ON, mSortedOn);
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

            setResult(NavigationCodes.FILTER_APPLIED);
            finish();
        }
    }
}
