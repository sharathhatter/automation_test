package com.bigbasket.mobileapp.task;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.CheckoutProduct;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.HttpCode;
import com.bigbasket.mobileapp.util.MessageCode;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class CoUpdateReservationTask extends AsyncTask<String, String, String> {

    private static final String TAG = CoUpdateReservationTask.class.getName();
    private static final String URL = MobileApiUrl.getBaseAPIUrl() + "co-update-reservation/";
    //private CheckoutActivity activity;
    //private int arrayListPosition;
    //private String updateType;
    private ProgressDialog progressDialog;
    private HttpOperationResult httpOperationResult;
    private BaseActivity activity;
    private boolean removeAll;
    private int finalQty;
    private String productId;
    private ArrayList<CheckoutProduct> productWithNoStockList;
    private ArrayList<CheckoutProduct> productWithSomeStockList;

//    public CoUpdateReservationTask(CheckoutActivity activity, int arrayListPosition, String updateType) {
//        this.activity = activity;
//        this.arrayListPosition = arrayListPosition;
//        this.updateType = updateType;
//    }

    public CoUpdateReservationTask(BaseActivity activity, boolean removeAll, String productId, int finalQty) {
        this.activity = activity;
        this.removeAll = removeAll;
        this.finalQty = finalQty;
        this.productId = productId;
    }

    public CoUpdateReservationTask(BaseActivity activity, boolean removeAll, ArrayList<CheckoutProduct> productWithNoStockList,
                                   ArrayList<CheckoutProduct> productWithSomeStockList) {
        this.activity = activity;
        this.removeAll = removeAll;
        this.productWithNoStockList = productWithNoStockList;
        this.productWithSomeStockList = productWithSomeStockList;
    }

    @Override
    protected String doInBackground(String... arg0) {
        if (isCancelled()) {
            return null;
        }
        if (activity.checkInternetConnection()) {
            JSONArray finalItem = new JSONArray();
            JSONObject updateItem = new JSONObject();
            try {
                if (removeAll) {
                    updateItem.put(Constants.PROD_ID, productId);
                    updateItem.put(Constants.FINAL_QTY, finalQty);
                    finalItem.put(updateItem);
                } else {
                    if (productWithNoStockList != null && productWithNoStockList.size() > 0) {
                        for (int i = 0; i < productWithNoStockList.size(); i++) {
                            updateItem = new JSONObject();
                            updateItem.put(Constants.PROD_ID, productWithNoStockList.get(i).getId());
                            updateItem.put(Constants.FINAL_QTY, Integer.parseInt(productWithNoStockList.get(i).getReserveQuantity()));
                            finalItem.put(updateItem);
                        }
                    }
                    if (productWithSomeStockList != null && productWithSomeStockList.size() > 0) {
                        for (int i = 0; i < productWithSomeStockList.size(); i++) {
                            updateItem = new JSONObject();
                            updateItem.put(Constants.PROD_ID, productWithSomeStockList.get(i).getId());
                            updateItem.put(Constants.FINAL_QTY, Integer.parseInt(productWithSomeStockList.get(i).getReserveQuantity()));
                            finalItem.put(updateItem);
                        }
                    }
                }
                SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(activity);
                String pid = prefer.getString(Constants.POTENTIAL_ORDER_ID, "");

                HashMap<String, String> postLoad = new HashMap<>();
                postLoad.put(Constants.P_ORDER_ID, pid);
                postLoad.put(Constants.ITEMS, finalItem.toString());


                AuthParameters authParameters = AuthParameters.getInstance(activity);
                HttpRequestData httpRequestData = new HttpRequestData(URL,
                        postLoad, true, authParameters.getBbAuthToken(), authParameters.getVisitorId(),
                        authParameters.getOsVersion(), new BasicCookieStore(), null);
                httpOperationResult = DataUtil.doHttpPost(httpRequestData);

            } catch (Exception e) {
                ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }
        } else {
            ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
            Log.d(TAG, "Sending message: MessageCode.INTERNET_ERROR");
        }

        return null;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (activity.isActivitySuspended()) {
            cancel(true);
        } else {
            progressDialog = ProgressDialog.show(activity, "", "Please wait");
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if (progressDialog != null && progressDialog.isShowing()) {
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException ex) {
                return;
            }
        } else {
            return;
        }
        if (httpOperationResult != null) {
            if (httpOperationResult.getResponseCode() == HttpCode.HTTP_OK) {
                JsonObject resultJson = new JsonParser().parse(httpOperationResult.getReponseString()).getAsJsonObject();
                String status = resultJson.get(Constants.STATUS).getAsString();
                SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(activity);
                SharedPreferences.Editor editor = prefer.edit();

                if (status.equalsIgnoreCase(Constants.OK)) {
                    if (removeAll) {
                        Toast.makeText(activity, "Updated successfully.", Toast.LENGTH_SHORT).show();
                        CartSummary cartInfo = ((CartInfoAware) activity).getCartInfo();
                        if (cartInfo.getNoOfItems() > 0) {
                            Intent i1 = new Intent(activity, BBActivity.class);
                            i1.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ADDRESS_SELECTION);
                            editor.commit();
                            activity.startActivityForResult(i1, Constants.GO_TO_HOME);
                            activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                        } else {
                            activity.goToHome();  // todo test this flow v.carefully
                        }
                    } else {
                        Intent intent = new Intent(activity, BBActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ADDRESS_SELECTION);
                        editor.commit();
                        activity.startActivityForResult(intent, Constants.GO_TO_HOME);
                        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                    }

                } else {  // its error case
                    String errorType = resultJson.get(Constants.ERROR_TYPE).getAsString();
                    Toast.makeText(activity, resultJson.get(Constants.MESSAGE).getAsString(), Toast.LENGTH_SHORT).show();
                    if (errorType != null && errorType.equalsIgnoreCase(Constants.POTENTIAL_ORDER_ID_EXPIRED)) {
                        activity.goToHome();
                    }
                }

            } else if (httpOperationResult.getResponseCode() == HttpCode.UNAUTHORIZED) {
                ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.UNAUTHORIZED);
                Log.d(TAG, "Sending message: MessageCode.UNAUTHORIZED");

            } else {
                ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }

        } else {
            ((HandlerAware) activity).getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
            Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
        }

        super.onPostExecute(result);
    }
}
