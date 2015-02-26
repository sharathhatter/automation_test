package com.bigbasket.mobileapp.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;

import java.util.ArrayList;
import java.util.HashMap;

public class SectionGridAdapter<T> extends BaseAdapter {

    protected Section section;
    protected ArrayList<SectionItem> sectionItems;
    protected HashMap<Integer, Renderer> rendererHashMap;
    protected Typeface typeface;
    protected T context;
    protected String screenName;
    private int numItems;
    private int defaultMargin;
    private int defaultTxtPadding;
    private int primaryTxtColor;
    private int primaryBkgColor;

    public SectionGridAdapter(T context, Section section,
                              HashMap<Integer, Renderer> rendererHashMap, Typeface typeface, String screenName) {
        this.section = section;
        this.context = context;
        this.sectionItems = section.getSectionItems();
        this.rendererHashMap = rendererHashMap;
        this.typeface = typeface;
        this.screenName = screenName;
        this.numItems = sectionItems.size();
        Context ctx = ((ActivityAware) context).getCurrentActivity();
        this.defaultMargin = (int) ctx.getResources().getDimension(R.dimen.margin_mini);
        this.defaultTxtPadding = (int) ctx.getResources().getDimension(R.dimen.padding_small);
        this.primaryTxtColor = ctx.getResources().getColor(R.color.uiv3_primary_text_color);
        this.primaryBkgColor = ctx.getResources().getColor(R.color.white);
    }

    @Override
    public int getCount() {
        return numItems;
    }

    @Override
    public Object getItem(int position) {
        return sectionItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 10;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        int viewType = getItemViewType(position);
        if (viewType == SectionItem.VIEW_UNKNOWN) {
            view = new View(((ActivityAware) context).getCurrentActivity());
        } else {
            ViewHolder viewHolder;
            if (view == null) {
                LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
                int layoutId = SectionItem.getLayoutResId(viewType);
                view = inflater.inflate(layoutId, parent, false);
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                view.setLayoutParams(layoutParams);

                viewHolder = new ViewHolder(view);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            SectionItem sectionItem = sectionItems.get(position);
            boolean applyRight = position != numItems - 1;

            Renderer renderer = rendererHashMap != null ?
                    rendererHashMap.get(sectionItem.getRenderingId()) : null;
            ViewGroup layoutSection = viewHolder.getLayoutSection();
            if (layoutSection != null) {
                ViewGroup.LayoutParams layoutSectionLayoutParams = layoutSection.getLayoutParams();
                layoutSectionLayoutParams.height = sectionItem.getHeight(((ActivityAware) context).getCurrentActivity(), renderer);
                layoutSection.setLayoutParams(layoutSectionLayoutParams);
            }

            TextView txtTitle = viewHolder.getTxtTitle();
            TextView txtDescription = viewHolder.getTxtDescription();
            ImageView imgInRow = viewHolder.getImgInRow();

            if (txtTitle != null) {
                if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    txtTitle.setVisibility(View.VISIBLE);
                    txtTitle.setTypeface(typeface);
                    txtTitle.setText(sectionItem.getTitle().getText());
                    Renderer itemRenderer = rendererHashMap != null ?
                            rendererHashMap.get(sectionItem.getTitle().getRenderingId()) : null;
                    if (itemRenderer != null) {
                        itemRenderer.setRendering(txtTitle, defaultMargin, defaultMargin, true, true, true, true);
                    } else {
                        txtTitle.setTextColor(primaryTxtColor);
                        txtTitle.setBackgroundColor(primaryBkgColor);
                        txtTitle.setPadding(defaultTxtPadding, defaultTxtPadding, defaultTxtPadding, defaultTxtPadding);
                    }
                } else {
                    txtTitle.setVisibility(View.GONE);
                }
            }

            if (txtDescription != null) {
                if (sectionItem.getDescription() != null && !TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                    txtDescription.setVisibility(View.VISIBLE);
                    txtDescription.setTypeface(typeface);
                    txtDescription.setText(sectionItem.getDescription().getText());
                    Renderer itemRenderer = rendererHashMap != null ?
                            rendererHashMap.get(sectionItem.getDescription().getRenderingId()) : null;
                    if (itemRenderer != null) {
                        itemRenderer.setRendering(txtTitle, defaultMargin, defaultMargin, true, true, true, true);
                    } else {
                        txtDescription.setTextColor(primaryTxtColor);
                        txtDescription.setBackgroundColor(primaryBkgColor);
                        txtDescription.setPadding(defaultTxtPadding, defaultTxtPadding, defaultTxtPadding, defaultTxtPadding);
                    }
                } else {
                    txtDescription.setVisibility(View.GONE);
                }
            }

            if (imgInRow != null) {
                if (!TextUtils.isEmpty(sectionItem.getImage())) {
                    imgInRow.setVisibility(View.VISIBLE);
                    sectionItem.displayImage(imgInRow);
                } else {
                    imgInRow.setVisibility(View.GONE);
                }
            }

            if (renderer != null) {
                ViewGroup sectionLayoutContainer = viewHolder.getSectionLayoutContainer();
                if (sectionLayoutContainer != null) {
                    int margin = renderer.getSafeMargin(0);
                    if (margin > 0) {
                        sectionLayoutContainer.setPadding(margin, 0, applyRight ? margin : 0, 0);
                    }
                }

            }

        }
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        SectionItem sectionItem = sectionItems.get(position);
        Renderer renderer = rendererHashMap != null ?
                rendererHashMap.get(sectionItem.getRenderingId()) : null;
        return sectionItem.getItemViewType(renderer);
    }


    protected class ViewHolder {

        private View itemView;
        private ImageView imgInRow;
        private TextView txtTitle;
        private TextView txtDescription;
        private ViewGroup sectionLayoutContainer;

        private ViewGroup layoutSection;

        public ViewGroup getLayoutSection() {
            if (layoutSection == null) {
                layoutSection = (ViewGroup) itemView.findViewById(R.id.layoutSection);
            }
            return layoutSection;
        }

        public ViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public ImageView getImgInRow() {
            if (imgInRow == null) {
                imgInRow = (ImageView) itemView.findViewById(R.id.imgInRow);
            }
            return imgInRow;
        }

        public TextView getTxtTitle() {
            if (txtTitle == null) {
                txtTitle = (TextView) itemView.findViewById(R.id.txtTitle);
            }
            return txtTitle;
        }

        public TextView getTxtDescription() {
            if (txtDescription == null) {
                txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            }
            return txtDescription;
        }

        public ViewGroup getSectionLayoutContainer() {
            if (sectionLayoutContainer == null) {
                sectionLayoutContainer = (ViewGroup) itemView.findViewById(R.id.layoutCarouselContainer);
            }
            return sectionLayoutContainer;
        }

//        @Override
//        public void onClick(View v) {
//            OnSectionItemClickListener sectionItemClickListener =
//                    new OnSectionItemClickListener<>(((ActivityAware) context).getCurrentActivity(),
//                            section, sectionItems.get(getPosition()), screenName);
//            sectionItemClickListener.onClick(v);
//        }
    }
}
