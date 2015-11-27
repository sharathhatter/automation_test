package com.bigbasket.mobileapp.handler.click;

import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.util.Constants;

import java.util.ArrayList;

public class OnBrandPageListener<T extends AppOperationAware> implements View.OnClickListener {
    private LaunchProductListAware ctx;

    public OnBrandPageListener(LaunchProductListAware ctx) {
        this.ctx = ctx;
    }

    @Override
    public void onClick(View v) {
        if (ctx == null) return;
        String brandSlug = (String) v.getTag(R.id.brand_slug);
        if (TextUtils.isEmpty(brandSlug)) return;
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.BRAND));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, brandSlug));
        ctx.launchProductList(nameValuePairs, null, null);
    }
}
