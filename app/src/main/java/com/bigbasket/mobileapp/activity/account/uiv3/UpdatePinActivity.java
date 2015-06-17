package com.bigbasket.mobileapp.activity.account.uiv3;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.UpdatePin;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class UpdatePinActivity extends BackButtonActivity {
    private String currentPin;
    private EditText editTextNewPin;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.deliveryPin));
        getCurrentMemberPin();
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.update_pin, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_submit_pin:
                onUpdatePinButtonClicked();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getCurrentMemberPin() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.getCurrentMemberPin(new Callback<ApiResponse<UpdatePin>>() {
            @Override
            public void success(ApiResponse<UpdatePin> updatePinApiResponse, Response response) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                if (updatePinApiResponse.status == 0) {
                    renderCurrentMemberPin(updatePinApiResponse.apiResponseContent.currentPin);
                } else {
                    handler.sendEmptyMessage(updatePinApiResponse.status, updatePinApiResponse.message);
                }

            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                try {
                    hideProgressView();
                } catch (IllegalArgumentException e) {
                    return;
                }
                handler.handleRetrofitError(error, true);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (currentPin != null)
            outState.putString(Constants.CURRENT_PIN, currentPin);
        super.onSaveInstanceState(outState);
    }

    public void onUpdatePinButtonClicked() {
        if (editTextNewPin != null && editTextNewPin.isEnabled()) {
            editTextNewPin.setError(null);
            final String newPin = editTextNewPin.getText().toString();
            if (!TextUtils.isEmpty(newPin)) {
                if (newPin.equalsIgnoreCase(currentPin)) {
                    finish();
                } else if (newPin.length() != 4) {
                    editTextNewPin.setError(getString(R.string.min4digit));
                } else if (newPin.equals(Constants.FOUR_ZERO)) {
                    editTextNewPin.setError(getString(R.string.not0000));
                } else {
                    updatePinServerCall(newPin);
                }
            } else {
                editTextNewPin.setError(getString(R.string.enterPin));
            }
        } else {
            finish();
        }
    }

    private void updateEditTextField(String newPin) {
        currentPin = newPin;
        editTextNewPin.setText(newPin);
        editTextNewPin.setEnabled(false);
        showToast(getString(R.string.pinUpdatedSuccessfully));
    }

    private void updatePinServerCall(final String newPin) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.updateCurrentMemberPin(newPin, new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse apiResponse, Response response) {
                hideProgressDialog();
                int status = apiResponse.status;
                if (status == 0) {
                    updateEditTextField(newPin);
                    BaseActivity.hideKeyboard(getCurrentActivity(), editTextNewPin);
                    finish();
                } else {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.FAILURE_REASON, apiResponse.message);
                    trackEvent(TrackingAware.UPDATE_PIN_CLICKED, map);
                    handler.sendEmptyMessage(apiResponse.status, apiResponse.message);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.FAILURE_REASON, error.toString());
                trackEvent(TrackingAware.UPDATE_PIN_CLICKED, map);
            }
        });
    }

    private void renderCurrentMemberPin(String currentPin) {
        if (currentPin == null) return;

        this.currentPin = currentPin;
        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        contentLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getCurrentActivity());
        View view = inflater.inflate(R.layout.uiv3_updatepin, contentLayout, false);

        TextView textPinLabel = (TextView) view.findViewById(R.id.textPinLabel);
        textPinLabel.setTypeface(faceRobotoRegular);

        editTextNewPin = (EditText) view.findViewById(R.id.editTextNewPin);
        editTextNewPin.setText(currentPin);
        editTextNewPin.setEnabled(false);

        ImageView imgEditPin = (ImageView) view.findViewById(R.id.imgEditPin);
        imgEditPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextNewPin.setFocusable(true);
                editTextNewPin.setEnabled(true);
                BaseActivity.showKeyboard(editTextNewPin);
            }
        });

        TextView textUpdatePinInfo1 = (TextView) view.findViewById(R.id.textUpdatePinInfo1);
        textUpdatePinInfo1.setTypeface(faceRobotoRegular);
        TextView textUpdatePinInfo2 = (TextView) view.findViewById(R.id.textUpdatePinInfo2);
        textUpdatePinInfo2.setTypeface(faceRobotoRegular);
        TextView textUpdatePinInfo3 = (TextView) view.findViewById(R.id.textUpdatePinInfo3);
        textUpdatePinInfo3.setTypeface(faceRobotoBold);
        TextView textUpdatePinInfo4 = (TextView) view.findViewById(R.id.textUpdatePinInfo4);
        textUpdatePinInfo4.setTypeface(faceRobotoRegular);

        contentLayout.removeAllViews();
        contentLayout.addView(view);

        trackEvent(TrackingAware.CHANGE_PIN_SHOWN, null);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_EDIT_PIN_SCREEN;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (editTextNewPin != null && getCurrentActivity() != null) {
            BaseActivity.hideKeyboard(getCurrentActivity(), editTextNewPin);
        }
    }
}
