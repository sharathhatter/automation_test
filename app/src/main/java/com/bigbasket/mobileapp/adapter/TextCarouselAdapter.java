package com.bigbasket.mobileapp.adapter;

import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;

import java.util.HashMap;

public class TextCarouselAdapter<T> extends CarouselAdapter<T> {
    public TextCarouselAdapter(T context, Section section, HashMap<Integer, Renderer> rendererHashMap, int layoutId, Typeface typeface) {
        super(context, section, rendererHashMap, layoutId, typeface);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        SectionItem sectionItem = sectionItems.get(position);
        TextView txtTitle = holder.getTxtTitle();
        setSectionTextView(sectionItem.getTitle(), txtTitle);
        Renderer sectionRenderer = rendererHashMap != null ?
                rendererHashMap.get(sectionItem.getRenderingId()) : null;
        LinearLayout layoutCarouselContainer = holder.getLayoutCarouselContainer();
        if (sectionRenderer != null) {
            int margin = sectionRenderer.getSafeMargin(0);
            if (margin > 0) {
                if (position == getItemCount() - 1) {
                    layoutCarouselContainer.setPadding(margin, 0, margin, 0);
                } else {
                    layoutCarouselContainer.setPadding(margin, 0, 0, 0);
                }
            }
            if (sectionRenderer.getNativeBkgColor() != 0) {
                holder.getLayoutCarouselSubContainer().setBackgroundColor(sectionRenderer.getNativeBkgColor());
            }
        }
    }
}
