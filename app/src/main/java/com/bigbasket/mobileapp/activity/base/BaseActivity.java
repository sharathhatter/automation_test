package com.bigbasket.mobileapp.activity.base;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
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
import com.bigbasket.mobileapp.activity.StartActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.CheckoutQCActivity;
import com.bigbasket.mobileapp.adapter.account.AreaPinInfoAdapter;
import com.bigbasket.mobileapp.adapter.order.PrescriptionImageAdapter;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.MessageHandler;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.COMarketPlaceAware;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.order.COReserveQuantity;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.task.UploadImageService;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.UIUtil;
import com.demach.konotor.Konotor;

import org.apache.http.client.CookieStore;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

public abstract class BaseActivity extends ActionBarActivity implements COMarketPlaceAware,
        COReserveQuantityCheckAware, CancelableAware, ProgressIndicationAware, ActivityAware,
        ConnectivityAware {

    public static Typeface faceRupee;
    public static Typeface faceRobotoRegular;
    protected Handler handler = new MessageHandler(getCurrentActivity());
    private boolean isActivitySuspended;
    //protected MarketPlace marketPlace;
    protected COReserveQuantity coReserveQuantity;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isActivitySuspended = false;

        faceRupee = Typeface.createFromAsset(getAssets(), "Rupee.ttf");
        faceRobotoRegular = Typeface.createFromAsset(getAssets(), "Roboto-Regular.ttf");
    }

    public boolean checkInternetConnection() {
        return DataUtil.isInternetAvailable(getCurrentActivity());
    }

    protected ProgressDialog progressDialog = null;

    public void showProgressDialog(String msg) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage(msg);
        progressDialog.show();
    }

    public void hideProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showProgressView() {
    }

    @Override
    public void hideProgressView() {
    }

    public void onHttpError() {

    }

    public abstract BaseActivity getCurrentActivity();

    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
    }

    @Override
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        Handler handler = new MessageHandler(getCurrentActivity(), marketPlace);
        if (marketPlace.isRuleValidationError()) {
            handler.sendEmptyMessage(MessageCode.GO_MARKET_PLACE);
        } else if (marketPlace.isAgeCheckRequired() || marketPlace.isPharamaPrescriptionNeeded()) {
            handler.sendEmptyMessage(MessageCode.GO_AGE_VALIDATION);
        } else {
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
            String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
            new COReserveQuantityCheckTask(getCurrentActivity(), pharmaPrescriptionId).execute();
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
        intent.putExtra(Constants.QC_LEN, coReserveQuantity.getQc_len());
        startActivityForResult(intent, Constants.GO_TO_HOME);
    }


    public void startAsyncActivity(String url, HashMap<String, String> params,
                                   boolean post, @Nullable AuthParameters authParameters,
                                   CookieStore cookieStore) {
        startAsyncActivity(url, params, post, authParameters, cookieStore, null);
    }

    public void startAsyncActivity(String url, HashMap<String, String> params,
                                   boolean post, @Nullable AuthParameters authParameters,
                                   CookieStore cookieStore, @Nullable HashMap<Object, String> additionalCtx) {
        startAsyncActivity(url, params, post, authParameters, cookieStore, additionalCtx, false);
    }

    public void startAsyncActivity(String url, HashMap<String, String> params,
                                   boolean post, @Nullable AuthParameters authParameters,
                                   CookieStore cookieStore, @Nullable HashMap<Object, String> additionalCtx,
                                   boolean noProgressView) {
        if (DataUtil.isInternetAvailable(getCurrentActivity())) {
            if (isSuspended()) {
                return;
            }
            if (!noProgressView) {
                showProgressDialog(getString(R.string.please_wait));
            }
            String authToken = null, vid = null, osVersion = null;
            if (authParameters != null) {
                authToken = authParameters.getBbAuthToken();
                vid = authParameters.getVisitorId();
                osVersion = authParameters.getOsVersion();
            }
            HttpRequestData httpRequestData = new HttpRequestData(url, params, post,
                    authToken, vid, osVersion, cookieStore, additionalCtx);
            new HttpAsyncActivity().execute(httpRequestData);
        } else {
            handler.sendEmptyMessage(MessageCode.INTERNET_ERROR);
        }
    }

    private class HttpAsyncActivity extends AsyncTask<HttpRequestData, Integer, HttpOperationResult> {

        protected HttpOperationResult doInBackground(HttpRequestData... httpRequestDatas) {
            if (isCancelled()) {
                return null;
            }
            HttpRequestData httpRequestData = httpRequestDatas[0];
            HttpOperationResult httpOperationResult;
            httpOperationResult = httpRequestData.isPost() ? DataUtil.doHttpPost(httpRequestData)
                    : DataUtil.doHttpGet(httpRequestData);
            return httpOperationResult;
        }

        protected void onPostExecute(HttpOperationResult httpOperationResult) {
            Log.d("OnPostExecute", "");
            try {
                hideProgressDialog();
            } catch (IllegalArgumentException e) {
                return;
            }
            if (httpOperationResult != null) {
                onAsyncTaskComplete(httpOperationResult);
            } else {
                onHttpError();
            }
        }
    }

    protected void onResume() {
        super.onResume();
        com.facebook.AppEventsLogger.activateApp(getCurrentActivity(), Constants.FB_APP_ID);
        PrescriptionImageAdapter prescriptionImageAdapter = new PrescriptionImageAdapter(getCurrentActivity());
        if (!prescriptionImageAdapter.exists()) {
            stopService(new Intent(this, UploadImageService.class));
        }
        isActivitySuspended = false;

        initializeKonotor();
    }

    public void launchKonotor() {
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        if (!authParameters.isAuthTokenEmpty()) {
            Konotor.getInstance(getApplicationContext()).launchFeedbackScreen(this);
        } else {
            showAlertDialog(getCurrentActivity(), null, "You are not signed in.\nPlease sign-in to continue",
                    Constants.LOGIN_REQUIRED);
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

    public void showAlertDialog(Context context, String title,
                                String msg) {
        showAlertDialog(context, title, msg, null);
    }

    public void showAlertDialog(Context context, String title,
                                String msg, final String sourceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title == null ? "BigBasket" : title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onPositiveButtonClicked(dialog, which, sourceName, null);
            }
        });
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.show();
    }

    public void showAlertDialogFinish(Context context, String title,
                                      String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title == null ? "BigBasket" : title);
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (getCurrentActivity() != null) {
                    getCurrentActivity().finish();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivitySuspended = true;
    }

    public void showAlertDialog(String msg) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getCurrentActivity());
        alertDialogBuilder.setTitle("BigBasket");
        alertDialogBuilder.setMessage(msg).setCancelable(false).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                onPositiveButtonClicked(dialog, id, null, null);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        if (isSuspended())
            return;
        alertDialog.show();
    }

    public void showAlertDialog(Context context, String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName) {
        showAlertDialog(context, title, msg, dialogButton, nxtDialogButton, sourceName, null, null);
    }

    public void showAlertDialog(Context context, String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName,
                                final String passedValue) {
        showAlertDialog(context, title, msg, dialogButton, nxtDialogButton, sourceName, passedValue, null);
    }

    public void showAlertDialog(Context context, String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton, final String sourceName,
                                final Object passedValue, String positiveBtnText) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
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
            if (nxtDialogButton.equals(DialogButton.NO))
                builder.setNegativeButton(R.string.noTxt, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int id) {
                        onNegativeButtonClicked(dialogInterface, id, sourceName);
                    }
                });
        }
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.show();
    }

    public void showAlertDialog(Context context, String title,
                                String msg, DialogButton dialogButton,
                                DialogButton nxtDialogButton) {
        showAlertDialog(context, title, msg, dialogButton, nxtDialogButton, null);
    }

    public Spannable asRupeeSpannable(String amtTxt) {
        return UIUtil.asRupeeSpannable(amtTxt, faceRupee);
    }

    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.LOGIN_REQUIRED:
                    Intent loginIntent = new Intent(this, SignInActivity.class);
                    startActivityForResult(loginIntent, Constants.GO_TO_HOME);
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
        if (resultCode == Constants.GO_TO_HOME) {
            if (!(getCurrentActivity() instanceof StartActivity)) {
                setResult(Constants.GO_TO_HOME);
                finish();
            }
        }
//        } else if (resultCode == Constants.GO_TO_SLOT_SELECTION && !(getCurrentActivity() instanceof DeliverySlotsActivity)) {
//            setResult(Constants.GO_TO_SLOT_SELECTION);
//            finish();
//        } else if (resultCode == Constants.GO_TO_SHOP) {
//            if (getCurrentActivity() instanceof HomeActivity) {
//                navigateToFooterActivity(ShopActivity.class);
//            } else if (!(getCurrentActivity() instanceof ShopActivity)) {
//                setResult(Constants.GO_TO_SHOP);
//                finish();
//            }
//        } else if (resultCode == Constants.GO_TO_PRODUCTS) {
//            if (getCurrentActivity() instanceof HomeActivity) {
//                navigateToFooterActivity(TopCategoryActivity.class);
//            } else if (!(getCurrentActivity() instanceof TopCategoryActivity)) {
//                setResult(Constants.GO_TO_PRODUCTS);
//                finish();
//            }
//        } else if (resultCode == Constants.GO_TO_OFFERS) {
//            if (getCurrentActivity() instanceof HomeActivity) {
//                navigateToFooterActivity(OfferSelectActivity.class);
//            } else if (!(getCurrentActivity() instanceof OfferSelectActivity)) {
//                setResult(Constants.GO_TO_OFFERS);
//                finish();
//            }
//        }
        else {
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
        setResult(Constants.GO_TO_HOME);
        getCurrentActivity().finish();
//        Intent intent = new Intent(this, BBActivity.class);
//        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_HOME);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        } else {
//            intent.setFlags(IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
//        }
//        startActivity(intent);
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

    public boolean isSuspended() {
        return isActivitySuspended;
    }

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

    public Handler getHandler() {
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
            startActivityForResult(intent, Constants.GO_TO_HOME);
        } else {
            doLogout();
        }
    }

    public boolean isSocialLogin() {
        return PreferenceManager.
                getDefaultSharedPreferences(getCurrentActivity()).contains(Constants.SOCIAL_ACCOUNT_TYPE);
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
    }

    public void onLoginSuccess() {
        AuthParameters.updateInstance(getCurrentActivity());
        Intent data = new Intent();
        data.putExtra(Constants.LOGOUT, true);
        setResult(Constants.GO_TO_HOME, data);
        finish();
    }

    public abstract void onChangeTitle(String title);

    protected void reportFormInputFieldError(EditText editText, String errMsg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editText.setError(errMsg);
        } else {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(errMsg);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, errMsg.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            editText.setError(spannableStringBuilder);
        }
    }

    protected void reportFormInputFieldError(AutoCompleteTextView autoCompleteTextView, String errMsg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            autoCompleteTextView.setError(errMsg);
        } else {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(errMsg);
            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, errMsg.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            autoCompleteTextView.setError(spannableStringBuilder);
        }
    }
}