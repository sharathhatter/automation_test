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
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;

import java.util.ArrayList;
import java.util.HashMap;

public class CarouselAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    protected Section section;
    protected ArrayList<SectionItem> sectionItems;
    protected HashMap<Integer, Renderer> rendererHashMap;
    protected Typeface typeface;
    protected T context;
    protected String screenName;
    private int numItems;
    private int fourDp;
    private int eightDp;
    private int columnWidth;
    private String baseImgUrl;

    public CarouselAdapter(T context, Section section, String baseImgUrl,
                           HashMap<Integer, Renderer> rendererHashMap, Typeface typeface, String screenName) {
        this.section = section;
        this.context = context;
        this.sectionItems = section.getSectionItems();
        this.rendererHashMap = rendererHashMap;
        this.typeface = typeface;
        this.screenName = screenName;
        this.numItems = sectionItems.size();
        Context ctx = ((ActivityAware) context).getCurrentActivity();
        this.fourDp = (int) ctx.getResources().getDimension(R.dimen.margin_mini);
        this.eightDp = (int) ctx.getResources().getDimension(R.dimen.padding_small);
        this.columnWidth = (int) ctx.getResources().getDimension(R.dimen.grid_width);
        this.baseImgUrl = baseImgUrl;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        if (viewType == SectionItem.VIEW_UNKNOWN) {
            return new FixedLayoutViewHolder(new View(((ActivityAware) context).getCurrentActivity()));
        } else {
            int layoutId = SectionItem.getLayoutResId(viewType);
            View view = inflater.inflate(layoutId, parent, false);
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            layoutParams.width = columnWidth;
            view.setLayoutParams(layoutParams);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == SectionItem.VIEW_UNKNOWN) {
            return;
        }
        ViewHolder holder = (ViewHolder) viewHolder;
        SectionItem sectionItem = sectionItems.get(position);
        Renderer renderer = rendererHashMap != null ?
                rendererHashMap.get(sectionItem.getRenderingId()) : null;

        TextView txtTitle = holder.getTxtTitle();
        TextView txtDescription = holder.getTxtDescription();
        ImageView imgInRow = holder.getImgInRow();

        ViewGroup layoutSection = holder.getLayoutSection();
        if (layoutSection != null) {
            ViewGroup.LayoutParams layoutSectionLayoutParams = layoutSection.getLayoutParams();
            layoutSectionLayoutParams.height = sectionItem.getHeight(((ActivityAware) context).getCurrentActivity(), renderer);
            layoutSection.setLayoutParams(layoutSectionLayoutParams);
        }

        if (txtTitle != null) {
            if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setTypeface(typeface);
                txtTitle.setText(sectionItem.getTitle().getText());
                Renderer itemRenderer = rendererHashMap != null ?
                        rendererHashMap.get(sectionItem.getTitle().getRenderingId()) : null;
                if (itemRenderer != null) {
                    itemRenderer.setRendering(txtTitle, fourDp, fourDp, true, true, true, true);
                    if (itemRenderer.getPadding() == 0 && sectionItem.isOverlayWithAdjacentTitleDesc(viewType)) {
                        itemRenderer.adjustTitlePaddingForOverlayWithAdjacentTitleAndDesc(eightDp, fourDp, txtTitle, txtDescription, sectionItem);
                    }
                } else if (sectionItem.isOverlayWithAdjacentTitleDesc(viewType)) {
                    txtTitle.setPadding(eightDp, eightDp, eightDp, txtDescription != null && sectionItem.hasDescription() ? fourDp : eightDp);
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
                    itemRenderer.setRendering(txtDescription, fourDp, fourDp, true, true, true, true);
                    if (itemRenderer.getPadding() == 0 && sectionItem.isOverlayWithAdjacentTitleDesc(viewType)) {
                        itemRenderer.adjustDescPaddingForOverlayWithAdjacentTitleAndDesc(eightDp, 0, txtTitle, txtDescription, sectionItem);
                    }
                } else if (sectionItem.isOverlayWithAdjacentTitleDesc(viewType)) {
                    txtDescription.setPadding(txtTitle != null && sectionItem.hasTitle() ? 0 : eightDp, eightDp, eightDp, eightDp);
                }
            } else {
                txtDescription.setVisibility(View.GONE);
            }
        }

        if (imgInRow != null) {
            if (sectionItem.hasImage()) {
                imgInRow.setVisibility(View.VISIBLE);
                sectionItem.displayImage(((ActivityAware) context).getCurrentActivity(),
                        baseImgUrl, imgInRow);
                imgInRow.setBackgroundColor(((ActivityAware) context).getCurrentActivity().randomColor());
            } else {
                imgInRow.setVisibility(View.GONE);
            }
        }

        if (renderer != null) {
            ViewGroup sectionLayoutContainer = holder.getSectionLayoutContainer();
            if (sectionLayoutContainer != null) {
                int margin = renderer.getSafeMargin(0);
                if (margin > 0) {
                    sectionLayoutContainer.setPadding(0, 0, margin, 0);
                }
            }
        } else {
            ViewGroup sectionLayoutContainer = holder.getSectionLayoutContainer();
            if (sectionLayoutContainer != null) {
                sectionLayoutContainer.setPadding(0, 0, fourDp, 0);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        SectionItem sectionItem = sectionItems.get(position);
        Renderer renderer = rendererHashMap != null ?
                rendererHashMap.get(sectionItem.getRenderingId()) : null;
        return sectionItem.getItemViewType(renderer, section.getSectionType());
    }

    @Override
    public int getItemCount() {
        return numItems;
    }

    protected class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView imgInRow;
        private TextView txtTitle;
        private TextView txtDescription;
        private ViewGroup sectionLayoutContainer;
        private ViewGroup layoutSection;

        public ViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public ViewGroup getLayoutSection() {
            if (layoutSection == null) {
                layoutSection = (ViewGroup) itemView.findViewById(R.id.layoutSection);
            }
            return layoutSection;
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
                sectionLayoutContainer = (ViewGroup) itemView.findViewById(R.id.sectionLayoutContainer);
            }
            return sectionLayoutContainer;
        }

        @Override
        public void onClick(View v) {
            OnSectionItemClickListener sectionItemClickListener =
                    new OnSectionItemClickListener<>(((ActivityAware) context).getCurrentActivity(),
                            section, sectionItems.get(getPosition()), screenName);
            sectionItemClickListener.onClick(v);
        }
    }
}
