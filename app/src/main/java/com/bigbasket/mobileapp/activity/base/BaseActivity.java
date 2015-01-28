package com.bigbasket.mobileapp.activity.base;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
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

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.CheckoutQCActivity;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.handler.LocalyticsHandler;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.COMarketPlaceAware;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.CitySpecificAppSettings;
import com.bigbasket.mobileapp.model.SectionManager;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.task.UploadImageService;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;
import com.demach.konotor.Konotor;
import com.google.gson.Gson;
import com.localytics.android.LocalyticsAmpSession;
import com.moe.pushlibrary.MoEHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public abstract class BaseActivity extends ActionBarActivity implements COMarketPlaceAware,
        COReserveQuantityCheckAware, CancelableAware, ProgressIndicationAware, ActivityAware,
        ConnectivityAware, TrackingAware, ApiErrorAware {

    public static Typeface faceRupee;
    public static Typeface faceRobotoRegular;
    protected BigBasketMessageHandler handler;
    protected boolean isActivitySuspended;
    protected COReserveQuantity coReserveQuantity;
    private MoEHelper moEHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new BigBasketMessageHandler<>(getCurrentActivity());
        isActivitySuspended = false;

        faceRupee = Typeface.createFromAsset(getAssets(), "Rupee.ttf");
        faceRobotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
        moEHelper = new MoEHelper(getCurrentActivity());
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
        BigBasketMessageHandler bigBasketMessageHandler = new BigBasketMessageHandler<>(getCurrentActivity(), marketPlace);
        if (marketPlace.isRuleValidationError()) {
            bigBasketMessageHandler.sendEmptyMessage(NavigationCodes.GO_MARKET_PLACE);
        } else if (marketPlace.isAgeCheckRequired() || marketPlace.isPharamaPrescriptionNeeded()) {
            bigBasketMessageHandler.sendEmptyMessage(NavigationCodes.GO_AGE_VALIDATION);
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
        //intent.putExtra(Constants.QC_LEN, coReserveQuantity.getQc_len());  //todo send complete coReserveQuantity
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActivitySuspended = false;
        moEHelper.onStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActivitySuspended = true;
        moEHelper.onStop(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivitySuspended = true;
        moEHelper.onPause(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        isActivitySuspended = false;
    }

    protected void onResume() {
        super.onResume();

        PrescriptionImageAdapter prescriptionImageAdapter = new PrescriptionImageAdapter(getCurrentActivity());
        if (!prescriptionImageAdapter.exists()) {
            stopService(new Intent(this, UploadImageService.class));
        }
        isActivitySuspended = false;

        initializeKonotor();
        moEHelper.onResume(this);
    }

    public void launchKonotor() {
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        if (!authParameters.isAuthTokenEmpty()) {
            Konotor.getInstance(getApplicationContext()).launchFeedbackScreen(this);
        } else {
            showAlertDialog(null, getString(R.string.login_required),
                    NavigationCodes.GO_TO_LOGIN);
        }
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
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
        builder.setTitle(title == null ? "BigBasket" : title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onPositiveButtonClicked(dialog, which, sourceName, valuePassed);
            }
        });
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.show();
    }

    public void showAlertDialogFinish(String title, String msg) {
        showAlertDialogFinish(title, msg, -1);
    }

    public void showAlertDialogFinish(String title, String msg, final int resultCode) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity());
        builder.setTitle(title == null ? "BigBasket" : title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getCurrentActivity() != null) {
                    if (resultCode > -1) {
                        getCurrentActivity().setResult(resultCode);
                    }
                    getCurrentActivity().finish();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.show();
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
        if (dialogButton != null && nxtDialogButton != null) {
            if (dialogButton.equals(DialogButton.YES) || dialogButton.equals(DialogButton.OK)) {
                if (TextUtils.isEmpty(positiveBtnText)) {
                    int textId = dialogButton.equals(DialogButton.YES) ? R.string.yesTxt : R.string.ok;
                    positiveBtnText = getString(textId);
                }
                builder.setPositiveButton(positiveBtnText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onPositiveButtonClicked(dialogInterface, id, sourceName, passedValue);
                    }
                });
            }
            if (nxtDialogButton.equals(DialogButton.NO) || nxtDialogButton.equals(DialogButton.CANCEL)) {
                int textId = nxtDialogButton.equals(DialogButton.NO) ? R.string.noTxt : R.string.cancel;
                builder.setNegativeButton(textId, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onNegativeButtonClicked(dialogInterface, id, sourceName);
                    }
                });
            }
        }
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.show();
    }

    public void showAlertDialog(String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton) {
        showAlertDialog(title, msg, dialogButton, nxtDialogButton, null);
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case NavigationCodes.GO_TO_LOGIN:
                    Intent loginIntent = new Intent(this, SignInActivity.class);
                    startActivityForResult(loginIntent, NavigationCodes.GO_TO_HOME);
                    break;
            }
        }
    }

    protected void onNegativeButtonClicked(DialogInterface dialogInterface, int id, String sourceName) {

    }

    protected void removeViaInvoiceFlag() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(Constants.VIA_INVOICE);
        editor.commit();
    }

    public void triggerActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        if (resultCode == NavigationCodes.GO_TO_HOME) {
            setResult(NavigationCodes.GO_TO_HOME);
            finish();
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


    public void goToHome() {
        setResult(NavigationCodes.GO_TO_HOME);
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

    public void onLogoutRequested() {
        if (isSocialLogin()) {
            Intent intent = new Intent(getCurrentActivity(), SignInActivity.class);
            intent.putExtra(Constants.SOCIAL_LOGOUT, true);
            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        } else {
            trackEvent(TrackingAware.MY_ACCOUNT_LOGOUT, null);
            doLogout();
        }
    }

    public boolean isSocialLogin() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        return preferences.contains(Constants.SOCIAL_ACCOUNT_TYPE)
                && SocialAccount.getSocialLoginTypes().contains(preferences.getString(Constants.SOCIAL_ACCOUNT_TYPE, ""));
    }

    public void doLogout() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(Constants.FIRST_NAME);
        editor.remove(Constants.BBTOKEN_KEY);
        editor.remove(Constants.OLD_BBTOKEN_KEY);
        editor.remove(Constants.MID_KEY);
        editor.remove(Constants.MEMBER_FULL_NAME_KEY);
        editor.remove(Constants.MEMBER_EMAIL_KEY);
        editor.remove(Constants.SOCIAL_ACCOUNT_TYPE);
        editor.commit();
        AuthParameters.updateInstance(getCurrentActivity());
        LocalyticsAmpSession localyticsSession = LocalyticsHandler.getInstance(getApplication()).getSession();

        String analyticsAdditionalAttrsJson = preferences.getString(Constants.ANALYTICS_ADDITIONAL_ATTRS, null);
        editor.remove(Constants.ANALYTICS_ADDITIONAL_ATTRS);

        if (localyticsSession != null) {
            localyticsSession.setCustomerName(null);
            localyticsSession.setCustomerEmail(null);
            localyticsSession.setCustomerId(null);
            localyticsSession.setCustomerData(LocalyticsHandler.CUSTOMER_MOBILE, null);
            localyticsSession.setCustomerData(LocalyticsHandler.CUSTOMER_HUB, null);
            localyticsSession.setCustomerData(LocalyticsHandler.CUSTOMER_MOBILE, null);
            localyticsSession.setCustomerData(LocalyticsHandler.CUSTOMER_REGISTERED_ON, null);
            localyticsSession.setCustomerData(LocalyticsHandler.CUSTOMER_BDAY, null);
            localyticsSession.setCustomerData(LocalyticsHandler.CUSTOMER_GENDER, null);
            localyticsSession.setCustomerData(LocalyticsHandler.CUSTOMER_CITY, null);
            if (!TextUtils.isEmpty(analyticsAdditionalAttrsJson)) {
                Gson gson = new Gson();
                HashMap<String, Object> additionalAttrMap = new HashMap<>();
                additionalAttrMap = (HashMap<String, Object>) gson.fromJson(analyticsAdditionalAttrsJson, additionalAttrMap.getClass());
                if (additionalAttrMap != null) {
                    for (Map.Entry<String, Object> entry : additionalAttrMap.entrySet()) {
                        localyticsSession.setCustomerData(entry.getKey(), null);
                    }
                }
            }
        }
    }

    public void onLoginSuccess() {
        AuthParameters.updateInstance(getCurrentActivity());
        CitySpecificAppSettings.clearInstance(getCurrentActivity());
        SectionManager.clearAllSectionData(getCurrentActivity());
        Intent data = new Intent();
        data.putExtra(Constants.LOGOUT, true);
        setResult(NavigationCodes.GO_TO_HOME, data);
        finish();
    }

    public abstract void onChangeTitle(String title);

    protected void reportFormInputFieldError(EditText editText, String errMsg) {
        UIUtil.reportFormInputFieldError(editText, errMsg);
    }

    protected void reportFormInputFieldError(AutoCompleteTextView autoCompleteTextView, String errMsg) {
        UIUtil.reportFormInputFieldError(autoCompleteTextView, errMsg);
    }

    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs) {
        // todo check if MoEngage enabled
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        if (!authParameters.isMoEngageEnabled()) return;
        trackEvent(eventName, eventAttribs, null, null);
    }


    @Override
    public void trackEvent(String eventName, Map<String, String> eventAttribs, String source, String sourceValue) {
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
                moEHelper.trackEvent(eventName, analyticsJsonObj);
            } catch (JSONException e) {
                Log.e("Analytics", "Failed to send event = " + eventName + " to analytics");
            }
        }
        LocalyticsHandler localyticsHandler = LocalyticsHandler.getInstance(getApplication());
        if (authParameters.isLocalyticsEnabled() && localyticsHandler != null && localyticsHandler.getSession() != null) {
            localyticsHandler.getSession().tagEvent(eventName, eventAttribs);
        }
    }

    @Override
    public void showApiErrorDialog(String message) {
        showAlertDialog(message);
    }

    @Override
    public void showApiErrorDialog(String message, boolean finish) {
        if (finish) {
            showAlertDialogFinish(null, message);
        } else {
            showAlertDialog(message);
        }
    }

    @Override
    public void showApiErrorDialog(String message, String sourceName, Object valuePassed) {
        showAlertDialog(null, message, sourceName, valuePassed);
    }

    @Override
    public void showApiErrorDialog(String message, int resultCode) {
        showAlertDialogFinish(null, message, resultCode);
    }
}