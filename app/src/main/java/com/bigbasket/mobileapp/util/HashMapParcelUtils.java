package com.bigbasket.mobileapp.util;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by muniraju on 29/02/16.
 */
public class HashMapParcelUtils {

    public static Bundle mapToBundle(Map<String, String> map) {
        if (map == null) {
            return null;
        }
        Bundle bundle = new Bundle(map.size());
        for (Map.Entry<String, String> e : map.entrySet()) {
            bundle.putString(e.getKey(), e.getValue());
        }
        return bundle;
    }

    public static HashMap<String, String> bundleToMap(Bundle bundle) {
        if (bundle == null || bundle.keySet() == null) {
            return null;
        }
        HashMap<String, String> map = new HashMap<>(bundle.keySet().size());
        for (String key : bundle.keySet()) {
            map.put(key, bundle.getString(key));
        }
        return map;
    }

}
