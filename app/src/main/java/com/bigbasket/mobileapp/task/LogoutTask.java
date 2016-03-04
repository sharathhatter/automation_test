package com.bigbasket.mobileapp.task;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.adapter.db.DynamicPageDbHelper;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.application.BaseApplication;
import com.bigbasket.mobileapp.handler.AnalyticsIdentifierKeys;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.OnLogoutListener;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.NewRelicWrapper;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Response;

public class LogoutTask extends AsyncTask<Void, Void, ErrorResponse> {

    private AppOperationAware ctx;

    public LogoutTask(AppOperationAware ctx) {
        if (!(ctx instanceof OnLogoutListener)) {
            throw new IllegalArgumentException("Must implement OnLogoutListener");
        }
        this.ctx = ctx;
    }

    @Override
    @Nullable
    protected ErrorResponse doInBackground(Void... params) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(ctx.getCurrentActivity());
        Call<BaseApiResponse> logoutCall = bigBasketApiService.logout();
        try {
            Response<BaseApiResponse> response = logoutCall.execute();
            if (response.isSuccess()) {
                if (response.body().status == 0) {
                    syncAppData(ctx.getCurrentActivity());
                    return null;
                }
                return new ErrorResponse(response.body().status,
                        response.body().message, ErrorResponse.API_ERROR);
            } else {
                return new ErrorResponse(response.code(),
                        response.message(), ErrorResponse.HTTP_ERROR);
            }
        } catch (IOException e) {
            return new ErrorResponse(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void syncAppData(AppOperationAware appOperationAware) {
        DynamicPageDbHelper.clearAll(appOperationAware.getCurrentActivity());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(appOperationAware.getCurrentActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.FIRST_NAME_PREF);
        editor.remove(Constants.BBTOKEN_KEY);
        editor.remove(Constants.OLD_BBTOKEN_KEY);
        editor.remove(Constants.MID_KEY);
        editor.remove(Constants.MEMBER_FULL_NAME_KEY);
        editor.remove(Constants.MEMBER_EMAIL_KEY);
        editor.remove(Constants.UPDATE_PROFILE_IMG_URL);
        editor.remove(Constants.IS_KIRANA);
        editor.remove(Constants.HAS_USER_GIVEN_RATING);
        editor.remove(Constants.DATE_SINCE_RATING_HAS_SHOWN);
        editor.remove(Constants.DAYS_PERIOD);
        editor.commit();
        AuthParameters.reset();
        AppDataDynamic.reset(appOperationAware.getCurrentActivity());

        String analyticsAdditionalAttrsJson = preferences.getString(Constants.ANALYTICS_ADDITIONAL_ATTRS, null);
        editor.remove(Constants.ANALYTICS_ADDITIONAL_ATTRS);

        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_ID, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_EMAIL, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_NAME, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_MOBILE, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_HUB, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_BDAY, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_GENDER, null);
        LocalyticsWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_CITY, null);

        NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_EMAIL, null);
        NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_ID, null);
        NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.USER_NAME, null);
        NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_MOBILE, null);
        NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_REGISTERED_ON, null);
        NewRelicWrapper.setIdentifier(AnalyticsIdentifierKeys.CUSTOMER_CITY, null);

        BaseApplication.updateGAUserId(appOperationAware.getCurrentActivity(), null);

        if (!TextUtils.isEmpty(analyticsAdditionalAttrsJson)) {
            Gson gson = new Gson();
            HashMap<String, Object> additionalAttrMap = new HashMap<>();
            additionalAttrMap = (HashMap<String, Object>) gson.fromJson(analyticsAdditionalAttrsJson, additionalAttrMap.getClass());
            if (additionalAttrMap != null) {
                for (Map.Entry<String, Object> entry : additionalAttrMap.entrySet()) {
                    LocalyticsWrapper.setIdentifier(entry.getKey(), null);
                }
            }
        }
    }

    @Override
    protected void onPostExecute(ErrorResponse errorResponse) {
        super.onPostExecute(errorResponse);
        if (errorResponse == null || errorResponse.getCode() == 401/* Unauthorized, Already logged out */) {
            ((OnLogoutListener) ctx).onLogoutSuccess();
        } else {
            ((OnLogoutListener) ctx).onLogoutFailure(errorResponse);
        }
    }
}
