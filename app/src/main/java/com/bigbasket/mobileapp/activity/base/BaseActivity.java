package com.bigbasket.mobileapp.activity.base;

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
import com.bigbasket.mobileapp.activity.HomeActivity;
import com.bigbasket.mobileapp.activity.TutorialActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.BBUnifiedInboxActivity;
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
import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.moe.pushlibrary.MoEHelper;
import com.newrelic.agent.android.NewRelic;

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

    public static Typeface faceRupee;
    protected static Typeface faceRobotoRegular, faceRobotoLight, faceRobotoMedium,
            faceRobotoBold;
    protected BigBasketMessageHandler handler;
    protected MoEHelper moEHelper;
    private boolean isActivitySuspended;
    private ProgressDialog progressDialog = null;
    private AppEventsLogger fbLogger;
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
        handler = new BigBasketMessageHandler<>(getCurrentActivity());
        isActivitySuspended = false;

        faceRupee = FontHolder.getInstance(this).getFaceRupee();
        faceRobotoRegular = FontHolder.getInstance(this).getFaceRobotoRegular();
        faceRobotoMedium = FontHolder.getInstance(this).getFaceRobotoMedium();
        faceRobotoBold = FontHolder.getInstance(this).getFaceRobotoBold();
        faceRobotoLight = FontHolder.getInstance(this).getFaceRobotoLight();
        moEHelper = MoEngageWrapper.getMoHelperObj(getCurrentActivity());
        fbLogger = AppEventsLogger.newLogger(getApplicationContext());
        mNavigationContext = getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX);
        NewRelic.setInteractionName(getCurrentActivity().getClass().getSimpleName());
    }

    public MoEHelper getMoEHelper() {
        return moEHelper;
    }

    @Override
    public boolean checkInternetConnection() {
        return DataUtil.isInternetAvailable(getCurrentActivity());
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
                ft.commit();
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

    public abstract BaseActivity getCurrentActivity();

    @Override
    protected void onStart() {
        super.onStart();
        isActivitySuspended = false;
        MoEngageWrapper.onStart(moEHelper, getCurrentActivity());
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivitySuspended = true;
        MoEngageWrapper.onStop(moEHelper, getCurrentActivity());
        FacebookEventTrackWrapper.deactivateApp(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivitySuspended = true;
        MoEngageWrapper.onPause(moEHelper, getCurrentActivity());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LocalyticsWrapper.onPause();
        }
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
        if (isPendingGoToHome()) {
            goToHome();
            return;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            LocalyticsWrapper.onResume();
        }

        MoEngageWrapper.onResume(moEHelper, getCurrentActivity());
        FacebookEventTrackWrapper.activateApp(getCurrentActivity());
    }

    public void launchMoEngageCommunicationHub() {
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        if (!authParameters.isAuthTokenEmpty()) {
            Intent communicationHunIntent = new Intent(this, BBUnifiedInboxActivity.class);
            startActivity(communicationHunIntent);
        } else {
            showToast(getString(R.string.loginToContinue));
            Bundle bundle = new Bundle(1);
            bundle.putInt(Constants.FRAGMENT_CODE, FragmentCodes.START_COMMUNICATION_HUB);
            launchLogin(getCurrentNavigationContext(), bundle, true);
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
    public final void onDialogCancelled(int reqCode) {

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

        if (isSuspended())
            return;
        ConfirmationDialogFragment dialogFragment = ConfirmationDialogFragment.newInstance(
                requestCode, title, msg, positiveBtnText,
                negativeBtnText, passedValue, false);
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
                launchLogin(getCurrentNavigationContext(), valuePassed, true);
                break;
        }

    }

    public void launchViewBasketScreen() {
        Intent intent = new Intent(getCurrentActivity(), ShowCartActivity.class);
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
        } else if (requestCode == NavigationCodes.TUTORIAL_SEEN) {
            handleTutorialResponse(resultCode);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showToast(String txt) {
        Toast toast = Toast.makeText(getCurrentActivity(), txt, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    protected String getValueOrBlank(String val) {
        return !TextUtils.isEmpty(val) ? val : "";
    }

    public void goToHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent intent = new Intent(getCurrentActivity(), HomeActivity.class);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
            editor.putBoolean(Constants.IS_PENDING_GO_TO_HOME, true);
            editor.apply();

            Intent data = new Intent();
            setResult(NavigationCodes.GO_TO_HOME, data);
            getCurrentActivity().finish();
        }
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
        final AreaPinInfoDbHelper areaPinInfoDbHelper = new AreaPinInfoDbHelper(getCurrentActivity());
        ArrayList<String> areaPinArrayList = areaPinInfoDbHelper.getPinList(cityName);
        ArrayAdapter<String> pinAdapter = new ArrayAdapter<>(getCurrentActivity(),
                android.R.layout.select_dialog_item, areaPinArrayList);
        editTextPincode.setThreshold(1);
        editTextPincode.setAdapter(pinAdapter);

        ArrayList<String> areaNameArrayList = areaPinInfoDbHelper.getAreaNameList(cityName);
        final ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(getCurrentActivity(),
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
        trackEvent(eventName, eventAttribs, source, sourceValue, getCurrentNavigationContext(),
                isCustomerValueIncrease, sendToFacebook);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs) {
        trackEvent(eventName, eventAttribs, null, null, false);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source, String sourceValue) {
        trackEvent(eventName, eventAttribs, source, sourceValue, getCurrentNavigationContext(),
                false, false);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue, boolean isCustomerValueIncrease) {
        trackEvent(eventName, eventAttribs, source, sourceValue, getCurrentNavigationContext(),
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
        Log.d(getCurrentActivity().getClass().getName(), "Sending event = " + eventName +
                ", eventAttribs = " + eventAttribs + ", " +
                ", sourceValue = " + sourceValue + ", isCustomerValueIncrease = "
                + isCustomerValueIncrease);
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
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
        if (authParameters.isLocalyticsEnabled()) {
            if (isCustomerValueIncrease)
                LocalyticsWrapper.tagEvent(eventName, eventAttribs, Constants.CUSTOMER_VALUE_INCREASE);
            else
                LocalyticsWrapper.tagEvent(eventName, eventAttribs);
        }

        if (sendToFacebook && authParameters.isFBLoggerEnabled()) {
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
    public String getCurrentNavigationContext() {
        return mNavigationContext;
    }

    @Override
    public void setCurrentNavigationContext(@Nullable String nc) {
        mNavigationContext = nc;
    }

    @Nullable
    @Override
    public String getNextScreenNavigationContext() {
        return mNextScreenNavigationContext;
    }

    @Override
    public void setNextScreenNavigationContext(@Nullable String nc) {
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
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        return preferences.getBoolean(Constants.IS_BASKET_COUNT_DIRTY, false);
    }

    protected boolean isPendingGoToHome() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        return preferences.getBoolean(Constants.IS_PENDING_GO_TO_HOME, false);
    }

    protected void removePendingGoToHome() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.remove(Constants.IS_PENDING_GO_TO_HOME);
        editor.apply();
    }

    public void removePendingCodes() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.remove(Constants.FRAGMENT_CODE);
        editor.remove(Constants.DEEP_LINK);
        editor.apply();
        removePendingGoToHome();
    }

    protected void togglePasswordView(EditText passwordEditText, boolean show) {
        Drawable rightDrawable;
        if (!show) {
            rightDrawable = ContextCompat.getDrawable(getCurrentActivity(),
                    R.drawable.ic_visibility_white_18dp);
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, rightDrawable, null);
            logShowPasswordEnabled(TrackEventkeys.YES, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        } else {
            rightDrawable = ContextCompat.getDrawable(getCurrentActivity(),
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
        Intent loginIntent = new Intent(this, SignInActivity.class);
        loginIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        loginIntent.putExtra(Constants.GO_TO_HOME, shouldGoBackToHomePage);
        if (params != null) {
            if (params.containsKey(Constants.DEEPLINK_URL)) {
                loginIntent.putExtra(Constants.DEEP_LINK, params.getString(Constants.DEEPLINK_URL));
            } else if (params.containsKey(Constants.FRAGMENT_CODE)) {
                loginIntent.putExtra(Constants.FRAGMENT_CODE, params.getInt(Constants.FRAGMENT_CODE));
            }
        }
        startActivityForResult(loginIntent, NavigationCodes.GO_TO_HOME);
    }

    protected void changeCity(City city) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CITY, city.getName())
                .putString(Constants.CITY_ID, String.valueOf(city.getId()))
                .putBoolean(Constants.HAS_USER_CHOSEN_CITY, true)
                .apply();

        DynamicPageDbHelper.clearAllAsync(getCurrentActivity());
        AppDataDynamic.reset(this);
    }

    protected void launchRegistrationPage() {
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra(Constants.DEEP_LINK, getIntent().getStringExtra(Constants.DEEP_LINK));
        intent.putExtra(Constants.FRAGMENT_CODE, getIntent().getStringExtra(Constants.FRAGMENT_CODE));
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void launchProductList(ArrayList<NameValuePair> nameValuePairs, @Nullable String sectionName,
                                  @Nullable String sectionItemName) {
        if (nameValuePairs != null && nameValuePairs.size() > 0) {
            Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
            intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
            String title = sectionItemName != null ? sectionItemName : null;
            if (!TextUtils.isEmpty(title)) {
                intent.putExtra(Constants.TITLE, title);
            }
            getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }

    @Override
    public void launchStoreList(String destinationSlug) {
        if (!TextUtils.isEmpty(destinationSlug)) {
            Intent intent = new Intent(getCurrentActivity(), BBSpecialityShopsActivity.class);
            intent.putExtra(Constants.CATEGORY, destinationSlug);
            getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }

    @Override
    public void launchShoppingList(ShoppingListName shoppingListName) {
        Intent intent = new Intent(getCurrentActivity(), ShoppingListSummaryActivity.class);
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
            intent.putExtra(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
        }
        super.startActivityForResult(intent, requestCode);
    }

    protected void launchTutorial(int resultCode) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isTutorialShown = preferences.getBoolean(Constants.TUTORIAL_SEEN, false);
        if (isTutorialShown) {
            handleTutorialResponse(resultCode);
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(Constants.TUTORIAL_SEEN, true);
            editor.apply();
            Intent intent = new Intent(this, TutorialActivity.class);
            intent.putExtra(Constants.ACTION_TAB_TAG, resultCode);
            startActivityForResult(intent, NavigationCodes.TUTORIAL_SEEN);
        }
    }

    protected void handleTutorialResponse(int resultCode) {
        switch (resultCode) {
            case NavigationCodes.LAUNCH_LOGIN:
                launchLogin(TrackEventkeys.NAVIGATION_CTX_LANDING_PAGE, true);
                break;
            case NavigationCodes.LAUNCH_CITY:
                showChangeCity(true, TrackEventkeys.NAVIGATION_CTX_LANDING_PAGE, false);
                break;
            case NavigationCodes.LAUNCH_SIGNUP:
                launchRegistrationPage();
                break;
        }
    }

    public void showChangeCity(boolean isFirstTime, String nc, boolean reopenLandingPage) {
        Intent intent = new Intent(getCurrentActivity(), ChooseLocationActivity.class);
        intent.putExtra(TrackEventkeys.NAVIGATION_CTX, nc);
        intent.putExtra(Constants.IS_FIRST_TIME, isFirstTime);
        intent.putExtra(Constants.REOPEN_LANDING_PAGE, reopenLandingPage);
        getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    /**************
     * code for Android M Support
     ******************/

    public boolean handlePermission(String permission, int requestCode) {
        if (hasPermissionGranted(permission)) {
            return true;
        } else {
            requestPermission(permission, requestCode);
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
}