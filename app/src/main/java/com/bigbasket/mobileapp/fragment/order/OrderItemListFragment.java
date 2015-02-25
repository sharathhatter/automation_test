package com.bigbasket.mobileapp.fragment.order;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.cart.CartItemHeader;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.order.OrderItemDisplaySource;
import com.bigbasket.mobileapp.model.order.OrderSummary;
import com.bigbasket.mobileapp.model.slot.SlotGroup;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class OrderItemListFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OrderSummary orderSummary = getArguments().getParcelable(Constants.ORDER_REVIEW_SUMMARY);
        renderCartItems(orderSummary);
    }

    private void renderCartItems(final OrderSummary orderSummary) {
        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        showProgressView();
        HashMap<String, String> fulfillmentInfoIdAndIconHashMap = new HashMap<>();
        HashMap<String, AnnotationInfo> annotationHashMap = new HashMap<>();

        List<Object> cartItemConsolidatedList = new ArrayList<>();

        // Add cartitems and top-category headers
        for (CartItemList cartItemList : orderSummary.getCartItems()) {
            CartItemHeader cartItemHeader = new CartItemHeader();
            cartItemHeader.setTopCatItems(cartItemList.getTopCatItems());
            cartItemHeader.setTopCatName(cartItemList.getTopCatName());
            cartItemHeader.setTopCatTotal(cartItemList.getTopCatTotal());
            cartItemConsolidatedList.add(cartItemHeader);

            int numCartItems = cartItemList.getCartItems().size();
            ArrayList<CartItem> cartItems = cartItemList.getCartItems();
            for (int i = 0; i < numCartItems; i++) {
                CartItem cartItem = cartItems.get(i);
                cartItem.setIndex(i);
                cartItemConsolidatedList.add(cartItem);
                if (!TextUtils.isEmpty(cartItem.getFulfillmentId())) {
                    fulfillmentInfoIdAndIconHashMap.put(cartItem.getFulfillmentId(), "");
                }
            }
        }

        // Add fulfillment details to fulfillmentInfoIdMap and also add FulFillmentInfo to consolidated list
        ArrayList<SlotGroup> slotGroups = orderSummary.getSlotGroups();
        if (slotGroups != null && slotGroups.size() > 0) {
            for (SlotGroup slotGroup : slotGroups) {
                if (fulfillmentInfoIdAndIconHashMap.containsKey(slotGroup.getFulfillmentInfo().getFulfillmentId())) {
                    fulfillmentInfoIdAndIconHashMap.put(slotGroup.getFulfillmentInfo().getFulfillmentId(),
                            slotGroup.getFulfillmentInfo().getIcon());
                    cartItemConsolidatedList.add(slotGroup.getFulfillmentInfo());
                }
            }
        }

        // Updated annotationMap, and add annotation object to consolidated list
        ArrayList<AnnotationInfo> annotationInfos = orderSummary.getAnnotationInfos();
        if (annotationInfos != null && annotationInfos.size() > 0) {
            for (AnnotationInfo annotationInfo : annotationInfos) {
                annotationHashMap.put(annotationInfo.getAnnotationId(), annotationInfo);
                if (TextUtils.isEmpty(annotationInfo.getDescription()) || TextUtils.isEmpty(annotationInfo.getDisplayName())) {
                    continue;
                }
                cartItemConsolidatedList.add(annotationInfo);
            }
        }

        // Render the consolidated listview
        ListView listView = new ListView(getActivity());
        listView.setDividerHeight(0);
        listView.setDivider(null);
        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter<>(cartItemConsolidatedList,
                this, faceRupee, faceRobotoRegular, OrderItemDisplaySource.ORDER_DISPLAY, true,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, orderSummary.getBaseImgUrl(), null);
        listView.setAdapter(activeOrderRowAdapter);
        hideProgressView();
        contentView.addView(listView);
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public void setTitle() {
        // Do nothing
    }

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderItemListFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_REVIEW_ITEMS_SCREEN;
    }
}