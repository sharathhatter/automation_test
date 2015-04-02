package com.bigbasket.mobileapp.activity.base;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SignupActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.AgeValidationActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.BasketValidationActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.CheckoutQCActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.COMarketPlaceAware;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.EmailAddressAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.task.UploadImageService;
import com.bigbasket.mobileapp.task.uiv3.LoadEmailAddressTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.FacebookEventTrackWrapper;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.util.analytics.MoEngageWrapper;
import com.demach.konotor.Konotor;
import com.facebook.AppEventsLogger;
import com.moe.pushlibrary.MoEHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public abstract class BaseActivity extends ActionBarActivity implements COMarketPlaceAware,
        COReserveQuantityCheckAware, CancelableAware, ProgressIndicationAware, ActivityAware,
        ConnectivityAware, TrackingAware, ApiErrorAware, EmailAddressAware {

    public static Typeface faceRupee;
    public static Typeface faceRobotoRegular, faceRobotoLight;
    protected BigBasketMessageHandler handler;
    protected boolean isActivitySuspended;
    protected COReserveQuantity coReserveQuantity;
    private MoEHelper moEHelper;
    private AppEventsLogger fbLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new BigBasketMessageHandler<>(getCurrentActivity());
        isActivitySuspended = false;

        faceRupee = FontHolder.getInstance(this).getFaceRupee();
        faceRobotoRegular = FontHolder.getInstance(this).getFaceRobotoRegular();
        faceRobotoLight = FontHolder.getInstance(this).getFaceRobotoLight();
        moEHelper = MoEngageWrapper.getMoHelperObj(getCurrentActivity());
        LocalyticsWrapper.integrate(this);
        fbLogger = AppEventsLogger.newLogger(getCurrentActivity());
    }

    @Override
    public boolean checkInternetConnection() {
        return DataUtil.isInternetAvailable(getCurrentActivity());
    }

    protected ProgressDialog progressDialog = null;

    @Override
    public void showProgressDialog(String msg) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
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
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        if (marketPlace.isRuleValidationError()) {
            Intent intent = new Intent(getCurrentActivity(), BasketValidationActivity.class);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } else if (marketPlace.isAgeCheckRequired() || marketPlace.isPharamaPrescriptionNeeded()
                || marketPlace.hasTermsAndCond()) {
            Intent intent = new Intent(getCurrentActivity(), AgeValidationActivity.class);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } else {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
            String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
            new COReserveQuantityCheckTask<>(getCurrentActivity(), pharmaPrescriptionId).startTask();
        }
    }


    @Override
    public COReserveQuantity getCOReserveQuantity() {
        return coReserveQuantity;
    }

    @Override
    public void setCOReserveQuantity(COReserveQuantity coReserveQuantity) {
        this.coReserveQuantity = coReserveQuantity;
    }

    @Override
    public void onCOReserveQuantityCheck() {
        Intent intent = new Intent(getCurrentActivity(), CheckoutQCActivity.class);
        intent.putExtra(Constants.CO_RESERVE_QTY_DATA, coReserveQuantity);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

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
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivitySuspended = true;
        MoEngageWrapper.onPause(moEHelper, getCurrentActivity());
        LocalyticsWrapper.onPause();
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
        initializeKonotor();
        MoEngageWrapper.onResume(moEHelper, getCurrentActivity());
        prescriptionImageUploadHandler();
    }

    private void prescriptionImageUploadHandler() {
        PrescriptionImageAdapter prescriptionImageAdapter = new PrescriptionImageAdapter(getCurrentActivity());
        if (!prescriptionImageAdapter.exists()) {
            stopService(new Intent(this, UploadImageService.class));
        } else if (prescriptionImageAdapter.hasData() && !isMyServiceRunning(UploadImageService.class)) {
            startService(new Intent(getCurrentActivity(), UploadImageService.class));
        }
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) return true;
        }
        return false;
    }

    public void launchKonotor() {
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        if (!authParameters.isAuthTokenEmpty()) {
            Konotor.getInstance(getApplicationContext()).launchFeedbackScreen(this);
        } else {
            showAlertDialog(null, getString(R.string.login_required),
                    NavigationCodes.GO_TO_LOGIN);
        }

        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_TOPNAV);
        trackEvent(TrackingAware.COMMUNICATION_HUB_CLICKED, eventAttribs);
    }

    protected void initializeKonotor() {
        AuthParameters authParameters = AuthParameters.getInstance(this);
        if (!authParameters.isAuthTokenEmpty()) {
            Konotor.getInstance(getApplicationContext())
                    .withLaunchMainActivityOnFinish(true)
                    .withUserName(authParameters.getMemberFullName())
                    .withIdentifier(authParameters.getMid())
                    .withUserEmail(authParameters.getMemberEmail())
                    .withWelcomeMessage(this.getResources().getString(R.string.konotorWelcomeMessage))
                    .withNoAudioRecording(true)
                    .withFeedbackScreenTitle(getResources().getString(R.string.bbCommHub))
                    .withLinking("bigbasket://[a-z0-9A-Z\\?\\&\\=]+", "bigbasket")
                    .init(Constants.KONOTOR_APP_ID, Constants.KONOTOR_APP_KEY);
        } else {
            Konotor.getInstance(getApplicationContext())
                    .withLaunchMainActivityOnFinish(true)
                    .withWelcomeMessage(this.getResources().getString(R.string.konotorWelcomeMessage))
                    .withNoAudioRecording(true)
                    .withFeedbackScreenTitle(getResources().getString(R.string.bbCommHub))
                    .withLinking("bigbasket://[a-z0-9A-Z\\?\\&\\=]+", "bigbasket")
                    .init(Constants.KONOTOR_APP_ID, Constants.KONOTOR_APP_KEY);
        }
    }

    public void updateKonotor() {
        AuthParameters authParameters = AuthParameters.getInstance(this);
        if (!authParameters.isAuthTokenEmpty()) {
            Konotor.getInstance(getApplicationContext())
                    .withUserName(authParameters.getMemberFullName())
                    .withIdentifier(authParameters.getMid())
                    .withUserEmail(authParameters.getMemberEmail())
                    .update();
        }
    }

    public void showAlertDialog(String title, String msg) {
        showAlertDialog(title, msg, null);
    }

    public void showAlertDialog(String title, String msg, String sourceName) {
        showAlertDialog(title, msg, sourceName, null);
    }

    public void showAlertDialog(String title, String msg, final String sourceName, final Object valuePassed) {
        MaterialDialog.Builder builder = UIUtil.getMaterialDialogBuilder(getCurrentActivity())
                .title(title == null ? "BigBasket" : title)
                .content(msg)
                .positiveText(R.string.ok)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        onPositiveButtonClicked(dialog, sourceName, valuePassed);
                    }
                });
        if (isSuspended())
            return;
        builder.show();
    }

    public void showAlertDialogFinish(String title, String msg) {
        showAlertDialogFinish(title, msg, -1);
    }

    public void showAlertDialogFinish(String title, String msg, final int resultCode) {
        MaterialDialog.Builder builder = UIUtil.getMaterialDialogBuilder(getCurrentActivity())
                .title(title == null ? "BigBasket" : title)
                .content(msg)
                .cancelable(false)
                .positiveText(R.string.ok)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        if (resultCode > -1) {
                            getCurrentActivity().setResult(resultCode);
                        }
                        getCurrentActivity().finish();
                    }
                });
        if (isSuspended())
            return;
        builder.show();
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
        MaterialDialog.Builder builder = UIUtil.getMaterialDialogBuilder(getCurrentActivity())
                .title(title)
                .content(msg);
        if (dialogButton != null && nxtDialogButton != null) {
            if (dialogButton.equals(DialogButton.YES) || dialogButton.equals(DialogButton.OK)) {
                if (TextUtils.isEmpty(positiveBtnText)) {
                    int textId = dialogButton.equals(DialogButton.YES) ? R.string.yesTxt : R.string.ok;
                    positiveBtnText = getString(textId);
                }
                builder.positiveText(positiveBtnText);
            }
            if (nxtDialogButton.equals(DialogButton.NO) || nxtDialogButton.equals(DialogButton.CANCEL)) {
                int textId = nxtDialogButton.equals(DialogButton.NO) ? R.string.noTxt : R.string.cancel;
                builder.negativeText(textId);
            }
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    onPositiveButtonClicked(dialog, sourceName, passedValue);
                }

                @Override
                public void onNegative(MaterialDialog dialog) {
                    onNegativeButtonClicked(dialog, sourceName);
                }
            });
        }
        if (isSuspended())
            return;
        builder.show();
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
                    Intent loginIntent = new Intent(this, SignInActivity.class);
                    if (valuePassed != null && valuePassed instanceof Uri) {
                        loginIntent.putExtra(Constants.DEEP_LINK, valuePassed.toString());
                    }
                    startActivityForResult(loginIntent, NavigationCodes.GO_TO_HOME);
                    break;
            }
        }
    }

    protected void onNegativeButtonClicked(DialogInterface dialogInterface, String sourceName) {

    }

    public void triggerActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        if (resultCode == NavigationCodes.GO_TO_HOME || resultCode == NavigationCodes.CITY_CHANGED) {
            boolean reloadApp = data != null && data.getBooleanExtra(Constants.RELOAD_APP, false);
            goToHome(reloadApp, resultCode);
        } else if (resultCode == NavigationCodes.GO_TO_SLOT_SELECTION) {
            setResult(NavigationCodes.GO_TO_SLOT_SELECTION);
            finish();
        } else if (resultCode == NavigationCodes.GO_TO_QC) {
            setResult(NavigationCodes.GO_TO_QC);
            finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showToast(String txt) {
        Toast toast = Toast.makeText(getCurrentActivity(), txt, Toast.LENGTH_SHORT);
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
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putBoolean(Constants.IS_PENDING_GO_TO_HOME, true);
        editor.putBoolean(Constants.RELOAD_APP, reloadApp);
        editor.commit();

        Intent data = new Intent();
        data.putExtra(Constants.RELOAD_APP, reloadApp);
        setResult(resultCode, data);
        getCurrentActivity().finish();
    }

    public String getDecimalAmount(Double amount) {
        int amountInt = amount.intValue();
        if (amountInt == amount)
            return String.valueOf(amountInt);
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);
        return (nf.format(amount).equals("0.00") || nf.format(amount).equals("0.0")) ? "0" : nf.format(amount);
    }

    public static void showKeyboard(final EditText editText) {
        (new Handler()).postDelayed(new Runnable() {

            public void run() {
                MotionEvent motionActionDown = MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0);
                MotionEvent motionActionUp = MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
                if (motionActionDown == null || motionActionUp == null) return;
                editText.dispatchTouchEvent(motionActionDown);
                editText.dispatchTouchEvent(motionActionUp);

            }
        }, 100);
    }

    public static void hideKeyboard(BaseActivity context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    public void setAreaPinCode(String areaName, AreaPinInfoAdapter areaPinInfoAdapter, EditText editTextPincode) {
        if (areaName != null) {
            String pinCode = areaPinInfoAdapter.getAreaPin(areaName);
            editTextPincode.setText(pinCode);
        }

    }

    public void setAdapterArea(final AutoCompleteTextView editTextArea, final EditText editTextPincode) {
        final AreaPinInfoAdapter areaPinInfoAdapter = new AreaPinInfoAdapter(getCurrentActivity());
        ArrayList<String> areaNameArrayList = areaPinInfoAdapter.getAreaNameList();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getCurrentActivity(), android.R.layout.select_dialog_item, areaNameArrayList);
        editTextArea.setThreshold(1);
        editTextArea.setAdapter(adapter);
        editTextArea.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String areaName = editTextArea.getText().toString(); //arg0.getAdapter().getItem(0)
                setAreaPinCode(areaName, areaPinInfoAdapter, editTextPincode);

            }
        });
    }

    public boolean getSystemAreaInfo() {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        String areaInfoCalledLast = prefer.getString("areaInfoCalledLast", null);
        SharedPreferences.Editor editor = prefer.edit();
        try {
            DateFormat format = new SimpleDateFormat("MM/dd/yyyy");
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

    public void removePharmaPrescriptionId() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.remove(Constants.PHARMA_PRESCRIPTION_ID);
        editor.commit();
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


    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source,
                           String sourceValue,
                           boolean isCustomerValueIncrease) {
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
                FacebookEventTrackWrapper.logAppEvent(fbLogger, eventName, paramBundle);
            } else {
                FacebookEventTrackWrapper.logAppEvent(fbLogger, eventName);
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
            Log.e("HomeFragment", "No target found for the pending deep-link");
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

    public boolean isPendingReloadApp() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        return preferences.getBoolean(Constants.RELOAD_APP, false);
    }

    protected void populateAutoComplete(AutoCompleteTextView emailView) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, new LoadEmailAddressTask<>(getCurrentActivity(), emailView).new ContactsLoader());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            // Use AccountManager (API 8+)
            new LoadEmailAddressTask<>(getCurrentActivity(), emailView).new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    public void addEmailsToAutoComplete(List<String> emailAddressCollection, AutoCompleteTextView emailView) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ?
                android.R.layout.simple_dropdown_item_1line : android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getCurrentActivity(), layout, emailAddressCollection);

        emailView.setAdapter(adapter);
    }

    public void togglePasswordView(EditText passwordEditText, boolean show) {
        if (!show) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            logShowPasswordEnabled(TrackEventkeys.YES, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            logShowPasswordEnabled(TrackEventkeys.NO, TrackEventkeys.NAVIGATION_CTX_LOGIN_PAGE);
        }
    }

    private void logShowPasswordEnabled(String enabled, String navigationCtx) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(TrackEventkeys.ENABLED, enabled);
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        trackEvent(TrackingAware.SHOW_PASSWORD_ENABLED, eventAttribs);
    }

    public void setTermsAndCondition(TextView txtVw, String prefix, String tncText, String separator, String privacyPolicyTxt) {
        txtVw.setTypeface(faceRobotoRegular);
        SpannableString spannableString = new SpannableString(prefix + tncText + separator + privacyPolicyTxt);
        spannableString.setSpan(new UnderlineSpan(), prefix.length(), prefix.length() + tncText.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new TncClickListener(), prefix.length(), prefix.length() + tncText.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uiv3_secondary_text_color)), prefix.length(), prefix.length() + tncText.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        spannableString.setSpan(new UnderlineSpan(), prefix.length() + tncText.length() + separator.length(),
                prefix.length() + tncText.length() + separator.length() + privacyPolicyTxt.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new PrivacyPolicyClickListener(), prefix.length() + tncText.length() + separator.length(),
                prefix.length() + tncText.length() + separator.length() + privacyPolicyTxt.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.uiv3_secondary_text_color)), prefix.length() + tncText.length() + separator.length(),
                prefix.length() + tncText.length() + separator.length() + privacyPolicyTxt.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtVw.setText(spannableString);
        txtVw.setClickable(true);
        txtVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent flatPageWebviewActivity = new Intent(getCurrentActivity(), FlatPageWebViewActivity.class);
                flatPageWebviewActivity.putExtra(Constants.WEBVIEW_URL, MobileApiUrl.DOMAIN + "terms-and-conditions/");
                flatPageWebviewActivity.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.termsAndCondHeading));
                startActivityForResult(flatPageWebviewActivity, NavigationCodes.GO_TO_HOME);
            }
        });
    }

    private class TncClickListener extends ClickableSpan {

        @Override
        public void onClick(View widget) {
            Intent flatPageWebviewActivity = new Intent(getCurrentActivity(), FlatPageWebViewActivity.class);
            flatPageWebviewActivity.putExtra(Constants.WEBVIEW_URL, MobileApiUrl.DOMAIN + "terms-and-conditions/");
            flatPageWebviewActivity.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.termsAndCondHeading));
            startActivityForResult(flatPageWebviewActivity, NavigationCodes.GO_TO_HOME);
        }
    }

    private class PrivacyPolicyClickListener extends ClickableSpan {

        @Override
        public void onClick(View widget) {
            Intent flatPageWebviewActivity = new Intent(getCurrentActivity(), FlatPageWebViewActivity.class);
            flatPageWebviewActivity.putExtra(Constants.WEBVIEW_URL, MobileApiUrl.DOMAIN + "privacy-policy/");
            flatPageWebviewActivity.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.privacyPolicy));
            startActivityForResult(flatPageWebviewActivity, NavigationCodes.GO_TO_HOME);
        }
    }

    public void launchLogin(String navigationCtx) {
        Intent intent = new Intent(this, SignInActivity.class);
        intent.putExtra(TrackEventkeys.NAVIGATION_CTX, navigationCtx);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    public void launchRegistrationPage() {
        trackEvent(TrackingAware.NEW_USER_REGISTER_CLICKED, null);
        Intent intent = new Intent(this, SignupActivity.class);
        intent.putExtra(Constants.DEEP_LINK, getIntent().getStringExtra(Constants.DEEP_LINK));
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }
}