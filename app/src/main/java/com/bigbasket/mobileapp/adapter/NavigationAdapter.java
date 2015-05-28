package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.SubNavigationAware;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;

import java.util.ArrayList;

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_SECTION_ITEM = 0;
    public static final int VIEW_TYPE_HEADER = 1;

    private Context context;
    private Typeface typeface;
    private ArrayList<SectionNavigationItem> sectionNavigationItems;
    private String screenName;
    private String baseImgUrl;

    public NavigationAdapter(Context context, Typeface typeface, ArrayList<SectionNavigationItem>
            sectionNavigationItems, String screenName, @Nullable String baseImgUrl) {
        this.context = context;
        this.typeface = typeface;
        this.sectionNavigationItems = sectionNavigationItems;
        this.screenName = screenName;
        this.baseImgUrl = baseImgUrl;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType) {
            case VIEW_TYPE_SECTION_ITEM:
                View row = inflater.inflate(R.layout.uiv3_main_nav_list_row, parent, false);
                return new NavViewHolder(row);
            case VIEW_TYPE_HEADER:
                row = inflater.inflate(R.layout.uiv3_main_nav_list_header, parent, false);
                return new NavViewHeaderHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SectionNavigationItem sectionNavigationItem = sectionNavigationItems.get(position);
        if (getItemViewType(position) == VIEW_TYPE_SECTION_ITEM) {
            TextView txtNavListRow = ((NavViewHolder) holder).getTxtNavListRow();
            ImageView imgNavItem = ((NavViewHolder) holder).getImgNavItem();
            ImageView imgNavItemExpand = ((NavViewHolder) holder).getImgNavItemExpand();
            TextView txtNavListRowSubTitle = ((NavViewHolder) holder).getTxtNavListRowSubTitle();
            if (sectionNavigationItem.getSectionItem().getTitle() != null &&
                    !TextUtils.isEmpty(sectionNavigationItem.getSectionItem().getTitle().getText())) {
                txtNavListRow.setText(sectionNavigationItem.getSectionItem().getTitle().getText());
            } else {
                txtNavListRow.setText("");
            }
            if (sectionNavigationItem.getSectionItem().hasImage()) {
                imgNavItem.setVisibility(View.VISIBLE);
                sectionNavigationItem.getSectionItem().displayImage(context, baseImgUrl, imgNavItem);
            } else {
                imgNavItem.setVisibility(View.GONE);
            }
            if (sectionNavigationItem.getSectionItem().getDescription() != null &&
                    !TextUtils.isEmpty(sectionNavigationItem.getSectionItem().getDescription().getText())) {
                txtNavListRowSubTitle.setVisibility(View.VISIBLE);
                txtNavListRowSubTitle.setText(sectionNavigationItem.getSectionItem().getDescription().getText());
            } else {
                txtNavListRowSubTitle.setVisibility(View.GONE);
            }
            if (sectionNavigationItem.getSectionItem().getSubSectionItems() != null
                    && sectionNavigationItem.getSectionItem().getSubSectionItems().size() > 0) {
                imgNavItemExpand.setVisibility(View.VISIBLE);
            } else {
                imgNavItemExpand.setVisibility(View.GONE);
            }
        } else {
            TextView txtNavListRowHeader = ((NavViewHeaderHolder) holder).getTxtNavListRowHeader();
            if (sectionNavigationItem.getSection().getTitle() != null &&
                    !TextUtils.isEmpty(sectionNavigationItem.getSection().getTitle().getText())) {
                txtNavListRowHeader.setText(sectionNavigationItem.getSection().getTitle().getText());
                txtNavListRowHeader.setVisibility(View.VISIBLE);
            } else {
                txtNavListRowHeader.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (sectionNavigationItems.get(position).isHeader()) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_SECTION_ITEM;
    }

    @Override
    public int getItemCount() {
        return sectionNavigationItems.size();
    }

    private class NavViewHeaderHolder extends RecyclerView.ViewHolder {
        private TextView txtNavListRowHeader;

        private NavViewHeaderHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtNavListRowHeader() {
            if (txtNavListRowHeader == null) {
                txtNavListRowHeader = (TextView) itemView.findViewById(R.id.txtNavListRowHeader);
                txtNavListRowHeader.setTypeface(typeface);
            }
            return txtNavListRowHeader;
        }
    }

    private class NavViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgNavItem;
        private TextView txtNavListRow;
        private TextView txtNavListRowSubTitle;
        private ImageView imgNavItemExpand;

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

        public TextView getTxtNavListRowSubTitle() {
            if (txtNavListRowSubTitle == null) {
                txtNavListRowSubTitle = (TextView) itemView.findViewById(R.id.txtNavListRowSubTitle);
                txtNavListRowSubTitle.setTypeface(typeface);
            }
            return txtNavListRowSubTitle;
        }

        public ImageView getImgNavItemExpand() {
            if (imgNavItemExpand == null) {
                imgNavItemExpand = (ImageView) itemView.findViewById(R.id.imgNavItemExpand);
            }
            return imgNavItemExpand;
        }

        @Override
        public void onClick(View v) {
            SectionNavigationItem sectionNavigationItem = sectionNavigationItems.get(getPosition());
            if (!sectionNavigationItem.isHeader()) {
                if (sectionNavigationItem.getSectionItem() != null && sectionNavigationItem.getSectionItem().getSubSectionItems() != null
                        && sectionNavigationItem.getSectionItem().getSubSectionItems().size() > 0) {
                    ((SubNavigationAware) context).onSubNavigationRequested(sectionNavigationItem.getSection(),
                            sectionNavigationItem.getSectionItem());
                } else {
                    new OnSectionItemClickListener<>(context, sectionNavigationItem.getSection(),
                            sectionNavigationItem.getSectionItem(), screenName).onClick(v);
                }
            }
        }
    }
}
