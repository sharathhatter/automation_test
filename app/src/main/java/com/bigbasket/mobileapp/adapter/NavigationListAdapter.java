package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.navigation.NavigationItem;
import com.bigbasket.mobileapp.model.navigation.NavigationSubItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public class NavigationListAdapter extends BaseExpandableListAdapter {

    private ArrayList<NavigationItem> navigationItems;
    private Context context;
    private Typeface typeface;

    public NavigationListAdapter(Context context, ArrayList<NavigationItem> navigationItems, Typeface typeface) {
        this.navigationItems = navigationItems;
        this.context = context;
        this.typeface = typeface;
    }

    @Override
    public int getGroupCount() {
        return navigationItems.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        ArrayList<NavigationSubItem> navigationSubItems = navigationItems.get(groupPosition).getNavigationSubItems();
        return navigationSubItems != null ? navigationSubItems.size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return navigationItems.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        ArrayList<NavigationSubItem> navigationSubItems = navigationItems.get(groupPosition).getNavigationSubItems();
        return navigationSubItems != null ? navigationSubItems.get(childPosition) : null;
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
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_main_nav_list_row, null);
        }
        NavigationItem navigationItem = navigationItems.get(groupPosition);
        TextView txtNavListRow = (TextView) row.findViewById(R.id.txtNavListRow);
        txtNavListRow.setTypeface(typeface);
        txtNavListRow.setText(navigationItem.getItemName());
        Drawable expandArrowDrawable = null;
        if (navigationItem.isExpandable()) {
            int expandableArrowDrawableId;
            if (isExpanded) {
                expandableArrowDrawableId = R.drawable.small_down_arrow;
            } else {
                expandableArrowDrawableId = R.drawable.small_list_arrow;
            }
            expandArrowDrawable = context.getResources().getDrawable(expandableArrowDrawableId);
        }
        txtNavListRow.setCompoundDrawablesWithIntrinsicBounds(context.getResources().getDrawable(navigationItem.getDrawableId()),
                null, expandArrowDrawable, null);
        return row;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        NavigationItem navigationItem = navigationItems.get(groupPosition);
        ArrayList<NavigationSubItem> navigationSubItems = navigationItem.getNavigationSubItems();
        if (!navigationItem.isExpandable() || navigationSubItems == null || navigationSubItems.size() == 0) {
            return null;
        }
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.uiv3_main_nav_list_row, null);
        }
        NavigationSubItem navigationSubItem = navigationSubItems.get(childPosition);
        TextView txtNavListRow = (TextView) row.findViewById(R.id.txtNavListRow);
        ProgressBar progressBarNavItem = (ProgressBar) row.findViewById(R.id.progressBarNavItem);
        ImageView imgNavItem = (ImageView) row.findViewById(R.id.imgNavItem);
        if (navigationSubItem.isLoading()) {
            progressBarNavItem.setVisibility(View.VISIBLE);
            txtNavListRow.setVisibility(View.GONE);
            imgNavItem.setVisibility(View.GONE);
        } else {
            txtNavListRow.setVisibility(View.VISIBLE);
            progressBarNavItem.setVisibility(View.GONE);
            txtNavListRow.setPadding(context.getResources().getDimensionPixelSize(R.dimen.padding_small), 0, 0, 0);
            txtNavListRow.setTypeface(typeface);
            txtNavListRow.setText(navigationSubItem.getItemName());

            if (navigationSubItem.getDrawableId() > 0) {
                imgNavItem.setImageDrawable(context.getResources().getDrawable(navigationSubItem.getDrawableId()));
            } else if (!TextUtils.isEmpty(navigationSubItem.getImageUrl())) {
                ImageLoader.getInstance().displayImage(navigationSubItem.getImageUrl(), imgNavItem);
            } else {
                imgNavItem.setVisibility(View.GONE);
            }
        }
        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}