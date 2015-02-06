package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.SectionData;

import retrofit.RetrofitError;

public interface DynamicScreenAware {
    public void onDynamicScreenSuccess(String screenName, SectionData sectionData);

    public void onDynamicScreenFailure(RetrofitError error);

    public void onDynamicScreenFailure(int error, String msg);
}
