package com.bigbasket.mobileapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class DeepLinkDispatcherActivity extends BaseActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        launchCorrespondingActivity();
    }

    private void launchCorrespondingActivity() {
        Uri uri = getIntent().getData();
        if (uri == null) {
            finish();
            return;
        }
        String idStr = uri.getQueryParameter(Constants.ID);
        if (TextUtils.isEmpty(idStr) && !TextUtils.isEmpty(uri.getHost()) && !uri.getHost().equals(Constants.WALLET)) {
            finish();
            return;
        }

        switch (uri.getHost()) {
            case Constants.PROMO:
                String promoId = uri.getQueryParameter(Constants.ID);
                //Intent intent = new
                //int promoId = Integer.parseInt(idStr);
//                Intent intent = new Intent(this, PromoDetailActivity.class);
//                intent.putExtra(Constants.PROMO_ID, promoId);
//                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.FEEDBACK:
//                intent = new Intent(this, CustomerFeedbackActivity.class);
//                intent.putExtra(Constants.CASE_ID, idStr);
//                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.ORDER:
//                intent = new Intent(this, OrderReview.class);
//                intent.putExtra(Constants.ORDER_ID, idStr);
//                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            case Constants.WALLET:
//                intent = new Intent(this, DoWalletActivity.class);
//                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
//                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                break;
            case Constants.CATEGORY_LANDING:
                break;
            case Constants.PRODUCT_DETAIL:
                break;
            case Constants.ORDER_ITEMS:
                break;
            case Constants.ALL_SL:
                break;
            case Constants.SL_SUMMARY:
                break;
            case Constants.SL_PRODUCTS:
                break;
            case Constants.PROMO_LIST:
                break;
            default:
                finish();
                break;
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (getParent() == null) {
            Intent intent = new Intent(this, StartActivity.class);
            startActivity(intent);
        }
        finish();
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    @Override
    public String getScreenTag(){
        return TrackEventkeys.DEEP_LINK_DISPATCHER_SCREEN;
    }
}