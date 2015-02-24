package com.bigbasket.mobileapp.task;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateProductQtyResponseModel;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateReservationResponseModel;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.ConnectivityAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.OnUpdateReserveQtyAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.order.CheckoutProduct;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CoUpdateReservationTask<T> {

    private static final String TAG = CoUpdateReservationTask.class.getName();
    private T ctx;
    private boolean removeAll;
    private int finalQty;
    private String productId;
    private ArrayList<CheckoutProduct> productWithNoStockList;
    private ArrayList<CheckoutProduct> productWithSomeStockList;

    public CoUpdateReservationTask(T ctx, boolean removeAll, String productId, int finalQty) {
        this.ctx = ctx;
        this.removeAll = removeAll;
        this.finalQty = finalQty;
        this.productId = productId;
    }

    public CoUpdateReservationTask(T ctx, boolean removeAll, ArrayList<CheckoutProduct> productWithNoStockList,
                                   ArrayList<CheckoutProduct> productWithSomeStockList) {
        this.ctx = ctx;
        this.removeAll = removeAll;
        this.productWithNoStockList = productWithNoStockList;
        this.productWithSomeStockList = productWithSomeStockList;
    }

    public void startTask() {
        if (!((ConnectivityAware) ctx).checkInternetConnection()) {
            ((HandlerAware) ctx).getHandler().sendOfflineError();
            return;
        }
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
            SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(((ActivityAware) ctx).getCurrentActivity());
            String pid = prefer.getString(Constants.POTENTIAL_ORDER_ID, "");
            BigBasketApiService bigBasketApiService = BigBasketApiAdapter.
                    getApiService(((ActivityAware) ctx).getCurrentActivity());
            ((ProgressIndicationAware) ctx).showProgressDialog("Please wait...");
            bigBasketApiService.coUpdateReservation(pid, finalItem.toString(), new Callback<UpdateReservationResponseModel<UpdateProductQtyResponseModel>>() {
                @Override
                public void success(UpdateReservationResponseModel<UpdateProductQtyResponseModel> coUpdateReservationApiResponse, Response response) {
                    if (((CancelableAware) ctx).isSuspended()) {
                        return;
                    } else {
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException ex) {
                            return;
                        }
                    }
                    switch (coUpdateReservationApiResponse.status) {
                        case Constants.OK:
                            if (removeAll) {
                                Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                                        coUpdateReservationApiResponse.message, Toast.LENGTH_SHORT).show();
                                ((CartInfoAware) ctx).setCartInfo(coUpdateReservationApiResponse.apiResponseContent.cartSummary);
                                ((CartInfoAware) ctx).updateUIForCartInfo();
                                ((CartInfoAware) ctx).markBasketDirty();
                                CartSummary cartInfo = ((CartInfoAware) ctx).getCartInfo();
                                if (cartInfo.getNoOfItems() > 0) {
                                    ((OnUpdateReserveQtyAware) ctx).onUpdateReserveSuccessResponse();
                                } else {
                                    Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                                            ((ActivityAware) ctx).getCurrentActivity().getResources().getString(R.string.basketEmpty),
                                            Toast.LENGTH_SHORT).show();
                                    ((ActivityAware) ctx).getCurrentActivity().goToHome(false);
                                }
                            } else {
                                ((CartInfoAware) ctx).setCartInfo(coUpdateReservationApiResponse.apiResponseContent.cartSummary);
                                ((CartInfoAware) ctx).updateUIForCartInfo();
                                ((CartInfoAware) ctx).markBasketDirty();
                                CartSummary cartInfo = ((CartInfoAware) ctx).getCartInfo();
                                if (cartInfo.getNoOfItems() > 0) {
                                    Intent intent = new Intent(((ActivityAware) ctx).getCurrentActivity(), BackButtonActivity.class);
                                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ADDRESS_SELECTION);
                                    ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                                } else {
                                    Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                                            coUpdateReservationApiResponse.message, Toast.LENGTH_SHORT).show();
                                    ((ActivityAware) ctx).getCurrentActivity().goToHome(false);
                                }
                            }
                            break;
                        case Constants.ERROR:
                            switch (coUpdateReservationApiResponse.getErrorTypeAsInt()) {
                                case ApiErrorCodes.POTENITAL_ORDER_EXPIRED:
                                    ((ActivityAware) ctx).getCurrentActivity().goToHome(false);
                                    break;
                                default:
                                    ((HandlerAware) ctx).getHandler().
                                            sendEmptyMessage(coUpdateReservationApiResponse.getErrorTypeAsInt(),
                                                    coUpdateReservationApiResponse.message);
                                    break;
                            }
                            break;
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    if (((CancelableAware) ctx).isSuspended()) {
                        return;
                    } else {
                        try {
                            ((ProgressIndicationAware) ctx).hideProgressDialog();
                        } catch (IllegalArgumentException ex) {
                            return;
                        }
                    }
                    ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
                }
            });
        } catch (JSONException e) {
            ((HandlerAware) ctx).getHandler().sendEmptyMessage(ApiErrorCodes.INVALID_FIELD, null);
            Log.d(TAG, "Sending message: ApiErrorCodes.SERVER_ERROR");
        }
    }

}
