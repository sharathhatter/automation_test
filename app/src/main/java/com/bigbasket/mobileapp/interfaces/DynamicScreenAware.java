package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.section.SectionData;

public interface DynamicScreenAware {
    void onDynamicScreenSuccess(String screenName, SectionData sectionData);

    void onDynamicScreenFailure(Throwable t);

    void onDynamicScreenFailure(int error, String msg);
}
