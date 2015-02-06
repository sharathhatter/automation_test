package com.bigbasket.mobileapp.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SectionTextItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class CarouselAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Section section;
    protected ArrayList<SectionItem> sectionItems;
    protected HashMap<Integer, Renderer> rendererHashMap;
    protected Typeface typeface;
    protected T context;

    private static final int VIEW_TYPE_TEXT_IMG = 0;
    private static final int VIEW_TYPE_TEXT_DESC = 1;
    private static final int VIEW_TYPE_TEXT_ONLY = 2;

    public CarouselAdapter(T context, Section section,
                           HashMap<Integer, Renderer> rendererHashMap, Typeface typeface) {
        this.section = section;
        this.context = context;
        this.sectionItems = section.getSectionItems();
        this.rendererHashMap = rendererHashMap;
        this.typeface = typeface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        switch (viewType) {
            case VIEW_TYPE_TEXT_IMG:
                View row = inflater.inflate(R.layout.uiv3_carousel_row, parent, false);
                return new ViewHolder(row, typeface);
            case VIEW_TYPE_TEXT_DESC:
                row = inflater.inflate(R.layout.uiv3_text_desc_carousel_row, parent, false);
                return new ViewHolder(row, typeface);
            default:
                row = inflater.inflate(R.layout.uiv3_text_carousel_row, parent, false);
                return new ViewHolder(row, typeface);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        SectionItem sectionItem = sectionItems.get(position);

        SectionTextItem titleTextItem = sectionItem.getTitle();
        SectionTextItem descTextItem = sectionItem.getDescription();

        if (titleTextItem != null && !TextUtils.isEmpty(titleTextItem.getText())) {
            TextView txtTitle = holder.getTxtTitle();
            setSectionTextView(sectionItem.getTitle(), txtTitle);
        }

        if (descTextItem != null && !TextUtils.isEmpty(descTextItem.getText())) {
            TextView txtDescription = holder.getTxtDescription();
            setSectionTextView(sectionItem.getDescription(), txtDescription);
        }

        if (!TextUtils.isEmpty(sectionItem.getImage())) {
            ImageView imgInRow = holder.getImgInRow();
            ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgInRow);
        }

        LinearLayout layoutCarouselContainer = holder.getLayoutCarouselContainer();

        Renderer sectionRenderer = rendererHashMap != null ?
                rendererHashMap.get(sectionItem.getRenderingId()) : null;
        if (sectionRenderer != null) {
            int margin = sectionRenderer.getSafeMargin(0);
            if (margin > 0) {
                if (position == getItemCount() - 1) {
                    layoutCarouselContainer.setPadding(margin, 0, margin, 0);
                } else {
                    layoutCarouselContainer.setPadding(margin, 0, 0, 0);
                }
            }
        }
    }

    protected void setSectionTextView(SectionTextItem sectionTextView, TextView txtVw) {
        if (sectionTextView == null || TextUtils.isEmpty(sectionTextView.getText())) {
            txtVw.setVisibility(View.GONE);
        } else {
            Renderer renderer = rendererHashMap != null ?
                    rendererHashMap.get(sectionTextView.getRenderingId()) : null;
            txtVw.setVisibility(View.VISIBLE);
            txtVw.setText(sectionTextView.getText());
            if (renderer != null) {
                renderer.setRendering(txtVw, 0, Renderer.PADDING);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        SectionItem sectionItem = sectionItems.get(position);
        SectionTextItem titleTextItem = sectionItem.getTitle();
        SectionTextItem descTextItem = sectionItem.getDescription();

        boolean isTitlePresent = titleTextItem != null && !TextUtils.isEmpty(titleTextItem.getText());
        boolean isDescPresent = descTextItem != null && !TextUtils.isEmpty(descTextItem.getText());
        boolean isImgPresent = !TextUtils.isEmpty(sectionItem.getImage());

        if (isTitlePresent && isDescPresent) {
            if (isImgPresent) {
                return VIEW_TYPE_TEXT_IMG;
            }
            return VIEW_TYPE_TEXT_DESC;
        } else {
            return VIEW_TYPE_TEXT_ONLY;
        }
    }

    @Override
    public int getItemCount() {
        return sectionItems.size();
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private Typeface typeface;
        private ImageView imgInRow;
        private TextView txtTitle;
        private TextView txtDescription;
        private RelativeLayout layoutCarouselSubContainer;
        private LinearLayout layoutCarouselContainer;

        public ViewHolder(View itemView, Typeface typeface) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.typeface = typeface;
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
                txtTitle.setTypeface(typeface);
            }
            return txtTitle;
        }

        public TextView getTxtDescription() {
            if (txtDescription == null) {
                txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
                txtDescription.setTypeface(typeface);
            }
            return txtDescription;
        }

        public RelativeLayout getLayoutCarouselSubContainer() {
            if (layoutCarouselSubContainer == null) {
                layoutCarouselSubContainer = (RelativeLayout) itemView.findViewById(R.id.layoutCarouselSubContainer);
            }
            return layoutCarouselSubContainer;
        }

        public LinearLayout getLayoutCarouselContainer() {
            if (layoutCarouselContainer == null) {
                layoutCarouselContainer = (LinearLayout) itemView.findViewById(R.id.layoutCarouselContainer);
            }
            return layoutCarouselContainer;
        }

        @Override
        public void onClick(View v) {
            OnSectionItemClickListener sectionItemClickListener =
                    new OnSectionItemClickListener<>(((ActivityAware) context).getCurrentActivity(),
                            section, sectionItems.get(getPosition()));
            sectionItemClickListener.onClick(v);
        }
    }
}
