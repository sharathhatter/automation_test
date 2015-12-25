package com.bigbasket.mobileapp.handler.click;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.service.AnalyticsIntentService;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

/**
 * Created by muniraju on 17/12/15.
 */
public class OnSponsoredItemClickListener<T extends AppOperationAware>
        extends OnSectionItemClickListener<T> {
    private final Map<String, String> analyticsAttrs;
    private String analyticsAttrsJsonString ;

    public OnSponsoredItemClickListener(T context) {
        super(context);
        analyticsAttrs = null;
    }

    public OnSponsoredItemClickListener(T context, @Nullable Section section,
                                        @Nullable SectionItem sectionItem,
                                        @Nullable String screenName,
                                        Map<String, String> analyticsAttrs) {
        super(context, section, sectionItem, screenName);
        this.analyticsAttrs = analyticsAttrs;
    }

    @Override
    protected String getAdditionalNcValue() {
        return "spons.promo";
    }

    @Override
    protected void onSectionClick() {
        super.onSectionClick();
        if (context == null || context.getCurrentActivity() == null || context.isSuspended()) {
            return;
        }
        Context appContext = context.getCurrentActivity().getApplicationContext();
        if(analyticsAttrsJsonString == null && analyticsAttrs != null && !analyticsAttrs.isEmpty()) {
            Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();
            analyticsAttrsJsonString = gson.toJson(analyticsAttrs);
        }
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appContext);
        AnalyticsIntentService.startUpdateSponsoredProductEvent(
                appContext,
                true,
                sectionItem.getId(),
                preferences.getString(Constants.CITY_ID, null),
                analyticsAttrsJsonString);
    }
}
