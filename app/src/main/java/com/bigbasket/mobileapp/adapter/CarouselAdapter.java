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
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SectionTextItem;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;

public class CarouselAdapter<T> extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {

    private ArrayList<SectionItem> sectionItems;
    private HashMap<Integer, Renderer> rendererHashMap;
    private Typeface typeface;
    private T context;
    private int layoutId;

    public CarouselAdapter(T context, ArrayList<SectionItem> sectionItems,
                           HashMap<Integer, Renderer> rendererHashMap, int layoutId, Typeface typeface) {
        this.context = context;
        this.layoutId = layoutId;
        this.sectionItems = sectionItems;
        this.rendererHashMap = rendererHashMap;
        this.typeface = typeface;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        View row = inflater.inflate(layoutId, null);
        return new ViewHolder(row, typeface);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SectionItem sectionItem = sectionItems.get(position);
        TextView txtTitle = holder.getTxtTitle();
        TextView txtDescription = holder.getTxtDescription();
        ImageView imgInRow = holder.getImgInRow();
        LinearLayout layoutCarouselContainer = holder.getLayoutCarouselContainer();

        setSectionTextView(sectionItem.getTitle(), txtTitle, position);
        setSectionTextView(sectionItem.getDescription(), txtDescription, position);

        Renderer sectionRenderer = rendererHashMap != null ?
                rendererHashMap.get(sectionItem.getRenderingId()) : null;
        if (sectionRenderer != null) {
            int margin = sectionRenderer.getSafeMargin(0);
            if (margin > 0) {
//                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT);
                if (position == getItemCount() - 1) {
                    layoutCarouselContainer.setPadding(margin, 0, margin, 0);
                } else {
                    layoutCarouselContainer.setPadding(margin, 0, 0, 0);
                }
//                layoutCarouselSubContainer.setLayoutParams(layoutParams);
            }
        }

        if (!TextUtils.isEmpty(sectionItem.getImage())) {
            imgInRow.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgInRow);
        } else {
            imgInRow.setVisibility(View.GONE);
        }
    }

    private void setSectionTextView(SectionTextItem sectionTextView, TextView txtVw, int position) {
        if (sectionTextView == null || TextUtils.isEmpty(sectionTextView.getText())) {
            txtVw.setVisibility(View.GONE);
        } else {
            Renderer renderer = rendererHashMap != null ?
                    rendererHashMap.get(sectionTextView.getRenderingId()) : null;
            txtVw.setVisibility(View.VISIBLE);
            txtVw.setText(sectionTextView.getText());
            if (renderer != null) {
                txtVw.setBackgroundColor(renderer.getNativeBkgColor());
                txtVw.setTextColor(renderer.getNativeTextColor());
                int padding = renderer.getSafePadding(Renderer.PADDING);
                int margin = renderer.getSafeMargin(0);
                txtVw.setPadding(padding, padding, padding, padding);
                if (margin > 0) {
                    RelativeLayout.LayoutParams layoutParams =
                            new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    if (position == getItemCount() - 1) {
                        layoutParams.setMargins(margin, margin, margin, margin);
                    } else {
                        layoutParams.setMargins(margin, margin, 0, margin);
                    }
                    txtVw.setLayoutParams(layoutParams);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return sectionItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private Typeface typeface;
        private ImageView imgInRow;
        private TextView txtTitle;
        private TextView txtDescription;
        private RelativeLayout layoutCarouselSubContainer;
        private LinearLayout layoutCarouselContainer;

        public ViewHolder(View itemView, Typeface typeface) {
            super(itemView);
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
    }
}
