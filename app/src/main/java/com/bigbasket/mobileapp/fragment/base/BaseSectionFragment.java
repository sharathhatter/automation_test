package com.bigbasket.mobileapp.fragment.base;

import android.graphics.Color;
import android.os.Bundle;
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
import com.bigbasket.mobileapp.adapter.TextCarouselAdapter;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.util.Constants;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

public abstract class BaseSectionFragment extends BaseFragment {

    protected SectionData mSectionData;

    public void displaySections(LinearLayout mainLayout) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
                    View tileView = getTileView(section, inflater);
                    if (tileView != null) {
                        mainLayout.addView(tileView);
                    }
                    break;
                case Section.TEXT_TILE:
                    tileView = getTileView(section, inflater);
                    if (tileView != null) {
                        mainLayout.addView(tileView);
                    }
                    break;
                case Section.PRODUCT_CAROUSEL:
                    View productCarouselView = getCarouselView(section, inflater,
                            R.layout.uiv3_product_carousel_row, R.layout.uiv3_horizontal_product_recyclerview, mainLayout);
                    if (productCarouselView != null) {
                        mainLayout.addView(productCarouselView);
                    }
                    break;
                case Section.NON_PRODUCT_CAROUSEL:
                    View carouselView = getCarouselView(section, inflater,
                            R.layout.uiv3_carousel_row, R.layout.uiv3_horizontal_recycler_view, mainLayout);
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
                case Section.TEXT_CAROUSEL:
                    View txtCarouselView = getCarouselView(section, inflater,
                            R.layout.uiv3_text_carousel_row, R.layout.uiv3_horizontal_recycler_view,
                            mainLayout);
                    if (txtCarouselView != null) {
                        mainLayout.addView(txtCarouselView);
                    }
                    break;
            }
        }
    }

    private View getBannerView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseSlider = inflater.inflate(R.layout.uiv3_image_slider, parent, false);
        SliderLayout bannerSlider = (SliderLayout) baseSlider.findViewById(R.id.imgSlider);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (!TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView.description(sectionItem.getTitle().getText())
                        .image(sectionItem.getImage());
                bannerSlider.addSlider(textSliderView);
                textSliderView.setOnSliderClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
            } else {
                DefaultSliderView defaultSliderView = new DefaultSliderView(getActivity());
                defaultSliderView.image(sectionItem.getImage());
                defaultSliderView.setOnSliderClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
                bannerSlider.addSlider(defaultSliderView);
            }
        }
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
                txtSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
                layoutSalutationItem.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(sectionItem.getImage())) {
                layoutSalutationItem.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgSalutationItem);
                imgSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
            }
        }
        return baseSalutation;
    }

    private View getCarouselView(Section section, LayoutInflater inflater, int layoutId,
                                 int listLayoutId, ViewGroup parent) {
        View baseProductCarousel = inflater.inflate(listLayoutId, parent, false);
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
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(linearLayoutManager);
        horizontalRecyclerView.setHasFixedSize(false);

        if (section.getSectionType().equals(Section.TEXT_CAROUSEL)) {
            TextCarouselAdapter textCarouselAdapter = new TextCarouselAdapter<>(this, section, mSectionData.getRenderersMap(),
                    layoutId, faceRobotoRegular);
            horizontalRecyclerView.setAdapter(textCarouselAdapter);
        } else {
            CarouselAdapter carouselAdapter = new CarouselAdapter<>(this, section, mSectionData.getRenderersMap(),
                    layoutId, faceRobotoRegular);
            horizontalRecyclerView.setAdapter(carouselAdapter);
        }
        return baseProductCarousel;
    }

    private View getInfoWidgetView(Section section) {
        if (section.getDescription() == null || TextUtils.isEmpty(section.getDescription().getText()))
            return null;
        WebView webView = new WebView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        Renderer renderer = mSectionData.getRenderersMap() != null ?
                mSectionData.getRenderersMap().get(section.getDescription().getRenderingId()) : null;
        if (renderer != null) {
            int margin = renderer.getSafeMargin(0);
            layoutParams.setMargins(margin, margin, margin, 0);
        }
        webView.setLayoutParams(layoutParams);
        webView.loadData(section.getDescription().getText(), "text/html", "UTF-8");
        return webView;
    }

    private View getImageView(Section section) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
        LinearLayout.LayoutParams baseLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(baseLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (TextUtils.isEmpty(sectionItem.getImage()))
                continue;
            ImageView imageView = new ImageView(getActivity());
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(imageView, 0, 0);
            }
            imageView.setLayoutParams(layoutParams);
            imageView.setOnClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
            ImageLoader.getInstance().displayImage(sectionItem.getImage(), imageView);
        }
        return linearLayout;
    }

    private View getMsgView(Section section, LayoutInflater inflater) {
        LinearLayout linearLayout = new LinearLayout(getActivity());
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
            txtVw.setOnClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
            txtVw.setTypeface(faceRobotoRegular);
            linearLayout.addView(txtVw);
        }
        return linearLayout;
    }

    private View getMenuView(Section section, LayoutInflater inflater) {
        LinearLayout menuContainer = new LinearLayout(getActivity());
        menuContainer.setOrientation(LinearLayout.VERTICAL);

        int layoutMargin = (int) getResources().getDimension(R.dimen.margin_small);
        LinearLayout.LayoutParams menuContainerLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        menuContainerLayoutParams.setMargins(layoutMargin, layoutMargin, layoutMargin, 0);
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
                txtVw.setBackgroundColor(getResources().getColor(R.color.uiv3_accent_color));
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
            txtListText.setOnClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
            View viewSeparator = base.findViewById(R.id.viewSeparator);
            viewSeparator.setVisibility(View.VISIBLE);
            menuContainer.addView(base);
        }
        return menuContainer;
    }

    private View getTileView(Section section, LayoutInflater inflater) {
        LinearLayout tileContainer = new LinearLayout(getActivity());
        tileContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tileContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tileContainerParams.setMargins(8, 4, 8, 4);
        tileContainer.setLayoutParams(tileContainerParams);
        double tileWidth = getTileImageWidth(section, 8);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (section.getSectionType().equals(Section.TILE)) {
                View tileItemView = inflater.inflate(R.layout.uiv3_image_caption_layout, tileContainer, false);
                setTileLayoutParams(tileWidth, ViewGroup.LayoutParams.WRAP_CONTENT, tileItemView);
                TextView txtCaption = (TextView) tileItemView.findViewById(R.id.txtCaption);
                ImageView imgContent = (ImageView) tileItemView.findViewById(R.id.imgContent);
                if (!TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    txtCaption.setTypeface(faceRobotoRegular);
                    txtCaption.setText(sectionItem.getTitle().getText());
                } else {
                    txtCaption.setVisibility(View.GONE);
                }
                imgContent.setOnClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
                ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgContent);
                tileContainer.addView(tileItemView);
            } else if (section.getSectionType().equals(Section.TEXT_TILE)) {
                if (sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    continue;
                }
                View tileItemView = inflater.inflate(R.layout.uiv3_text_carousel_row, tileContainer, false);
                TextView txtTitle = (TextView) tileItemView.findViewById(R.id.txtTitle);
                txtTitle.setTypeface(faceRobotoRegular);
                setTileLayoutParams(tileWidth, getResources().getDimension(R.dimen.carousel_img_height),
                        tileItemView);
                txtTitle.setText(sectionItem.getTitle().getText());
                tileItemView.setOnClickListener(new OnSectionItemClickListener<>(this, section, sectionItem));
                tileContainer.addView(tileItemView);
            }
        }
        return tileContainer;
    }

    private void setTileLayoutParams(double tileWidth, double tileHeight, View tileItemView) {
        LinearLayout.LayoutParams tileItemLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tileItemLayoutParams.width = (int) tileWidth;
        if (tileHeight > 0) {
            tileItemLayoutParams.height = (int) tileHeight;
        }
        tileItemLayoutParams.setMargins(0, 0, 8, 0);
        tileItemView.setLayoutParams(tileItemLayoutParams);
    }

    private double getTileImageWidth(Section section, double extraSpace) {
        double width = 0;
        if (section.getSectionItems() == null || section.getSectionItems().size() == 0) {
            return width;
        }
        width = getResources().getDisplayMetrics().widthPixels;
        int eachImageRightMargin = 8;
        int numImages = section.getSectionItems().size();
        return (width - extraSpace - (eachImageRightMargin * numImages)) / numImages;
    }

    protected void retainSectionState(Bundle outState) {
        if (mSectionData != null) {
            outState.putParcelable(Constants.SECTIONS, mSectionData);
        }
    }

    protected boolean tryRestoreSectionState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSectionData = savedInstanceState.getParcelable(Constants.SECTIONS);
            return mSectionData != null && mSectionData.getSections() != null &&
                    mSectionData.getSections().size() > 0;
        }
        return false;
    }
}