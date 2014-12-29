package com.bigbasket.mobileapp.adapter.product;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.product.FilterOptionCategory;
import com.bigbasket.mobileapp.model.product.FilterOptionItem;
import com.bigbasket.mobileapp.model.product.FilteredOn;

import java.util.ArrayList;
import java.util.List;

public class FilterByAdapter extends BaseExpandableListAdapter {

    private List<FilterOptionCategory> filterOptionCategories;
    private ArrayList<FilteredOn> filteredOnList;
    private Context context;

    public FilterByAdapter(List<FilterOptionCategory> filterOptionCategories,
                           ArrayList<FilteredOn> filteredOnList,
                           Context context) {
        this.filterOptionCategories = filterOptionCategories;
        this.filteredOnList = filteredOnList;
        this.context = context;
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
        FilterbyHeaderViewHolder filterbyViewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_product_filterby_header, null);
            filterbyViewHolder = new FilterbyHeaderViewHolder(row);
            row.setTag(filterbyViewHolder);
        } else {
            filterbyViewHolder = (FilterbyHeaderViewHolder) row.getTag();
        }

        FilterOptionCategory filterOptionCategory = filterOptionCategories.get(groupPosition);
        TextView txtListRow = filterbyViewHolder.getTxtListRow();
        txtListRow.setText(filterOptionCategory.getFilterName());
        int expandIndicatorDrawable = R.drawable.ic_keyboard_arrow_right_grey600_24dp;
        if (isExpanded) {
            expandIndicatorDrawable = R.drawable.ic_keyboard_arrow_down_grey600_24dp;
            txtListRow.setTextColor(context.getResources().getColor(R.color.white));
            row.setBackgroundColor(context.getResources().getColor(R.color.fbutton_color_nephritis));
        } else {
            txtListRow.setTextColor(context.getResources().getColor(R.color.primary_text_default_material_light));
            row.setBackgroundColor(context.getResources().getColor(R.color.background_material_light));
        }
        txtListRow.setCompoundDrawablesWithIntrinsicBounds(null, null,
                context.getResources().getDrawable(expandIndicatorDrawable), null);
        return row;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        FilterbyViewHolder filterbyViewHolder;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_product_filterby_list_row, null);
            filterbyViewHolder = new FilterbyViewHolder(row);
            row.setTag(filterbyViewHolder);
        } else {
            filterbyViewHolder = (FilterbyViewHolder) row.getTag();
        }
        final FilterOptionCategory filterOptionCategory = filterOptionCategories.get(groupPosition);
        final FilterOptionItem filterOptionItem = filterOptionCategory.getFilterOptionItems().get(childPosition);
        TextView txtListRow = filterbyViewHolder.getTxtListRow();
        CheckBox chkFilter = filterbyViewHolder.getChkFilter();
        chkFilter.setChecked(filterOptionItem.isSelected());
        txtListRow.setText(filterOptionItem.getDisplayName());
        chkFilter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterOptionItem.setSelected(isChecked);
                FilteredOn filteredOn = FilteredOn.getFilteredOn(filteredOnList, filterOptionCategory.getFilterSlug());
                if (filteredOn == null) {
                    filteredOn = new FilteredOn(filterOptionItem.getFilterValueSlug());
                    filteredOnList.add(filteredOn);
                }
                ArrayList<String> values = filteredOn.getFilterValues();
                if (values == null) {
                    values = new ArrayList<>();
                }
                if (values.size() == 0) {
                    filteredOn.setFilterValues(values);
                }
                boolean hasFilterOption = values.contains(filterOptionItem.getFilterValueSlug());
                if (!isChecked) {
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

    private static class FilterbyHeaderViewHolder {
        private TextView txtListRow;
        private View base;

        private FilterbyHeaderViewHolder(View base) {
            this.base = base;
        }

        public TextView getTxtListRow() {
            if (txtListRow == null) {
                txtListRow = (TextView) base.findViewById(R.id.txtListRow);
            }
            return txtListRow;
        }
    }

    private static class FilterbyViewHolder {
        private View base;
        private TextView txtListRow;
        private CheckBox chkFilter;

        public FilterbyViewHolder(View base) {
            this.base = base;
        }

        public TextView getTxtListRow() {
            if (txtListRow == null) {
                txtListRow = (TextView) base.findViewById(R.id.txtListRow);
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
