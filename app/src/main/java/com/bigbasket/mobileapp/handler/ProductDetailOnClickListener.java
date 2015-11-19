package com.bigbasket.mobileapp.handler;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonWithBasketButtonActivity;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;

public class ProductDetailOnClickListener<T extends AppOperationAware> implements View.OnClickListener {
    //    private String skuId;
    private T ctx;

    public ProductDetailOnClickListener(T context) {
//        this.skuId = skuId;
        this.ctx = context;
    }

    @Override
    public void onClick(View v) {
        if (ctx == null) return;
        String skuId = (String) v.getTag(R.id.sku_id);
        if (TextUtils.isEmpty(skuId)) return;
        Intent intent = new Intent(ctx.getCurrentActivity(), BackButtonWithBasketButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PRODUCT_DETAIL);
        intent.putExtra(Constants.SKU_ID, skuId);
        if (ctx instanceof Fragment) {
            ((Fragment) ctx).startActivityForResult(intent, NavigationCodes.BASKET_CHANGED);
        } else {
            ctx.getCurrentActivity().startActivityForResult(intent, NavigationCodes.BASKET_CHANGED);
        }
    }
}
