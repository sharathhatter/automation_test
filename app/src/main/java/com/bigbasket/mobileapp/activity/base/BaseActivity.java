package com.bigbasket.mobileapp.activity.base;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.appsflyer.AppsFlyerLib;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.CommunicationHubActivity;
import com.bigbasket.mobileapp.activity.HomeActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.ChooseLocationActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SignupActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.ShowCartActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.activity.specialityshops.BBSpecialityShopsActivity;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoDbHelper;
import com.bigbasket.mobileapp.adapter.db.DynamicPageDbHelper;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.base.ProgressDialogFragment;
import com.bigbasket.mobileapp.fragment.dialogs.ConfirmationDialogFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.AnalyticsNavigationContextAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.interfaces.LaunchStoreListAware;
import com.bigbasket.mobileapp.interfaces.OnBasketChangeListener;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.analytics.FacebookEventTrackWrapper;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.bigbasket.mobileapp.util.analytics.NewRelicWrapper;
import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.moe.pushlibrary.MoEHelper;
import com.newrelic.agent.android.NewRelic;
import com.newrelic.agent.android.instrumentation.Trace;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public abstract class BaseActivity extends AppCompatActivity implements
        AppOperationAware, TrackingAware, ApiErrorAware,
        LaunchProductListAware, OnBasketChangeListener, AnalyticsNavigationContextAware,
        LaunchStoreListAware, ConfirmationDialogFragment.ConfirmationDialogCallback {

    protected Typeface faceRupee;
    protected Typeface faceRobotoRegular, faceRobotoLight, faceRobotoMedium,
            faceRobotoBold;
    protected BigBasketMessageHandler handler;
    protected MoEHelper moEHelper;
    private boolean isActivitySuspended;
    private ProgressDialog progressDialog = null;
    private String mNavigationContext;
    private String mNextScreenNavigationContext;

    private String PROGRESS_DIALOG_TAG;

    public static void showKeyboard(final View view) {
        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                MotionEvent motionActionDown = MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0);
                MotionEvent motionActionUp = MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
                if (motionActionDown == null || motionActionUp == null) return;
                view.dispatchTouchEvent(motionActionDown);
                view.dispatchTouchEvent(motionActionUp);
                if (view instanceof EditText) {
                    EditText editText = (EditText) view;
                    editText.setSelection(editText.getText().length());
                }
            }
        }, 100);
    }

    public static void hideKeyboard(Context context, View view) {
        if (context == null || view == null) return;
        IBinder token = view.getWindowToken();
        if (token == null) return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new BigBasketMessageHandler<>(this);
        isActivitySuspended = false;
        FontHolder fontHolder = FontHolder.getInstance(getApplicationContext());
        faceRupee = fontHolder.getFaceRupee();
        faceRobotoRegular = fontHolder.getFaceRobotoRegular();
        faceRobotoMedium = fontHolder.getFaceRobotoMedium();
        faceRobotoBold = fontHolder.getFaceRobotoBold();
        faceRobotoLight = fontHolder.getFaceRobotoLight();
        moEHelper = MoEngageWrapper.getMoHelperObj(getApplicationContext());
        mNavigationContext = getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX);
        NewRelic.setInteractionName(getClass().getSimpleName());
    }

    @Override
    public void onBackPressed() {
        //Workaround to avoid IllegalStateException: Can not perform this action after onSaveInstanceState
        onStateNotSaved();
        try {
            super.onBackPressed();
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }

    public MoEHelper getMoEHelper() {
        return moEHelper;
    }

    @Override
    public boolean checkInternetConnection() {
        return DataUtil.isInternetAvailable(getApplicationContext());
    }

    @Override
    public void showProgressDialog(String msg) {
        showProgressDialog(msg, true);
    }

    @Override
    public void showProgressDialog(String msg, boolean cancelable) {
        showProgressDialog(msg, cancelable, false);
    }

    @Override
    public void showProgressDialog(String msg, boolean cancelable, boolean isDeterminate) {
        if (isSuspended()) return;
        String progressDialogTag = getProgressDialogTag();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(progressDialogTag);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        if (fragment != null) {
            ft.remove(fragment);
        }
        fragment = ProgressDialogFragment.newInstance(msg, cancelable, isDeterminate);
        ft.add(fragment, progressDialogTag);
        if (!isSuspended()) {
            try {
                ft.commitAllowingStateLoss();
            } catch (IllegalStateException ex) {
                Crashlytics.logException(ex);
            }
        }
    }

    @Override
    public void hideProgressDialog() {
        String progressDialogTag = getProgressDialogTag();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(progressDialogTag);
        if (fragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            try {
                ft.remove(fragment);
            } finally {
                if (!isSuspended()) {
                    ft.commitAllowingStateLoss();
                }
            }
        }
    }

    private String getProgressDialogTag() {
        if (PROGRESS_DIALOG_TAG == null) {
            synchronized (this) {
                if (PROGRESS_DIALOG_TAG == null) {
                    PROGRESS_DIALOG_TAG = getScreenTag() + "#ProgressDilog";
                }
            }
        }
        return PROGRESS_DIALOG_TAG;
    }

    @Override
    public void showProgressView() {
        showProgressDialog(getString(R.string.please_wait));
    }

    @Override
    public void hideProgressView() {
        if (isSuspended()) return;
        try {
            hideProgressDialog();
        } catch (IllegalArgumentException e) {

        }
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Trace
    @Override
    protected void onStart() {
        super.onStart();
        isActivitySuspended = false;
        MoEngageWrapper.onStart(moEHelper, this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivitySuspended = true;
        MoEngageWrapper.onStop(moEHelper, this);
        FacebookEventTrackWrapper.deactivateApp(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivitySuspended = true;
        MoEngageWrapper.onPause(moEHelper, this);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isActivitySuspended = false;
        MoEngageWrapper.onNewIntent(moEHelper, this, intent);
    }

    protected void onResume() {
        super.onResume();
        isActivitySuspended = false;

        MoEngageWrapper.onResume(moEHelper, this);
        FacebookEventTrackWrapper.activateApp(getApplicationContext());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        moEHelper.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        moEHelper.onRestoreInstanceState(savedInstanceState);
    }

    public void launchMoEngageCommunicationHub() {
        AuthParameters authParameters = AuthParameters.getInstance(getApplicationContext());
        if (!authParameters.isAuthTokenEmpty()) {
            Intent communicationHunIntent = new Intent(this, CommunicationHubActivity.class);
            communicationHunIntent.putExtra(Constants.COMMUNICATION_HUB_FAQ_SHOW, false);
            communicationHunIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(communicationHunIntent);
        } else {
            showToast(getString(R.string.loginToContinue));
            Bundle bundle = new Bundle(1);
            bundle.putInt(Constants.FRAGMENT_CODE, FragmentCodes.START_COMMUNICATION_HUB);
            launchLogin(getPreviousScreenName(), bundle, true);
        }

        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.ACCOUNT_MENU);
        trackEvent(TrackingAware.COMMUNICATION_HUB_SHOWN, eventAttribs);
    }

    public void launchMoEngageCommunicationHubWithFAQ() {
        AuthParameters authParameters = AuthParameters.getInstance(getApplicationContext());
        if (!authParameters.isAuthTokenEmpty()) {
            Intent communicationHunIntent = new Intent(this, CommunicationHubActivity.class);
            communicationHunIntent.putExtra(Constants.COMMUNICATION_HUB_FAQ_SHOW, true);

            startActivity(communicationHunIntent);
        } else {
            showToast(getString(R.string.loginToContinue));
            Bundle bundle = new Bundle(1);
            bundle.putInt(Constants.FRAGMENT_CODE, FragmentCodes.START_COMMUNICATION_HUB);
            launchLogin(getPreviousScreenName(), bundle, true);
        }

        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.ACCOUNT_MENU);
        trackEvent(TrackingAware.COMMUNICATION_HUB_SHOWN, eventAttribs);
    }

    public void showAlertDialog(String title, String msg) {
        showAlertDialog(title, msg, -1);
    }

    public void showAlertDialog(String title, String msg, int requestCode) {
        showAlertDialog(title, msg, requestCode, null);
    }

    public void showAlertDialog(String title, String msg, final int requestCode, final Bundle valuePassed) {

        if (isSuspended())
            return;
        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                requestCode, title == null ? "BigBasket" : title, msg, getString(R.string.ok),
                null, false);
        try {
            dialogFragment.show(getSupportFragmentManager(), getScreenTag() + "#AlertDialog");
        } catch (IllegalStateException ex) {
            Crashlytics.logException(ex);
        }
    }

    @Override
    public final void onDialogConfirmed(int reqCode, Bundle data, boolean isPositive) {
        if (isPositive) {
            onPositiveButtonClicked(reqCode, data);
            if (data != null && data.getBoolean(Constants.FINISH_ACTIVITY, false)) {
                if (data.containsKey(Constants.ACTIVITY_RESULT_CODE)) {
                    setResult(data.getInt(Constants.ACTIVITY_RESULT_CODE, RESULT_OK));
                }
                finish();
            }
        } else {
            onNegativeButtonClicked(reqCode, data);
        }
    }


    @Override
    public void onDialogCancelled(int reqCode, Bundle data) {

    }

    public void showAlertDialogFinish(String title, String msg) {
        showAlertDialogFinish(title, msg, -1);
    }

    public void showAlertDialogFinish(String title, String msg, final int resultCode) {
        if (isSuspended())
            return;
        Bundle data = new Bundle(2);
        data.putBoolean(Constants.FINISH_ACTIVITY, true);
        data.putInt(Constants.ACTIVITY_RESULT_CODE, resultCode);
        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                0, title == null ? getString(R.string.app_name) : title, msg, getString(R.string.ok),
                null, data, false);
        try {
            dialogFragment.show(getSupportFragmentManager(), getScreenTag() + "#AlertDialog");
        } catch (IllegalStateException ex) {
            Crashlytics.logException(ex);
        }
    }

    public void showAlertDialog(String msg) {
        showAlertDialog(null, msg);
    }

    public void showAlertDialog(String title,
                                String msg, @DialogButton.ButtonType int dialogButton,
                                @DialogButton.ButtonType int nxtDialogButton, final int requestCode) {
        showAlertDialog(title, msg, dialogButton, nxtDialogButton, requestCode, null, null);
    }

    public void showAlertDialog(String title,
                                String msg, @DialogButton.ButtonType int dialogButton,
                                @DialogButton.ButtonType int nxtDialogButton, final int requestCode,
                                final Bundle passedValue) {
        showAlertDialog(title, msg, dialogButton, nxtDialogButton, requestCode, passedValue, null);
    }

    public void showAlertDialog(String title,
                                String msg, @DialogButton.ButtonType int dialogButton,
                                @DialogButton.ButtonType int nxtDialogButton, final int requestCode,
                                final Bundle passedValue, String positiveBtnText) {
        String negativeButtonText = null;

        if (dialogButton != DialogButton.NONE &&
                (dialogButton == DialogButton.YES || dialogButton == DialogButton.OK)) {
            if (TextUtils.isEmpty(positiveBtnText)) {
                int textId = dialogButton == DialogButton.YES ? R.string.yesTxt : R.string.ok;
                positiveBtnText = getString(textId);
            }
        }
        if (nxtDialogButton != DialogButton.NONE &&
                (nxtDialogButton == DialogButton.NO || nxtDialogButton == DialogButton.CANCEL)) {
            int textId = nxtDialogButton == DialogButton.NO ? R.string.noTxt : R.string.cancel;
            negativeButtonText = getString(textId);
        }
        if (isSuspended())
            return;
        // Defensive check
        if (TextUtils.isEmpty(positiveBtnText) && TextUtils.isEmpty(negativeButtonText)) {
            positiveBtnText = getString(R.string.ok);
        }
        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                requestCode, title == null ? getString(R.string.app_name) : title, msg, positiveBtnText,
                negativeButtonText, passedValue, false);
        try {
            dialogFragment.show(getSupportFragmentManager(), getScreenTag() + "#AlertDialog");
        } catch (IllegalStateException ex) {
            Crashlytics.logException(ex);
        }
    }

    public void showAlertDialog(String title,
                                String msg, String positiveBtnText,
                                String negativeBtnText, final int requestCode,
                                final Bundle passedValue) {
        showAlertDialog(title, msg, positiveBtnText, negativeBtnText, requestCode, passedValue,
                false);
    }

    public void showAlertDialog(String title, String msg, String positiveBtnText,
                                String negativeBtnText, final int requestCode,
                                final Bundle passedValue, boolean isCancellable) {
            if (isSuspended())
                return;
            ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                requestCode, title, msg, positiveBtnText,
                negativeBtnText, passedValue, isCancellable);
        try {
            dialogFragment.show(getSupportFragmentManager(), getScreenTag() + "#AlertDialog");
        } catch (IllegalStateException ex) {
            Crashlytics.logException(ex);
        }
    }

    public void showAlertDialog(String title,
                                String msg, @DialogButton.ButtonType int dialogButton,
                                @DialogButton.ButtonType int nxtDialogButton) {
        showAlertDialog(title, msg, dialogButton, nxtDialogButton, 0);
    }

    protected void onPositiveButtonClicked(int sourceName,
                                           Bundle valuePassed) {
        switch (sourceName) {
            case NavigationCodes.GO_TO_LOGIN:
                launchLogin(getPreviousScreenName(), valuePassed, true);
                break;
            case NavigationCodes.RC_PERMISSIONS_SETTINGS :
                startPermissionsSettingsActivity(this);
                break;
        }
    }

    public void launchViewBasketScreen() {
        Intent intent = new Intent(this, ShowCartActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    protected void onNegativeButtonClicked(int requestCode, Bundle data) {

    }

    public void triggerActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        if (resultCode == NavigationCodes.GO_TO_HOME) {
            goToHome();
        } else if (resultCode == NavigationCodes.GO_TO_QC) {
            setResult(NavigationCodes.GO_TO_QC);
            finish();
        } else if (resultCode == NavigationCodes.BASKET_CHANGED) {
            onBasketChanged(data);
            // Initiate Fragment callback (if-any) to sync cart
            super.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showToast(String txt) {
        Toast toast = Toast.makeText(this, txt, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    protected String getValueOrBlank(String val) {
        return !TextUtils.isEmpty(val) ? val : "";
    }

    public void goToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
    }

    private void setAreaPinCode(String areaName, AreaPinInfoDbHelper areaPinInfoDbHelper, EditText editTextPincode,
                                String cityName) {
        if (!TextUtils.isEmpty(areaName)) {
            String pinCode = areaPinInfoDbHelper.getAreaPin(areaName, cityName);
            editTextPincode.setText(pinCode);
        }
    }

    protected void setAdapterArea(final AutoCompleteTextView editTextArea, final AutoCompleteTextView editTextPincode,
                                  final String cityName) {
        final AreaPinInfoDbHelper areaPinInfoDbHelper = new AreaPinInfoDbHelper(getApplicationContext());
        ArrayList<String> areaPinArrayList = areaPinInfoDbHelper.getPinList(cityName);
        ArrayAdapter<String> pinAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, areaPinArrayList);
        editTextPincode.setThreshold(1);
        editTextPincode.setAdapter(pinAdapter);

        ArrayList<String> areaNameArrayList = areaPinInfoDbHelper.getAreaNameList(cityName);
        final ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_item, areaNameArrayList);
        editTextArea.setThreshold(1);
        editTextArea.setAdapter(areaAdapter);

        editTextPincode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                editTextArea.setText("");
                String pinCode = editTextPincode.getText().toString();
                ArrayList<String> areaNameArrayList = areaPinInfoDbHelper.getAreaName(pinCode, cityName);
                if (areaNameArrayList.size() > 1) {
                    areaAdapter.clear();
                    for (String areaName : areaNameArrayList)
                        areaAdapter.add(areaName);
                    areaAdapter.notifyDataSetChanged();
                    editTextArea.requestFocus();
                    editTextArea.showDropDown();
                } else if (areaNameArrayList.size() == 1) {
                    editTextArea.setText(areaNameArrayList.get(0));
                }
            }
        });


        editTextArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String areaName = editTextArea.getText().toString();
                setAreaPinCode(areaName, areaPinInfoDbHelper, editTextPincode, cityName);
            }
        });
    }

    @Override
    public boolean isSuspended() {
        return isActivitySuspended || isFinishing();
    }

    @Override
    public void setSuspended(boolean state) {
        isActivitySuspended = state;
    }

    @Override
    protected void onDestroy() {
        isActivitySuspended = true;
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    public abstract void onChangeFragment(AbstractFragment newFragment);

    public int randomColor() {
        Random random = new Random();
        String[] colorsArr = getResources().getStringArray(R.array.letterImageViewColors);
        return Color.parseColor(colorsArr[random.nextInt(colorsArr.length)]);
    }

    public abstract void onChangeTitle(String title);

    public void trackEventAppsFlyer(String eventName) {
        try {
            AppsFlyerLib.trackEvent(getApplicationContext(), eventName, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void trackEventAppsFlyer(String eventName, HashMap<String, Object> eventAttr) {
        try {
            AppsFlyerLib.trackEvent(getApplicationContext(), eventName, eventAttr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void trackEventAppsFlyer(String eventName, String valueToSum, Map<String, String> mapAttr) {
        try {
            AppsFlyerLib.sendTrackingWithEvent(getApplicationContext(), eventName, valueToSum);
            if (mapAttr != null && mapAttr.size() > 0) {
                Bundle bundleAttr = new Bundle();
                for (Map.Entry<String, String> entry : mapAttr.entrySet()) {
                    bundleAttr.putString(entry.getKey(), entry.getValue());
                }
                AppEventsLogger fbLogger = AppEventsLogger.newLogger(getApplicationContext());
                FacebookEventTrackWrapper.logAppEvent(fbLogger, eventName, Double.parseDouble(valueToSum), bundleAttr);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs,
                           String source, String sourceValue, boolean isCustomerValueIncrease,
                           boolean sendToFacebook) {
        trackEvent(eventName, eventAttribs, source, sourceValue, getPreviousScreenName(),
                isCustomerValueIncrease, sendToFacebook);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs) {
        trackEvent(eventName, eventAttribs, null, null, false);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source, String sourceValue) {
        trackEvent(eventName, eventAttribs, source, sourceValue, getPreviousScreenName(),
                false, false);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue, boolean isCustomerValueIncrease) {
        trackEvent(eventName, eventAttribs, source, sourceValue, getPreviousScreenName(),
                isCustomerValueIncrease, false);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue, String nc, boolean isCustomerValueIncrease,
                           boolean sendToFacebook) {
        if (eventAttribs != null && eventAttribs.containsKey(TrackEventkeys.NAVIGATION_CTX)) {
            // Someone has already set nc, so don't override it
            nc = null;
        }
        if (!TextUtils.isEmpty(nc)) {
            if (eventAttribs == null) {
                eventAttribs = new HashMap<>();
            }
            eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, nc);
        }
        if (eventAttribs != null && eventAttribs.containsKey(TrackEventkeys.NAVIGATION_CTX)) {
            nc = eventAttribs.get(TrackEventkeys.NAVIGATION_CTX);
            if (!TextUtils.isEmpty(nc)) {
                nc = nc.replace(" ", "-").toLowerCase(Locale.getDefault());
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, nc);
            }
        }
        Log.d(getScreenTag(), "Sending event = " + eventName +
                ", eventAttribs = " + eventAttribs + ", " +
                ", sourceValue = " + sourceValue + ", isCustomerValueIncrease = "
                + isCustomerValueIncrease);
        AuthParameters authParameters = AuthParameters.getInstance(getApplicationContext());
        if (authParameters.isMoEngageEnabled()) {
            JSONObject analyticsJsonObj = new JSONObject();
            try {
                if (eventAttribs != null) {
                    for (Map.Entry<String, String> entry : eventAttribs.entrySet()) {
                        analyticsJsonObj.put(entry.getKey(), entry.getValue());
                    }
                }
                MoEngageWrapper.trackEvent(moEHelper, eventName, analyticsJsonObj);
            } catch (JSONException e) {
                Log.e("Analytics", "Failed to send event = " + eventName + " to analytics");
            }
        }
        if (authParameters.isNewRelicEnabled()) {
            if (eventAttribs != null) {
                Map<String, Object> newRelicAttributes = new HashMap<>();
                try {
                    for (Map.Entry<String, String> entry : eventAttribs.entrySet()) {
                        newRelicAttributes.put(entry.getKey(), entry.getValue());
                    }
                    boolean isEventRecored = NewRelicWrapper.recordEvent(eventName, newRelicAttributes);
                    if (!isEventRecored)
                        Log.e("Analytics", "Failed to send event = " + eventName + " to NewRelic");

                } catch (Exception e) {
                    Log.e("Analytics", "Failed to send event = " + eventName + " to NewRelic");
                }
            }
        }

        if (authParameters.isLocalyticsEnabled()) {
            if (isCustomerValueIncrease)
                LocalyticsWrapper.tagEvent(eventName, eventAttribs, Constants.CUSTOMER_VALUE_INCREASE);
            else
                LocalyticsWrapper.tagEvent(eventName, eventAttribs);
        }

        if (sendToFacebook && authParameters.isFBLoggerEnabled()) {
            AppEventsLogger fbLogger = AppEventsLogger.newLogger(getApplicationContext());
            if (eventAttribs != null) {
                Bundle paramBundle = new Bundle();
                for (Map.Entry<String, String> eventAttrib : eventAttribs.entrySet())
                    paramBundle.putString(eventAttrib.getKey(), eventAttrib.getValue());
                FacebookEventTrackWrapper.logAppEvent(fbLogger, eventName.replace(".", "_"), paramBundle);
            } else {
                FacebookEventTrackWrapper.logAppEvent(fbLogger, eventName.replace(".", "_"));
            }
        }
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message) {
        showAlertDialog(title, message);
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, boolean finish) {
        if (finish) {
            showAlertDialogFinish(title, message);
        } else {
            showAlertDialog(title, message);
        }
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, int requestCode, Bundle valuePassed) {
        showAlertDialog(title, message, requestCode, valuePassed);
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, int resultCode) {
        showAlertDialogFinish(title, message, resultCode);
    }

    public abstract String getScreenTag();

    @Nullable
    @Override
    public String getPreviousScreenName() {
        return mNavigationContext;
    }

    @Override
    public void setPreviousScreenName(@Nullable String nc) {
        mNavigationContext = nc;
    }

    @Nullable
    @Override
    public String getCurrentScreenName() {
        return mNextScreenNavigationContext;
    }

    @Override
    public void setCurrentScreenName(@Nullable String nc) {
        mNextScreenNavigationContext = nc;
    }

    public void launchAppDeepLink(String uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.putExtra(Constants.HAS_PARENT, true);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } catch (ActivityNotFoundException e) {

        }
    }

    public boolean isBasketDirty() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getBoolean(Constants.IS_BASKET_COUNT_DIRTY, false);
    }

    public void removePendingCodes() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        editor.remove(Constants.FRAGMENT_CODE);
        editor.remove(Constants.DEEP_LINK);
        editor.apply();
    }

    protected void togglePasswordView(EditText passwordEditText, boolean show) {
        Drawable rightDrawable;
        if (!show) {
            rightDrawable = ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ic_visibility_white_18dp);
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
            logShowPasswordEnabled(TrackEventkeys.YES, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        } else {
            rightDrawable = ContextCompat.getDrawable(getApplicationContext(),
                    R.drawable.ic_visibility_off_white_18dp);
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            logShowPasswordEnabled(TrackEventkeys.NO, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        }

        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, String.valueOf(show));
        trackEvent(TrackingAware.SHOW_PASSWORD_ENABLED, eventAttribs);
        passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
    }

    private void logShowPasswordEnabled(String enabled, String navigationCtx) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, enabled);
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        trackEvent(TrackingAware.SHOW_PASSWORD_ENABLED, eventAttribs);
    }

    public void launchLogin(String navigationCtx, boolean shouldGoBackToHomePage) {
        launchLogin(navigationCtx, null, shouldGoBackToHomePage);
    }
    public void launchLogin(String navigationCtx, Bundle params, boolean shouldGoBackToHomePage) {
        launchLogin(navigationCtx, params, shouldGoBackToHomePage, NavigationCodes.GO_TO_HOME);
    }

    public void launchLogin(String navigationCtx, Bundle params, boolean shouldGoBackToHomePage,
                            int requestCode) {
        Intent loginIntent = new Intent(this, SignInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        loginIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        loginIntent.putExtra(Constants.GO_TO_HOME, shouldGoBackToHomePage);
        if (params != null) {
            if (params.containsKey(Constants.DEEPLINK_URL)) {
                loginIntent.putExtra(Constants.DEEP_LINK, params.getString(Constants.DEEPLINK_URL));
            } else if (params.containsKey(Constants.FRAGMENT_CODE)) {
                loginIntent.putExtra(Constants.FRAGMENT_CODE, params.getInt(Constants.FRAGMENT_CODE));
            }
        }
        startActivityForResult(loginIntent, requestCode);
    }

    protected void changeCity(City city) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CITY, city.getName())
                .putString(Constants.CITY_ID, String.valueOf(city.getId()))
                .putBoolean(Constants.HAS_USER_CHOSEN_CITY, true)
                .apply();

        DynamicPageDbHelper.clearAllAsync(getApplicationContext());
        AppDataDynamic.reset(this);
    }

    protected void launchRegistrationPage() {
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra(Constants.DEEP_LINK, getIntent().getStringExtra(Constants.DEEP_LINK));
        intent.putExtra(Constants.FRAGMENT_CODE, getIntent().getStringExtra(Constants.FRAGMENT_CODE));
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void launchProductList(ArrayList<NameValuePair> nameValuePairs, @Nullable String sectionName,
                                  @Nullable String sectionItemName) {
        if (nameValuePairs != null && nameValuePairs.size() > 0) {
            Intent intent = new Intent(this, ProductListActivity.class);
            intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
            String title = sectionItemName != null ? sectionItemName : null;
            if (!TextUtils.isEmpty(title)) {
                intent.putExtra(Constants.TITLE, title);
            }
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }

    @Override
    public void launchStoreList(String destinationSlug) {
        if (!TextUtils.isEmpty(destinationSlug)) {
            Intent intent = new Intent(this, BBSpecialityShopsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Constants.CATEGORY, destinationSlug);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }

    @Override
    public void launchShoppingList(ShoppingListName shoppingListName) {
        Intent intent = new Intent(this, ShoppingListSummaryActivity.class);
        intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onBasketChanged(@Nullable Intent data) {
        markBasketChanged(data);
    }

    @Override
    public void markBasketChanged(@Nullable Intent data) {
        setResult(NavigationCodes.BASKET_CHANGED, data);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (intent != null) {
            intent.putExtra(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
        }
        super.startActivityForResult(intent, requestCode);
    }

    public void showChangeCity(boolean isFirstTime, String nc, boolean reopenLandingPage) {
        Intent intent = new Intent(this, ChooseLocationActivity.class);
        intent.putExtra(TrackEventkeys.NAVIGATION_CTX, nc);
        intent.putExtra(Constants.IS_FIRST_TIME, isFirstTime);
        intent.putExtra(Constants.REOPEN_LANDING_PAGE, reopenLandingPage);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    /**************
     * code for Android M Support
     ******************/

//    public boolean handlePermission(String permission, int requestCode) {
//        return handlePermission(permission, null, requestCode);
//    }
    public boolean handlePermission(String permission, String rationale, int requestCode) {
        if (hasPermissionGranted(permission)) {
            return true;
        } else {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
                    && !TextUtils.isEmpty(rationale)) {
                Bundle bundle = new Bundle(2);
                bundle.putString(Constants.KEY_PERMISSION, permission);
                bundle.putInt(Constants.KEY_PERMISSION_RC, requestCode);
                showAlertDialog(getString(R.string.permission_rationale_dialog_title), rationale,
                        getString(R.string.action_settings),
                        getString(R.string.cancel),
                        NavigationCodes.RC_PERMISSIONS_SETTINGS,
                        bundle, true);
            } else {
                requestPermission(permission, requestCode);
            }
        }
        return false;
    }

    public boolean hasPermissionGranted(String permission) {
        int result = ContextCompat.checkSelfPermission(this, permission);
        return result == PackageManager.PERMISSION_GRANTED;
    }


    private void requestPermission(String permission, int requestCode) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.M)
    protected static void startPermissionsSettingsActivity(Activity activity){
        try {
            final Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + activity.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            activity.startActivityForResult(intent, NavigationCodes.RC_PERMISSIONS_SETTINGS);
        } catch (ActivityNotFoundException ex){
            Crashlytics.logException(ex);
        }
    }

}