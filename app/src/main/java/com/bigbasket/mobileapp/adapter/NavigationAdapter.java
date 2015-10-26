package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.SubNavigationAware;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SubSectionItem;

import java.util.ArrayList;
import java.util.HashMap;

public class NavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_SECTION_ITEM = 0;
    public static final int VIEW_TYPE_HEADER = 1;
    public static final int VIEW_TYPE_SECTION_ITEM_VERTICAL = 2;
    public static final int VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_VERTICAL = 3;
    public static final int VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_HORIZONTAL = 4;

    private Context context;
    private Typeface typeface;
    private ArrayList<SectionNavigationItem> sectionNavigationItems;
    private String screenName;
    private String baseImgUrl;

    @Nullable
    private HashMap<Integer, Renderer> rendererHashMap;
    @Nullable
    private SectionItem parentSectionItem;

    public NavigationAdapter(Context context, Typeface typeface,
                             ArrayList<SectionNavigationItem> sectionNavigationItems,
                             String screenName, @Nullable String baseImgUrl,
                             @Nullable HashMap<Integer, Renderer> rendererHashMap) {
        this.context = context;
        this.typeface = typeface;
        this.sectionNavigationItems = sectionNavigationItems;
        this.screenName = screenName;
        this.baseImgUrl = baseImgUrl;
        this.rendererHashMap = rendererHashMap;
    }

    public NavigationAdapter(Context context, Typeface typeface,
                             ArrayList<SectionNavigationItem> sectionNavigationItems,
                             String screenName, @Nullable String baseImgUrl,
                             @Nullable HashMap<Integer, Renderer> rendererHashMap,
                             @Nullable SectionItem parentSectionItem) {
        this(context, typeface, sectionNavigationItems, screenName, baseImgUrl,
                rendererHashMap);
        this.parentSectionItem = parentSectionItem;
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
            case VIEW_TYPE_SECTION_ITEM_VERTICAL:
                row = inflater.inflate(R.layout.uiv3_main_nav_list_vertical_row, parent, false);
                return new SubNavHeaderViewHolder(row);
            case VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_HORIZONTAL:
                row = inflater.inflate(R.layout.uiv3_left_nav_sub_menu_text_header, parent, false);
                return new SubNavHeaderViewHolder(row);
            case VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_VERTICAL:
                row = inflater.inflate(R.layout.uiv3_main_nav_list_vertical_row, parent, false);
                return new SubNavHeaderViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SectionNavigationItem sectionNavigationItem = sectionNavigationItems.get(position);
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_SECTION_ITEM) {
            NavViewHolder navViewHolder = (NavViewHolder) holder;
            TextView txtNavListRow = navViewHolder.getTxtNavListRow();
            ImageView imgNavItem = navViewHolder.getImgNavItem();
            ImageView imgNavItemExpand = navViewHolder.getImgNavItemExpand();
            TextView txtNavListRowSubTitle = navViewHolder.getTxtNavListRowSubTitle();
            SectionItem sectionItem = sectionNavigationItem.getSectionItem();
            if (sectionItem.getTitle() != null &&
                    !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                txtNavListRow.setText(sectionItem.getTitle().getText());
            } else {
                txtNavListRow.setText("");
            }
            if (sectionNavigationItem.getSectionItem().hasImage()) {
                imgNavItem.setVisibility(View.VISIBLE);
                sectionNavigationItem.getSectionItem().displayImage(context, baseImgUrl, imgNavItem,
                        R.drawable.loading_small, false);
            } else {
                imgNavItem.setVisibility(View.GONE);
            }
            if (sectionItem.getDescription() != null &&
                    !TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                txtNavListRowSubTitle.setVisibility(View.VISIBLE);
                txtNavListRowSubTitle.setText(sectionItem.getDescription().getText());
            } else {
                txtNavListRowSubTitle.setVisibility(View.GONE);
            }
            if (sectionItem.getSubSectionItems() != null
                    && sectionItem.getSubSectionItems().size() > 0) {
                imgNavItemExpand.setVisibility(View.VISIBLE);
            } else {
                imgNavItemExpand.setVisibility(View.GONE);
            }
        } else if (viewType == VIEW_TYPE_HEADER) {
            TextView txtNavListRowHeader = ((NavViewHeaderHolder) holder).getTxtNavListRowHeader();
            if (sectionNavigationItem.getSection().getTitle() != null &&
                    !TextUtils.isEmpty(sectionNavigationItem.getSection().getTitle().getText())) {
                txtNavListRowHeader.setText(sectionNavigationItem.getSection().getTitle().getText());
                txtNavListRowHeader.setVisibility(View.VISIBLE);
            } else {
                txtNavListRowHeader.setVisibility(View.GONE);
            }
        } else if (viewType == VIEW_TYPE_SECTION_ITEM_VERTICAL ||
                viewType == VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_VERTICAL) {
            SubNavHeaderViewHolder viewHolder = (SubNavHeaderViewHolder) holder;
            TextView txtNavListRow = viewHolder.getTxtNavListRow();
            ImageView imgNavItem = viewHolder.getImgNavItem();
            SectionItem sectionItem = sectionNavigationItem.getSectionItem();
            String sectionTitle = sectionItem.getTitle() != null ?
                    sectionItem.getTitle().getText() : null;
            if (!TextUtils.isEmpty(sectionTitle) && !TextUtils.isEmpty(sectionTitle.trim())) {
                txtNavListRow.setText(sectionTitle);
                txtNavListRow.setVisibility(View.VISIBLE);
            } else {
                txtNavListRow.setText("");
                txtNavListRow.setVisibility(View.GONE);
            }
            if (sectionItem.hasImage()) {
                imgNavItem.setVisibility(View.VISIBLE);
                sectionNavigationItem.getSectionItem().displayImage(context, baseImgUrl, imgNavItem,
                        R.drawable.loading_nav_header, true);
            } else {
                imgNavItem.setVisibility(View.GONE);
            }

            TextView txtNavMainItem = viewHolder.getTxtNavMainItem();
            if (viewType == VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_VERTICAL &&
                    parentSectionItem != null && parentSectionItem.getTitle() != null
                    && !TextUtils.isEmpty(parentSectionItem.getTitle().getText())) {
                txtNavMainItem.setTypeface(typeface);
                txtNavMainItem.setText(parentSectionItem.getTitle().getText());
                txtNavMainItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((SubNavigationAware) context).onSubNavigationHideRequested(true);
                    }
                });
            } else {
                txtNavMainItem.setVisibility(View.GONE);
            }
        } else if (viewType == VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_HORIZONTAL) {
            SubNavHeaderViewHolder viewHolder = (SubNavHeaderViewHolder) holder;
            TextView txtNavMainItem = viewHolder.getTxtNavMainItem();
            if (parentSectionItem != null && parentSectionItem.getTitle() != null
                    && !TextUtils.isEmpty(parentSectionItem.getTitle().getText())) {
                txtNavMainItem.setTypeface(typeface);
                txtNavMainItem.setText(parentSectionItem.getTitle().getText());
                txtNavMainItem.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((SubNavigationAware) context).onSubNavigationHideRequested(true);
                    }
                });
            } else {
                txtNavMainItem.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (sectionNavigationItems.get(position).isHeader()) {
            return VIEW_TYPE_HEADER;
        } else {
            SectionNavigationItem sectionNavigationItem = sectionNavigationItems.get(position);
            SectionItem sectionItem = sectionNavigationItem.getSectionItem();
            boolean isLink = false;
            if (sectionItem instanceof SubSectionItem) {
                isLink = ((SubSectionItem) sectionItem).isLink();
            }
            Renderer renderer = rendererHashMap != null ? rendererHashMap.get(sectionItem.getRenderingId()) : null;
            boolean hasParentSectionItemText = parentSectionItem != null && parentSectionItem.getTitle() != null
                    && !TextUtils.isEmpty(parentSectionItem.getTitle().getText());
            if (renderer != null && renderer.getOrientation() == Renderer.VERTICAL) {
                if (isLink && position == 0) {
                    return VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_VERTICAL;
                }
                return VIEW_TYPE_SECTION_ITEM_VERTICAL;
            } else if (position == 0 && hasParentSectionItemText) {
                return VIEW_TYPE_SUB_MENU_SECTION_ITEM_HEADER_HORIZONTAL;
            }
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
            int pos = getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                SectionNavigationItem sectionNavigationItem = sectionNavigationItems.get(pos);
                if (!sectionNavigationItem.isHeader()) {
                    if (sectionNavigationItem.getSectionItem() != null && sectionNavigationItem.getSectionItem().getSubSectionItems() != null
                            && sectionNavigationItem.getSectionItem().getSubSectionItems().size() > 0) {
                        ((SubNavigationAware) context).onSubNavigationRequested(sectionNavigationItem.getSection(),
                                sectionNavigationItem.getSectionItem(), baseImgUrl, rendererHashMap);
                    } else {
                        new OnSectionItemClickListener<>(context, sectionNavigationItem.getSection(),
                                sectionNavigationItem.getSectionItem(), screenName).onClick(v);
                    }
                }
            }
        }
    }

    private class SubNavHeaderViewHolder extends NavViewHolder {
        private TextView txtNavMainItem;

        public SubNavHeaderViewHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtNavMainItem() {
            if (txtNavMainItem == null) {
                txtNavMainItem = (TextView) itemView.findViewById(R.id.txtNavMainItem);
                txtNavMainItem.setTypeface(typeface);
            }
            return txtNavMainItem;
        }
    }
}
