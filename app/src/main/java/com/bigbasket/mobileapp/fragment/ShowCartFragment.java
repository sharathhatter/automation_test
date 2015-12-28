package com.bigbasket.mobileapp.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.cart.CartItemHeader;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.order.OrderItemDisplaySource;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowCartFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.cart_fragment_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() == null) return;

        ArrayList<CartItemList> cartItemLists = getArguments().getParcelableArrayList(Constants.CART_ITEMS);
        String baseImageUrl = getArguments().getString(Constants.BASE_IMG_URL);
        ArrayList<FulfillmentInfo> fulfillmentInfos = getArguments().getParcelableArrayList(Constants.FULFILLMENT_INFO);
        ArrayList<AnnotationInfo> annotationInfoArrayList = getArguments().getParcelableArrayList(Constants.ANNOTATION_INFO);
        int currentTabIndex = getArguments().getInt(Constants.CURRENT_TAB_INDEX);

        if (cartItemLists != null) {
            renderCartItemList(cartItemLists, baseImageUrl, fulfillmentInfos, annotationInfoArrayList, currentTabIndex);
        }
    }

    private void renderCartItemList(ArrayList<CartItemList> cartItemLists, String baseImageUrl,
                                    ArrayList<FulfillmentInfo> fulfillmentInfos,
                                    ArrayList<AnnotationInfo> annotationInfoArrayList,
                                    int currentTabIndex) {
        if (getContentView() == null || getCurrentActivity() == null) return;

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
                cartItemHeaderList.add(cartItems.get(i));
            }
        }

        HashMap<String, String> fulfillmentInfoIdAndIconHashMap = new HashMap<>();
        if (fulfillmentInfos != null) {
            for (FulfillmentInfo fullfillmentInfo : fulfillmentInfos) {
                fulfillmentInfoIdAndIconHashMap.put(fullfillmentInfo.getFulfillmentId(),
                        fullfillmentInfo.getIcon());
                if (fullfillmentInfo.getDisplayName() == null ||
                        fullfillmentInfo.getIcon() == null)
                    continue;
                cartItemHeaderList.add(fullfillmentInfo);
            }
        }

        HashMap<String, AnnotationInfo> annotationHashMap = new HashMap<>();
        if (annotationInfoArrayList != null) {
            for (AnnotationInfo anAnnotationInfoArrayList : annotationInfoArrayList) {
                annotationHashMap.put(anAnnotationInfoArrayList.getAnnotationId(),
                        anAnnotationInfoArrayList);
                if (anAnnotationInfoArrayList.getDescription() == null ||
                        anAnnotationInfoArrayList.getIconUrl() == null)
                    continue;
                cartItemHeaderList.add(anAnnotationInfoArrayList);
            }
        }

        ActiveOrderRowAdapter activeOrderRowAdapter = new ActiveOrderRowAdapter<>(cartItemHeaderList,
                getCurrentActivity(),
                faceRupee, faceRobotoRegular, OrderItemDisplaySource.BASKET, false,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, baseImageUrl,
                getNextScreenNavigationContext(), currentTabIndex);
        RecyclerView listShowCart = (RecyclerView) getContentView().findViewById(R.id.list_show_cart);
        listShowCart.setLayoutManager(new LinearLayoutManager(getActivity()));
        listShowCart.setAdapter(activeOrderRowAdapter);

        //scroll list to position
        onBasketProductAlterScrollHandler(listShowCart, cartItemHeaderList);
    }

    private void onBasketProductAlterScrollHandler(RecyclerView listShowCart, List<Object> cartItemHeaderList) {
        int listScrollPosition = getArguments().getInt(Constants.ITEM_SCROLL_POSITION, 0);
        if (listScrollPosition > 2 && cartItemHeaderList.size() > 2) {
            listShowCart.scrollToPosition(listScrollPosition);
        }
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

    @NonNull
    @Override
    public String getInteractionName() {
        return "ShowCartFragment";
    }
}
