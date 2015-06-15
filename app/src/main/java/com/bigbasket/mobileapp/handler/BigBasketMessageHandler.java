package com.bigbasket.mobileapp.handler;

import android.text.TextUtils;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.ApiErrorAware;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.net.HttpURLConnection;

import retrofit.RetrofitError;

public class BigBasketMessageHandler<T> {

    private T ctx;

    public BigBasketMessageHandler(T ctx) {
        this.ctx = ctx;
    }

    public void sendEmptyMessage(int what, String message, boolean finish) {

        switch (what) {
            case ApiErrorCodes.POTENTIAL_ORDER_SLOT_EXPIRED:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.slotNotAvailable),
                        NavigationCodes.GO_TO_SLOT_SELECTION);
                break;
            case ApiErrorCodes.POTENITAL_ORDER_EXPIRED:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.potentialOrderIdExpired),
                        NavigationCodes.GO_TO_HOME);
                break;
            case ApiErrorCodes.INTERNAL_SERVER_ERROR:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.headingServerError),
                        getString(R.string.server_error), finish);
                break;
            case ApiErrorCodes.INVALID_USER:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.potentialOrderIdExpired),
                        NavigationCodes.GO_TO_HOME);
                break;
            case ApiErrorCodes.INVALID_FIELD:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.headingServerError),
                        getString(R.string.server_error), true);
                break;
            case ApiErrorCodes.EMAIL_ALREADY_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.REGISTERED_EMAIL));
                break;
            case ApiErrorCodes.CART_NOT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.cartNotExistsError));
                break;
            case ApiErrorCodes.INVALID_USER_PASSED:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.INVALID_USER_PASS));
                break;
            case ApiErrorCodes.INVALID_EVOUCHER:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, message);
                break;
            case ApiErrorCodes.LOGIN_REQUIRED:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.signIn), getString(R.string.login_required), NavigationCodes.GO_TO_LOGIN, null);
                break;
            case ApiErrorCodes.BASKET_EMPTY:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.basketEmpty), true);
                break;
            case ApiErrorCodes.NO_ITEMS_IN_CART:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.basketEmpty));
                break;
            case ApiErrorCodes.LIST_SLUG_MISSING:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.headingServerError),
                        getString(R.string.server_error), true);
                break;
            case ApiErrorCodes.PRODUCT_DOES_NOT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, getString(R.string.product_id_invalid), true);
                break;
            case ApiErrorCodes.LIST_WITH_NAME_ALREADY_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.list_exists));
                break;
            case ApiErrorCodes.MEMBER_SHOPPING_LIST_DOESNT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.list_exists), true);
                break;
            case ApiErrorCodes.PROMO_NOT_EXIST:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, getString(R.string.invalid_promo), true);
                break;
            case ApiErrorCodes.PROMO_CATEGORY_NOT_EXIST:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.no_promo_cat), true);
                break;
            case ApiErrorCodes.PROMO_CRITERIA_SET_NOT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.invalid_promo), true);
                break;
            case ApiErrorCodes.PROMO_NOT_ACTIVE:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.invalid_promo), true);
                break;
            case ApiErrorCodes.INVALID_PROMO:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.server_error), true);
                break;
            case ApiErrorCodes.ORDER_IN_PROGRESS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, message);
                break;
            case ApiErrorCodes.PRODUCT_QTY_LIMIT:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, !TextUtils.isEmpty(message) ? message : getString(R.string.invalidBasketQty));
                break;
            case ApiErrorCodes.ADDRESS_NOT_SERVED:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, message, true);
                break;
            case ApiErrorCodes.EMAIL_DOESNT_EXISTS:
                ((ApiErrorAware) ctx).showApiErrorDialog(null, message, finish);
                break;
            case NavigationCodes.ADD_TO_SHOPPINGLIST_OK:
                if (((ActivityAware) ctx).getCurrentActivity() != null) {
                    Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                            "Added successfully", Toast.LENGTH_SHORT).show();
                }
                break;
            case NavigationCodes.DELETE_FROM_SHOPPING_LIST_OK:
                if (((ActivityAware) ctx).getCurrentActivity() != null) {
                    Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                            "Successfully deleted!", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                ((ApiErrorAware) ctx).showApiErrorDialog(null,
                        !TextUtils.isEmpty(message) ? message : getString(R.string.server_error), finish);
                break;
        }
    }

    public void sendEmptyMessage(int what, String message) {
        sendEmptyMessage(what, message, false);
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
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.headingNetworkError),
                        getString(R.string.msgNetworkError), finish);
                break;
            case HTTP:
                if (error.getResponse() == null) {
                    ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.headingNetworkError),
                            getString(R.string.msgNetworkError), finish);
                } else {
                    handleHttpError(error.getResponse().getStatus(), error.getResponse().getReason(), sourceName, finish);
                }
                break;
            case CONVERSION:
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.headingServerError),
                        getString(R.string.server_error), true);
                break;
            case UNEXPECTED:
                throw error;
        }
    }

    public void handleRetrofitError(RetrofitError error) {
        handleRetrofitError(error, null);
    }

    public void handleHttpError(int errorCode, String reasonPhrase, String sourceName, boolean finish) {
        if (errorCode == HttpURLConnection.HTTP_UNAVAILABLE) {
            if (!finish) {
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.weAreDown), getString(R.string.serviceUnavailable), sourceName, null);
            } else {
                ((ActivityAware) ctx).getCurrentActivity().showAlertDialogFinish(getString(R.string.weAreDown), getString(R.string.serviceUnavailable));
            }
        } else if (errorCode == HttpURLConnection.HTTP_BAD_GATEWAY || errorCode == HttpURLConnection.HTTP_INTERNAL_ERROR) {
            if (!finish) {
                ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.weAreOverloaded), getString(R.string.weAreOverloadedDesc), sourceName, null);
            } else {
                ((ActivityAware) ctx).getCurrentActivity().showAlertDialogFinish(getString(R.string.weAreOverloaded), getString(R.string.weAreOverloadedDesc));
            }
        } else if (errorCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
            showUnauthorised();
        } else {
            String msg = "HTTP " + errorCode + " : " + reasonPhrase;
            if (!finish) {
                ((ApiErrorAware) ctx).showApiErrorDialog(null, msg, sourceName, null);
            } else {
                ((ApiErrorAware) ctx).showApiErrorDialog(null, msg, true);
            }
        }
    }

    private void showUnauthorised() {
        ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.signIn),
                getString(R.string.login_required), NavigationCodes.GO_TO_LOGIN, null);
    }

    public void sendOfflineError() {
        sendOfflineError(false);
    }

    public void sendOfflineError(boolean finish) {
        ((ApiErrorAware) ctx).showApiErrorDialog(getString(R.string.headingConnectionOffline),
                getString(R.string.connectionOffline), finish);
    }
}
