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
import android.webkit.WebView;
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
import com.nostra13.universalimageloader.core.ImageLoader;

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
        if (mSectionData == null || mSectionData.getRenderersMap() == null) return null;
        parseRendererColors();
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
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
                case Section.TEXT_TILE:
                    tileView = getTileView(section, inflater, mainLayout);
                    if (tileView != null) {
                        mainLayout.addView(tileView);
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
                    View menuView = getMenuView(section, inflater);
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
        baseSlider.setBackgroundColor(Color.CYAN);
        return baseSlider;
    }

    private View getSalutationView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseSalutation = inflater.inflate(R.layout.uiv3_salutation_box, parent, false);
        TextView txtSalutationTitle = (TextView) baseSalutation.findViewById(R.id.txtSalutationTitle);
        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            txtSalutationTitle.setTypeface(faceRobotoRegular);
            txtSalutationTitle.setText(section.getTitle().getText());
        } else {
            txtSalutationTitle.setVisibility(View.GONE);
        }
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
            if (!TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                txtSalutationItem.setTypeface(faceRobotoRegular);
                txtSalutationItem.setText(sectionItem.getTitle().getText());
                txtSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                layoutSalutationItem.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(sectionItem.getImage())) {
                layoutSalutationItem.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgSalutationItem);
                imgSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
            }
        }
        return baseSalutation;
    }

    private View getCarouselView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseProductCarousel = inflater.inflate(R.layout.uiv3_horizontal_recycler_view, parent, false);
        TextView txtListTitle = (TextView) baseProductCarousel.findViewById(R.id.txtListTitle);
        if (section.getTitle() == null || TextUtils.isEmpty(section.getTitle().getText())) {
            txtListTitle.setVisibility(View.GONE);
        } else {
            txtListTitle.setTypeface(faceRobotoRegular);
            txtListTitle.setText(section.getTitle().getText());
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtListTitle, 0, 0);
            }
        }

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
            WebView webView = new WebView(context);
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
            ImageLoader.getInstance().displayImage(sectionItem.getImage(), imageView);
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
                renderer.setRendering(txtVw, 0, 0);
            }
            txtVw.setText(sectionItem.getTitle().getText());
            txtVw.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
            txtVw.setTypeface(faceRobotoRegular);
            linearLayout.addView(txtVw);
        }
        return linearLayout;
    }

    private View getMenuView(Section section, LayoutInflater inflater) {
        LinearLayout menuContainer = new LinearLayout(context);
        menuContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams menuContainerLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        menuContainerLayoutParams.setMargins(defaultMargin, defaultMargin, defaultMargin, 0);
        menuContainer.setLayoutParams(menuContainerLayoutParams);

        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            View view = inflater.inflate(R.layout.uiv3_list_text, menuContainer, false);
            TextView txtVw = (TextView) view.findViewById(R.id.txtListText);
            view.findViewById(R.id.listArrow).setVisibility(View.GONE);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtVw, 0, 0);
            } else {
                txtVw.setBackgroundColor(context.getResources().getColor(R.color.uiv3_accent_color));
                txtVw.setTextColor(Color.WHITE);
            }
            txtVw.setText(section.getTitle().getText());
            txtVw.setTypeface(faceRobotoRegular);
            txtVw.setGravity(Gravity.LEFT);
            menuContainer.addView(view);
        }
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (sectionItem == null || sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText()))
                continue;
            View base = inflater.inflate(R.layout.uiv3_list_text, menuContainer, false);
            TextView txtListText = (TextView) base.findViewById(R.id.txtListText);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtListText, 0, 0);
            }
            txtListText.setTypeface(faceRobotoRegular);
            txtListText.setText(sectionItem.getTitle().getText());
            txtListText.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
            View viewSeparator = base.findViewById(R.id.viewSeparator);
            viewSeparator.setVisibility(View.VISIBLE);
            menuContainer.addView(base);
        }
        return menuContainer;
    }

    private View getTileView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View base = inflater.inflate(R.layout.uiv3_tile_container, parent, false);
        TextView txtListTitle = (TextView) base.findViewById(R.id.txtListTitle);
        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtListTitle, 0, 0);
            }
            txtListTitle.setText(section.getTitle().getText());
            txtListTitle.setTypeface(faceRobotoRegular);
        }
        LinearLayout tileContainer = (LinearLayout) base.findViewById(R.id.layoutTileContainer);
        LinearLayout.LayoutParams tileContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tileContainer.setLayoutParams(tileContainerParams);
        double tileWidth = getTileImageWidth(section, 0, 0);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (section.getSectionType().equals(Section.TILE)) {
                View tileItemView = inflater.inflate(R.layout.uiv3_image_caption_layout, tileContainer, false);
                Renderer renderer = mSectionData.getRenderersMap() != null ?
                        mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
                setTileLayoutParams(tileWidth, ViewGroup.LayoutParams.WRAP_CONTENT, tileItemView, renderer);
                TextView txtCaption = (TextView) tileItemView.findViewById(R.id.txtCaption);
                ImageView imgContent = (ImageView) tileItemView.findViewById(R.id.imgContent);
                if (!TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    txtCaption.setTypeface(faceRobotoRegular);
                    txtCaption.setText(sectionItem.getTitle().getText());
                } else {
                    txtCaption.setVisibility(View.GONE);
                }
                imgContent.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgContent);
                tileContainer.addView(tileItemView);
            } else if (section.getSectionType().equals(Section.TEXT_TILE)) {
                if (sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    continue;
                }
                boolean isDescPresent = sectionItem.getDescription() != null &&
                        !TextUtils.isEmpty(sectionItem.getDescription().getText());
                int layoutId = isDescPresent ? R.layout.uiv3_text_desc_carousel_row : R.layout.uiv3_text_carousel_row;
                View tileItemView = inflater.inflate(layoutId, tileContainer, false);
                TextView txtTitle = (TextView) tileItemView.findViewById(R.id.txtTitle);
                txtTitle.setTypeface(faceRobotoRegular);

                Renderer renderer = mSectionData.getRenderersMap() != null ?
                        mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
                setTileLayoutParams(tileWidth, context.getResources().getDimension(R.dimen.carousel_img_height),
                        tileItemView, renderer);
                txtTitle.setText(sectionItem.getTitle().getText());
                tileItemView.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem));
                tileContainer.addView(tileItemView);
            }
        }
        return base;
    }

    private void setTileLayoutParams(double tileWidth, double tileHeight, View tileItemView,
                                     Renderer renderer) {
        LinearLayout.LayoutParams tileItemLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tileItemLayoutParams.width = (int) tileWidth;
        if (tileHeight > 0) {
            tileItemLayoutParams.height = (int) tileHeight;
        }

        if (renderer != null) {
            renderer.setRendering(tileItemView, 0, 0);
        } else {
            tileItemLayoutParams.setMargins(0, 0, defaultMargin, 0);
        }

        tileItemView.setLayoutParams(tileItemLayoutParams);
    }

    private double getTileImageWidth(Section section, double extraSpace, int eachImageRightMargin) {
        double width = 0;
        if (section.getSectionItems() == null || section.getSectionItems().size() == 0) {
            return width;
        }
        width = context.getResources().getDisplayMetrics().widthPixels;
        int numImages = section.getSectionItems().size();
        return (width - extraSpace - (eachImageRightMargin * numImages)) / numImages;
    }
}
