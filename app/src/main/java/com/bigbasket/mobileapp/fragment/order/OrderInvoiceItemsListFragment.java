package com.bigbasket.mobileapp.fragment.order;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.cart.CartItemHeader;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.model.order.OrderItemDisplaySource;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.RecyclerViewDividerItemDecoration;

import java.util.ArrayList;
import java.util.List;


public class OrderInvoiceItemsListFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        OrderInvoice orderInvoice = getArguments().getParcelable(Constants.ACTION_TAB_TAG);
        setTitle("Order Details");
        if (orderInvoice == null) return;
        ///logOrderItemTabClicked(orderInvoice);
        renderCartItems(orderInvoice);
        setNextScreenNavigationContext(TrackEventkeys.NC_ORDER_DETAIL);
    }

    private void renderCartItems(OrderInvoice orderInvoice) {
        if (getActivity() == null) return;

        ViewGroup contentView = getContentView();
        if (contentView == null) return;

        showProgressView();

        List<Object> cartItemConsolidatedList = new ArrayList<>();

        // Add cartitems and top-category headers
        for (CartItemList cartItemList : orderInvoice.getCartItems()) {
            CartItemHeader cartItemHeader = new CartItemHeader();
            cartItemHeader.setTopCatItems(cartItemList.getTopCatItems());
            cartItemHeader.setTopCatName(cartItemList.getTopCatName());
            cartItemHeader.setTopCatTotal(cartItemList.getTopCatTotal());
            cartItemConsolidatedList.add(cartItemHeader);

            int numCartItems = cartItemList.getCartItems().size();
            ArrayList<CartItem> cartItems = cartItemList.getCartItems();
            for (int i = 0; i < numCartItems; i++) {
                CartItem cartItem = cartItems.get(i);
                cartItemConsolidatedList.add(cartItem);
            }
        }

        RecyclerView recyclerView = UIUtil.getResponsiveRecyclerView(getActivity(), 1, 1, contentView);
        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter<>(cartItemConsolidatedList, this,
                faceRupee, faceRobotoRegular, OrderItemDisplaySource.ORDER_DISPLAY, true,
                null, null, orderInvoice.getBaseImgUrl(), null, Constants.DEFAULT_TAB_INDEX);
        recyclerView.addItemDecoration(new RecyclerViewDividerItemDecoration(getActivity()));
        recyclerView.setAdapter(activeOrderRowAdapter);
        hideProgressView();
        contentView.addView(recyclerView);
    }

    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return OrderInvoiceItemsListFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ORDER_DETAILS_ITEMS_SCREEN;
    }
}
