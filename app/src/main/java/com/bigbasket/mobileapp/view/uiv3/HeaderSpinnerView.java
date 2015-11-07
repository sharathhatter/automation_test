package com.bigbasket.mobileapp.view.uiv3;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.handler.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionTextItem;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.HashMap;

public class HeaderSpinnerView<T> {
    private T ctx;
    private Typeface typeface;
    @Nullable
    private Section headSection;
    private int defaultSelectedIdx;
    @Nullable
    private String fallbackHeaderTitle;
    private ListView listHeaderDropdown;
    private TextView txtChildDropdownTitle;
    private Toolbar toolbar;
    private ViewGroup layoutChildToolbarContainer;
    private TextView txtToolbarDropdown;
    private ImageView imgCloseChildDropdown;
    private ViewGroup layoutListHeader;

    private HeaderSpinnerView(T ctx, Typeface typeface, @Nullable Section headSection, int defaultSelectedIdx,
                              @Nullable String fallbackHeaderTitle, ListView listHeaderDropdown,
                              TextView txtChildDropdownTitle, Toolbar toolbar,
                              ViewGroup layoutChildToolbarContainer,
                              TextView txtToolbarDropdown, ImageView imgCloseChildDropdown,
                              ViewGroup layoutListHeader) {
        this.ctx = ctx;
        this.typeface = typeface;
        this.headSection = headSection;
        this.defaultSelectedIdx = defaultSelectedIdx;
        this.fallbackHeaderTitle = fallbackHeaderTitle;
        this.listHeaderDropdown = listHeaderDropdown;
        this.txtChildDropdownTitle = txtChildDropdownTitle;
        this.toolbar = toolbar;
        this.layoutChildToolbarContainer = layoutChildToolbarContainer;
        this.txtToolbarDropdown = txtToolbarDropdown;
        this.imgCloseChildDropdown = imgCloseChildDropdown;
        this.layoutListHeader = layoutListHeader;
    }

    public void setView() {
        if (headSection != null && headSection.getSectionItems() != null
                && headSection.getSectionItems().size() > 0) {
            final OnChildDropdownRequested onChildDropdownRequested =
                    new OnChildDropdownRequested(layoutChildToolbarContainer,
                            ((AppOperationAware) ctx).getCurrentActivity());

            if (toolbar.findViewById(txtToolbarDropdown.getId()) == null) {
                txtToolbarDropdown.setTypeface(typeface);
                toolbar.addView(txtToolbarDropdown);
            }
            toolbar.setTitle("");
            ((AppOperationAware) ctx).getCurrentActivity().setTitle("");
            txtToolbarDropdown.setOnClickListener(onChildDropdownRequested);
            imgCloseChildDropdown.setOnClickListener(onChildDropdownRequested);

            if (defaultSelectedIdx >= headSection.getSectionItems().size()) {
                // Defensive check
                defaultSelectedIdx = 0;
            }

            SectionTextItem title = headSection.getSectionItems().get(defaultSelectedIdx).getTitle();
            txtToolbarDropdown.setText(title != null ? title.getText() : "");
            txtChildDropdownTitle.setTypeface(typeface);
            txtChildDropdownTitle.setText(title != null ? title.getText() : "");
            txtChildDropdownTitle.setOnClickListener(onChildDropdownRequested);

            BBArrayAdapter bbArrayAdapter = new BBArrayAdapter<>(((AppOperationAware) ctx).getCurrentActivity(),
                    R.layout.uiv3_product_header_list_item, headSection.getSectionItems(),
                    typeface, ((AppOperationAware) ctx).getCurrentActivity().getResources().getColor(R.color.uiv3_primary_text_color), Color.WHITE);
            bbArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            listHeaderDropdown.setAdapter(bbArrayAdapter);

            listHeaderDropdown.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (position != Spinner.INVALID_POSITION) {
                        if (position != defaultSelectedIdx) {
                            new OnSectionItemClickListener<>(((AppOperationAware) ctx).getCurrentActivity(), headSection,
                                    headSection.getSectionItems().get(position),
                                    TrackingAware.PRODUCT_LIST_HEADER).onClick(view);
                        }
                        onChildDropdownRequested.onClick(view);  // Manually simulate click to hide dropdown
                    }
                }
            });
            layoutListHeader.setOnClickListener(onChildDropdownRequested);

        } else {
            layoutChildToolbarContainer.setVisibility(View.GONE);
            listHeaderDropdown.setAdapter(null);
            toolbar.setTitle(fallbackHeaderTitle);
            if (txtToolbarDropdown != null) {
                txtToolbarDropdown.setOnClickListener(null);
                toolbar.removeView(txtToolbarDropdown);
            }
        }
    }

    public static class OnChildDropdownRequested implements View.OnClickListener {

        private ViewGroup layoutChildToolbarContainer;
        private BaseActivity activity;

        public OnChildDropdownRequested(ViewGroup layoutChildToolbarContainer,
                                        BaseActivity activity) {
            this.layoutChildToolbarContainer = layoutChildToolbarContainer;
            this.activity = activity;
        }

        @Override
        public void onClick(View v) {
            if (layoutChildToolbarContainer.getVisibility() == View.VISIBLE) {
                layoutChildToolbarContainer.setVisibility(View.GONE);
                if (activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().show();
                }
                UIUtil.changeStatusBarColor(activity,
                        R.color.uiv3_status_bar_background);
            } else {
                layoutChildToolbarContainer.setVisibility(View.VISIBLE);
                UIUtil.changeStatusBarColor(activity,
                        R.color.uiv3_grey_status_bar_color);
                if (activity.getSupportActionBar() != null) {
                    activity.getSupportActionBar().hide();
                }
            }
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, activity.getNextScreenNavigationContext());
            activity.trackEvent(TrackingAware.PRODUCT_LIST_HEADER_CLICKED, map);
        }
    }


    public static class HeaderSpinnerViewBuilder<T> {
        private T ctx;
        private Typeface typeface;
        private Section headSection;
        private int defaultSelectedIdx;
        private String fallbackHeaderTitle;
        private ListView listHeaderDropdown;
        private TextView txtChildDropdownTitle;
        private Toolbar toolbar;
        private ViewGroup layoutChildToolbarContainer;
        private TextView txtToolbarDropdown;
        private ImageView imgCloseChildDropdown;
        private ViewGroup layoutListHeader;

        public HeaderSpinnerViewBuilder() {
        }

        public HeaderSpinnerViewBuilder withCtx(T ctx) {
            this.ctx = ctx;
            return this;
        }

        public HeaderSpinnerViewBuilder withTypeface(Typeface typeface) {
            this.typeface = typeface;
            return this;
        }

        public HeaderSpinnerViewBuilder withHeadSection(Section headSection) {
            this.headSection = headSection;
            return this;
        }

        public HeaderSpinnerViewBuilder withDefaultSelectedIdx(int defaultSelectedIdx) {
            this.defaultSelectedIdx = defaultSelectedIdx;
            return this;
        }

        public HeaderSpinnerViewBuilder withFallbackHeaderTitle(String fallbackHeaderTitle) {
            this.fallbackHeaderTitle = fallbackHeaderTitle;
            return this;
        }

        public HeaderSpinnerViewBuilder withListHeaderDropdown(ListView listHeaderDropdown) {
            this.listHeaderDropdown = listHeaderDropdown;
            return this;
        }

        public HeaderSpinnerViewBuilder withTxtChildDropdownTitle(TextView txtChildDropdownTitle) {
            this.txtChildDropdownTitle = txtChildDropdownTitle;
            return this;
        }

        public HeaderSpinnerViewBuilder withToolbar(Toolbar toolbar) {
            this.toolbar = toolbar;
            return this;
        }

        public HeaderSpinnerViewBuilder withLayoutChildToolbarContainer(ViewGroup layoutChildToolbarContainer) {
            this.layoutChildToolbarContainer = layoutChildToolbarContainer;
            return this;
        }

        public HeaderSpinnerViewBuilder withTxtToolbarDropdown(TextView txtToolbarDropdown) {
            this.txtToolbarDropdown = txtToolbarDropdown;
            return this;
        }

        public HeaderSpinnerViewBuilder withImgCloseChildDropdown(ImageView imgCloseChildDropdown) {
            this.imgCloseChildDropdown = imgCloseChildDropdown;
            return this;
        }

        public HeaderSpinnerViewBuilder withLayoutListHeader(ViewGroup layoutListHeader) {
            this.layoutListHeader = layoutListHeader;
            return this;
        }

        public HeaderSpinnerView build() {
            return new HeaderSpinnerView<>(ctx, typeface, headSection, defaultSelectedIdx,
                    fallbackHeaderTitle, listHeaderDropdown, txtChildDropdownTitle, toolbar,
                    layoutChildToolbarContainer, txtToolbarDropdown, imgCloseChildDropdown,
                    layoutListHeader);
        }
    }
}
