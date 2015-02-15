package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.CarouselAdapter;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.util.UIUtil;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;

import org.apmem.tools.layouts.FlowLayout;

import java.util.ArrayList;

public class SectionView {

    private Context context;
    private Typeface faceRobotoRegular;
    private SectionData mSectionData;
    private int defaultMargin;

    public SectionView(Context context, Typeface faceRobotoRegular, SectionData mSectionData) {
        this.context = context;
        this.faceRobotoRegular = faceRobotoRegular;
        this.mSectionData = mSectionData;
        this.defaultMargin = (int) context.getResources().getDimension(R.dimen.margin_mini);
    }

    private void parseRendererColors() {
        if (mSectionData == null || mSectionData.getRenderersMap() == null) return;
        int defaultTextColor = context.getResources().getColor(R.color.uiv3_secondary_text_color);
        for (Renderer renderer : mSectionData.getRenderersMap().values()) {
            if (!TextUtils.isEmpty(renderer.getBackgroundColor())) {
                renderer.setNativeBkgColor(UIUtil.parseAsNativeColor(renderer.getBackgroundColor(), Color.WHITE));
            }
            if (!TextUtils.isEmpty(renderer.getTextColor())) {
                renderer.setNativeTextColor(UIUtil.parseAsNativeColor(renderer.getTextColor(), defaultTextColor));
            }
        }
    }

    @Nullable
    public View getView() {
        if (mSectionData == null || mSectionData.getSections() == null || mSectionData.getSections().size() == 0)
            return null;
        parseRendererColors();
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(context.getResources().getColor(R.color.uiv3_list_bkg_color));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (Section section : mSectionData.getSections()) {
            switch (section.getSectionType()) {
                case Section.BANNER:
                    View bannerView = getBannerView(section, inflater, mainLayout);
                    if (bannerView != null) {
                        mainLayout.addView(bannerView);
                    }
                    break;
                case Section.SALUTATION:
                    View salutationView = getSalutationView(section, inflater, mainLayout);
                    if (salutationView != null) {
                        mainLayout.addView(salutationView);
                    }
                    break;
                case Section.TILE:
                    View tileView = getTileView(section, inflater, mainLayout);
                    if (tileView != null) {
                        mainLayout.addView(tileView);
                    }
                    break;
                case Section.GRID:
                    View grid = getGridLayoutView(section, inflater, mainLayout);
                    if (grid != null) {
                        mainLayout.addView(grid);
                    }
                    break;
                case Section.PRODUCT_CAROUSEL:
                    View productCarouselView = getCarouselView(section, inflater, mainLayout);
                    if (productCarouselView != null) {
                        mainLayout.addView(productCarouselView);
                    }
                    break;
                case Section.NON_PRODUCT_CAROUSEL:
                    View carouselView = getCarouselView(section, inflater, mainLayout);
                    if (carouselView != null) {
                        mainLayout.addView(carouselView);
                    }
                    break;
                case Section.INFO_WIDGET:
                    View infoWidgetView = getInfoWidgetView(section);
                    if (infoWidgetView != null) {
                        mainLayout.addView(infoWidgetView);
                    }
                    break;
                case Section.AD_IMAGE:
                    View imageView = getImageView(section);
                    if (imageView != null) {
                        mainLayout.addView(imageView);
                    }
                    break;
                case Section.MSG:
                    View msgView = getMsgView(section, inflater);
                    if (msgView != null) {
                        mainLayout.addView(msgView);
                    }
                    break;
                case Section.MENU:
                    View menuView = getMenuView(section, inflater, mainLayout);
                    if (menuView != null) {
                        mainLayout.addView(menuView);
                    }
                    break;
            }
        }
        return mainLayout;
    }

    private View getBannerView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseSlider = inflater.inflate(R.layout.uiv3_image_slider, parent, false);
        SliderLayout bannerSlider = (SliderLayout) baseSlider.findViewById(R.id.imgSlider);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (!TextUtils.isEmpty(sectionItem.getImage())) {
                DefaultSliderView defaultSliderView = new DefaultSliderView(context);
                defaultSliderView.image(sectionItem.getImage());
                defaultSliderView.setOnSliderClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                bannerSlider.addSlider(defaultSliderView);
            }

        }
        return baseSlider;
    }

    private View getSalutationView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseSalutation = inflater.inflate(R.layout.uiv3_salutation_box, parent, false);
        formatSectionTitle(baseSalutation, R.id.txtSalutationTitle, section);
        ArrayList<SectionItem> sectionItems = section.getSectionItems();
        for (int i = 0; i < sectionItems.size(); i++) {
            if (i > 2) {
                // This widget at max can only support up to 3 items
                continue;
            }
            SectionItem sectionItem = sectionItems.get(i);
            TextView txtSalutationItem;
            ImageView imgSalutationItem;
            LinearLayout layoutSalutationItem;
            switch (i) {
                case 0:
                    layoutSalutationItem = (LinearLayout) baseSalutation.findViewById(R.id.layoutSalutationItem1);
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem1);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem1);
                    break;
                case 1:
                    layoutSalutationItem = (LinearLayout) baseSalutation.findViewById(R.id.layoutSalutationItem2);
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem2);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem2);
                    break;
                case 2:
                    layoutSalutationItem = (LinearLayout) baseSalutation.findViewById(R.id.layoutSalutationItem3);
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem3);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem3);
                    break;
                default:
                    layoutSalutationItem = (LinearLayout) baseSalutation.findViewById(R.id.layoutSalutationItem1);
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem1);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem1);
                    break;
            }
            if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                txtSalutationItem.setTypeface(faceRobotoRegular);
                txtSalutationItem.setText(sectionItem.getTitle().getText());
                txtSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                layoutSalutationItem.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(sectionItem.getImage())) {
                layoutSalutationItem.setVisibility(View.VISIBLE);
                sectionItem.displayImage(imgSalutationItem);
                imgSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
            }
        }
        return baseSalutation;
    }

    private View getCarouselView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseProductCarousel = inflater.inflate(R.layout.uiv3_horizontal_recycler_view, parent, false);
        formatSectionTitle(baseProductCarousel, R.id.txtListTitle, section);

        RecyclerView horizontalRecyclerView = (RecyclerView) baseProductCarousel.findViewById(R.id.horizontalRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(linearLayoutManager);
        horizontalRecyclerView.setHasFixedSize(false);

        CarouselAdapter carouselAdapter = new CarouselAdapter<>(context, section, mSectionData.getRenderersMap(),
                faceRobotoRegular);
        horizontalRecyclerView.setAdapter(carouselAdapter);
        return baseProductCarousel;
    }

    private View getInfoWidgetView(Section section) {
        if (section.getSectionItems() == null || section.getSectionItems().size() == 0)
            return null;
        LinearLayout base = new LinearLayout(context);
        base.setOrientation(LinearLayout.VERTICAL);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (sectionItem.getDescription() == null || TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                continue;
            }
            BBWebView webView = new BBWebView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getDescription().getRenderingId()) : null;
            if (renderer != null) {
                int margin = renderer.getSafeMargin(0);
                layoutParams.setMargins(margin, margin, margin, 0);
            }
            webView.setLayoutParams(layoutParams);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadData(sectionItem.getDescription().getText(), "text/html", "UTF-8");
            base.addView(webView);
        }
        return base;
    }

    private View getImageView(Section section) {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams baseLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(baseLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (TextUtils.isEmpty(sectionItem.getImage()))
                continue;
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(imageView, 0, 0);
            }
            imageView.setLayoutParams(layoutParams);
            imageView.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
            sectionItem.displayImage(imageView);
        }
        return linearLayout;
    }

    private View getMsgView(Section section, LayoutInflater inflater) {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams baseLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(baseLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText()))
                continue;
            TextView txtVw = (TextView) inflater.inflate(R.layout.uiv3_msg_text, linearLayout, false);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtVw, 0, 0, true, true, true, false);
            }
            txtVw.setText(sectionItem.getTitle().getText());
            txtVw.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
            txtVw.setTypeface(faceRobotoRegular);
            linearLayout.addView(txtVw);
        }
        return linearLayout;
    }

    private View getMenuView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View base = inflater.inflate(R.layout.card_view, parent, false);
        LinearLayout menuContainer = (LinearLayout) base.findViewById(R.id.layoutCardContent);

        LinearLayout.LayoutParams menuContainerLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        menuContainerLayoutParams.setMargins(defaultMargin,
                (int) context.getResources().getDimension(R.dimen.padding_normal), defaultMargin, 0);
        base.setLayoutParams(menuContainerLayoutParams);

        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            View view = inflater.inflate(R.layout.uiv3_list_text, menuContainer, false);
            TextView txtVw = (TextView) view.findViewById(R.id.txtListText);
            view.findViewById(R.id.listArrow).setVisibility(View.GONE);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtVw, 0, 0);
            } else {
                txtVw.setBackgroundColor(context.getResources().getColor(R.color.uiv3_menu_header));
                txtVw.setTextColor(context.getResources().getColor(R.color.uiv3_primary_text_color));
            }
            txtVw.setText(section.getTitle().getText());
            txtVw.setTypeface(faceRobotoRegular);
            txtVw.setGravity(Gravity.LEFT);
            menuContainer.addView(view);
        }

        int numItems = section.getSectionItems().size();
        for (int i = 0; i < numItems; i++) {
            SectionItem sectionItem = section.getSectionItems().get(i);
            if (sectionItem == null || sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText()))
                continue;
            View itemView = inflater.inflate(R.layout.uiv3_list_text, menuContainer, false);
            TextView txtListText = (TextView) itemView.findViewById(R.id.txtListText);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtListText, 0, 0);
            } else {
                itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
            }
            txtListText.setTypeface(faceRobotoRegular);
            txtListText.setText(sectionItem.getTitle().getText());
            txtListText.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
            View viewSeparator = itemView.findViewById(R.id.viewSeparator);
            viewSeparator.setVisibility(i == numItems - 1 ? View.GONE : View.VISIBLE);
            menuContainer.addView(itemView);
        }
        return base;
    }

    private View getGridLayoutView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View base = inflater.inflate(R.layout.uiv3_grid_container, parent, false);
        formatSectionTitle(base, R.id.txtListTitle, section);

        FlowLayout tileContainer = (FlowLayout) base.findViewById(R.id.layoutGrid);
        LinearLayout.LayoutParams tileContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tileContainerParams.setMargins(defaultMargin, 0, defaultMargin, 0);
        tileContainer.setLayoutParams(tileContainerParams);
        ArrayList<SectionItem> sectionItems = section.getSectionItems();
        int numSectionItems = sectionItems.size();
        for (int i = 0; i < numSectionItems; i++) {
            SectionItem sectionItem = sectionItems.get(i);
            boolean applyRight = i != numSectionItems - 1;
            if (sectionItem.getViewType() == SectionItem.VIEW_TYPE_TEXT_IMG) {
                View tileItemView = inflater.inflate(R.layout.uiv3_grid_image_caption_layout, tileContainer, false);
                Renderer renderer = mSectionData.getRenderersMap() != null ?
                        mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
                if (renderer != null) {
                    int width = (int) context.getResources().getDimension(R.dimen.grid_width);
                    renderer.setRendering(tileItemView, 0, 0, false, true, applyRight, true, width);
                    int margin = renderer.getSafeMargin(defaultMargin);
                    if (margin > 0) {
                        FlowLayout.LayoutParams layoutParams = new FlowLayout.
                                LayoutParams(width,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(0, margin, applyRight ? margin : 0, margin);
                        tileItemView.setLayoutParams(layoutParams);
                    }
                } else {
                    FlowLayout.LayoutParams layoutParams = new FlowLayout.
                            LayoutParams((int) context.getResources().getDimension(R.dimen.grid_width),
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0, defaultMargin, applyRight ? defaultMargin : 0, defaultMargin);
                    tileItemView.setLayoutParams(layoutParams);
                }
                TextView txtCaption = (TextView) tileItemView.findViewById(R.id.txtCaption);
                ImageView imgContent = (ImageView) tileItemView.findViewById(R.id.imgContent);
                if (sectionItem.getTitle() != null &&
                        !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    txtCaption.setTypeface(faceRobotoRegular);
                    txtCaption.setText(sectionItem.getTitle().getText());
                    Renderer textRenderer = mSectionData.getRenderersMap() != null ?
                            mSectionData.getRenderersMap().get(sectionItem.getTitle().getRenderingId()) : null;
                    if (textRenderer != null) {
                        textRenderer.setRendering(txtCaption, 0, 0);
                    }
                } else {
                    txtCaption.setVisibility(View.GONE);
                }
                imgContent.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                sectionItem.displayImage(imgContent);
                tileContainer.addView(tileItemView);
            } else {
                if (sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    continue;
                }
                boolean isDescPresent = sectionItem.getDescription() != null &&
                        !TextUtils.isEmpty(sectionItem.getDescription().getText());
                int layoutId = isDescPresent ? R.layout.uiv3_text_desc_carousel_row : R.layout.uiv3_text_carousel_row;
                View tileItemView = inflater.inflate(layoutId, tileContainer, false);
                TextView txtTitle = (TextView) tileItemView.findViewById(R.id.txtTitle);
                txtTitle.setTypeface(faceRobotoRegular);
                txtTitle.setText(sectionItem.getTitle().getText());

                Renderer renderer = mSectionData.getRenderersMap() != null ?
                        mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
                if (renderer != null) {
                    int width = (int) context.getResources().getDimension(R.dimen.grid_width);
                    renderer.setRendering(tileItemView, defaultMargin, 0, false, true, applyRight, true, width);
                    int margin = renderer.getSafeMargin(defaultMargin);
                    if (margin > 0) {
                        FlowLayout.LayoutParams layoutParams = new FlowLayout.
                                LayoutParams(width,
                                (int) context.getResources().getDimension(R.dimen.carousel_img_height));
                        layoutParams.setMargins(0, margin, applyRight ? margin : 0, margin);
                        tileItemView.setLayoutParams(layoutParams);
                    }
                } else {
                    FlowLayout.LayoutParams layoutParams = new FlowLayout.
                            LayoutParams((int) context.getResources().getDimension(R.dimen.grid_width),
                            (int) context.getResources().getDimension(R.dimen.carousel_img_height));
                    layoutParams.setMargins(0, defaultMargin, applyRight ? defaultMargin : 0, defaultMargin);
                    tileItemView.setLayoutParams(layoutParams);
                }

                if (isDescPresent) {
                    TextView txtDescription = (TextView) tileItemView.findViewById(R.id.txtDescription);
                    txtDescription.setTypeface(faceRobotoRegular);
                    txtDescription.setText(sectionItem.getDescription().getText());
                }
                tileItemView.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                tileContainer.addView(tileItemView);
            }
        }
        return base;
    }

    private View getTileView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View base = inflater.inflate(R.layout.uiv3_tile_container, parent, false);
        formatSectionTitle(base, R.id.txtListTitle, section);

        LinearLayout tileContainer = (LinearLayout) base.findViewById(R.id.layoutTileContainer);
        LinearLayout.LayoutParams tileContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tileContainerParams.setMargins(defaultMargin,
                (int) context.getResources().getDimension(R.dimen.margin_normal), defaultMargin, 0);
        tileContainer.setLayoutParams(tileContainerParams);
        ArrayList<SectionItem> sectionItems = section.getSectionItems();
        int numSectionItems = sectionItems.size();
        for (int i = 0; i < numSectionItems; i++) {
            SectionItem sectionItem = sectionItems.get(i);
            boolean applyRight = i != numSectionItems - 1;
            if (sectionItem.getViewType() == SectionItem.VIEW_TYPE_TEXT_IMG) {
                View tileItemView = inflater.inflate(R.layout.uiv3_image_caption_layout, tileContainer, false);
                Renderer renderer = mSectionData.getRenderersMap() != null ?
                        mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
                if (renderer != null) {
                    renderer.setRendering(tileItemView, 0, 0, false, true, applyRight, true, 0);
                } else {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.
                            LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    layoutParams.setMargins(0, defaultMargin, applyRight ? defaultMargin : 0, defaultMargin);
                    tileItemView.setLayoutParams(layoutParams);
                }
                TextView txtCaption = (TextView) tileItemView.findViewById(R.id.txtCaption);
                ImageView imgContent = (ImageView) tileItemView.findViewById(R.id.imgContent);
                if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    txtCaption.setTypeface(faceRobotoRegular);
                    txtCaption.setText(sectionItem.getTitle().getText());
                    Renderer textRenderer = mSectionData.getRenderersMap() != null ?
                            mSectionData.getRenderersMap().get(sectionItem.getTitle().getRenderingId()) : null;
                    if (textRenderer != null) {
                        textRenderer.setRendering(txtCaption, 0, 0);
                    }
                } else {
                    txtCaption.setVisibility(View.GONE);
                }
                imgContent.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                sectionItem.displayImage(imgContent);
                tileContainer.addView(tileItemView);
            } else {
                if (sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    continue;
                }
                boolean isDescPresent = sectionItem.getDescription() != null &&
                        !TextUtils.isEmpty(sectionItem.getDescription().getText());
                int layoutId = isDescPresent ? R.layout.uiv3_text_desc_tile_row : R.layout.uiv3_text_tile_row;
                View tileItemView = inflater.inflate(layoutId, tileContainer, false);
                TextView txtTitle = (TextView) tileItemView.findViewById(R.id.txtTitle);
                txtTitle.setTypeface(faceRobotoRegular);

                Renderer renderer = mSectionData.getRenderersMap() != null ?
                        mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
                if (renderer != null) {
                    renderer.setRendering(tileItemView, 0, 0, false, true, applyRight, true, 0);
                } else {
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.
                            LayoutParams(0, (int) context.getResources().getDimension(R.dimen.carousel_img_height), 1);
                    layoutParams.setMargins(0, defaultMargin, applyRight ? defaultMargin : 0, defaultMargin);
                    tileItemView.setLayoutParams(layoutParams);
                }
                txtTitle.setText(sectionItem.getTitle().getText());
                tileItemView.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                tileContainer.addView(tileItemView);
            }
        }
        return base;
    }

    private void formatSectionTitle(View parent, int txtViewId, Section section) {
        TextView txtVw = (TextView) parent.findViewById(txtViewId);
        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            txtVw.setTypeface(faceRobotoRegular);
            txtVw.setText(section.getTitle().getText());
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtVw
                        , 0, 0);
            }
        } else {
            txtVw.setVisibility(View.GONE);
        }
    }
}
