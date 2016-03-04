package com.bigbasket.mobileapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.CarouselAdapter;
import com.bigbasket.mobileapp.handler.click.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.service.AnalyticsIntentService;
import com.bigbasket.mobileapp.slider.Indicators.PagerIndicator;
import com.bigbasket.mobileapp.slider.SliderLayout;
import com.bigbasket.mobileapp.slider.SliderTypes.BaseSliderView;
import com.bigbasket.mobileapp.slider.SliderTypes.DefaultSliderView;
import com.bigbasket.mobileapp.slider.SliderTypes.HelpSliderView;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;
import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SectionView {

    private Context context;
    private Typeface faceRobotoRegular;
    private SectionData mSectionData;
    private int fourDp;
    private int eightDp;
    private int sixteenDp;
    private String screenName;
    private int marginBetweenWidgets;
    private boolean isHelp;
    private ArrayList<Integer> dynamicTiles;
    private int availableScreenWidth;
    private boolean skipImageMemoryCache;
    private RecyclerView recyclerView;
    private SectionRowAdapter sectionRowAdapter;

    public SectionView(Context context, Typeface faceRobotoRegular, SectionData sectionData, String screenName) {
        this.context = context;
        this.faceRobotoRegular = faceRobotoRegular;
        this.mSectionData = sectionData;
        this.screenName = screenName;
        this.fourDp = (int) context.getResources().getDimension(R.dimen.margin_mini);
        this.eightDp = (int) context.getResources().getDimension(R.dimen.margin_small);
        this.sixteenDp = (int) context.getResources().getDimension(R.dimen.margin_normal);
        this.marginBetweenWidgets = (int) context.getResources().getDimension(R.dimen.margin_mini);
        this.availableScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        this.availableScreenWidth -= marginBetweenWidgets;
        parseRendererColors();
    }


    public SectionView(Context context, Typeface faceRobotoRegular, SectionData sectionData,
                       String screenName, boolean isHelp, boolean skipImageMemoryCache) {
        this(context, faceRobotoRegular, sectionData, screenName);
        this.isHelp = isHelp;
        this.skipImageMemoryCache = skipImageMemoryCache;
    }

    private void parseRendererColors() {
        if (mSectionData == null || mSectionData.getRenderersMap() == null) return;
        int defaultTextColor = ContextCompat.getColor(context, R.color.uiv3_secondary_text_color);
        for (Renderer renderer : mSectionData.getRenderersMap().values()) {
            if (!TextUtils.isEmpty(renderer.getBackgroundColor())) {
                renderer.setNativeBkgColor(UIUtil.parseAsNativeColor(renderer.getBackgroundColor(), Color.WHITE));
            }
            if (!TextUtils.isEmpty(renderer.getTextColor())) {
                renderer.setNativeTextColor(UIUtil.parseAsNativeColor(renderer.getTextColor(), defaultTextColor));
            }
        }
    }

    public void setSectionData(SectionData mSectionData) {
        this.mSectionData = mSectionData;
        parseRendererColors();
        if(sectionRowAdapter != null) {
            sectionRowAdapter.notifyDataSetChanged();
            if(recyclerView != null && recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(sectionRowAdapter);
            }
        }
    }


    /**
     * Don't use function view as much as possible since it puts all the views into memory
     * Use this only when there's already a scrollable view on the page
     */
    @Nullable
    public View getView() {
        if (mSectionData == null || mSectionData.getSections() == null || mSectionData.getSections().size() == 0)
            return null;
        LinearLayout mainLayout = new LinearLayout(context);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.uiv3_list_bkg_color));
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ArrayList<Section> sections = mSectionData.getSections();
        int marginBetweenWidgets = (int) context.getResources().getDimension(R.dimen.margin_mini);
        for (int i = 0; i < sections.size(); i++) {
            View sectionView;
            try {
                Section section = sections.get(i);
                sectionView = getViewToRender(section, inflater, mainLayout, i, null);
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
    public RecyclerView getRecyclerView(ViewGroup parent) {
        if (mSectionData == null || mSectionData.getSections() == null || mSectionData.getSections().size() == 0)
            return null;
        if(recyclerView == null) {
            recyclerView = UIUtil.getResponsiveRecyclerView(context, 1, 1, parent);
            sectionRowAdapter = new SectionRowAdapter();
            recyclerView.setAdapter(sectionRowAdapter);
        }
        return recyclerView;
    }

    @Nullable
    public View getViewToRender(Section section, LayoutInflater inflater, ViewGroup mainLayout,
                                int position, OnSectionItemClickListener sectionItemClickListener) {
        switch (section.getSectionType()) {
            case Section.BANNER:
                return getBannerView(section, inflater, mainLayout, sectionItemClickListener);
            case Section.SALUTATION:
                return getSalutationView(section, inflater, mainLayout, sectionItemClickListener);
            case Section.TILE:
                return getTileView(section, inflater, mainLayout, false, position, sectionItemClickListener);
            case Section.GRID:
                return getGridLayoutView(section, inflater, mainLayout, position, sectionItemClickListener);
            case Section.PRODUCT_CAROUSEL:
                return getCarouselView(section, inflater, mainLayout);
            case Section.NON_PRODUCT_CAROUSEL:
                return getCarouselView(section, inflater, mainLayout);
            case Section.INFO_WIDGET:
                return getInfoWidgetView(section);
            case Section.AD_IMAGE:
                return getTileView(section, inflater, mainLayout, true, position, sectionItemClickListener);
            case Section.SALUTATION_TITLE:
                return getMsgView(section, inflater);
            case Section.MSG:
                return getMsgView(section, inflater);
            case Section.MENU:
                return getMenuView(section, inflater);
        }
        return null;
    }

    private View getBannerView(Section section, LayoutInflater inflater, ViewGroup parent,
                               OnSectionItemClickListener sectionItemClickListener) {
        View baseSlider = createBannerView(inflater, parent);
        final SliderLayout bannerSlider = (SliderLayout) baseSlider.findViewById(R.id.imgSlider);
        bindBannerData(bannerSlider, section, sectionItemClickListener);
        return baseSlider;
    }

    private View createBannerView(LayoutInflater inflater, ViewGroup parent) {
        return inflater.inflate(R.layout.uiv3_image_slider, parent, false);
    }

    private void bindBannerData(SliderLayout bannerSlider, Section section,
                                OnSectionItemClickListener sectionItemClickListener) {
        ViewGroup.LayoutParams bannerLayoutParams = bannerSlider.getLayoutParams();
        if (bannerLayoutParams != null && !isHelp) {
            bannerLayoutParams.height = section.getWidgetHeight(context, mSectionData.getRenderersMap(), true);
            bannerSlider.setLayoutParams(bannerLayoutParams);
        }
        ArrayList<BaseSliderView> sliderViews = new ArrayList<>(section.getSectionItems().size());
        for (SectionItem sectionItem : section.getSectionItems()) {
            if (sectionItem.hasImage()) {
                BaseSliderView sliderView;
                if (isHelp) {
                    sliderView = new HelpSliderView(context);
                } else {
                    sliderView = new DefaultSliderView(context);
                    sliderView.setScaleType(BaseSliderView.ScaleType.CenterInside);
                }
                int targetWidth = sectionItem.getActualWidth(context);
                int targetHeight = sectionItem.getActualHeight(context);
                targetHeight = adjustHeightForScreenWidth(targetWidth, targetHeight, availableScreenWidth);
                targetWidth = availableScreenWidth;
                if (!TextUtils.isEmpty(sectionItem.getImage())) {
                    sliderView.setWidth(targetWidth)
                            .setHeight(targetHeight)
                            .image(sectionItem.getImage());
                } else if (!TextUtils.isEmpty(sectionItem.getImageName())) {
                    sliderView.setWidth(targetWidth)
                            .setHeight(targetHeight)
                            .image(sectionItem.constructImageUrl(
                                    context, mSectionData.getBaseImgUrl()));
                } else {
                    continue;
                }
                Map<String, String> analyticsAttrs = mSectionData.getAnalyticsAttrs(sectionItem.getId());
                if (sectionItemClickListener == null) {
                    sectionItemClickListener = new OnSectionItemClickListener<>((AppOperationAware) context,
                            section, sectionItem, screenName, analyticsAttrs);
                }
                sliderView.setTag(R.id.section_item_tag_id, sectionItem);
                sliderView.setOnSliderClickListener(sectionItemClickListener);
                sliderViews.add(sliderView);
            }

        }
        bannerSlider.changeSliders(sliderViews);

        if (sliderViews.size() > 1) {
            bannerSlider.setAutoCycle(true);
        } else {
            bannerSlider.setAutoCycle(false);
            if (section.getSectionItems().size() <= 1) {
                bannerSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Invisible);
            } else {
                bannerSlider.setIndicatorVisibility(PagerIndicator.IndicatorVisibility.Visible);
            }
        }

    }

    private View getSalutationView(Section section, LayoutInflater inflater, ViewGroup parent,
                                   OnSectionItemClickListener sectionItemClickListener) {
        View baseSalutation = inflater.inflate(R.layout.uiv3_salutation_box, parent, false);
        formatSection(baseSalutation, R.id.txtSalutationTitle, section);
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
            if (sectionItemClickListener == null) {
                sectionItemClickListener = new OnSectionItemClickListener<>(
                        (AppOperationAware) context, section, sectionItem, screenName,
                        mSectionData.getAnalyticsAttrs(sectionItem.getId()));
            }
            if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                txtSalutationItem.setTypeface(faceRobotoRegular);
                txtSalutationItem.setText(sectionItem.getTitle().getText());
                txtSalutationItem.setTag(R.id.section_item_tag_id, sectionItem);
                txtSalutationItem.setOnClickListener(sectionItemClickListener);
                layoutSalutationItem.setVisibility(View.VISIBLE);
            }
            if (sectionItem.hasImage()) {
                layoutSalutationItem.setVisibility(View.VISIBLE);
                sectionItem.displayImage(context, mSectionData.getBaseImgUrl(),
                        imgSalutationItem, R.drawable.loading_small);
                imgSalutationItem.setTag(R.id.section_item_tag_id, sectionItem);
                imgSalutationItem.setOnClickListener(sectionItemClickListener);
            }
        }
        return baseSalutation;
    }

    private View getCarouselView(Section section, LayoutInflater inflater, ViewGroup parent) {
        View baseProductCarousel = inflater.inflate(R.layout.uiv3_horizontal_recycler_view, parent, false);
        formatSection(baseProductCarousel, R.id.txtListTitle, section);
        setViewMoreBehaviour(baseProductCarousel.findViewById(R.id.btnMore), section, section.getMoreSectionItem());

        RecyclerView horizontalRecyclerView = (RecyclerView) baseProductCarousel.findViewById(R.id.horizontalRecyclerView);
        int carouselHeight = section.getWidgetHeight(context, mSectionData.getRenderersMap(), false);
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

    @SuppressLint("SetJavaScriptEnabled")
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
            txtVw.setOnClickListener(new OnSectionItemClickListener<>(
                    (AppOperationAware) context, section, sectionItem, screenName,
                    mSectionData.getAnalyticsAttrs(sectionItem.getId())));
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
                itemView.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            }

            int layoutMenuTxtPadding = sectionItem.hasDescription() && sectionItem.hasTitle() ? eightDp : sixteenDp;
            layoutMenuTxt.setPadding(sixteenDp, layoutMenuTxtPadding, layoutMenuTxtPadding, layoutMenuTxtPadding);

            if (sectionItem.hasImage()) {
                sectionItem.displayImage(context, mSectionData.getBaseImgUrl(), imgInRow, R.drawable.loading_small);
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
            itemView.setOnClickListener(new OnSectionItemClickListener<>(
                    (AppOperationAware) context, section, sectionItem, screenName,
                    mSectionData.getAnalyticsAttrs(sectionItem.getId())));
            View viewSeparator = itemView.findViewById(R.id.viewSeparator);
            viewSeparator.setVisibility(i == numItems - 1 ? View.GONE : View.VISIBLE);
            menuContainer.addView(itemView);
        }
        return menuContainer;
    }

    private View getGridLayoutView(final Section section, LayoutInflater inflater, ViewGroup parent,
                                   int position, OnSectionItemClickListener sectionItemClickListener) {

        ArrayList<ArrayList<SectionItem>> sectionItemRows = new ArrayList<>();
        int numCols = context.getResources().getInteger(R.integer.numGridCols);
        int sz = section.getSectionItems().size();
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
        formatSection(base, R.id.txtListTitle, section);
        setViewMoreBehaviour(base.findViewById(R.id.btnMore), section, section.getMoreSectionItem());
        ViewGroup layoutGridContainer = (ViewGroup) base.findViewById(R.id.layoutGridContainer);

        for (ArrayList<SectionItem> sectionItems : sectionItemRows) {

            View tileBase = inflater.inflate(R.layout.uiv3_tile_container, parent, false);
            tileBase.findViewById(R.id.txtListTitle).setVisibility(View.GONE);
            tileBase.findViewById(R.id.btnMore).setVisibility(View.GONE);

            LinearLayout tileContainer = (LinearLayout) tileBase.findViewById(R.id.layoutTileContainer);
            addTilesToParent(tileContainer, false, section, sectionItems, inflater, false,
                    position, sectionItemClickListener);
            layoutGridContainer.addView(tileBase);
        }
        return base;
    }

    private View getTileView(Section section, LayoutInflater inflater, ViewGroup parent,
                             boolean isVertical, int position,
                             OnSectionItemClickListener sectionItemClickListener) {
        View base = inflater.inflate(R.layout.uiv3_tile_container, parent, false);
        formatSection(base, R.id.txtListTitle, section);
        setViewMoreBehaviour(base.findViewById(R.id.btnMore), section, section.getMoreSectionItem());

        LinearLayout tileContainer = (LinearLayout) base.findViewById(R.id.layoutTileContainer);
        if (isVertical) {
            tileContainer.setOrientation(LinearLayout.VERTICAL);
        }
        ArrayList<SectionItem> sectionItems = section.getSectionItems();
        addTilesToParent(tileContainer, isVertical, section, sectionItems, inflater, true,
                position, sectionItemClickListener);
        return base;
    }

    private void addTilesToParent(ViewGroup tileContainer, boolean isVertical,
                                  Section section, ArrayList<SectionItem> sectionItems,
                                  LayoutInflater inflater,
                                  boolean stretchImage, int postion,
                                  OnSectionItemClickListener sectionItemClickListener) {
        boolean hasSectionTitle = section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText());
        int numSectionItems = sectionItems.size();
        Typeface titleTypeface = isVertical ? FontHolder.getInstance(context).getFaceRobotoMedium() : faceRobotoRegular;
        for (int i = 0; i < numSectionItems; i++) {
            SectionItem sectionItem = sectionItems.get(i);
            boolean applyRight = !isVertical && i != numSectionItems - 1;

            Renderer renderer = mSectionData.getRenderersMap() != null ?
                    mSectionData.getRenderersMap().get(sectionItem.getRenderingId()) : null;

            boolean isItemOrientationVertical = renderer != null && renderer.getOrientation() == Renderer.VERTICAL;
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
                    StringBuilder titleText = new StringBuilder(sectionItem.getTitle().getText());
                    if (sectionItem.isExpressDynamicTitle()) {
                        String expressAvailability = AppDataDynamic.getInstance(context).getExpressAvailability();
                        if (!TextUtils.isEmpty(expressAvailability)) {
                            titleText.append(" ").append(expressAvailability);
                            if (dynamicTiles == null) {
                                dynamicTiles = new ArrayList<>();
                            }
                            dynamicTiles.add(postion);
                        }
                    }
                    txtTitle.setText(titleText);
                    Renderer itemRenderer = mSectionData.getRenderersMap() != null ?
                            mSectionData.getRenderersMap().get(sectionItem.getTitle().getRenderingId()) : null;
                    if (itemRenderer != null) {
                        itemRenderer.setRendering(txtTitle, fourDp, fourDp, true, true, true, true);
                        if (itemRenderer.getPadding() == 0 && sectionItem.isOverlayWithAdjacentTitleDesc(viewType)
                                && isItemOrientationVertical) {
                            itemRenderer.adjustTitlePaddingForOverlayWithAdjacentTitleAndDesc(eightDp, fourDp, txtTitle, txtDescription, sectionItem);
                        }
                    } else {
                        int bottomPadding = txtDescription != null && sectionItem.hasDescription() ? fourDp : eightDp;
                        txtTitle.setPadding(eightDp, eightDp, eightDp,
                                isItemOrientationVertical ? bottomPadding : 0);
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
                        if (itemRenderer.getPadding() == 0 && sectionItem.isOverlayWithAdjacentTitleDesc(viewType)
                                && isItemOrientationVertical) {
                            itemRenderer.adjustDescPaddingForOverlayWithAdjacentTitleAndDesc(eightDp, 0, txtTitle, txtDescription, sectionItem);
                        }
                    } else {
                        int leftPadding = txtTitle != null && sectionItem.hasTitle() ? 0 : eightDp;
                        txtDescription.setPadding(isItemOrientationVertical ? leftPadding : eightDp,
                                isItemOrientationVertical ? eightDp : 0, eightDp, eightDp);
                    }
                } else {
                    txtDescription.setVisibility(View.GONE);
                }
            }

            if (imgInRow != null) {
                if (sectionItem.hasImage()) {
                    int targetImgWidth = 0;
                    int targetImgHeight = 0;
                    if (isVertical) {
                        ViewGroup.LayoutParams lp = imgInRow.getLayoutParams();
                        if (lp != null) {
                            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                            imgInRow.setLayoutParams(lp);
                        }
                        targetImgWidth = sectionItem.getActualWidth(context);
                        targetImgHeight = sectionItem.getActualHeight(context);
                    } else if (!stretchImage) {
                        // By default images are stretched
                        ViewGroup.LayoutParams lp = imgInRow.getLayoutParams();
                        if (lp != null) {
                            lp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                            imgInRow.setLayoutParams(lp);
                        }
                    }
                    if (isVertical && stretchImage) {
                        // It is an ad-image
                        if (targetImgWidth > 0 && targetImgHeight > 0 && availableScreenWidth > 0) {
                            targetImgHeight =
                                    adjustHeightForScreenWidth(targetImgWidth, targetImgHeight, availableScreenWidth);
                            targetImgWidth = availableScreenWidth;
                        }
                    }
                    sectionItem.displayImage(context, mSectionData.getBaseImgUrl(), imgInRow,
                            stretchImage ? R.drawable.loading_large : R.drawable.loading_small,
                            false, targetImgWidth, targetImgHeight, skipImageMemoryCache);
                } else {
                    imgInRow.setVisibility(View.GONE);
                }
            }

            ViewGroup layoutSection = (ViewGroup) view.findViewById(R.id.layoutSection);
            if (!isVertical && layoutSection != null && sectionItem.doesViewRequireMinHeight(viewType)) {
                int minHeight = sectionItem.getHeight(context, renderer);
                layoutSection.setMinimumHeight(minHeight);
            }

            if (renderer != null) {
                renderer.setRendering(layoutSection, 0, 0, false, true, applyRight, true);
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
                if (renderer != null && renderer.getOrientation() == Renderer.HORIZONTAL) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
            } else {
                layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).setMargins(0, i != 0 ? fourDp : 0, 0, 0);
                }
            }
            view.setLayoutParams(layoutParams);
            view.setTag(R.id.section_item_tag_id, sectionItem);
            if (sectionItemClickListener == null) {
                sectionItemClickListener = new OnSectionItemClickListener<>(
                        (AppOperationAware) context, section, sectionItem, screenName,
                        mSectionData.getAnalyticsAttrs(sectionItem.getId()));
            }

            view.setOnClickListener(sectionItemClickListener);
            tileContainer.addView(view);
        }
    }

    private static int adjustHeightForScreenWidth(int originalWidth, int originalHeight,
                                                  int totalWidthAvailable) {
        double aspectRatio = (double) originalWidth / (double) originalHeight;
        return (int) ((double) totalWidthAvailable / aspectRatio);
    }

    private void formatSection(View parent, @IdRes int txtViewId, Section section) {
        Renderer sectionRender = mSectionData.getRenderersMap() != null ?
                mSectionData.getRenderersMap().get(section.getRenderingId()) : null;
        if (sectionRender != null) {
            if (!TextUtils.isEmpty(sectionRender.getBackgroundColor())) {
                parent.setBackgroundColor(sectionRender.getNativeBkgColor());
                if (section.getSectionType().equals(Section.NON_PRODUCT_CAROUSEL)
                        || section.getSectionType().equals(Section.PRODUCT_CAROUSEL)) {
                    parent.setPadding(eightDp, eightDp, eightDp, eightDp);

                    ViewGroup.LayoutParams baseViewLayoutParams = parent.getLayoutParams();
                    if (baseViewLayoutParams != null && baseViewLayoutParams instanceof ViewGroup.MarginLayoutParams) {
                        ((ViewGroup.MarginLayoutParams) baseViewLayoutParams).leftMargin = fourDp;
                        ((ViewGroup.MarginLayoutParams) baseViewLayoutParams).rightMargin = fourDp;
                        parent.setLayoutParams(baseViewLayoutParams);
                    }
                }
            }
        }

        if (txtViewId != 0) {
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
        view.setOnClickListener(new OnSectionItemClickListener<>(
                (AppOperationAware) context, section, moreSectionItem, screenName,
                mSectionData.getAnalyticsAttrs(moreSectionItem.getId())));
    }

    @Nullable
    public ArrayList<Integer> getDynamicTiles() {
        return dynamicTiles;
    }

    private class SectionRowAdapter extends RecyclerView.Adapter<SectionRowHolder> {

        @Override
        public int getItemViewType(int position) {
            SectionData sectionData = mSectionData;
            if(sectionData == null) {
                return super.getItemViewType(position);
            }
            Section section = sectionData.getSections().get(position);
            switch (section.getSectionType()) {
                case Section.BANNER:
                    return 1;
            }
            return super.getItemViewType(position);
        }

        @Override
        public SectionRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(context);
            switch (viewType) {
                case 1:
                    View banner = createBannerView(inflater, parent);
                    return new BannerRowHolder(banner);
                default:
                    LinearLayout viewGroup = (LinearLayout)inflater.inflate(R.layout.uiv3_section_row, parent, false);
                    return new SectionRowHolder(viewGroup);

            }

        }

        @Override
        public void onBindViewHolder(SectionRowHolder holder, int position) {
            Log.d("Section", "Requesting UI view for position = " + position);
            SectionData sectionData = mSectionData;
            if(sectionData == null) {
                holder.itemView.setVisibility(View.GONE);
                return;
            } else {
                holder.itemView.setVisibility(View.VISIBLE);
            }
            Section section = sectionData.getSections().get(position);
            ViewGroup sectionViewHolderRow = null;

            try {
                View sectionView;
                if(holder instanceof BannerRowHolder) {
                    BannerRowHolder bannerRowHolder = (BannerRowHolder)holder;
                    sectionView = bannerRowHolder.itemView;
                    bindBannerData(bannerRowHolder.bannerView, section, null);
                } else {
                    sectionViewHolderRow = holder.getRow();
                    sectionViewHolderRow.removeAllViews();
                    sectionView = getViewToRender(section, LayoutInflater.from(context),
                            sectionViewHolderRow, position, null);
                }
                if (sectionView == null || sectionView.getLayoutParams() == null) {
                    return;
                }
                if (!section.isShown() && section.getSectionItems() != null) {
                    section.setIsShown(true);
                    //TODO: Improve the impression tracking, by actually updating imps' count
                    // when a section item is really displayed,
                    // For Ex: in case of banner 2nd item is displayed after 4sec and so on..
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(
                            context.getApplicationContext());
                    String cityId = preferences.getString(Constants.CITY_ID, null);
                    Map<String, String> analyticsAttrs;
                    Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
                    for (SectionItem sectionItem : section.getSectionItems()) {
                        if (TextUtils.isEmpty(sectionItem.getId())) {
                            continue;
                        }
                        analyticsAttrs = sectionData.getAnalyticsAttrs(sectionItem.getId());
                        if (analyticsAttrs != null && !analyticsAttrs.isEmpty()) {
                            AnalyticsIntentService.startUpdateAnalyticsEvent(
                                    context,
                                    false,
                                    sectionItem.getId(),
                                    cityId,
                                    gson.toJson(analyticsAttrs));
                        }
                    }
                }
                if (section.getSectionType().equals(Section.SALUTATION))
                    return;
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
                        sectionView.getLayoutParams();
                layoutParams.topMargin = marginBetweenWidgets;
                if (position == getItemCount() - 1) {
                    layoutParams.bottomMargin = marginBetweenWidgets;
                }
                sectionView.setLayoutParams(layoutParams);
                if(! (holder instanceof BannerRowHolder)) {
                    sectionViewHolderRow.addView(sectionView);
                }
            } catch (Exception e) {
                //Ignore
                Crashlytics.logException(e);
            }
        }

        @Override
        public int getItemCount() {
            return mSectionData != null && mSectionData.getSections() != null
                    ? mSectionData.getSections().size() : 0;
        }
    }

    private static class SectionRowHolder extends RecyclerView.ViewHolder {

        public SectionRowHolder(View itemView) {
            super(itemView);
        }

        public ViewGroup getRow() {
            return (ViewGroup) itemView;
        }
    }

    private static class BannerRowHolder extends SectionRowHolder {

        public SliderLayout bannerView;

        public BannerRowHolder(View bannerSectionView) {
            super(bannerSectionView);
            this.bannerView = (SliderLayout) bannerSectionView.findViewById(R.id.imgSlider);
        }

    }
}
