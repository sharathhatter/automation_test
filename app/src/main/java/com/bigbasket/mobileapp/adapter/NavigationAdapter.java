package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
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
            } else if (sectionNavigationItem.getSectionItem().getTitle() != null &&
                    !TextUtils.isEmpty(sectionNavigationItem.getSectionItem().getTitle().getText())) {
                txtNavListRow.setText(sectionNavigationItem.getSectionItem().getTitle().getText());
            }
        }
    }

    private class NavViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgNavItem;
        private TextView txtNavListRow;

        public NavViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
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

        @Override
        public void onClick(View v) {
            SectionNavigationItem sectionNavigationItem = sectionNavigationItems.get(getPosition());
            if (!sectionNavigationItem.isSeparator()) {
                if (sectionNavigationItem.isHome()) {
                    ((ActivityAware) context).getCurrentActivity().goToHome();
                } else {
                    new OnSectionItemClickListener<>(context, sectionNavigationItem.getSection(),
                            sectionNavigationItem.getSectionItem()).onClick(v);
                }
            }
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
