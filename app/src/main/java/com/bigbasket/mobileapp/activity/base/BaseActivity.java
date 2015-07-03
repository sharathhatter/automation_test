package com.bigbasket.mobileapp.activity.base;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.bigbasket.mobileapp.activity.SplashActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SignupActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.ShowCartActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.LaunchProductListAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.FacebookEventTrackWrapper;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.facebook.appevents.AppEventsLogger;
import com.moe.pushlibrary.MoEHelper;
import com.moengage.addon.ubox.UnifiedInboxActivity;
import com.newrelic.agent.android.NewRelic;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public abstract class BaseActivity extends AppCompatActivity implements
        CancelableAware, ProgressIndicationAware, ActivityAware,
        ConnectivityAware, TrackingAware, ApiErrorAware,
        LaunchProductListAware {

    public static Typeface faceRupee;
    public static Typeface faceRobotoRegular, faceRobotoLight, faceRobotoMedium,
            faceRobotoBold;
    protected BigBasketMessageHandler handler;
    protected boolean isActivitySuspended;
    protected ProgressDialog progressDialog = null;
    protected MoEHelper moEHelper;
    private AppEventsLogger fbLogger;

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

    public static void hideKeyboard(BaseActivity context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

        NewRelic.setInteractionName(getCurrentActivity().getClass().getName());
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
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(cancelable);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    @Override
    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
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
        LocalyticsWrapper.onPause();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isActivitySuspended = false;
    }

    protected void onResume() {
        super.onResume();
        isActivitySuspended = false;
        if (isPendingGoToHome()) {
            goToHome(isPendingReloadApp());
            return;
        }
        MoEngageWrapper.onResume(moEHelper, getCurrentActivity());
        FacebookEventTrackWrapper.activateApp(getCurrentActivity());
    }

    public void launchMoEngageCommunicationHub() {
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        if (!authParameters.isAuthTokenEmpty()) {
            Intent communicationHunIntent = new Intent(this, UnifiedInboxActivity.class);
            startActivity(communicationHunIntent);
        } else {
            showToast(getString(R.string.loginToContinue));
            launchLogin(TrackEventkeys.NAVIGATION_CTX_LEFTNAV, FragmentCodes.START_COMMUNICATION_HUB);
        }

        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LEFTNAV);
        trackEvent(TrackingAware.COMMUNICATION_HUB_CLICKED, eventAttribs);
    }

    public void showAlertDialog(String title, String msg) {
        showAlertDialog(title, msg, null);
    }

    public void showAlertDialog(String title, String msg, String sourceName) {
        showAlertDialog(title, msg, sourceName, null);
    }

    public void showAlertDialog(String title, String msg, final String sourceName, final Object valuePassed) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity())
                .setTitle(title == null ? "BigBasket" : title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onPositiveButtonClicked(dialog, sourceName, valuePassed);
                    }
                })
                .setCancelable(false);
        if (isSuspended())
            return;
        builder.create().show();
    }

    public void showAlertDialogFinish(String title, String msg) {
        showAlertDialogFinish(title, msg, -1);
    }

    public void showAlertDialogFinish(String title, String msg, final int resultCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity())
                .setTitle(title == null ? "BigBasket" : title)
                .setMessage(msg)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (resultCode > -1) {
                            getCurrentActivity().setResult(resultCode);
                        }
                        dialog.dismiss();
                        getCurrentActivity().finish();
                    }
                })
                .setCancelable(false);
        if (isSuspended())
            return;
        builder.create().show();
    }

    public void showAlertDialog(String msg) {
        showAlertDialog(null, msg);
    }

    public void showAlertDialog(String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName) {
        showAlertDialog(title, msg, dialogButton, nxtDialogButton, sourceName, null, null);
    }

    public void showAlertDialog(String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName,
                                final String passedValue) {
        showAlertDialog(title, msg, dialogButton, nxtDialogButton, sourceName, passedValue, null);
    }

    public void showAlertDialog(String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName,
                                final Object passedValue, String positiveBtnText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
        builder.setTitle(title);
        builder.setMessage(msg);
        builder.setCancelable(false);
        if (dialogButton != null && nxtDialogButton != null) {
            if (dialogButton.equals(DialogButton.YES) || dialogButton.equals(DialogButton.OK)) {
                if (TextUtils.isEmpty(positiveBtnText)) {
                    int textId = dialogButton.equals(DialogButton.YES) ? R.string.yesTxt : R.string.ok;
                    positiveBtnText = getString(textId);
                }
                builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onPositiveButtonClicked(dialogInterface, sourceName, passedValue);
                    }
                });
            }
            if (nxtDialogButton.equals(DialogButton.NO) || nxtDialogButton.equals(DialogButton.CANCEL)) {
                int textId = nxtDialogButton.equals(DialogButton.NO) ? R.string.noTxt : R.string.cancel;
                builder.setNegativeButton(textId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onNegativeButtonClicked(dialogInterface, sourceName);
                    }
                });
            }
        }
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.setOnShowListener(new OnDialogShowListener());
        alertDialog.show();
    }

    public void showAlertDialog(String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton) {
        showAlertDialog(title, msg, dialogButton, nxtDialogButton, null);
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case NavigationCodes.GO_TO_LOGIN:
                    launchLogin(TrackEventkeys.NAVIGATION_CTX_DIALOG, valuePassed);
                    break;
            }
        }
    }

    public void launchViewBasketScreen() {
        Intent intent = new Intent(getCurrentActivity(), ShowCartActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    protected void onNegativeButtonClicked(DialogInterface dialogInterface, String sourceName) {

    }

    public void triggerActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        if (resultCode == NavigationCodes.GO_TO_HOME) {
            boolean reloadApp = (data != null && data.getBooleanExtra(Constants.RELOAD_APP, false));
            goToHome(reloadApp, resultCode);
        } else if (resultCode == NavigationCodes.GO_TO_QC) {
            setResult(NavigationCodes.GO_TO_QC);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showToast(String txt) {
        Toast toast = Toast.makeText(getCurrentActivity(), txt, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    public String getValueOrBlank(String val) {
        return !TextUtils.isEmpty(val) ? val : "";
    }

    public void goToHome(boolean reloadApp) {
        goToHome(reloadApp, NavigationCodes.GO_TO_HOME);
    }

    public void goToHome(boolean reloadApp, int resultCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            Intent intent;
            if (reloadApp) {
                intent = new Intent(getCurrentActivity(), SplashActivity.class);
                intent.putExtra(Constants.RELOAD_APP, true);
            } else {
                intent = new Intent(getCurrentActivity(), BBActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
            editor.putBoolean(Constants.IS_PENDING_GO_TO_HOME, true);
            editor.putBoolean(Constants.RELOAD_APP, reloadApp);
            editor.commit();

            Intent data = new Intent();
            data.putExtra(Constants.RELOAD_APP, reloadApp);
            setResult(resultCode, data);
            getCurrentActivity().finish();
        }
    }

    public void setAreaPinCode(String areaName, AreaPinInfoAdapter areaPinInfoAdapter, EditText editTextPincode) {
        if (!TextUtils.isEmpty(areaName)) {
            String pinCode = areaPinInfoAdapter.getAreaPin(areaName);
            editTextPincode.setText(pinCode);
        }
    }

    public void setAdapterArea(final AutoCompleteTextView editTextArea, final AutoCompleteTextView editTextPincode) {
        final AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(getCurrentActivity());
        ArrayList<String> areaPinArrayList = areaPinInfoAdapter.getPinList();
        ArrayAdapter<String> pinAdapter = new ArrayAdapter<>(getCurrentActivity(), android.R.layout.select_dialog_item, areaPinArrayList);
        editTextPincode.setThreshold(1);
        editTextPincode.setAdapter(pinAdapter);

        ArrayList<String> areaNameArrayList = areaPinInfoAdapter.getAreaNameList();
        final ArrayAdapter<String> areaAdapter = new ArrayAdapter<>(getCurrentActivity(), android.R.layout.select_dialog_item, areaNameArrayList);
        editTextArea.setThreshold(1);
        editTextArea.setAdapter(areaAdapter);

        editTextPincode.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                editTextArea.setText("");
                String pinCode = editTextPincode.getText().toString();
                ArrayList<String> areaNameArrayList = areaPinInfoAdapter.getAreaName(pinCode);
                if (areaNameArrayList != null && areaNameArrayList.size() > 1) {
                    areaAdapter.clear();
                    for (String areaName : areaNameArrayList)
                        areaAdapter.add(areaName);
                    areaAdapter.notifyDataSetChanged();
                    editTextArea.requestFocus();
                    editTextArea.showDropDown();
                } else if (areaNameArrayList != null && areaNameArrayList.size() == 1) {
                    editTextArea.setText(areaNameArrayList.get(0));
                }
            }
        });


        editTextArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String areaName = editTextArea.getText().toString();
                setAreaPinCode(areaName, areaPinInfoAdapter, editTextPincode);
            }
        });
    }

    public boolean getSystemAreaInfo() {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        String areaInfoCalledLast = prefer.getString(Constants.AREA_INFO_CALL_LAST, null);
        SharedPreferences.Editor editor = prefer.edit();
        try {
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            Date d1 = format.getCalendar().getTime();
            int days = 0;
            if (areaInfoCalledLast != null) {
                Date d2 = format.parse(areaInfoCalledLast);
                long diff = d1.getTime() - d2.getTime();
                days = (int) diff / (24 * 60 * 60 * 1000);
            }
            if (areaInfoCalledLast == null || days > 30) {
                String currentDate = format.format(d1);
                editor.putString("areaInfoCalledLast", currentDate);
                editor.commit();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean isSuspended() {
        return isActivitySuspended;
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

    public void reportFormInputFieldError(EditText editText, String errMsg) {
        UIUtil.reportFormInputFieldError(editText, errMsg);
    }

    protected void reportFormInputFieldError(AutoCompleteTextView autoCompleteTextView, String errMsg) {
        UIUtil.reportFormInputFieldError(autoCompleteTextView, errMsg);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs) {
        trackEvent(eventName, eventAttribs, null, null, false);
    }

    public void trackEventAppsFlyer(String eventName, String valueToSum, Map<String, String> mapAttr) {
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
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue,
                           boolean isCustomerValueIncrease) {
        Log.i(getCurrentActivity().getClass().getName(), "Sending event = " + eventName +
                " eventAttribs = " + eventAttribs);
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        if (authParameters.isMoEngageEnabled()) {
            JSONObject analyticsJsonObj = new JSONObject();
            try {
                if (eventAttribs != null) {
                    for (Map.Entry<String, String> entry : eventAttribs.entrySet()) {
                        analyticsJsonObj.put(entry.getKey(), entry.getValue());
                    }
                }
                if (!TextUtils.isEmpty(source)) {
                    analyticsJsonObj.put(Constants.SOURCE, source);
                }
                if (!TextUtils.isEmpty(sourceValue)) {
                    analyticsJsonObj.put(Constants.SOURCE_ID, sourceValue);
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

        if (authParameters.isFBLoggerEnabled()) {
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
    public void showApiErrorDialog(@Nullable String title, String message, String sourceName, Object valuePassed) {
        showAlertDialog(title, message, sourceName, valuePassed);
    }

    @Override
    public void showApiErrorDialog(@Nullable String title, String message, int resultCode) {
        showAlertDialogFinish(title, message, resultCode);
    }

    public abstract String getScreenTag();

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

    public boolean isPendingGoToHome() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        return preferences.getBoolean(Constants.IS_PENDING_GO_TO_HOME, false);
    }

    public void removePendingGoToHome() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.remove(Constants.IS_PENDING_GO_TO_HOME);
        editor.remove(Constants.RELOAD_APP);
        editor.commit();
    }

    public void removePendingCodes() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.remove(Constants.FRAGMENT_CODE);
        editor.remove(Constants.DEEP_LINK);
        editor.commit();
        removePendingGoToHome();
    }

    public boolean isPendingReloadApp() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        return preferences.getBoolean(Constants.RELOAD_APP, false);
    }

    public void togglePasswordView(EditText passwordEditText, boolean show) {
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

    public void launchLogin(String navigationCtx) {
        launchLogin(navigationCtx, null);
    }

    public void launchLogin(String navigationCtx, Object params) {
        Intent loginIntent = new Intent(this, SignInActivity.class);
        loginIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        if (params != null) {
            if (params instanceof Uri) {
                loginIntent.putExtra(Constants.DEEP_LINK, params.toString());
            } else {
                loginIntent.putExtra(Constants.FRAGMENT_CODE, params.toString());
            }
        }
        startActivityForResult(loginIntent, NavigationCodes.GO_TO_HOME);
    }

    public void launchRegistrationPage() {
        trackEvent(TrackingAware.NEW_USER_REGISTER_CLICKED, null);
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
            if (!TextUtils.isEmpty(sectionName) || !TextUtils.isEmpty(sectionItemName))
                intent.putExtra(TrackEventkeys.NAVIGATION_CTX, sectionName + "." + sectionItemName);
            String title = sectionItemName != null ? sectionItemName : null;
            if (!TextUtils.isEmpty(title)) {
                intent.putExtra(Constants.TITLE, title);
            }
            getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }
}