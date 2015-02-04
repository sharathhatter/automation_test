package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;

import java.util.ArrayList;

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_SECTION_ITEM = 0;
    public static final int VIEW_TYPE_SEPARATOR = 1;

    private Context context;
    private Typeface typeface;
    private ArrayList<SectionNavigationItem> sectionNavigationItems;

    public NavigationAdapter(Context context, Typeface typeface, ArrayList<SectionNavigationItem> sectionNavigationItems) {
        this.context = context;
        this.typeface = typeface;
        this.sectionNavigationItems = sectionNavigationItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType) {
            case VIEW_TYPE_SECTION_ITEM:
                View row = inflater.inflate(R.layout.uiv3_main_nav_list_row, parent, false);
                return new NavViewHolder(row);
            case VIEW_TYPE_SEPARATOR:
                row = inflater.inflate(R.layout.uiv3_main_nav_list_separator, parent, false);
                return new FixedLayoutViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SECTION_ITEM) {
            TextView txtNavListRow = ((NavViewHolder) holder).getTxtNavListRow();
            SectionNavigationItem sectionNavigationItem = sectionNavigationItems.get(position);
            if (sectionNavigationItem.isHome()) {
                txtNavListRow.setText("Home");
            } else {
                txtNavListRow.setText(sectionNavigationItems.get(position).getSectionItem().
                        getTitle().getText());
            }
        }
    }

    private class NavViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgNavItem;
        private TextView txtNavListRow;

        public NavViewHolder(View itemView) {
            super(itemView);
        }

        public ImageView getImgNavItem() {
            if (imgNavItem == null) {
                imgNavItem = (ImageView) itemView.findViewById(R.id.imgNavItem);
            }
            return imgNavItem;
        }

        public TextView getTxtNavListRow() {
            if (txtNavListRow == null) {
                txtNavListRow = (TextView) itemView.findViewById(R.id.txtNavListRow);
                txtNavListRow.setTypeface(typeface);
            }
            return txtNavListRow;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (sectionNavigationItems.get(position).isSeparator()) {
            return VIEW_TYPE_SEPARATOR;
        }
        return VIEW_TYPE_SECTION_ITEM;
    }

    @Override
    public int getItemCount() {
        return sectionNavigationItems.size();
    }
}
