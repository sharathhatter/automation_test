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
    protected int layoutId;

    public CarouselAdapter(T context, Section section,
                           HashMap<Integer, Renderer> rendererHashMap, int layoutId, Typeface typeface) {
        this.section = section;
        this.context = context;
        this.layoutId = layoutId;
        this.sectionItems = section.getSectionItems();
        this.rendererHashMap = rendererHashMap;
        this.typeface = typeface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        View row = inflater.inflate(layoutId, parent, false);
        return new ViewHolder(row, typeface);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        SectionItem sectionItem = sectionItems.get(position);
        TextView txtTitle = holder.getTxtTitle();
        TextView txtDescription = holder.getTxtDescription();
        ImageView imgInRow = holder.getImgInRow();
        LinearLayout layoutCarouselContainer = holder.getLayoutCarouselContainer();

        setSectionTextView(sectionItem.getTitle(), txtTitle);
        setSectionTextView(sectionItem.getDescription(), txtDescription);

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

        if (!TextUtils.isEmpty(sectionItem.getImage())) {
            imgInRow.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgInRow);
        } else {
            imgInRow.setVisibility(View.GONE);
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
