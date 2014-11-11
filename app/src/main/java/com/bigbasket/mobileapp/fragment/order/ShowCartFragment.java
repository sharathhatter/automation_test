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
import android.view.*;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.cart.*;
import com.bigbasket.mobileapp.model.order.OrderItemDisplaySource;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.*;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        super.onAsyncTaskComplete(httpOperationResult);
        JsonObject jsonObject = new JsonParser().parse(httpOperationResult.
                getReponseString()).getAsJsonObject();
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = prefer.edit();
        if (httpOperationResult.getUrl().contains(Constants.CART_GET)) {
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            if (status == 0) {
                JsonObject responseJsonObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                JsonObject cartItemsJsonObject = responseJsonObject.get(Constants.CART_ITEMS).getAsJsonObject();
                JsonObject cartSummaryJsonObj = responseJsonObject.get(Constants.CART_SUMMARY).getAsJsonObject();
                CartSummary cartSummary = ParserUtil.parseCartSummary(cartSummaryJsonObj);
                if (!isReadOnly) {
                    setCartInfo(cartSummary);
                    setBasketNumItemsDisplay();
                    editor.putString(Constants.GET_CART, String.valueOf(cartSummary.getNoOfItems()));
                }
                if (responseJsonObject.has(Constants.FULFILLMENT_INFOS) &&
                        !responseJsonObject.get(Constants.FULFILLMENT_INFOS).isJsonNull()) {
                    JsonArray cartFulfillmentInfoJsonArray = responseJsonObject.get(Constants.FULFILLMENT_INFOS).getAsJsonArray();
                    fullfillmentInfos = ParserUtil.parseCartFulfillmentInfoList(cartFulfillmentInfoJsonArray);
                }

                if (responseJsonObject.has(Constants.ANNOTATION_INFO) &&
                        !responseJsonObject.get(Constants.ANNOTATION_INFO).isJsonNull()) {
                    JsonArray cartAnnotaionInfoJsonArray = responseJsonObject.get(Constants.ANNOTATION_INFO).getAsJsonArray();
                    annotationInfoArrayList = ParserUtil.parseCartAnnotationInfoList(cartAnnotaionInfoJsonArray);
                }

                if (cartItemsJsonObject.has(Constants.ITEMS) &&
                        !cartItemsJsonObject.get(Constants.ITEMS).isJsonNull()) {
                    JsonArray cartItemsJsonArray = cartItemsJsonObject.get(Constants.ITEMS).getAsJsonArray();
                    cartItemLists = ParserUtil.parseCartItemList(cartItemsJsonArray);
                    String baseImageUrl = cartItemsJsonObject.get(Constants.BASE_IMG_URL).getAsString();
                    renderCartItemList(cartSummary, baseImageUrl);
                } else {
                    //showBasketEmptyMessage(); //todo
                    editor.putString(Constants.GET_CART, "0");
                }
            } else {
                showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR));
            }
        } else if (httpOperationResult.getUrl().contains(Constants.CART_EMPTY)) {
            if (jsonObject.has(Constants.SUCCESS) && !jsonObject.get(Constants.SUCCESS).isJsonNull()) {
                boolean success = jsonObject.get(Constants.SUCCESS).getAsBoolean();
                if (success) {
                    editor.putString(Constants.GET_CART, "0");
                    //showBasketEmptyMessage();
                    CartSummary cartSummary = new CartSummary(0, 0, 0);
                    setCartInfo(cartSummary);
                    setBasketNumItemsDisplay();
                    finishFragment();
                }
            } else if (jsonObject.has(Constants.STATUS) && !jsonObject.get(Constants.STATUS).isJsonNull()) {
                String status = jsonObject.get(Constants.STATUS).getAsString();
                if (status.equalsIgnoreCase(Constants.ERROR)) {
                    int errorType = jsonObject.get(Constants.ERROR_TYPE).getAsInt();
                    if (errorType == ExceptionUtil.CART_NOT_EXISTS_ERROR) {
                        showErrorMsg("Cart is already empty");
                    }
                }
            }
        }
        editor.commit();
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
        TextView txtCheckout = (TextView) basketView.findViewById(R.id.txtListFooter);
        txtCheckout.setText(getString(R.string.check_out));
        txtCheckout.setTypeface(faceRobotoThin);
        txtCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
                new COReserveQuantityCheckTask(getFragment(), pharmaPrescriptionId).execute();
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

    private BaseFragment getFragment() {
        return this;
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
        txtTotalCartItems.setTypeface(faceRobotoThin);
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
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.CART_EMPTY, null, true,
                false, null);
    }

    private void getCartItems() {
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.CART_GET, null, false,
                true, null);
    }


    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
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
