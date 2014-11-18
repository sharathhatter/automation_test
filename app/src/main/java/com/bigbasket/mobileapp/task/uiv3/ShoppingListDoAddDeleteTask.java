package com.bigbasket.mobileapp.task.uiv3;

import android.os.AsyncTask;
import android.util.Log;

import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.model.request.HttpRequestData;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MessageCode;
import com.google.gson.Gson;

import org.apache.http.HttpStatus;
import org.apache.http.impl.client.BasicCookieStore;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShoppingListDoAddDeleteTask extends AsyncTask<String, Long, Void> {
    private static final String TAG = ShoppingListDoAddDeleteTask.class.getName();
    private String url;
    private List<ShoppingListName> selectedShoppingListNames;
    private BaseFragment fragment;
    private ShoppingListOption shoppingListOption;
    private HttpOperationResult httpOperationResult;

    public ShoppingListDoAddDeleteTask(BaseFragment fragment, String url,
                                       List<ShoppingListName> selectedShoppingListNames,
                                       ShoppingListOption shoppingListOption) {
        this.fragment = fragment;
        this.url = url;
        this.selectedShoppingListNames = selectedShoppingListNames;
        this.shoppingListOption = shoppingListOption;
    }

    @Override
    protected Void doInBackground(String... params) {
        if (isCancelled()) {
            return null;
        }
        if (fragment.checkInternetConnection()) {
            String selectedProductId = ((ShoppingListNamesAware) fragment).getSelectedProductId();
            HashMap<String, String> paramMap = new HashMap<>();
            List<String> slugList = new ArrayList<>();
            for (ShoppingListName shoppingListName : selectedShoppingListNames) {
                slugList.add(shoppingListName.getSlug());
            }
            Gson gson = new Gson();
            paramMap.put("product_id", selectedProductId);
            paramMap.put("slugs", gson.toJson(slugList));
            AuthParameters authParameters = AuthParameters.getInstance(fragment.getActivity());
            HttpRequestData httpRequestData = new HttpRequestData(url, paramMap, true, authParameters.getBbAuthToken(),
                    authParameters.getVisitorId(), authParameters.getOsVersion(), new BasicCookieStore(), null);
            httpOperationResult = DataUtil.doHttpPost(httpRequestData);
        } else {
            fragment.getHandler().sendEmptyMessage(MessageCode.INTERNET_ERROR);
            Log.d(TAG, "Sending message: MessageCode.INTERNET_ERROR");
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (fragment.isSuspended()) {
            cancel(true);
        } else {
            fragment.showProgressDialog("Please wait...");
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        if (fragment.isSuspended()) {
            return;
        } else {
            fragment.hideProgressDialog();
        }
        if (httpOperationResult != null) {
            if (httpOperationResult.getResponseCode() == HttpStatus.SC_OK || httpOperationResult.getResponseCode() == HttpStatus.SC_ACCEPTED) {
                JSONObject jsonObject;
                try {
                    jsonObject = new JSONObject(httpOperationResult.getReponseString());
                } catch (JSONException e) {
                    fragment.getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                    Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR due to invalid json");
                    return;
                }
                if (jsonObject.optString(Constants.STATUS).equals("OK")) {
                    switch (shoppingListOption) {
                        case ADD_TO_LIST:
                            fragment.getHandler().sendEmptyMessage(MessageCode.ADD_TO_SHOPPINGLIST_OK);
                            Log.d(TAG, "Sending message: MessageCode.ADD_TO_SHOPPINGLIST_OK");
                            break;
                        case DELETE_ITEM:
                            fragment.getHandler().sendEmptyMessage(MessageCode.DELETE_FROM_SHOPPING_LIST_OK);
                            ((ShoppingListNamesAware) fragment).postShoppingListItemDeleteOperation();
                            Log.d(TAG, "Sending message: MessageCode.DELETE_FROM_SHOPPING_LIST_OK");
                            break;
                    }
                } else {
                    fragment.getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                    Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
                }

            } else if (httpOperationResult.getResponseCode() == HttpStatus.SC_UNAUTHORIZED) {
                fragment.getHandler().sendEmptyMessage(MessageCode.UNAUTHORIZED);
                Log.d(TAG, "Sending message: MessageCode.UNAUTHORIZED");

            } else {
                fragment.getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
                Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
            }

        } else {
            fragment.getHandler().sendEmptyMessage(MessageCode.SERVER_ERROR);
            Log.d(TAG, "Sending message: MessageCode.SERVER_ERROR");
        }

        super.onPostExecute(result);
    }
}
