package com.bigbasket.mobileapp.fragment.order;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.cart.CartItemHeader;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.order.OrderItemDisplaySource;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.task.COMarketPlaceCheckTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShowCartFragment extends BaseFragment {

    private boolean isReadOnly;
    private ArrayList<CartItemList> cartItemLists;
    private ArrayList<FulfillmentInfo> fullfillmentInfos;
    private ArrayList<AnnotationInfo> annotationInfoArrayList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.basket_page_menu, menu);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //getCartItems();
    }

    @Override
    public void onResume() {
        super.onResume();
        String fulfillmentIds = null;
        if (getArguments() != null) {
            fulfillmentIds = getArguments().getString(Constants.INTERNAL_VALUE);
            getCartItems(fulfillmentIds);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty_basket:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                if (preferences.getString(Constants.GET_CART, "0") != null
                        && !preferences.getString(Constants.GET_CART, "0").equals("0")) {
                    showAlertDialog(null, "Remove all the products from basket?", DialogButton.YES,
                            DialogButton.NO, Constants.EMPTY_BASKET, null, "Empty Basket");
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logViewBasketEvent(CartSummary cartSummary, Map<String, String> eventAttribs) {// todo event log for view basket
        if (cartSummary == null) return;
        eventAttribs.put(TrackEventkeys.TOTAL_ITEMS_IN_BASKET, String.valueOf(cartSummary.getNoOfItems()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_VALUE, String.valueOf(cartSummary.getTotal()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_SAVING, String.valueOf(cartSummary.getSavings()));
        trackEvent(TrackingAware.BASKET_VIEW, eventAttribs);
    }

    private void renderCartItemList(CartSummary cartSummary, String baseImageUrl) {
        Map<String, String> eventAttribs = new HashMap<>();

        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        contentView.removeAllViews();
        contentView.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View basketView = inflater.inflate(R.layout.uiv3_list_with_action, contentView, false);

        ListView cartItemListView = (ListView) basketView.findViewById(R.id.listWithFixedFooter);
        View basketSummaryView = getCartSummaryView(cartSummary, cartItemListView);

        List<Object> cartItemHeaderList = new ArrayList<>();
        for (CartItemList cartItemInfoArray : cartItemLists) {
            CartItemHeader cartItemHeader = new CartItemHeader();
            cartItemHeaderList.add(cartItemHeader);
            eventAttribs.put(cartItemInfoArray.getTopCatName() + " Items", String.valueOf(cartItemInfoArray.getTopCatItems()));
            eventAttribs.put(cartItemInfoArray.getTopCatName() + " Value", String.valueOf(cartItemInfoArray.getTopCatTotal()));
            cartItemHeader.setTopCatName(cartItemInfoArray.getTopCatName());
            cartItemHeader.setTopCatItems(cartItemInfoArray.getTopCatItems());
            cartItemHeader.setTopCatTotal(cartItemInfoArray.getTopCatTotal());

            int cartItemsSize = cartItemInfoArray.getCartItems().size();
            ArrayList<CartItem> cartItems = cartItemInfoArray.getCartItems();
            for (int i = 0; i < cartItemsSize; i++) {
                cartItems.get(i).setIndex(i);
                cartItemHeaderList.add(cartItems.get(i));
                if (cartItems.get(i).getPromoAppliedType() == 2 ||
                        cartItems.get(i).getPromoAppliedType() == 3) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.PROMO_NAME, cartItems.get(i).getCartItemPromoInfo().getPromoInfo().getPromoName());
                    trackEvent(TrackingAware.PROMO_REDEEMED, map);
                }
            }
        }
        HashMap<String, String> fulfillmentInfoIdAndIconHashMap = new HashMap<>();
        if (fullfillmentInfos != null) {
            for (FulfillmentInfo fullfillmentInfo : fullfillmentInfos) {
                fulfillmentInfoIdAndIconHashMap.put(fullfillmentInfo.getFulfillmentId(), fullfillmentInfo.getIcon());
                if (fullfillmentInfo.getDisplayName() == null || fullfillmentInfo.getIcon() == null)
                    continue;
                cartItemHeaderList.add(fullfillmentInfo);
            }
        }

        HashMap<String, AnnotationInfo> annotationHashMap = new HashMap<>();
        if (annotationInfoArrayList != null) {
            for (AnnotationInfo anAnnotationInfoArrayList : annotationInfoArrayList) {
                annotationHashMap.put(anAnnotationInfoArrayList.getAnnotationId(), anAnnotationInfoArrayList);
                if (anAnnotationInfoArrayList.getDescription() == null || anAnnotationInfoArrayList.getIconUrl() == null)
                    continue;
                cartItemHeaderList.add(anAnnotationInfoArrayList);
            }
        }

        cartItemListView.addHeaderView(basketSummaryView);
        Button btnFooterCheckout = (Button) basketView.findViewById(R.id.btnListFooter);
        btnFooterCheckout.setText(getString(R.string.check_out).toUpperCase());
        btnFooterCheckout.setTypeface(faceRobotoRegular);
        btnFooterCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCartInfo() != null && getCartInfo().getNoOfItems() > 0) {
                    if (AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
                        showAlertDialog("Login", getString(R.string.login_to_place_order),
                                DialogButton.OK, DialogButton.NO, NavigationCodes.GO_TO_LOGIN, null, "Login");
                    } else {
                        new COMarketPlaceCheckTask<>(getCurrentActivity()).startTask();
                    }
                }
            }
        });
        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter<>(cartItemHeaderList, this,
                faceRupee, faceRobotoRegular, OrderItemDisplaySource.BASKET, isReadOnly,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, baseImageUrl, TrackEventkeys.VIEW_BASKET);
        cartItemListView.setDivider(null);
        cartItemListView.setDividerHeight(0);
        cartItemListView.setAdapter(activeOrderRowAdapter);
        contentView.addView(basketView);

        logViewBasketEvent(cartSummary, eventAttribs);
    }

    @Override
    public void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.EMPTY_BASKET:
                    if (cartItemLists != null)
                        emptyCart();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
        }
    }

    public View getCartSummaryView(CartSummary cartSummary, ViewGroup parent) {
        if (cartSummary == null) return null;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_basket_header, parent, false);
        TextView lblSaving = (TextView) base.findViewById(R.id.lblSaving);
        lblSaving.setTypeface(faceRobotoRegular);

        TextView txtSaving = (TextView) base.findViewById(R.id.txtSaving);
        txtSaving.setTypeface(faceRobotoRegular);

        String totalSaveAmount = ((BaseActivity) getActivity()).getDecimalAmount(cartSummary.getSavings());
        if (!totalSaveAmount.equals("0")) {
            Spannable savingSpannable = new SpannableString("` " + ((BaseActivity) getActivity()).getDecimalAmount(cartSummary.getSavings()));
            savingSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtSaving.setText(savingSpannable);
        } else {
            txtSaving.setVisibility(View.GONE);
            lblSaving.setVisibility(View.GONE);
        }


        TextView txtTotalCartItems = (TextView) base.findViewById(R.id.txtTotalCartItems);
        txtTotalCartItems.setTypeface(faceRobotoRegular);
        if (cartSummary.getNoOfItems() != 0) {
            if (cartSummary.getNoOfItems() > 1) {
                txtTotalCartItems.setText(cartSummary.getNoOfItems() + " Items | ");
            } else {
                txtTotalCartItems.setText(cartSummary.getNoOfItems() + " Item | ");
            }
        } else {
            txtTotalCartItems.setVisibility(View.GONE);
        }

        String totalBasketAmount = ((BaseActivity) getActivity()).getDecimalAmount(cartSummary.getTotal());
        TextView txtTotal = (TextView) base.findViewById(R.id.txtTotal);
        txtTotal.setTypeface(faceRobotoRegular);
        if (!totalBasketAmount.equals("0")) {
            Spannable totalSpannable = new SpannableString("` " + totalBasketAmount);
            totalSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtTotal.setText(totalSpannable);
        } else {
            txtTotal.setText("");
            txtTotal.setVisibility(View.INVISIBLE);
        }
        return base;
    }


    public final void setBasketNumItemsDisplay() {
        if (getActivity() == null || getCartInfo() == null) return;
        updateUIForCartInfo();
        markBasketDirty();
    }

    private void emptyCart() {
        if (getActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getActivity())) return;
        trackEvent(TrackingAware.BASKET_EMPTY, null);
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.emptyCart(new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse cartEmptyApiResponseCallback, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                if (cartEmptyApiResponseCallback.status == 0) {
                    editor.putString(Constants.GET_CART, "0");
                    showBasketEmptyMessage();
                    CartSummary cartSummary = new CartSummary(0, 0, 0);
                    setCartInfo(cartSummary);
                    setBasketNumItemsDisplay();
                } else if (cartEmptyApiResponseCallback.status == ApiErrorCodes.CART_NOT_EXISTS) {
                    showErrorMsg("Cart is already empty");
                } else {
                    handler.sendEmptyMessage(cartEmptyApiResponseCallback.status,
                            cartEmptyApiResponseCallback.message);
                }
                editor.commit();

            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error);
            }
        });
    }


    private void getCartItems(String fulfillmentIds) {
        if (getActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getActivity())) return;
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.cartGet(fulfillmentIds, new Callback<ApiResponse<CartGetApiResponseContent>>() {
            @Override
            public void success(ApiResponse<CartGetApiResponseContent> cartGetApiResponseContentApiResponse, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                if (cartGetApiResponseContentApiResponse.status == 0) {
                    CartSummary cartSummary = cartGetApiResponseContentApiResponse.apiResponseContent.cartSummary;
                    if (!isReadOnly) {
                        setCartInfo(cartSummary);
                        setBasketNumItemsDisplay();
                        editor.putString(Constants.GET_CART,
                                String.valueOf(cartSummary.getNoOfItems()));
                    }
                    fullfillmentInfos = cartGetApiResponseContentApiResponse.apiResponseContent.fulfillmentInfos;
                    annotationInfoArrayList = cartGetApiResponseContentApiResponse.apiResponseContent.annotationInfos;
                    if (cartGetApiResponseContentApiResponse.apiResponseContent.
                            cartGetApiCartItemsContent != null
                            && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists != null
                            && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists.size() > 0) {
                        cartItemLists = cartGetApiResponseContentApiResponse.apiResponseContent.
                                cartGetApiCartItemsContent.cartItemLists;
                        renderCartItemList(cartSummary, cartGetApiResponseContentApiResponse
                                .apiResponseContent.cartGetApiCartItemsContent.baseImgUrl);
                    } else {
                        showBasketEmptyMessage();
                        editor.putString(Constants.GET_CART, "0");
                    }
                } else {
                    handler.sendEmptyMessage(cartGetApiResponseContentApiResponse.status,
                            cartGetApiResponseContentApiResponse.message);
                }
                editor.commit();
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error);
            }
        });
    }


    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void changeFragment(AbstractFragment newFragment) {
        setHasOptionsMenu(false);
        if (getCurrentActivity() != null) {
            getCurrentActivity().invalidateOptionsMenu();
        }
        super.changeFragment(newFragment);
    }

    @Override
    public void onBackResume() {
        super.onBackResume();
        setHasOptionsMenu(true);
        if (getCurrentActivity() != null) {
            getCurrentActivity().invalidateOptionsMenu();
        }
    }

    private void showBasketEmptyMessage() {
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_empty_data_text, contentView, false);
        TextView txtEmptyDataMsg = (TextView) base.findViewById(R.id.txtEmptyDataMsg);
        txtEmptyDataMsg.setText(getString(R.string.BASKET_EMPTY));
        contentView.removeAllViews();
        contentView.addView(base);
    }

    @Override
    public String getTitle() {
        return "Your Basket";
    }


    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty,
                                                    @Nullable View productView) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty, viewIncQty,
                btnAddToBasket, editTextQty, product, qty, productView);
        getCartItems(null);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShowCartFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.VIEW_BASKET_SCREEN;
    }
}
