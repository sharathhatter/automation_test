package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.SectionData;

import retrofit.RetrofitError;

public interface DynamicScreenAware {
    void onDynamicScreenSuccess(String screenName, SectionData sectionData);

    void onDynamicScreenFailure(RetrofitError error);

    void onDynamicScreenFailure(int error, String msg);
}
