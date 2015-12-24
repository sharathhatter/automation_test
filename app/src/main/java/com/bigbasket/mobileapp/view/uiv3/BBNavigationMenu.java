package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.ScrimInsetsFrameLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.NavigationAdapter;
import com.bigbasket.mobileapp.devconfig.DevConfigViewHandler;
import com.bigbasket.mobileapp.fragment.account.AccountView;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.NavigationSelectedValueAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SubSectionItem;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.SectionCursorHelper;

import java.util.ArrayList;
import java.util.HashMap;

public class BBNavigationMenu extends ScrimInsetsFrameLayout {

    public interface Callback {
        void onChangeAddressRequested();

        String onSubCategoryIdRequested();
    }

    @Nullable
    private RecyclerView mNavRecyclerView;
    @Nullable
    private AnimatedRelativeLayout mListSubNavigationParent;
    @Nullable
    private RecyclerView mListSubNavigation;
    @Nullable
    private Callback mCallback;
    private AppOperationAware mAppOperationAware;

    public BBNavigationMenu(Context context) {
        super(context);
        init();
    }

    public BBNavigationMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        View.inflate(getContext(), R.layout.uiv3_left_nav_layout, this);
        mNavRecyclerView = (RecyclerView) findViewById(R.id.listNavigation);
        if (mNavRecyclerView != null) {
            // Assign a layout manager as soon as RecyclerView is inflated to prevent onDestroy crash
            mNavRecyclerView.setHasFixedSize(false);
            mNavRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        mListSubNavigation = (RecyclerView) findViewById(R.id.listSubNavigation);
        if (mListSubNavigation != null) {
            // Assign a layout manager as soon as RecyclerView is inflated to prevent onDestroy crash
            mListSubNavigation.setHasFixedSize(false);
            mListSubNavigation.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        mListSubNavigationParent = (AnimatedRelativeLayout) findViewById(R.id.layoutSubNavigationItems);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void attachAppOperationAware(AppOperationAware appOperationAware) {
        this.mAppOperationAware = appOperationAware;
    }

    public void onSubNavigationHideRequested(boolean animated) {
        if (mListSubNavigation != null) {
            mListSubNavigation.setAdapter(null);
        }
        if (mListSubNavigationParent != null) {
            mListSubNavigationParent.setVisibility(View.GONE, animated);
        }
    }

    public void onSubNavigationRequested(Section section, SectionItem sectionItem, String baseImgUrl,
                                         HashMap<Integer, Renderer> rendererHashMap) {
        Typeface faceRobotoMedium = FontHolder.getInstance(getContext()).getFaceRobotoMedium();
        if (sectionItem.getSubSectionItems() == null
                || sectionItem.getSubSectionItems().isEmpty()){
            return;
        }
        ArrayList<SectionItem> subNavigationSectionItems = new ArrayList<>(
                sectionItem.getSubSectionItems().size());
        subNavigationSectionItems.addAll(sectionItem.getSubSectionItems());


        if (mListSubNavigationParent == null) {
            mListSubNavigationParent = (AnimatedRelativeLayout) findViewById(R.id.layoutSubNavigationItems);
        }

        if (mListSubNavigation == null) {
            mListSubNavigation = (RecyclerView) findViewById(R.id.listSubNavigation);
            mListSubNavigation.setHasFixedSize(false);
            mListSubNavigation.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();
        section = new Section(sectionItem.getTitle(), sectionItem.getDescription(), Section.MENU,
                subNavigationSectionItems, null);
        SectionCursorHelper.setSectionNavigationItemList(sectionNavigationItems,
                subNavigationSectionItems, section);
        String selectedId = mCallback != null ? mCallback.onSubCategoryIdRequested() : null;

        NavigationAdapter navigationAdapter = new NavigationAdapter(getContext(), faceRobotoMedium,
                sectionNavigationItems,
                AbstractDynamicPageSyncService.MAIN_MENU, baseImgUrl, rendererHashMap, sectionItem,
                this);
        if (!TextUtils.isEmpty(selectedId)) {
            navigationAdapter.setSelectedCategoryString(selectedId);
        }
        mListSubNavigation.setAdapter(navigationAdapter);
        mListSubNavigationParent.setVisibility(View.VISIBLE, true);
    }

    public void onDrawerClosed() {
        if (mListSubNavigationParent != null && mListSubNavigationParent.getVisibility() == View.VISIBLE) {
            onSubNavigationHideRequested(false);
        }
        if (mNavRecyclerView != null) {
            if (mNavRecyclerView.getVisibility() != View.VISIBLE) {
                // User was in settings menu, now restore the default state to avoid confusion
                ImageView imgSwitchNav = (ImageView) findViewById(R.id.imgSwitchNav);
                if (imgSwitchNav != null) {
                    toggleNavigationArea(imgSwitchNav);
                }
            }
            mNavRecyclerView.scrollToPosition(0);
        }
    }

    public void onDrawerOpened() {
        if (mNavRecyclerView != null && mNavRecyclerView.getAdapter() != null) {
            mNavRecyclerView.getAdapter().notifyDataSetChanged();
        }
        if (mListSubNavigation != null && mListSubNavigation.getAdapter() != null)
            mListSubNavigation.getAdapter().notifyDataSetChanged();
    }

    public void setNavigationAdapter(@NonNull NavigationAdapter navigationAdapter) {
        if (mNavRecyclerView != null) {
            mNavRecyclerView.setAdapter(navigationAdapter);
        }
    }

    public void showMenuLoading(boolean visible) {
        ProgressBar progressBarMenu = (ProgressBar) findViewById(R.id.progressBarMenu);
        if (visible) {
            progressBarMenu.setVisibility(View.VISIBLE);
            if (mNavRecyclerView != null) {
                mNavRecyclerView.setVisibility(View.GONE);
            }
        } else {
            progressBarMenu.setVisibility(View.GONE);
            if (mNavRecyclerView != null) {
                mNavRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void toggleNavigationArea(ImageView imgSwitchNav) {
        if (mNavRecyclerView == null) return;
        ProgressBar progressBarMenu = (ProgressBar) findViewById(R.id.progressBarMenu);
        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);

        int drawableId;
        if (mNavRecyclerView.getVisibility() == View.VISIBLE) {
            if (progressBarMenu.getVisibility() != View.VISIBLE) {  // If main menu is downloading, then don't toggle any visibility
                mNavRecyclerView.setVisibility(View.GONE);
            }
            lstMyAccount.setVisibility(View.VISIBLE);
            drawableId = R.drawable.ic_menu_white_36dp;
        } else {
            lstMyAccount.setVisibility(View.GONE);
            if (progressBarMenu.getVisibility() != View.VISIBLE) { // If main menu is downloading, then don't toggle any visibility
                mNavRecyclerView.setVisibility(View.VISIBLE);
            }
            drawableId = R.drawable.settings;
        }
        imgSwitchNav.setImageDrawable(ContextCompat.getDrawable(getContext(), drawableId));
    }

    public void displayHeader() {
        Typeface faceRobotoMedium = FontHolder.getInstance(getContext()).getFaceRobotoMedium();
        TextView txtNavSalutation = (TextView) findViewById(R.id.txtNavSalutation);
        txtNavSalutation.setTypeface(faceRobotoMedium);
        TextView lblWelCome = (TextView) findViewById(R.id.lblWelcome);
        lblWelCome.setTypeface(faceRobotoMedium);
        ImageView imgSwitchNav = (ImageView) findViewById(R.id.imgSwitchNav);

        AuthParameters authParameters = AuthParameters.getInstance(getContext());

        ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(getContext()).getAddressSummaries();
        if (addressSummaries != null && addressSummaries.size() > 0) {
            setCityText(addressSummaries.get(0).toString());
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            setCityText(preferences.getString(Constants.CITY, ""));
        }
        if (!authParameters.isAuthTokenEmpty()) {
            txtNavSalutation.setText(!TextUtils.isEmpty(authParameters.getMemberFullName()) ?
                    authParameters.getMemberFullName() : authParameters.getMemberEmail());
        } else {
            txtNavSalutation.setText(getContext().getString(R.string.bigbasketeer));
        }
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            DevConfigViewHandler.setView(lblWelCome);
        }

        imgSwitchNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNavigationArea((ImageView) v);
            }
        });
        imgSwitchNav.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.settings));
        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);
        lstMyAccount.setVisibility(View.GONE);

        if (mAppOperationAware != null) {
            new AccountView<>(mAppOperationAware, lstMyAccount);
        }
    }

    public void setCityText(String text) {
        Typeface faceRobotoMedium = FontHolder.getInstance(getContext()).getFaceRobotoMedium();
        TextView txtCityName = (TextView) findViewById(R.id.txtCityName);
        Button btnChangeAddress = (Button) findViewById(R.id.btnChangeAddress);
        if (txtCityName == null || btnChangeAddress == null) return;
        txtCityName.setTypeface(faceRobotoMedium);

        AuthParameters authParameters = AuthParameters.getInstance(getContext());
        if (authParameters.isAuthTokenEmpty() || authParameters.isMultiCityEnabled()) {
            View.OnClickListener changeAddressOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallback != null) {
                        mCallback.onChangeAddressRequested();
                    }
                }
            };
            txtCityName.setOnClickListener(changeAddressOnClickListener);
            btnChangeAddress.setOnClickListener(changeAddressOnClickListener);
        } else {
            btnChangeAddress.setVisibility(View.GONE);
        }
        txtCityName.setText(text);
    }

    public boolean onBackPressed(@Nullable DrawerLayout drawerLayout) {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            if (mListSubNavigationParent != null && mListSubNavigationParent.getVisibility() == View.VISIBLE) {
                mListSubNavigationParent.setVisibility(View.GONE, true);
                return true;
            } else {
                drawerLayout.closeDrawers();
                return true;
            }
        }
        return false;
    }

    public void onNavigationSelection(String name) {
        if (mNavRecyclerView != null && mNavRecyclerView.getAdapter() != null) {
            ((NavigationSelectedValueAware) mNavRecyclerView.getAdapter()).setSelectedNavigationCategory(name);
        }
    }
}
