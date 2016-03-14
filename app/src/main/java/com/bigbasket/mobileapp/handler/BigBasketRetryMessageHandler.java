package com.bigbasket.mobileapp.handler;

import android.content.Context;
import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.util.Constants;

/**
 * Created by manu on 3/2/16.
 */
public class BigBasketRetryMessageHandler extends BigBasketMessageHandler {

    Context context;
    public Bundle bundleData;

    public BigBasketRetryMessageHandler(ApiErrorAware ctx, Context context) {
        super(ctx);
        this.context = context;
    }

    public BigBasketRetryMessageHandler(ApiErrorAware ctx, Context context, Bundle bundle) {
        super(ctx);
        this.context = context;
        this.bundleData = bundle;
    }


    @Override
    public void sendOfflineError() {
        if (context instanceof BaseActivity) {
            ((AppOperationAware) context).getCurrentActivity().showAlertDialog(((BaseActivity) context).getCurrentActivity().getString(R.string.headingConnectionOffline),
                    context.getString(R.string.validationretry),
                    context.getString(R.string.retry),
                    context.getString(R.string.cancel),
                    Constants.OFFLINE_PAYMENT_SHOW_THANKYOU_ABORT_CONFIRMATION_DIALOG, bundleData);
        } else
            super.sendOfflineError();
    }

    @Override
    public void handleRetrofitError(Throwable throwable, boolean finish) {
        /**
         * handling all the retrofit, showing retry option to user
         * to ensure the validate payment api call gets success
         */
        sendOfflineError();
    }
}
