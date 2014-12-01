package com.bigbasket.mobileapp.fragment.order;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
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
import android.widget.ImageView;
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
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.ExceptionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
        getCartItems();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty_basket:
                showAlertDialog(getActivity(), null, "Remove all the products from basket?", DialogButton.YES,
                        DialogButton.NO, Constants.EMPTY_BASKET, null, "Empty Basket");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void renderCartItemList(CartSummary cartSummary, String baseImageUrl) {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        contentView.removeAllViews();
        contentView.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View basketView = inflater.inflate(R.layout.uiv3_list_with_action, null);

        View basketSummaryView = getCartSummaryView(cartSummary);

        List<Object> cartItemHeaderList = new ArrayList<>();
        for (CartItemList cartItemInfoArray : cartItemLists) {

            CartItemHeader cartItemHeader = new CartItemHeader();
            cartItemHeaderList.add(cartItemHeader);

            cartItemHeader.setTopCatName(cartItemInfoArray.getTopCatName());
            cartItemHeader.setTopCatItems(cartItemInfoArray.getTopCatItems());
            cartItemHeader.setTopCatTotal(cartItemInfoArray.getTopCatTotal());

            int cartItemsSize = cartItemInfoArray.getCartItems().size();
            ArrayList<CartItem> cartItems = cartItemInfoArray.getCartItems();
            for (int i = 0; i < cartItemsSize; i++) {
                cartItems.get(i).setIndex(i);
                cartItemHeaderList.add(cartItems.get(i));

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

        ListView cartItemListView = (ListView) basketView.findViewById(R.id.listWithFixedFooter);
        cartItemListView.addHeaderView(basketSummaryView);
        Button btnFooterCheckout = (Button) basketView.findViewById(R.id.btnListFooter);
        btnFooterCheckout.setText(getString(R.string.check_out));
        btnFooterCheckout.setTypeface(faceRobotoRegular);
        btnFooterCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cartInfo != null && cartInfo.getNoOfItems() > 0) {
                    if (AuthParameters.getInstance(getActivity()).isAuthTokenEmpty()) {
                        showAlertDialog(getActivity(), "Login", getString(R.string.login_to_place_order),
                                DialogButton.OK, DialogButton.NO, Constants.LOGIN_REQUIRED, null, "Login");
                    } else {
                        new COMarketPlaceCheckTask<>(getCurrentActivity()).execute();
                    }
                }
            }
        });
        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter(cartItemHeaderList, ((BaseActivity) getActivity()),
                this, faceRupee, faceRobotoRegular, OrderItemDisplaySource.BASKET, false,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, baseImageUrl);
        cartItemListView.setDivider(null);
        cartItemListView.setDividerHeight(0);
        cartItemListView.setAdapter(activeOrderRowAdapter);
        contentView.addView(basketView);
    }

    @Override
    public void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.EMPTY_BASKET:
                    if (cartItemLists != null)
                        emptyCart();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    public View getCartSummaryView(CartSummary cartSummary) {
        if (cartSummary == null) return null;
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_basket_header, null);
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
                txtTotalCartItems.setText(cartSummary.getNoOfItems() + " Items/ ");
            } else {
                txtTotalCartItems.setText(cartSummary.getNoOfItems() + " Item/ ");
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
        SharedPreferences.Editor editor = getActivity().getSharedPreferences("myCustomSharedPrefs", Activity.MODE_PRIVATE).edit();
        editor.putString(Constants.GET_CART, String.valueOf(cartInfo.getNoOfItems()));
        editor.commit();
        //cartCountTextView.setText("" + cartInfo.getNoOfItems());
        if (cartInfo.getNoOfItems() == 0) {
            //cartCountTextView.setVisibility(View.INVISIBLE);
        } else {
            //cartCountTextView.setVisibility(View.VISIBLE);
        }
    }


    private void emptyCart() {
        if (getActivity() == null) return;
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.emptyCart(new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse cartEmptyApiResponseCallback, Response response) {
                if (cartEmptyApiResponseCallback.status == 0) {
                    editor.putString(Constants.GET_CART, "0");
                    showBasketEmptyMessage();
                    CartSummary cartSummary = new CartSummary(0, 0, 0);
                    setCartInfo(cartSummary);
                    setBasketNumItemsDisplay();
                } else if (cartEmptyApiResponseCallback.status == ExceptionUtil.CART_NOT_EXISTS_ERROR) {
                    showErrorMsg("Cart is already empty");
                } else {
                    // TODO : Improve error handling
                    showErrorMsg("Server Error");
                }
                editor.commit();

            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private void getCartItems() {
        if (getActivity() == null) return;
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressView();
        bigBasketApiService.cartGet(new Callback<ApiResponse<CartGetApiResponseContent>>() {
            @Override
            public void success(ApiResponse<CartGetApiResponseContent> cartGetApiResponseContentApiResponse, Response response) {
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
                    showErrorMsg("Server Error");
                    // TODO : Improve error handling
                }
                editor.commit();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressView();
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
        View base = inflater.inflate(R.layout.uiv3_empty_data_text, null);
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
                                                    ImageView imgDecQty, ImageView imgIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, imgDecQty, imgIncQty,
                btnAddToBasket, editTextQty, product, qty);
        getCartItems();
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ShowCartFragment.class.getName();
    }
}
