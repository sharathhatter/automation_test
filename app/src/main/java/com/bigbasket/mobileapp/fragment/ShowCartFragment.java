package com.bigbasket.mobileapp.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.order.uiv3.ShowCartActivity;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
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
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mrudula on 1/9/15.
 */
public class ShowCartFragment extends BaseFragment {

    ViewGroup layoutCheckoutFooter;
    private ArrayList<FulfillmentInfo> fulfillmentInfos;
    private ArrayList<AnnotationInfo> annotationInfoArrayList;
    private ListView listView_cart;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment_list, container, false);
        listView_cart = (ListView) view.findViewById(R.id.listView_cart);
        layoutCheckoutFooter = (ViewGroup) view.findViewById(R.id.layoutCheckoutFooter);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() == null) return;

        ArrayList<CartItemList> cartItemLists = getArguments().getParcelableArrayList(Constants.CART_ITEMS);
        String baseImageUrl = getArguments().getString(Constants.BASE_IMG_URL);
        boolean isCurrentPageRequest = getArguments().getBoolean(Constants.CURRENT_PAGE);
        CartSummary cartSummary = getArguments().getParcelable(Constants.CART_SUMMARY);

        if (cartItemLists != null) {
            renderCartItemList(cartItemLists, baseImageUrl,isCurrentPageRequest,cartSummary);
        }
    }

    private void renderCartItemList(ArrayList<CartItemList> cartItemLists, String baseImageUrl,boolean isCurrentPageRequest,CartSummary cartSummary) {
        Map<String, String> eventAttribs = new HashMap<>();

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

        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter<>(cartItemHeaderList, getActivity(),
                faceRupee, faceRobotoRegular, OrderItemDisplaySource.BASKET, false,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, baseImageUrl, getNextScreenNavigationContext());

        listView_cart.setAdapter(activeOrderRowAdapter);

        if (!isCurrentPageRequest)
            logViewBasketEvent(cartSummary,eventAttribs);

    }

    private void logViewBasketEvent(CartSummary cartSummary, Map<String, String> eventAttribs) {
        if (cartSummary == null) return;
        eventAttribs.put(TrackEventkeys.TOTAL_ITEMS_IN_BASKET, String.valueOf(cartSummary.getNoOfItems()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_VALUE, String.valueOf(cartSummary.getTotal()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_SAVING, String.valueOf(cartSummary.getSavings()));
        trackEvent(TrackingAware.BASKET_VIEW_SHOWN, eventAttribs, null, null, false, true);
    }

    @Override
    public String getTitle() {
        return getString(R.string.my_basket_header);
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.listView_container) : null;
    }

    @Override
    public String getScreenTag() {
        return ShowCartFragment.class.getName();
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return TrackEventkeys.VIEW_BASKET_SCREEN;
    }
}
