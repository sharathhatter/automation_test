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

    public OnSponsoredItemClickListener(T context) {
        super(context);
    }

    public OnSponsoredItemClickListener(T context, @Nullable Section section,
                                        @Nullable SectionItem sectionItem,
                                        @Nullable String screenName,
                                        Map<String, String> analyticsAttrs) {
        super(context, section, sectionItem, screenName);
    }

    @Override
    protected String getAdditionalNcValue() {
        return "spons.promo";
    }
}
