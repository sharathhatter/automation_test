package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

public class SocialLoginConfirmActivity extends ChooseLocationActivity {

    private String mLoginType;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoginType = getIntent().getStringExtra(Constants.SOCIAL_LOGIN_TYPE);
    }

    @Override
    public void requestCityChange(City newCity) {
        String authToken = getIntent().getStringExtra(Constants.AUTH_TOKEN);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.socialRegisterMember(mLoginType, authToken,
                new LoginApiResponseCallback(null, null, false, mLoginType, authToken));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onAccountNotLinked();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
