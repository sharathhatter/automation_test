package com.bigbasket.mobileapp.handler;

import android.view.View;

import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

public class OnBrandPageListener implements View.OnClickListener {
    private String brandSlug;
    private LaunchProductListAware ctx;

    public OnBrandPageListener(LaunchProductListAware ctx, String brandSlug) {
        this.ctx = ctx;
        this.brandSlug = brandSlug;
    }

    @Override
    public void onClick(View v) {
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.BRAND.get()));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, brandSlug));
        ctx.launchProductList(nameValuePairs, null, null);
    }
}
