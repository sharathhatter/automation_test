package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
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
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShowCartActivity extends BackButtonActivity {

    private ArrayList<CartItemList> cartItemLists;
    private ArrayList<FulfillmentInfo> fulfillmentInfos;
    private ArrayList<AnnotationInfo> annotationInfoArrayList;
    @Nullable
    private MenuItem basketMenuItem;
    private TextView txtBasketSubTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.CO_BASKET);
        setTitle(getString(R.string.my_basket_header));
    }

    @Override
    public void onResume() {
        super.onResume();
        getCartItems(null, false);
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.empty_basket, menu);
        basketMenuItem = menu.getItem(0);
        basketMenuItem.setVisible(false);
    }

    private void renderHeaderView(int totalItemCount) {
        Toolbar toolbar = getToolbar();
        if (txtBasketSubTitle != null) toolbar.removeView(txtBasketSubTitle);
        txtBasketSubTitle = (TextView) getLayoutInflater().inflate(R.layout.basket_header_layout, toolbar, false);
        txtBasketSubTitle.setTypeface(faceRobotoRegular);
        toolbar.addView(txtBasketSubTitle);
        if (totalItemCount > 0) {
            String itemString = totalItemCount > 1 ? " Items" : " Item";
            txtBasketSubTitle.setText(totalItemCount + itemString);
            if (basketMenuItem != null) {
                basketMenuItem.setVisible(true);
            }
        } else {
            txtBasketSubTitle.setVisibility(View.GONE);
            if (basketMenuItem != null) {
                basketMenuItem.setVisible(false);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty_basket:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
                String numItems = preferences.getString(Constants.GET_CART, "0");
                if (!TextUtils.isEmpty(numItems) && !numItems.equals("0")) {
                    showAlertDialog(null, getString(R.string.removeAllProducts), DialogButton.YES,
                            DialogButton.NO, Constants.EMPTY_BASKET, null, getString(R.string.emptyBasket));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logViewBasketEvent(CartSummary cartSummary, Map<String, String> eventAttribs) {
        if (cartSummary == null) return;
        eventAttribs.put(TrackEventkeys.TOTAL_ITEMS_IN_BASKET, String.valueOf(cartSummary.getNoOfItems()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_VALUE, String.valueOf(cartSummary.getTotal()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_SAVING, String.valueOf(cartSummary.getSavings()));
        trackEvent(TrackingAware.BASKET_VIEW_SHOWN, eventAttribs, null, null, false, true);
    }

    private void renderCartItemList(CartSummary cartSummary, String baseImageUrl, boolean isCurrentPageRequest) {
        Map<String, String> eventAttribs = new HashMap<>();

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        contentLayout.removeAllViews();
        contentLayout.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        LayoutInflater inflater = getLayoutInflater();
        View basketView = inflater.inflate(R.layout.uiv3_recycler_view_with_action, contentLayout, false);

        RecyclerView cartItemRecyclerView = (RecyclerView) basketView.findViewById(R.id.recyclerView);
        UIUtil.configureRecyclerView(cartItemRecyclerView, this, 1, 1);
        //View basketSummaryView = getCartSummaryView(cartSummary, cartItemListView);

        List<Object> cartItemHeaderList = new ArrayList<>();
        int numItems = 0;
        for (CartItemList cartItemInfoArray : cartItemLists) {
            CartItemHeader cartItemHeader = new CartItemHeader();
            cartItemHeaderList.add(cartItemHeader);
            eventAttribs.put(cartItemInfoArray.getTopCatName() + " Items", String.valueOf(cartItemInfoArray.getTopCatItems()));
            eventAttribs.put(cartItemInfoArray.getTopCatName() + " Value", String.valueOf(cartItemInfoArray.getTopCatTotal()));
            cartItemHeader.setTopCatName(cartItemInfoArray.getTopCatName());
            cartItemHeader.setTopCatItems(cartItemInfoArray.getTopCatItems());
            cartItemHeader.setTopCatTotal(cartItemInfoArray.getTopCatTotal());

            int cartItemsSize = cartItemInfoArray.getCartItems().size();
            numItems += cartItemsSize;
            ArrayList<CartItem> cartItems = cartItemInfoArray.getCartItems();
            for (int i = 0; i < cartItemsSize; i++) {
                cartItemHeaderList.add(cartItems.get(i));
                if (cartItems.get(i).getPromoAppliedType() == 2 ||
                        cartItems.get(i).getPromoAppliedType() == 3) {
                    HashMap<String, String> map = new HashMap<>();
                    if (isCurrentPageRequest) {
                        map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                    } else {
                        map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentNavigationContext());
                    }
                    trackEvent(TrackingAware.PROMO_REDEEMED, map);
                }
            }
        }
        renderHeaderView(numItems);
        HashMap<String, String> fulfillmentInfoIdAndIconHashMap = new HashMap<>();
        if (fulfillmentInfos != null) {
            for (FulfillmentInfo fullfillmentInfo : fulfillmentInfos) {
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

        ViewGroup layoutCheckoutFooter = (ViewGroup) basketView.findViewById(R.id.layoutCheckoutFooter);
        final String cartTotal = UIUtil.formatAsMoney(cartSummary.getTotal());
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, cartTotal,
                getString(R.string.checkOut), true);
        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                trackEvent(TrackingAware.BASKET_CHECKOUT_CLICKED, map);
                if (getCartSummary() != null && getCartSummary().getNoOfItems() > 0) {
                    if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                        launchLogin(TrackEventkeys.NAVIGATION_CTX_SHOW_BASKET, NavigationCodes.GO_TO_BASKET);
                    } else {
                        startCheckout(cartTotal);
                    }
                }
            }
        });
        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter<>(cartItemHeaderList, this,
                faceRupee, faceRobotoRegular, OrderItemDisplaySource.BASKET, false,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, baseImageUrl, getNextScreenNavigationContext());
        cartItemRecyclerView.setAdapter(activeOrderRowAdapter);
        contentLayout.addView(basketView);

        if (!isCurrentPageRequest)
            logViewBasketEvent(cartSummary, eventAttribs);
    }

    private void startCheckout(String cartTotal) {
        Intent intent = new Intent(this, BackButtonActivity.class);
        setNextScreenNavigationContext(TrackEventkeys.CO_BASKET);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_DELIVERY_ADDRESS);
        intent.putExtra(Constants.TOTAL_BASKET_VALUE, cartTotal);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
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
            super.onPositiveButtonClicked(dialogInterface, null, valuePassed);
        }
    }


    public final void setBasketNumItemsDisplay() {
        if (getCartSummary() == null) return;
        updateUIForCartInfo();
        markBasketDirty();
    }

    private void emptyCart() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
        trackEvent(TrackingAware.BASKET_EMPTY_CLICKED, map);
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.emptyCart(new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse cartEmptyApiResponseCallback, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                markBasketChanged(null);
                if (cartEmptyApiResponseCallback.status == 0) {
                    editor.putString(Constants.GET_CART, "0");
                    showBasketEmptyMessage();
                    CartSummary cartSummary = new CartSummary(0, 0, 0);
                    setCartSummary(cartSummary);
                    setBasketNumItemsDisplay();
                } else if (cartEmptyApiResponseCallback.status == ApiErrorCodes.CART_NOT_EXISTS) {
                    showAlertDialog("Cart is already empty");
                } else {
                    handler.sendEmptyMessage(cartEmptyApiResponseCallback.status,
                            cartEmptyApiResponseCallback.message, true);
                }
                editor.apply();
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error);
            }
        });
    }


    private void getCartItems(String fulfillmentIds, final boolean isCurrentPageRequest) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.cartGet(isCurrentPageRequest ?
                        getNextScreenNavigationContext() : getCurrentNavigationContext(),
                fulfillmentIds, new Callback<ApiResponse<CartGetApiResponseContent>>() {
                    @Override
                    public void success(ApiResponse<CartGetApiResponseContent> cartGetApiResponseContentApiResponse, Response response) {
                        if (isSuspended()) return;
                        hideProgressView();
                        if (cartGetApiResponseContentApiResponse.status == 0) {
                            CartSummary cartSummary = cartGetApiResponseContentApiResponse.apiResponseContent.cartSummary;
                            setCartSummary(cartSummary);
                            setBasketNumItemsDisplay();
                            editor.putString(Constants.GET_CART,
                                    String.valueOf(cartSummary.getNoOfItems()));
                            fulfillmentInfos = cartGetApiResponseContentApiResponse.apiResponseContent.fulfillmentInfos;
                            annotationInfoArrayList = cartGetApiResponseContentApiResponse.apiResponseContent.annotationInfos;
                            if (cartGetApiResponseContentApiResponse.apiResponseContent.
                                    cartGetApiCartItemsContent != null
                                    && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists != null
                                    && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists.size() > 0) {
                                cartItemLists = cartGetApiResponseContentApiResponse.apiResponseContent.
                                        cartGetApiCartItemsContent.cartItemLists;
                                renderCartItemList(cartSummary, cartGetApiResponseContentApiResponse
                                        .apiResponseContent.cartGetApiCartItemsContent.baseImgUrl, isCurrentPageRequest);
                            } else {
                                showBasketEmptyMessage();
                                editor.putString(Constants.GET_CART, "0");
                            }
                        } else {
                            handler.sendEmptyMessage(cartGetApiResponseContentApiResponse.status,
                                    cartGetApiResponseContentApiResponse.message);
                        }
                        editor.apply();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        hideProgressView();
                        handler.handleRetrofitError(error, true);
                    }
                });
    }

    private void showBasketEmptyMessage() {
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_empty_data_text, contentView, false);
        ImageView imgEmptyPage = (ImageView) base.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_basket);
        TextView txtEmptyMsg1 = (TextView) base.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.empty_basket_txt1);
        TextView txtEmptyMsg2 = (TextView) base.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setText(R.string.empty_basket_txt2);
        Button btnBlankPage = (Button) base.findViewById(R.id.btnBlankPage);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(false);
            }
        });

        Toolbar toolbar = getToolbar();
        toolbar.setTitle(getString(R.string.my_basket_header));
        if (txtBasketSubTitle != null) txtBasketSubTitle.setVisibility(View.GONE);
        if (basketMenuItem != null) basketMenuItem.setVisible(false);
        contentView.removeAllViews();
        contentView.addView(base);
    }


    @Override
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket,
                                                    Product product, String qty,
                                                    @Nullable View productView, @Nullable HashMap<String, Integer> cartInfo,
                                                    @Nullable EditText editTextQty) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty, viewIncQty,
                btnAddToBasket, product, qty, productView, cartInfo, editTextQty);
        getCartItems(null, true);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.VIEW_BASKET_SCREEN;
    }
}
