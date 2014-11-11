package com.bigbasket.mobileapp.fragment.base;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.util.Constants;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.meetme.android.horizontallistview.HorizontalListView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class BaseSectionFragment extends BaseFragment {

    protected ArrayList<Section> mSections;
    protected HashMap<Integer, DestinationInfo> mDestinationInfoHashMap;

    public void displaySections(LinearLayout mainLayout) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (Section section : mSections) {
            switch (section.getSectionType()) {
                case Section.BANNER:
                    View bannerView = getBannerView(section, inflater);
                    mainLayout.addView(bannerView);
                    break;
                case Section.SALUTATION:
                    View salutationView = getSalutationView(section, inflater);
                    mainLayout.addView(salutationView);
                    break;
                case Section.TILE:
                    View tileView = getTileView(section, inflater);
                    mainLayout.addView(tileView);
                    break;
                case Section.PRODUCT_CAROUSEL:
                    View productCarouselView = getProductCarouselView(section, inflater);
                    mainLayout.addView(productCarouselView);
                    break;
                case Section.INFO_WIDGET:
                    View infoWidgetView = getInfoWidgetView(section);
                    mainLayout.addView(infoWidgetView);
                    break;
                case Section.IMAGE:
                    View imageView = getImageView(section);
                    mainLayout.addView(imageView);
                    break;
                case Section.AD:
                    imageView = getImageView(section);
                    mainLayout.addView(imageView);
                    break;
            }
        }
    }

    private View getBannerView(Section section, LayoutInflater inflater) {
        View baseSlider = inflater.inflate(R.layout.uiv3_image_slider, null);
        SliderLayout bannerSlider = (SliderLayout) baseSlider.findViewById(R.id.imgSlider);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (!TextUtils.isEmpty(sectionItem.getDisplayName())) {
                TextSliderView textSliderView = new TextSliderView(getActivity());
                textSliderView.description(sectionItem.getDisplayName()).image(sectionItem.getImage());
                bannerSlider.addSlider(textSliderView);
            } else {
                DefaultSliderView defaultSliderView = new DefaultSliderView(getActivity());
                defaultSliderView.image(sectionItem.getImage());
                bannerSlider.addSlider(defaultSliderView);
            }
        }
        return baseSlider;
    }

    private View getSalutationView(Section section, LayoutInflater inflater) {
        View baseSalutation = inflater.inflate(R.layout.uiv3_salutation_box, null);
        TextView txtSalutationTitle = (TextView) baseSalutation.findViewById(R.id.txtSalutationTitle);
        if (!TextUtils.isEmpty(section.getDisplayName())) {
            txtSalutationTitle.setTypeface(faceRobotoRegular);
            txtSalutationTitle.setText(section.getDisplayName());
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
            switch (i) {
                case 0:
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem1);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem1);
                    break;
                case 1:
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem2);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem2);
                    break;
                case 2:
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem3);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem3);
                    break;
                default:
                    txtSalutationItem = (TextView) baseSalutation.findViewById(R.id.txtSalutationItem1);
                    imgSalutationItem = (ImageView) baseSalutation.findViewById(R.id.imgSalutationItem1);
                    break;
            }
            if (!TextUtils.isEmpty(sectionItem.getDisplayName())) {
                txtSalutationItem.setTypeface(faceRobotoRegular);
                txtSalutationItem.setText(sectionItem.getDisplayName());
            }
            if (!TextUtils.isEmpty(sectionItem.getImage())) {
                ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgSalutationItem);
            }
        }
        return baseSalutation;
    }

    private View getProductCarouselView(Section section, LayoutInflater inflater) {
        View baseProductCarousel = inflater.inflate(R.layout.uiv3_horizontal_grid_layout, null);
        HorizontalListView gridViewProduct = (HorizontalListView) baseProductCarousel.findViewById(R.id.listViewProduct);
        ProductCarouselAdapter productCarouselAdapter = new ProductCarouselAdapter(section.getSectionItems());
        gridViewProduct.setAdapter(productCarouselAdapter);
        return baseProductCarousel;
    }

    private class ProductCarouselAdapter extends BaseAdapter {

        private ArrayList<SectionItem> productSectionItemList;

        public ProductCarouselAdapter(ArrayList<SectionItem> productSectionItemList) {
            this.productSectionItemList = productSectionItemList;
        }

        @Override
        public int getCount() {
            return productSectionItemList.size();
        }

        @Override
        public Object getItem(int position) {
            return productSectionItemList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.uiv3_product_grid_row, null);
            }
            SectionItem productSectionItem = productSectionItemList.get(position);
            ImageView imgProduct = (ImageView) row.findViewById(R.id.imgProduct);
            TextView txtProductDesc = (TextView) row.findViewById(R.id.txtProductDesc);
            if (!TextUtils.isEmpty(productSectionItem.getDisplayName())) {
                txtProductDesc.setVisibility(View.VISIBLE);
                txtProductDesc.setTypeface(faceRobotoRegular);
                txtProductDesc.setText(productSectionItem.getDisplayName());
            } else {
                txtProductDesc.setVisibility(View.GONE);
            }

            ImageLoader.getInstance().displayImage(productSectionItem.getImage(), imgProduct);
            return row;
        }
    }

    private View getInfoWidgetView(Section section) {
        WebView webView = new WebView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 4, 0, 0);
        webView.setLayoutParams(layoutParams);
        webView.loadData(section.getDisplayName(), "text/html", "UTF-8");
        return webView;
    }

    private View getImageView(Section section) {
        ImageView imageView = new ImageView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(layoutParams);
        ImageLoader.getInstance().displayImage(section.getDisplayName(), imageView);
        return imageView;
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
            View tileItemView = inflater.inflate(R.layout.uiv3_image_caption_layout, null);
            LinearLayout.LayoutParams tileItemLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            tileItemLayoutParams.width = (int) tileWidth;
            tileItemLayoutParams.setMargins(0, 0, 8, 0);
            tileItemView.setLayoutParams(tileItemLayoutParams);
            TextView txtCaption = (TextView) tileItemView.findViewById(R.id.txtCaption);
            ImageView imgContent = (ImageView) tileItemView.findViewById(R.id.imgContent);
            if (!TextUtils.isEmpty(sectionItem.getDisplayName())) {
                txtCaption.setTypeface(faceRobotoRegular);
                txtCaption.setText(sectionItem.getDisplayName());
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
        if (mSections != null) {
            outState.putParcelableArrayList(Constants.SECTIONS, mSections);
        }
        if (mDestinationInfoHashMap != null) {
            ArrayList<Integer> destInfoIdKeyArray = new ArrayList<>();
            ArrayList<DestinationInfo> destInfoObjArray = new ArrayList<>();
            for (Map.Entry<Integer, DestinationInfo> entry : mDestinationInfoHashMap.entrySet()) {
                destInfoIdKeyArray.add(entry.getKey());
                destInfoObjArray.add(entry.getValue());
            }
            outState.putIntegerArrayList(Constants.DESTINATION_INFO_ID, destInfoIdKeyArray);
            outState.putParcelableArrayList(Constants.DESTINATIONS_INFO, destInfoObjArray);
        }
    }

    protected boolean tryRestoreSectionState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSections = savedInstanceState.getParcelableArrayList(Constants.SECTIONS);
            ArrayList<Integer> destInfoIdKeyArray = savedInstanceState.getIntegerArrayList(Constants.DESTINATION_INFO_ID);
            ArrayList<DestinationInfo> destInfoObjArray = savedInstanceState.getParcelableArrayList(Constants.DESTINATIONS_INFO);

            if (destInfoIdKeyArray != null && destInfoObjArray != null) {
                mDestinationInfoHashMap = new HashMap<>();
                for (int i = 0; i < destInfoIdKeyArray.size(); i++) {
                    mDestinationInfoHashMap.put(destInfoIdKeyArray.get(i), destInfoObjArray.get(i));
                }
            }
            return mSections != null && mSections.size() > 0;
        }
        return false;
    }
}
