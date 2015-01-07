package com.bigbasket.mobileapp.fragment.base;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
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

    private View getSalutationView(Section section, LayoutInflater inflater) {
        View baseSalutation = inflater.inflate(R.layout.uiv3_salutation_box, null);
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
            if (!TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                txtSalutationItem.setTypeface(faceRobotoRegular);
                txtSalutationItem.setText(sectionItem.getTitle().getText());
            }
            if (!TextUtils.isEmpty(sectionItem.getImage())) {
                ImageLoader.getInstance().displayImage(sectionItem.getImage(), imgSalutationItem);
            }
        }
        return baseSalutation;
    }

    private View getProductCarouselView(Section section, LayoutInflater inflater) {
        View baseProductCarousel = inflater.inflate(R.layout.uiv3_horizontal_recycler_view, null);
        RecyclerView horizontalRecyclerView = (RecyclerView) baseProductCarousel.findViewById(R.id.horizontalRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(linearLayoutManager);
        horizontalRecyclerView.setHasFixedSize(true);
        horizontalRecyclerView.setAdapter(new ProductCarouselAdapter(section.getSectionItems()));
        return baseProductCarousel;
    }

    private class ProductCarouselAdapter extends RecyclerView.Adapter<ProductCarouselAdapter.ProductCarouselViewHolder> {

        private ArrayList<SectionItem> productSectionItemList;

        public ProductCarouselAdapter(ArrayList<SectionItem> productSectionItemList) {
            this.productSectionItemList = productSectionItemList;
        }

        @Override
        public ProductCarouselViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            View row = inflater.inflate(R.layout.uiv3_product_grid_row, null);
            return new ProductCarouselViewHolder(row, faceRobotoRegular);
        }

        @Override
        public void onBindViewHolder(ProductCarouselViewHolder holder, int position) {
            SectionItem productSectionItem = productSectionItemList.get(position);
            ImageView imgProduct = holder.getImgProduct();
            TextView txtProductDesc = holder.getTxtProductDesc();
            if (!TextUtils.isEmpty(productSectionItem.getTitle().getText())) {
                txtProductDesc.setVisibility(View.VISIBLE);
                txtProductDesc.setTypeface(faceRobotoRegular);
                txtProductDesc.setText(productSectionItem.getTitle().getText());
            } else {
                txtProductDesc.setVisibility(View.GONE);
            }
            ImageLoader.getInstance().displayImage(productSectionItem.getImage(), imgProduct);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return productSectionItemList.size();
        }

        public class ProductCarouselViewHolder extends RecyclerView.ViewHolder {

            private ImageView imgProduct;
            private TextView txtProductDesc;
            private Typeface typeface;

            public ProductCarouselViewHolder(View itemView, Typeface typeface) {
                super(itemView);
                this.typeface = typeface;
            }

            public ImageView getImgProduct() {
                if (imgProduct == null) {
                    imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
                }
                return imgProduct;
            }

            public TextView getTxtProductDesc() {
                if (txtProductDesc == null) {
                    txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
                    txtProductDesc.setTypeface(typeface);
                }
                return txtProductDesc;
            }
        }
    }

    private View getInfoWidgetView(Section section) {
        WebView webView = new WebView(getActivity());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 4, 0, 0);
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
            imageView.setLayoutParams(layoutParams);
            ImageLoader.getInstance().displayImage(sectionItem.getImage(), imageView);
        }
        return linearLayout;
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
