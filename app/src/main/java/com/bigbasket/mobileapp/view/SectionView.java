package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.CarouselAdapter;
import com.bigbasket.mobileapp.adapter.SectionGridAdapter;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.ExpandableHeightGridView;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.DefaultSliderView;

import java.util.ArrayList;

public class SectionView {

    private Context context;
    private Typeface faceRobotoRegular;
    private SectionData mSectionData;
    private int fourDp;
    private int eightDp;
    private int sixteenDp;
    private String screenName;

    public SectionView(Context context, Typeface faceRobotoRegular, SectionData mSectionData, String screenName) {
        this.context = context;
        this.faceRobotoRegular = faceRobotoRegular;
        this.mSectionData = mSectionData;
        this.screenName = screenName;
        this.fourDp = (int) context.getResources().getDimension(R.dimen.margin_mini);
        this.eightDp = (int) context.getResources().getDimension(R.dimen.margin_small);
        this.sixteenDp = (int) context.getResources().getDimension(R.dimen.margin_normal);
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
        ArrayList<Section> sections = mSectionData.getSections();
        int marginBetweenWidgets = (int) context.getResources().getDimension(R.dimen.margin_mini);
        for (int i = 0; i < sections.size(); i++) {
            View sectionView;
            try {
                Section section = sections.get(i);
                sectionView = getViewToRender(section, inflater, mainLayout);
                if (sectionView == null || sectionView.getLayoutParams() == null) {
                    continue;
                }
                if (section.getSectionType().equals(Section.SALUTATION))
                    continue;
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
                        sectionView.getLayoutParams();
                layoutParams.topMargin = marginBetweenWidgets;
                if (i == sections.size() - 1) {
                    layoutParams.bottomMargin = marginBetweenWidgets;
                }
                sectionView.setLayoutParams(layoutParams);
            } catch (Exception e) {
                sectionView = null;
                e.printStackTrace();
            }
            if (sectionView != null) {
                mainLayout.addView(sectionView);
            }
        }
        return mainLayout;
    }

    @Nullable
    private View getViewToRender(Section section, LayoutInflater inflater, LinearLayout mainLayout) {
        switch (section.getSectionType()) {
            case Section.BANNER:
                return getBannerView(section, inflater, mainLayout);
            case Section.SALUTATION:
                return getSalutationView(section, inflater, mainLayout);
            case Section.TILE:
                return getTileView(section, inflater, mainLayout, false);
            case Section.GRID:
                return getGridLayoutView(section, inflater, mainLayout);
            case Section.PRODUCT_CAROUSEL:
                return getCarouselView(section, inflater, mainLayout);
            case Section.NON_PRODUCT_CAROUSEL:
                return getCarouselView(section, inflater, mainLayout);
            case Section.INFO_WIDGET:
                return getInfoWidgetView(section);
            case Section.AD_IMAGE:
                return getTileView(section, inflater, mainLayout, true);
            case Section.SALUTATION_TITLE:
                return getMsgView(section, inflater);
            case Section.MSG:
                return getMsgView(section, inflater);
            case Section.MENU:
                return getMenuView(section, inflater);
        }
        return null;
    }

    private View getBannerView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseSlider = inflater.inflate(R.layout.uiv3_image_slider, parent, false);
        SliderLayout bannerSlider = (SliderLayout) baseSlider.findViewById(R.id.imgSlider);
        ViewGroup.LayoutParams bannerLayoutParams = bannerSlider.getLayoutParams();
        if (bannerLayoutParams != null) {
            bannerLayoutParams.height = section.getWidgetHeight(context, mSectionData.getRenderersMap());
            bannerSlider.setLayoutParams(bannerLayoutParams);
        }
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (sectionItem.hasImage()) {
                DefaultSliderView defaultSliderView = new DefaultSliderView(context);
                defaultSliderView.setScaleType(BaseSliderView.ScaleType.CenterInside);
                if (!TextUtils.isEmpty(sectionItem.getImage())) {
                    defaultSliderView.image(sectionItem.getImage());
                } else if (!TextUtils.isEmpty(sectionItem.getImageName())) {
                    defaultSliderView.image(mSectionData.getBaseImgUrl() +
                            UIUtil.getScreenDensity(context) + "/" + sectionItem.getImageName());
                } else {
                    continue;
                }
                defaultSliderView.setOnSliderClickListener(new OnSectionItemClickListener<>(context, section, sectionItem, screenName));
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
                break;
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
                txtSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem, screenName));
                layoutSalutationItem.setVisibility(View.VISIBLE);
            }
            if (sectionItem.hasImage()) {
                layoutSalutationItem.setVisibility(View.VISIBLE);
                sectionItem.displayImage(context, mSectionData.getBaseImgUrl(), imgSalutationItem);
                imgSalutationItem.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem, screenName));
            }
        }
        return baseSalutation;
    }

    private View getCarouselView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseProductCarousel = inflater.inflate(R.layout.uiv3_horizontal_recycler_view, parent, false);
        formatSectionTitle(baseProductCarousel, R.id.txtListTitle, section);
        setViewMoreBehaviour(baseProductCarousel.findViewById(R.id.btnMore), section, section.getMoreSectionItem());

        RecyclerView horizontalRecyclerView = (RecyclerView) baseProductCarousel.findViewById(R.id.horizontalRecyclerView);
        int carouselHeight = section.getWidgetHeight(context, mSectionData.getRenderersMap());
        ViewGroup.LayoutParams layoutParams = horizontalRecyclerView.getLayoutParams();
        layoutParams.height = carouselHeight;
        horizontalRecyclerView.setLayoutParams(layoutParams);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        horizontalRecyclerView.setLayoutManager(linearLayoutManager);
        horizontalRecyclerView.setHasFixedSize(false);

        CarouselAdapter carouselAdapter = new CarouselAdapter<>(context, section, mSectionData.getBaseImgUrl(),
                mSectionData.getRenderersMap(), faceRobotoRegular, screenName);
        horizontalRecyclerView.setAdapter(carouselAdapter);
        return baseProductCarousel;
    }

    private View getInfoWidgetView(Section section) {
        if (section.getSectionItems() == null || section.getSectionItems().size() == 0)
            return null;
        LinearLayout base = new LinearLayout(context);
        LinearLayout.LayoutParams baseLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        base.setLayoutParams(baseLayoutParams);
        base.setOrientation(LinearLayout.VERTICAL);
        ArrayList<SectionItem> sectionItems = section.getSectionItems();
        for (int i = 0; i < sectionItems.size(); i++) {
            SectionItem sectionItem = sectionItems.get(i);
            if (sectionItem.getDescription() == null || TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                continue;
            }
            BBWebView webView = new BBWebView(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;
            if (renderer != null) {
                int margin = renderer.getSafeMargin(0);
                layoutParams.setMargins(margin, margin, margin, 0);
            } else {
                // Not applying when i == 0 has parent already has a top-margin of 4dp by default
                layoutParams.setMargins(fourDp, i == 0 ? 0 : fourDp, fourDp, 0);
            }
            webView.setLayoutParams(layoutParams);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.loadData(sectionItem.getDescription().getText(), "text/html", "UTF-8");
            base.addView(webView);
        }
        return base;
    }

    private View getMsgView(Section section, LayoutInflater inflater) {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams baseLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(baseLayoutParams);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        boolean applyBottom = !section.getSectionType().equals(Section.SALUTATION_TITLE);
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (sectionItem.getTitle() == null || TextUtils.isEmpty(sectionItem.getTitle().getText()))
                continue;
            View view = inflater.inflate(R.layout.uiv3_msg_text, linearLayout, false);
            TextView txtVw = (TextView) view.findViewById(R.id.txtMsg);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtVw, 0, 0, true, true, true, false,
                        true, true, true, applyBottom);
            }
            txtVw.setText(sectionItem.getTitle().getText());
            txtVw.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem, screenName));
            txtVw.setTypeface(faceRobotoRegular);
            linearLayout.addView(view);
        }
        return linearLayout;
    }

    private View getMenuView(Section section, LayoutInflater inflater) {
        LinearLayout menuContainer = new LinearLayout(context);
        menuContainer.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams menuContainerLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        menuContainer.setLayoutParams(menuContainerLayoutParams);

        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            View view = inflater.inflate(R.layout.uiv3_section_title, menuContainer, false);
            TextView txtVw = (TextView) view.findViewById(R.id.txtListTitle);
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtVw, 0, 0);
            } else {
                txtVw.setPadding(txtVw.getPaddingLeft(), txtVw.getPaddingTop(),
                        txtVw.getPaddingRight(), fourDp);
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
            View itemView = inflater.inflate(R.layout.uiv3_section_menu_row, menuContainer, false);
            ViewGroup layoutMenuTxt = (ViewGroup) itemView.findViewById(R.id.layoutMenuTxt);
            TextView txtListText = (TextView) itemView.findViewById(R.id.txtListText);
            TextView txtListSubText = (TextView) itemView.findViewById(R.id.txtListSubText);
            ImageView imgInRow = (ImageView) itemView.findViewById(R.id.imgInRow);
            Renderer titleRenderer, descRenderer, itemRenderer;
            titleRenderer = descRenderer = itemRenderer = null;
            if (mSectionData.getRenderersMap() != null) {
                titleRenderer = mSectionData.getRenderersMap().get(sectionItem.getTitle().getRenderingId());
                descRenderer = sectionItem.getDescription() != null ?
                        mSectionData.getRenderersMap().get(sectionItem.getDescription().getRenderingId()) : null;
                itemRenderer = mSectionData.getRenderersMap().get(sectionItem.getRenderingId());
            }
            if (titleRenderer != null) {
                titleRenderer.setRendering(txtListText, 0, 0);
            }
            if (itemRenderer != null) {
                itemRenderer.setRendering(itemView, 0, 0);
            } else {
                itemView.setBackgroundColor(context.getResources().getColor(R.color.white));
            }

            int layoutMenuTxtPadding = sectionItem.hasDescription() && sectionItem.hasTitle() ? eightDp : sixteenDp;
            layoutMenuTxt.setPadding(sixteenDp, layoutMenuTxtPadding, layoutMenuTxtPadding, layoutMenuTxtPadding);

            if (sectionItem.hasImage()) {
                sectionItem.displayImage(context, mSectionData.getBaseImgUrl(), imgInRow);
            } else {
                imgInRow.setVisibility(View.GONE);
            }

            if (sectionItem.getDescription() != null && !TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                txtListSubText.setText(sectionItem.getDescription().getText());
                txtListSubText.setTypeface(faceRobotoRegular);
                if (descRenderer != null) {
                    descRenderer.setRendering(txtListSubText, 0, 0);
                }
            } else {
                txtListSubText.setVisibility(View.GONE);
            }
            txtListText.setTypeface(faceRobotoRegular);
            txtListText.setText(sectionItem.getTitle().getText());
            itemView.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem, screenName));
            View viewSeparator = itemView.findViewById(R.id.viewSeparator);
            viewSeparator.setVisibility(i == numItems - 1 ? View.GONE : View.VISIBLE);
            menuContainer.addView(itemView);
        }
        return menuContainer;
    }

    private View getGridLayoutView(final Section section, LayoutInflater inflater, ViewGroup parent) {

        ArrayList<ArrayList<SectionItem>> sectionItemRows = new ArrayList<>();
        int numCols = context.getResources().getInteger(R.integer.numGridCols);
        int sz  = section.getSectionItems().size();
        int numRowsExpected = (int) Math.ceil((double) sz / (double) numCols);
        Log.d("Section", "For grid, num rows expected = " + numRowsExpected);
        ArrayList<SectionItem> eachSectionItemRow = null;
        for (int i = 0; i < sz; i++) {
            if (i % numCols == 0) {
                if (eachSectionItemRow != null) {
                    sectionItemRows.add(eachSectionItemRow);
                }
                eachSectionItemRow = new ArrayList<>();
            }
            if (eachSectionItemRow != null) {
                eachSectionItemRow.add(section.getSectionItems().get(i));
            }
        }
        if (sectionItemRows.size() != numRowsExpected && eachSectionItemRow != null) {
            sectionItemRows.add(eachSectionItemRow);
        }

        View base = inflater.inflate(R.layout.uiv3_grid_container, parent, false);
        formatSectionTitle(base, R.id.txtListTitle, section);
        setViewMoreBehaviour(base.findViewById(R.id.btnMore), section, section.getMoreSectionItem());
        ViewGroup layoutGridContainer = (ViewGroup) base.findViewById(R.id.layoutGridContainer);
//
//        ExpandableHeightGridView layoutGrid = (ExpandableHeightGridView) base.findViewById(R.id.layoutGrid);
//        layoutGrid.setExpanded(true);
//        SectionGridAdapter sectionGridAdapter = new SectionGridAdapter<>(context, section, mSectionData.getBaseImgUrl(),
//                mSectionData.getRenderersMap(), faceRobotoRegular, screenName);
//        layoutGrid.setAdapter(sectionGridAdapter);
//        layoutGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                SectionItem sectionItem = section.getSectionItems().get(position);
//                new OnSectionItemClickListener<>(context, section, sectionItem, screenName).
//                        onClick(view);
//            }
//        });
//        return base;


        for (ArrayList<SectionItem> sectionItems: sectionItemRows) {

            View tileBase = inflater.inflate(R.layout.uiv3_tile_container, parent, false);
            tileBase.findViewById(R.id.txtListTitle).setVisibility(View.GONE);
            tileBase.findViewById(R.id.btnMore).setVisibility(View.GONE);

            LinearLayout tileContainer = (LinearLayout) tileBase.findViewById(R.id.layoutTileContainer);
            addTilesToParent(tileContainer, false, section, sectionItems, inflater, false);
            layoutGridContainer.addView(tileBase);
        }
        return base;
    }

    private View getTileView(Section section, LayoutInflater inflater, ViewGroup parent,
                             boolean isVertical) {
        View base = inflater.inflate(R.layout.uiv3_tile_container, parent, false);
        formatSectionTitle(base, R.id.txtListTitle, section);
        setViewMoreBehaviour(base.findViewById(R.id.btnMore), section, section.getMoreSectionItem());

        LinearLayout tileContainer = (LinearLayout) base.findViewById(R.id.layoutTileContainer);
        if (isVertical) {
            tileContainer.setOrientation(LinearLayout.VERTICAL);
        }
        ArrayList<SectionItem> sectionItems = section.getSectionItems();
        addTilesToParent(tileContainer, isVertical, section, sectionItems, inflater, true);
        return base;
    }

    private void addTilesToParent(ViewGroup tileContainer, boolean isVertical,
                                  Section section, ArrayList<SectionItem> sectionItems,
                                  LayoutInflater inflater,
                                  boolean stretchImage) {
        boolean hasSectionTitle = section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText());
        int numSectionItems = sectionItems.size();
        Typeface titleTypeface = isVertical ? FontHolder.getInstance(context).getFaceRobotoMedium() : faceRobotoRegular;
        for (int i = 0; i < numSectionItems; i++) {
            SectionItem sectionItem = sectionItems.get(i);
            boolean applyRight = !isVertical && i != numSectionItems - 1;

            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;

            int viewType = sectionItem.getItemViewType(renderer, section.getSectionType());
            if (viewType == SectionItem.VIEW_UNKNOWN) {
                continue;
            }
            int layoutId = SectionItem.getLayoutResId(viewType);
            if (layoutId == 0) {
                continue;
            }

            View view = inflater.inflate(layoutId, tileContainer, false);

            TextView txtTitle = (TextView) view.findViewById(R.id.txtTitle);
            TextView txtDescription = (TextView) view.findViewById(R.id.txtDescription);
            ImageView imgInRow = (ImageView) view.findViewById(R.id.imgInRow);

            if (txtTitle != null) {
                if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                    txtTitle.setTypeface(titleTypeface);
                    txtTitle.setText(sectionItem.getTitle().getText());
                    Renderer itemRenderer = mSectionData.getRenderersMap() != null ?
                            mSectionData.getRenderersMap().get(sectionItem.getTitle().getRenderingId()) : null;
                    if (itemRenderer != null) {
                        itemRenderer.setRendering(txtTitle, fourDp, fourDp, true, true, true, true);
                        if (itemRenderer.getPadding() == 0 && sectionItem.isOverlayWithAdjacentTitleDesc(viewType)) {
                            itemRenderer.adjustTitlePaddingForOverlayWithAdjacentTitleAndDesc(eightDp, fourDp, txtTitle, txtDescription, sectionItem);
                        }
                    } else {
                        txtTitle.setPadding(eightDp, eightDp, eightDp,
                                txtDescription != null && sectionItem.hasDescription() ? fourDp : eightDp);
                    }
                } else {
                    txtTitle.setVisibility(View.GONE);
                }
            }

            if (txtDescription != null) {
                if (sectionItem.getDescription() != null && !TextUtils.isEmpty(sectionItem.getDescription().getText())) {
                    txtDescription.setTypeface(faceRobotoRegular);
                    txtDescription.setText(sectionItem.getDescription().getText());
                    Renderer itemRenderer = mSectionData.getRenderersMap() != null ?
                            mSectionData.getRenderersMap().get(sectionItem.getDescription().getRenderingId()) : null;
                    if (itemRenderer != null) {
                        itemRenderer.setRendering(txtDescription, fourDp, fourDp, true, true, true, true);
                        if (itemRenderer.getPadding() == 0 && sectionItem.isOverlayWithAdjacentTitleDesc(viewType)) {
                            itemRenderer.adjustDescPaddingForOverlayWithAdjacentTitleAndDesc(eightDp, 0, txtTitle, txtDescription, sectionItem);
                        }
                    } else {
                        txtDescription.setPadding(txtTitle != null && sectionItem.hasTitle() ? 0 : eightDp, eightDp, eightDp, eightDp);
                    }
                } else {
                    txtDescription.setVisibility(View.GONE);
                }
            }

            if (imgInRow != null) {
                if (sectionItem.hasImage()) {
                    if (isVertical) {
                        ViewGroup.LayoutParams lp = imgInRow.getLayoutParams();
                        if (lp != null) {
                            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            imgInRow.setLayoutParams(lp);
                        }
                    } else if (!stretchImage) {
                        // By default images are stretched

                    }
                    sectionItem.displayImage(context, mSectionData.getBaseImgUrl(), imgInRow);
                } else {
                    imgInRow.setVisibility(View.GONE);
                }
            }

            ViewGroup layoutSection = (ViewGroup) view.findViewById(R.id.layoutSection);
            if (!isVertical && layoutSection != null && sectionItem.doesViewRequireMinHeight(viewType)) {
                int minHeight = sectionItem.getHeight(context, renderer);
                layoutSection.setMinimumHeight(minHeight);
            }

            if (isVertical) {
                ViewGroup sectionLayoutContainer = (ViewGroup) view.findViewById(R.id.sectionLayoutContainer);
                if (sectionLayoutContainer != null) {
                    ViewGroup.LayoutParams lp = sectionLayoutContainer.getLayoutParams();
                    if (lp != null) {
                        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        sectionLayoutContainer.setLayoutParams(lp);
                    }
                }
            }

            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (!isVertical) {
                if (layoutParams instanceof LinearLayout.LayoutParams) {
                    layoutParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                }
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(0, hasSectionTitle ? fourDp : 0, applyRight ? fourDp : 0, 0);
                }
            } else {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(0, i != 0 ? fourDp : 0, 0, 0);
                }
            }
            view.setLayoutParams(layoutParams);

            if (renderer != null) {
                renderer.setRendering(view, 0, 0, false, true, applyRight, true);
            }

            view.setOnClickListener(new OnSectionItemClickListener<>(context, section, sectionItem, screenName));
            tileContainer.addView(view);
        }
    }

    private void formatSectionTitle(View parent, int txtViewId, Section section) {
        TextView txtVw = (TextView) parent.findViewById(txtViewId);
        if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
            txtVw.setTypeface(FontHolder.getInstance(context).getFaceRobotoMedium());
            txtVw.setText(section.getTitle().getText());
            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(section.getTitle().getRenderingId()) : null;
            if (renderer != null) {
                renderer.setRendering(txtVw, 0, 0, true, true, true, false,
                        true, true, true, true);
            }
        } else {
            txtVw.setVisibility(View.GONE);
        }
    }

    private void setViewMoreBehaviour(View view, Section section, SectionItem moreSectionItem) {
        if (view == null) {
            return;
        }
        if (moreSectionItem == null || moreSectionItem.getDestinationInfo() == null ||
                TextUtils.isEmpty(moreSectionItem.getDestinationInfo().getDestinationType())) {
            view.setVisibility(View.GONE);
            return;
        }
        if (view instanceof TextView) {  // Will work for Button as well, since it extends TextView
            ((TextView) view).setTypeface(faceRobotoRegular);
            if (moreSectionItem.getTitle() != null && !TextUtils.isEmpty(moreSectionItem.getTitle().getText())) {
                ((TextView) view).setText(moreSectionItem.getTitle().getText());
            }
        }
        view.setOnClickListener(new OnSectionItemClickListener<>(context, section, moreSectionItem, screenName));
    }
}
