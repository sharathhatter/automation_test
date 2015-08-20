package com.bigbasket.mobileapp.interfaces.payment;

import android.content.Intent;

import java.util.HashMap;

/**
 * Created by jugal on 19/8/15.
 */
public interface MobikwikAware {
    void initializeMobikwik(HashMap<String, String> paymentParams);
}
