package com.bigbasket.mobileapp.util.analytics;

import com.crashlytics.android.Crashlytics;
import com.newrelic.agent.android.NewRelic;

import java.util.Map;

public class NewRelicWrapper {

    public static void setIdentifier(String key, String value) {
        try {
            NewRelic.setAttribute(key, value);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public static boolean recordEvent(String eventName, Map<String, Object> newRelicAttributes) {
        try {
            return NewRelic.recordEvent(eventName, newRelicAttributes);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return false;
    }
}
