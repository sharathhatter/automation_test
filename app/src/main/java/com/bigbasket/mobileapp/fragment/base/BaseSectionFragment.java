package com.bigbasket.mobileapp.fragment.base;

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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.CarouselAdapter;
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
                    View tileView = getTileView(section, inflater, mainLayout);
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
            } else {
                DefaultSliderView defaultSliderView = new DefaultSliderView(getActivity());
                defaultSliderView.image(sectionItem.getImage());
                bannerSlider.addSlider(defaultSliderView);
            }
        }
        return baseSlider;
    }

    private View getSalutationView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseSalutation = inflater.inflate(R.layout.uiv3_salutation_box, parent, false);
        TextView txtSalutationTitle = (TextView) baseSalutation.findViewById(R.id.txtSalutationTitle);
        if (!TextUtils.isEmpty(section.getTitle().getText())) {
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
                layoutSalutationItem.setVisibility(View.VISIBLE);
            }
            if (!TextUtils.isEmpty(sectionItem.getImage())) {
                layoutSalutationItem.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgSalutationItem);
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
                int margin = renderer.getSafeMargin(0);
                int padding = renderer.getSafePadding(0);
                if (margin > 0) {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(margin, margin, margin, margin);
                    txtListTitle.setLayoutParams(layoutParams);
                }
                if (padding > 0) {
                    txtListTitle.setPadding((int) getResources().getDimension(R.dimen.padding_mini), padding,
                            (int) getResources().getDimension(R.dimen.padding_mini), padding);
                }
            }
        }

        RecyclerView horizontalRecyclerView = (RecyclerView) baseProductCarousel.findViewById(R.id.horizontalRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(linearLayoutManager);
        horizontalRecyclerView.setHasFixedSize(false);

        CarouselAdapter carouselAdapter = new CarouselAdapter<>(this, section.getSectionItems(), mSectionData.getRenderersMap(),
                layoutId, faceRobotoRegular);
        horizontalRecyclerView.setAdapter(carouselAdapter);
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
                int margin = renderer.getSafeMargin(0);
                layoutParams.setMargins(margin, margin, margin, margin);
            }
            imageView.setLayoutParams(layoutParams);
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
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
            if (renderer != null) {
                int margin = renderer.getSafeMargin(0);
                int padding = renderer.getSafePadding(0);
                if (margin > 0) {
                    LinearLayout.LayoutParams txtLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    txtLayoutParams.setMargins(margin, margin, margin, margin);
                    txtVw.setLayoutParams(txtLayoutParams);
                }
                if (padding > 0) {
                    txtVw.setPadding(padding, padding, padding, padding);
                }
                txtVw.setTextColor(renderer.getNativeTextColor());
                txtVw.setBackgroundColor(renderer.getNativeBkgColor());
            }
            txtVw.setText(sectionItem.getTitle().getText());
            txtVw.setTypeface(faceRobotoRegular);
            linearLayout.addView(txtVw);
        }
        return linearLayout;
    }

    private View getMenuView(Section section, LayoutInflater inflater) {
        LinearLayout menuContainer = new LinearLayout(getActivity());
        menuContainer.setOrientation(LinearLayout.VERTICAL);
        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            TextView txtVw = (TextView) inflater.inflate(R.layout.uiv3_msg_text, menuContainer, false);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                int margin = renderer.getSafeMargin(0);
                int padding = renderer.getSafePadding(0);
                if (margin > 0) {
                    LinearLayout.LayoutParams txtLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    txtLayoutParams.setMargins(margin, margin, margin, margin);
                    txtVw.setLayoutParams(txtLayoutParams);
                }
                if (padding > 0) {
                    txtVw.setPadding(padding, padding, padding, padding);
                }
                txtVw.setBackgroundColor(renderer.getNativeBkgColor());
                txtVw.setTextColor(renderer.getNativeTextColor());
            }
            txtVw.setText(section.getTitle().getText());
            txtVw.setTypeface(faceRobotoRegular);
            txtVw.setGravity(Gravity.LEFT);
            menuContainer.addView(txtVw);
        }
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (sectionItem == null || sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText()))
                continue;
            View base = inflater.inflate(R.layout.uiv3_list_text, menuContainer, false);
            TextView txtListText = (TextView) base.findViewById(R.id.txtListText);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
            if (renderer != null) {
                int margin = renderer.getSafeMargin(0);
                int padding = renderer.getSafePadding(0);
                if (margin > 0) {
                    LinearLayout.LayoutParams txtLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    txtLayoutParams.setMargins(margin, margin, margin, margin);
                    txtListText.setLayoutParams(txtLayoutParams);
                }
                if (padding > 0) {
                    txtListText.setPadding(padding, padding, padding, padding);
                }
                txtListText.setBackgroundColor(renderer.getNativeBkgColor());
                txtListText.setTextColor(renderer.getNativeTextColor());
            }
            txtListText.setTypeface(faceRobotoRegular);
            txtListText.setText(sectionItem.getTitle().getText());
            View viewSeparator = base.findViewById(R.id.viewSeparator);
            viewSeparator.setVisibility(View.VISIBLE);
            menuContainer.addView(base);
        }
        return menuContainer;
    }

    private View getTileView(Section section, LayoutInflater inflater, ViewGroup parent) {
        LinearLayout tileContainer = new LinearLayout(getActivity());
        tileContainer.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tileContainerParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        tileContainerParams.setMargins(8, 4, 8, 4);
        tileContainer.setLayoutParams(tileContainerParams);
        double tileWidth = getTileImageWidth(section, 8);
        for (SectionItem sectionItem : section.getSectionItems()) {
            View tileItemView = inflater.inflate(R.layout.uiv3_image_caption_layout, tileContainer, false);
            LinearLayout.LayoutParams tileItemLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tileItemLayoutParams.width = (int) tileWidth;
            tileItemLayoutParams.setMargins(0, 0, 8, 0);
            tileItemView.setLayoutParams(tileItemLayoutParams);
            TextView txtCaption = (TextView) tileItemView.findViewById(R.id.txtCaption);
            ImageView imgContent = (ImageView) tileItemView.findViewById(R.id.imgContent);
            if (!TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                txtCaption.setTypeface(faceRobotoRegular);
                txtCaption.setText(sectionItem.getTitle().getText());
            } else {
                txtCaption.setVisibility(View.GONE);
            }
            ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgContent);
            tileContainer.addView(tileItemView);
        }
        return tileContainer;
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
