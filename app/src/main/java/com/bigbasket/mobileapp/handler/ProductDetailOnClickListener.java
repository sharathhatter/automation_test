package com.bigbasket.mobileapp.handler;

import android.content.Intent;
import android.view.View;

import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;

public class ProductDetailOnClickListener implements View.OnClickListener {
    private String skuId;
    private ActivityAware ctx;

    public ProductDetailOnClickListener(String skuId, ActivityAware context) {
        this.skuId = skuId;
        this.ctx = context;
    }

    @Override
    public void onClick(View v) {
        if (ctx == null) return;
        Intent intent = new Intent(ctx.getCurrentActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
        intent.putExtra(Constants.SKU_ID, skuId);
        ctx.getCurrentActivity().startActivityForResult(intent, Constants.GO_TO_HOME);
    }
}
