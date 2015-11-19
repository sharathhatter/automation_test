package com.bigbasket.mobileapp.util;

import com.crashlytics.android.Crashlytics;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public final class BBUrlEncodeUtils {
    private BBUrlEncodeUtils() {
    }

    public static HashMap<String, String> urlEncode(Map<String, String> map) {
        if (map == null) return null;
        HashMap<String, String> encodedMap = new HashMap<>();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                encodedMap.put(entry.getKey(), URLEncoder.encode(entry.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                Crashlytics.logException(e);
            }
        }
        return encodedMap;
    }
}
