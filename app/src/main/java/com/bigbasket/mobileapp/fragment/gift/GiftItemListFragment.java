package com.bigbasket.mobileapp.fragment.gift;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.gift.GiftItemListRecyclerAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.gift.GiftItemAware;
import com.bigbasket.mobileapp.interfaces.gift.GiftOperationAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.RecyclerViewDividerItemDecoration;


public class GiftItemListFragment extends BaseFragment implements GiftOperationAware {

    GiftItemListRecyclerAdapter mGiftItemListRecyclerAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_giftitem_listfragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadGiftItems();
        setAddMessageButton();
    }

    private void loadGiftItems() {
        View base = getView();
        if (base == null) return;

        Gift gift = ((GiftItemAware) getActivity()).getGifts();

        mGiftItemListRecyclerAdapter =
                new GiftItemListRecyclerAdapter<>(this, gift.getGiftItems(),
                        gift.getBaseImgUrl());

        RecyclerView giftRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerViewGifts);
        UIUtil.configureRecyclerView(giftRecyclerView, getActivity(), 1, 1);
        giftRecyclerView.addItemDecoration(new RecyclerViewDividerItemDecoration(getActivity()));

        giftRecyclerView.setAdapter(mGiftItemListRecyclerAdapter);

    }

    private void setAddMessageButton() {
        View base = getView();
        if (base == null) return;
        base.findViewById(R.id.btnAddMessage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewPager pager = (ViewPager) getActivity().findViewById(R.id.viewPager);
                pager.setCurrentItem(1);
            }
        });
    }


    @Override
    public String getTitle() {
        return getString(R.string.giftOptions);
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return null;
    }

    @Override
    public String getScreenTag() {
        return null;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return GiftItemListFragment.class.getName();
    }

    @Override
    public void updateGiftItemQty(int position, int reservedQty) {
        if (getCurrentActivity() == null) return;
        Gift gift = ((GiftItemAware) getActivity()).getGifts();
        GiftItem giftItem = gift.getGiftItems().get(position);
        if (reservedQty > giftItem.getQuantity()) {
            getCurrentActivity().showToast("You can't add more than " + giftItem.getQuantity()
                    + " item" + (giftItem.getQuantity() > 0 ? "s" : ""));
        } else if (reservedQty < 0) {
            getCurrentActivity().showToast("Gift quantity can't be negative");
        }
        giftItem.setReservedQty(reservedQty);
        mGiftItemListRecyclerAdapter.notifyItemChanged(position);
    }
}
