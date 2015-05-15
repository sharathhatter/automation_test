package com.bigbasket.mobileapp.adapter.product;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.Category;
import com.bigbasket.mobileapp.model.product.FilteredOn;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class SubCategoryListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private T ctx;
    private static final int VIEW_TYPE_SECTION = 0;
    private static final int VIEW_TYPE_CATEGORY = 1;
    private List<Category> categoryList;
    private LayoutInflater layoutInflater;
    private View mSectionView;

    public SubCategoryListAdapter(T ctx, List<Category> categoryList, View sectionView) {
        this.ctx = ctx;
        this.categoryList = categoryList;
        this.layoutInflater = LayoutInflater.from(((ActivityAware) ctx).getCurrentActivity());
        this.mSectionView = sectionView;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CATEGORY:
                View row = layoutInflater.inflate(R.layout.uiv3_list_text, parent, false);
                return new SubCatHolder(row);
            case VIEW_TYPE_SECTION:
                return new FixedLayoutViewHolder(mSectionView);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_CATEGORY) {
            final Category subcategory = categoryList.get(getActualPosition(position));
            SubCatHolder subCatHolder = (SubCatHolder) holder;
            TextView txtListItem = subCatHolder.getListText();
            txtListItem.setText(subcategory.getName());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mSectionView != null) {
            if (position == 0) {
                return VIEW_TYPE_SECTION;
            }
        }
        return VIEW_TYPE_CATEGORY;
    }

    public int getActualPosition(int position) {
        if (mSectionView != null) {
            return position - 1;
        }
        return position;
    }

    @Override
    public int getItemCount() {
        int sz = categoryList.size();
        return mSectionView != null ? sz + 1 : sz;
    }

    private class SubCatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtListText;

        private SubCatHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        private TextView getListText() {
            if (txtListText == null)
                txtListText = (TextView) itemView.findViewById(R.id.txtListText);
            return txtListText;
        }

        @Override
        public void onClick(View v) {
            int position = getActualPosition(getPosition());
            Intent intent = new Intent(((ActivityAware) ctx).getCurrentActivity(), ProductListActivity.class);
            ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
            nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY.get()));
            nameValuePairs.add(new NameValuePair(Constants.SLUG, categoryList.get(position).getSlug()));
            if (categoryList.get(position).getFilter() != null) {
                ArrayList<FilteredOn> filteredOns = new ArrayList<>();
                filteredOns.add(new FilteredOn(categoryList.get(position).getFilter()));
                nameValuePairs.add(new NameValuePair(Constants.FILTER_ON, new Gson().toJson(filteredOns)));
            }
            if (categoryList.get(position).getSortBy() != null) {
                nameValuePairs.add(new NameValuePair(Constants.SORT_ON, categoryList.get(position).getSortBy()));
            }
            intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
            intent.putExtra(Constants.TITLE, categoryList.get(position).getName());
            ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }
}
