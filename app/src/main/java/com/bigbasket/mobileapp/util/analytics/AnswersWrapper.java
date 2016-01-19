package com.bigbasket.mobileapp.util.analytics;


import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;

public class AnswersWrapper {

    public static void logCustom(CustomEvent customEvent) {
        try {
            Answers.getInstance().logCustom(customEvent);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }
}
