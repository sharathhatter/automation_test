package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.view.MenuItem;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.facebook.AccessToken;
import com.google.gson.Gson;

public class SocialLoginConfirmActivity extends ChangeCityActivity {

    private String mLoginType;

    @Override
    protected void changeCity(City city) {
        mLoginType = getIntent().getStringExtra(Constants.SOCIAL_LOGIN_TYPE);
        SocialAccount socialAccount = getIntent().getParcelableExtra(Constants.SOCIAL_LOGIN_PARAMS);
        onCreateNewAccount(socialAccount, city);
    }

    @Override
    public void onFacebookSignIn(AccessToken accessToken) {
        // Don't do anything
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onAccountNotLinked();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateNewAccount(SocialAccount socialAccount, City city) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgress(true);
        bigBasketApiService.socialRegisterMember(mLoginType, new Gson().toJson(socialAccount, SocialAccount.class),
                String.valueOf(city.getId()), new LoginApiResponseCallback(socialAccount.getEmail(), null, false, mLoginType, socialAccount));
    }

    public void showProgress(boolean show) {
        if (show) {
            showProgressDialog(getString(R.string.please_wait));
        } else {
            hideProgressDialog();
        }
    }

    @Override
    public void onBackPressed() {
        onAccountNotLinked();
    }

    private void onAccountNotLinked() {
        Intent data = new Intent();
        data.putExtra(Constants.SOCIAL_LOGIN_TYPE, mLoginType);
        setResult(Constants.SOCIAL_ACCOUNT_NOT_LINKED, data);
        finish();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SOCIAL_ACCOUNT_CONFIRMATION_SCREEN;
    }
}
