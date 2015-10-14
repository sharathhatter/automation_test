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

/**
 * Created by mrudula on 1/9/15.
 */
public class ShowCartFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.cart_fragment_list, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getArguments() == null) return;

        ArrayList<CartItemList> cartItemLists = getArguments().getParcelableArrayList(Constants.CART_ITEMS);
        String baseImageUrl = getArguments().getString(Constants.BASE_IMG_URL);
        ArrayList<FulfillmentInfo> fulfillmentInfos = getArguments().getParcelableArrayList(Constants.FULFILLMENT_INFO);
        ArrayList<AnnotationInfo> annotationInfoArrayList = getArguments().getParcelableArrayList(Constants.ANNOTATION_INFO);

        if (cartItemLists != null) {
            renderCartItemList(cartItemLists, baseImageUrl, fulfillmentInfos, annotationInfoArrayList);
        }
    }

    private void renderCartItemList(ArrayList<CartItemList> cartItemLists, String baseImageUrl, ArrayList<FulfillmentInfo> fulfillmentInfos, ArrayList<AnnotationInfo> annotationInfoArrayList) {
        if (getContentView() == null) return;
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
                getActivity(),
                faceRupee, faceRobotoRegular, OrderItemDisplaySource.BASKET, false,
                fulfillmentInfoIdAndIconHashMap, annotationHashMap, baseImageUrl,
                getNextScreenNavigationContext());
        RecyclerView list_show_cart = (RecyclerView) getContentView().findViewById(R.id.list_show_cart);
        list_show_cart.setLayoutManager(new LinearLayoutManager(getActivity()));
        list_show_cart.setAdapter(activeOrderRowAdapter);

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
