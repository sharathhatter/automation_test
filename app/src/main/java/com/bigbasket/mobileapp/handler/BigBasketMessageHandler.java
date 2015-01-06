package com.bigbasket.mobileapp.handler;

import android.content.Intent;
import android.text.TextUtils;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.AgeValidationActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.BasketValidationActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.CheckoutQCActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.interfaces.COReserveQuantityCheckAware;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

import org.apache.http.HttpStatus;

import retrofit.RetrofitError;

public class BigBasketMessageHandler<T> {

    private T ctx;
    private MarketPlace mMarketPlace;

    public BigBasketMessageHandler(T ctx) {
        this.ctx = ctx;
    }

    public BigBasketMessageHandler(T ctx, MarketPlace mMarketPlace) {
        this.ctx = ctx;
        this.mMarketPlace = mMarketPlace;
    }

    public void sendEmptyMessage(int what, String message, boolean finish) {
        switch (what) {
            case ApiErrorCodes.POTENTIAL_ORDER_SLOT_EXPIRED:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.slotNotAvailable),
                        NavigationCodes.GO_TO_SLOT_SELECTION);
                break;
            case ApiErrorCodes.POTENITAL_ORDER_EXPIRED:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.POTENTIAL_ORDER_ID_EXPERIED),
                        NavigationCodes.GO_TO_HOME);
                break;
            case ApiErrorCodes.INTERNAL_SERVER_ERROR:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.server_error), finish);
                break;
            case ApiErrorCodes.INVALID_USER:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.POTENTIAL_ORDER_ID_EXPERIED),
                        NavigationCodes.GO_TO_HOME);
                break;
            case ApiErrorCodes.INVALID_FIELD:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.server_error), true);
                break;
            case ApiErrorCodes.EMAIL_ALREADY_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.REGISTERED_EMAIL));
                break;
            case ApiErrorCodes.CART_NOT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.cartNotExistsError));
                break;
            case ApiErrorCodes.INVALID_USER_PASSED:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.INVALID_USER_PASS));
                break;
            case ApiErrorCodes.INVALID_EVOUCHER:
                ((ApiErrorAware) ctx).showApiErrorDialog(message);
                break;
            case ApiErrorCodes.LOGIN_REQUIRED:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.login_required), NavigationCodes.GO_TO_LOGIN, null);
                break;
            case ApiErrorCodes.BASKET_EMPTY:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.basketEmpty), true);
                break;
            case ApiErrorCodes.NO_ITEMS_IN_CART:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.basketEmpty));
                break;
            case ApiErrorCodes.LIST_SLUG_MISSING:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.server_error), true);
                break;
            case ApiErrorCodes.PRODUCT_DOES_NOT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.product_id_invalid), true);
                break;
            case ApiErrorCodes.LIST_WITH_NAME_ALREADY_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.list_exists));
                break;
            case ApiErrorCodes.MEMBER_SHOPPING_LIST_DOESNT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.list_exists), true);
                break;
            case ApiErrorCodes.PROMO_NOT_EXIST:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.invalid_promo), true);
                break;
            case ApiErrorCodes.PROMO_CATEGORY_NOT_EXIST:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.no_promo_cat), true);
                break;
            case ApiErrorCodes.PROMO_CRITERIA_SET_NOT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.invalid_promo), true);
                break;
            case ApiErrorCodes.PROMO_NOT_ACTIVE:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.invalid_promo), true);
                break;
            case ApiErrorCodes.INVALID_PROMO:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.server_error), true);
                break;
            case ApiErrorCodes.ORDER_IN_PROGRESS:
                ((ApiErrorAware) ctx).showApiErrorDialog(message);
                break;
            case ApiErrorCodes.PRODUCT_QTY_LIMIT:
                ((ApiErrorAware) ctx).showApiErrorDialog(!TextUtils.isEmpty(message) ? message : getString(R.string.invalidBasketQty));
                break;
            case ApiErrorCodes.ADDRESS_NOT_SERVED:
                ((ApiErrorAware) ctx).showApiErrorDialog(message, true);
                break;
            case NavigationCodes.ADD_TO_SHOPPINGLIST_OK:
                if (((ActivityAware) ctx).getCurrentActivity() != null) {
                    Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                            "Product successfully added to lists", Toast.LENGTH_SHORT).show();
                }
                break;
            case NavigationCodes.DELETE_FROM_SHOPPING_LIST_OK:
                if (((ActivityAware) ctx).getCurrentActivity() != null) {
                    Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                            "Product successfully deleted from list", Toast.LENGTH_SHORT).show();
                }
                break;
            case NavigationCodes.GO_MARKET_PLACE:
                Intent intent = new Intent(((ActivityAware) ctx).getCurrentActivity(), BasketValidationActivity.class);
                intent.putExtra(Constants.MARKET_PLACE_INTENT, mMarketPlace);
                ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;

            case NavigationCodes.GO_AGE_VALIDATION:
                intent = new Intent(((ActivityAware) ctx).getCurrentActivity(), AgeValidationActivity.class);
                intent.putExtra(Constants.MARKET_PLACE_INTENT, mMarketPlace);
                ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;

            case NavigationCodes.CO_RESERVE_QUANTITY_CHECK_OK:
                //int qc_len = ((COReserveQuantityCheckAware) ctx).getCOReserveQuantity().isQcHasErrors() ? 0 : 1;
                intent = new Intent(((ActivityAware) ctx).getCurrentActivity(), CheckoutQCActivity.class);
                intent.putExtra(Constants.CO_RESERVE_QTY_DATA, ((COReserveQuantityCheckAware) ctx).getCOReserveQuantity());
                //intent.putExtra(Constants.QC_LEN, qc_len);
                ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                break;
            default:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.server_error), finish);
                break;
        }
    }

    public void sendEmptyMessage(int what, String message) {
        sendEmptyMessage(what, message, false);
    }

    public void sendEmptyMessage(int what) {
        sendEmptyMessage(what, null);
    }

    private String getString(int resId) {
        return ((ActivityAware) ctx).getCurrentActivity().getString(resId);
    }

    public void handleRetrofitError(RetrofitError error, String sourceName) {
        handleRetrofitError(error, sourceName, false);
    }

    public void handleRetrofitError(RetrofitError error, boolean finish) {
        handleRetrofitError(error, null, finish);
    }

    public void handleRetrofitError(RetrofitError error, String sourceName, boolean finish) {
        switch (error.getKind()) {
            case NETWORK:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.msgNetworkError), finish);
                break;
            case HTTP:
                if (error.getResponse() == null) {
                    ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.msgNetworkError), finish);
                } else {
                    handleHttpError(error.getResponse().getStatus(), error.getResponse().getReason(), sourceName, finish);
                }
                break;
            case CONVERSION:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.server_error), true);
                break;
            case UNEXPECTED:
                throw error;
        }
    }

    public void handleRetrofitError(RetrofitError error) {
        handleRetrofitError(error, null);
    }

    public void handleHttpError(int errorCode, String reasonPhrase, String sourceName, boolean finish) {
        switch (errorCode) {
            case HttpStatus.SC_BAD_GATEWAY:
                if (!finish) {
                    ((ActivityAware) ctx).getCurrentActivity().showAlertDialog(getString(R.string.weAreDown), getString(R.string.serviceUnavailable), sourceName);
                } else {
                    ((ActivityAware) ctx).getCurrentActivity().showAlertDialogFinish(getString(R.string.weAreDown), getString(R.string.serviceUnavailable));
                }
                break;
            case HttpStatus.SC_UNAUTHORIZED:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.login_required), NavigationCodes.GO_TO_LOGIN, null);
                break;
            default:
                String msg = "HTTP " + errorCode + " : " + reasonPhrase;
                if (!finish) {
                    ((ApiErrorAware) ctx).showApiErrorDialog(msg, sourceName, null);
                } else {
                    ((ApiErrorAware) ctx).showApiErrorDialog(msg, true);
                }
                break;
        }
    }

    public void sendOfflineError() {
        sendOfflineError(false);
    }

    public void sendOfflineError(boolean finish) {
        ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.connectionOffline), finish);
    }
}
