package com.bigbasket.mobileapp.handler;


import android.content.Intent;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;

public class OnPromoClickListener<T extends AppOperationAware> implements View.OnClickListener {

    private T context;

    public OnPromoClickListener(T context) {
        this.context = context;
    }

    @Override
    public void onClick(View v) {
        if (context == null || v.getTag(R.id.promo_id) == null) return;
        int promoId = (int) v.getTag(R.id.promo_id);
        Intent promoDetailIntent = new Intent(context.getCurrentActivity(), BackButtonActivity.class);
        promoDetailIntent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
        promoDetailIntent.putExtra(Constants.PROMO_ID, promoId);
        context.getCurrentActivity().startActivityForResult(promoDetailIntent, NavigationCodes.GO_TO_HOME);
    }
}
