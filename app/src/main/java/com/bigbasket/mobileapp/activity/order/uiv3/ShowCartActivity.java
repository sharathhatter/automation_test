package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
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
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.facebook.share.widget.LikeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShowCartActivity extends BackButtonActivity {

    private ArrayList<CartItemList> cartItemLists;
    private ArrayList<FulfillmentInfo> fullfillmentInfos;
    private ArrayList<AnnotationInfo> annotationInfoArrayList;
    private MenuItem basketMenuItem;
    private TextView txtBasketSubTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.my_basket_header));
    }

    @Override
    public void onResume() {
        super.onResume();
        getCartItems(null);
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.empty_basket, menu);
        basketMenuItem = menu.getItem(0);
        basketMenuItem.setVisible(false);
    }

    private void renderHearView(int totalItemCount){
        Toolbar toolbar = getToolbar();
        if(txtBasketSubTitle!=null) toolbar.removeView(txtBasketSubTitle);
        txtBasketSubTitle = (TextView) getLayoutInflater().inflate(R.layout.basket_header_layout, toolbar, false);
        txtBasketSubTitle.setTypeface(faceRobotoRegular);
        toolbar.addView(txtBasketSubTitle);
        if(totalItemCount>0){
            String itemString = totalItemCount > 1 ? " Items" : " Item";
            txtBasketSubTitle.setText(totalItemCount + itemString);
            basketMenuItem.setVisible(true);
        }else {
            txtBasketSubTitle.setVisibility(View.GONE);
            basketMenuItem.setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty_basket:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
                if (preferences.getString(Constants.GET_CART, "0") != null
                        && !preferences.getString(Constants.GET_CART, "0").equals("0")) {
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
        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_TOPNAV);
        trackEvent(TrackingAware.BASKET_VIEW_CLICKED, eventAttribs);
    }

    private void renderCartItemList(CartSummary cartSummary, String baseImageUrl) {
        Map<String, String> eventAttribs = new HashMap<>();

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        contentLayout.removeAllViews();
        contentLayout.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        LayoutInflater inflater = getLayoutInflater();
        View basketView = inflater.inflate(R.layout.uiv3_list_with_action, contentLayout, false);

        ListView cartItemListView = (ListView) basketView.findViewById(R.id.listWithFixedFooter);
        //View basketSummaryView = getCartSummaryView(cartSummary, cartItemListView);

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
                cartItemHeaderList.add(cartItems.get(i));
                if (cartItems.get(i).getPromoAppliedType() == 2 ||
                        cartItems.get(i).getPromoAppliedType() == 3) {
                    trackEvent(TrackingAware.PROMO_REDEEMED, null);
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


        //todo change button style
        //cartItemListView.addHeaderView(basketSummaryView);
        Button btnFooterCheckout = (Button) basketView.findViewById(R.id.btnListFooter);
        btnFooterCheckout.setText(getString(R.string.check_out).toUpperCase());
        btnFooterCheckout.setTypeface(faceRobotoRegular);
        btnFooterCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCartInfo() != null && getCartInfo().getNoOfItems() > 0) {
                    if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                        if (getCurrentActivity() != null) {
                            //getCurrentActivity().launchLogin(TrackEventkeys.NAVIGATION_CTX_SHOW_BASKET,
                            // FragmentCodes.START_VIEW_BASKET); //todo check for this
                        }
                    } else {
                        new COMarketPlaceCheckTask<>(getCurrentActivity()).startTask();
                    }
                }
            }
        });
        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter<>(cartItemHeaderList, this,
                faceRupee, faceRobotoRegular, OrderItemDisplaySource.BASKET, false,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, baseImageUrl, getNavigationCtx());
        cartItemListView.setDivider(null);
        cartItemListView.setDividerHeight(0);
        cartItemListView.setAdapter(activeOrderRowAdapter);
        contentLayout.addView(basketView);

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
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_basket_header, parent, false);
        TextView lblSaving = (TextView) base.findViewById(R.id.lblSaving);
        lblSaving.setTypeface(faceRobotoRegular);

        TextView txtSaving = (TextView) base.findViewById(R.id.txtSaving);
        txtSaving.setTypeface(faceRobotoRegular);

        String totalSaveAmount = UIUtil.formatAsMoney(cartSummary.getSavings());
        if (!totalSaveAmount.equals("0")) {
            Spannable savingSpannable = new SpannableString("`" + UIUtil.formatAsMoney(cartSummary.getSavings()));
            savingSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtSaving.setText(savingSpannable);
        } else {
            txtSaving.setVisibility(View.GONE);
            lblSaving.setVisibility(View.GONE);
        }

        String totalBasketAmount = UIUtil.formatAsMoney(cartSummary.getTotal());
        TextView txtTotal = (TextView) base.findViewById(R.id.txtTotal);
        txtTotal.setTypeface(faceRobotoRegular);
        if (!totalBasketAmount.equals("0")) {
            Spannable totalSpannable = new SpannableString("`" + totalBasketAmount + (!totalSaveAmount.equals("0") ? " | " : ""));
            totalSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), 0, 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtTotal.setText(totalSpannable);
        } else {
            txtTotal.setText("");
            txtTotal.setVisibility(View.INVISIBLE);
        }

        // TextView on Gingerbread and Button on HoneyComb onwards
        View viewEmptyBasket = base.findViewById(R.id.viewEmptyBasket);
        // Since button extends textview hence it'll work for both
        ((TextView) viewEmptyBasket).setTypeface(faceRobotoRegular);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ((TextView) viewEmptyBasket).setAllCaps(false);
        }
        viewEmptyBasket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
                if (preferences.getString(Constants.GET_CART, "0") != null
                        && !preferences.getString(Constants.GET_CART, "0").equals("0")) {
                    showAlertDialog(null, getString(R.string.removeAllProducts), DialogButton.YES,
                            DialogButton.NO, Constants.EMPTY_BASKET, null, getString(R.string.emptyBasket));
                }
            }
        });
        return base;
    }


    public final void setBasketNumItemsDisplay() {
        if (getCartInfo() == null) return;
        updateUIForCartInfo();
        markBasketDirty();
    }

    private void emptyCart() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        trackEvent(TrackingAware.BASKET_EMPTY_CLICKED, null);
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
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
                    showAlertDialog("Cart is already empty");
                } else {
                    handler.sendEmptyMessage(cartEmptyApiResponseCallback.status,
                            cartEmptyApiResponseCallback.message, true);
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
        if (!DataUtil.isInternetAvailable(getCurrentActivity())){
            handler.sendOfflineError(true);
            return;
        }
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.cartGet(fulfillmentIds, new Callback<ApiResponse<CartGetApiResponseContent>>() {
            @Override
            public void success(ApiResponse<CartGetApiResponseContent> cartGetApiResponseContentApiResponse, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                if (cartGetApiResponseContentApiResponse.status == 0) {
                    CartSummary cartSummary = cartGetApiResponseContentApiResponse.apiResponseContent.cartSummary;
                        setCartInfo(cartSummary);
                        setBasketNumItemsDisplay();
                        editor.putString(Constants.GET_CART,
                                String.valueOf(cartSummary.getNoOfItems()));
                    fullfillmentInfos = cartGetApiResponseContentApiResponse.apiResponseContent.fulfillmentInfos;
                    annotationInfoArrayList = cartGetApiResponseContentApiResponse.apiResponseContent.annotationInfos;
                    if (cartGetApiResponseContentApiResponse.apiResponseContent.
                            cartGetApiCartItemsContent != null
                            && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists != null
                            && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists.size() > 0) {
                        cartItemLists = cartGetApiResponseContentApiResponse.apiResponseContent.
                                cartGetApiCartItemsContent.cartItemLists;
                        renderHearView(cartSummary.getNoOfItems());
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
        if(txtBasketSubTitle!=null) txtBasketSubTitle.setVisibility(View.GONE);
        if(basketMenuItem!=null) basketMenuItem.setVisible(false);
        contentView.removeAllViews();
        contentView.addView(base);
    }


    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket,
                                                    Product product, String qty,
                                                    @Nullable View productView) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty, viewIncQty,
                btnAddToBasket, product, qty, productView);
        getCartItems(null);
    }

    public String getNavigationCtx() {
        return TrackEventkeys.NAVIGATION_CTX_SHOW_BASKET;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.VIEW_BASKET_SCREEN;
    }
}
