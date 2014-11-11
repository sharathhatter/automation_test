package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FilterByAdapter extends BaseExpandableListAdapter {

    private List<FilterOptionCategory> filterOptionCategories;
    private Map<String, Set<String>> filteredOn;
    private Context context;
    private int childTextLeftPadding;
    private int childTextOtherPadding;

    public FilterByAdapter(List<FilterOptionCategory> filterOptionCategories,
                           Map<String, Set<String>> filteredOn,
                           Context context) {
        this.filterOptionCategories = filterOptionCategories;
        this.filteredOn = filteredOn;
        this.context = context;
        this.childTextLeftPadding = context.getResources().getDimensionPixelSize(R.dimen.padding_normal);
        this.childTextOtherPadding = context.getResources().getDimensionPixelSize(R.dimen.padding_small);
    }

    @Override
    public int getGroupCount() {
        return filterOptionCategories.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return filterOptionCategories.get(groupPosition).getFilterOptionItems().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return filterOptionCategories.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return filterOptionCategories.get(groupPosition).getFilterOptionItems().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition + childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View row = convertView;
        FilterbyViewHolder filterbyViewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_product_filterby_list_row, null);
            filterbyViewHolder = new FilterbyViewHolder(row, true, childTextLeftPadding, childTextOtherPadding);
            row.setTag(filterbyViewHolder);
        } else {
            filterbyViewHolder = (FilterbyViewHolder) row.getTag();
        }

        FilterOptionCategory filterOptionCategory = filterOptionCategories.get(groupPosition);
        TextView txtListRow = filterbyViewHolder.getTxtListRow();
        CheckBox chkFilter = filterbyViewHolder.getChkFilter();
        chkFilter.setVisibility(View.GONE);
        txtListRow.setText(filterOptionCategory.getFilterName());
        int expandIndicatorDrawable = isExpanded ? R.drawable.small_down_arrow : R.drawable.small_list_arrow;
        txtListRow.setCompoundDrawablesWithIntrinsicBounds(null, null,
                context.getResources().getDrawable(expandIndicatorDrawable), null);
        return row;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        FilterbyViewHolder filterbyViewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_product_filterby_list_row, null);
            filterbyViewHolder = new FilterbyViewHolder(row, false, childTextLeftPadding, childTextOtherPadding);
            row.setTag(filterbyViewHolder);
        } else {
            filterbyViewHolder = (FilterbyViewHolder) row.getTag();
        }
        final FilterOptionCategory filterOptionCategory = filterOptionCategories.get(groupPosition);
        final FilterOptionItem filterOptionItem = filterOptionCategory.getFilterOptionItems().get(childPosition);
        TextView txtListRow = filterbyViewHolder.getTxtListRow();
        final CheckBox chkFilter = filterbyViewHolder.getChkFilter();
        chkFilter.setChecked(filterOptionItem.isSelected());
        txtListRow.setText(filterOptionItem.getDisplayName());
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chkFilter.toggle();
                filterOptionItem.setSelected(chkFilter.isSelected());
                Set<String> values = filteredOn.get(filterOptionCategory.getFilterSlug());
                if (values == null) {
                    values = new HashSet<>();
                }
                if (values.size() == 0) {
                    filteredOn.put(filterOptionCategory.getFilterSlug(), values);
                }
                boolean hasFilterOption = values.contains(filterOptionItem.getFilterValueSlug());
                if (!chkFilter.isChecked()) {
                    if (hasFilterOption) {
                        values.remove(filterOptionItem.getFilterValueSlug());
                    }
                } else {
                    if (!hasFilterOption) {
                        values.add(filterOptionItem.getFilterValueSlug());
                    }
                }
            }
        });
        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private static class FilterbyViewHolder {
        private View base;
        private TextView txtListRow;
        private CheckBox chkFilter;
        private boolean isParent;
        private int childTextLeftPadding;
        private int childTextOtherPadding;

        public FilterbyViewHolder(View base, boolean isParent, int childTextLeftPadding, int childTextOtherPadding) {
            this.base = base;
            this.isParent = isParent;
            this.childTextLeftPadding = childTextLeftPadding;
            this.childTextOtherPadding = childTextOtherPadding;
        }

        public TextView getTxtListRow() {
            if (txtListRow == null) {
                txtListRow = (TextView) base.findViewById(R.id.txtListRow);
                if (!isParent) {
                    txtListRow.setPadding(childTextLeftPadding, childTextOtherPadding, childTextOtherPadding, childTextOtherPadding);
                }
            }
            return txtListRow;
        }

        public CheckBox getChkFilter() {
            if (chkFilter == null) {
                chkFilter = (CheckBox) base.findViewById(R.id.chkFilter);
            }
            return chkFilter;
        }
    }
}
