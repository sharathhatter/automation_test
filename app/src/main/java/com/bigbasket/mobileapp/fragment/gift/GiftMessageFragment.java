package com.bigbasket.mobileapp.fragment.gift;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.gift.GiftItemMessageRecyclerAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.gift.GiftItemAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.util.UIUtil;


public class GiftMessageFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_giftitem_listfragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderGiftMessages();
    }

    private void renderGiftMessages() {
        View base = getView();
        if (base == null) return;
        setUpGiftMsgFragment(true);
    }

    private void setUpGiftMsgFragment(boolean useCommonMsg) {
        View base = getView();
        if (base == null) return;
        RecyclerView recyclerViewGiftMsgs = (RecyclerView) base.findViewById(R.id.recyclerViewGifts);
        UIUtil.configureRecyclerView(recyclerViewGiftMsgs, getActivity(), 1, 1);

        Gift gift = ((GiftItemAware) getActivity()).getGifts();

        GiftItemMessageRecyclerAdapter giftItemMessageRecyclerAdapter =
                new GiftItemMessageRecyclerAdapter<>(this, gift, useCommonMsg);
        recyclerViewGiftMsgs.setAdapter(giftItemMessageRecyclerAdapter);
    }

    public void redrawGiftMessageRecyclerView(int position) {
        View base = getView();
        if (base == null) return;
        RecyclerView recyclerViewGiftMsgs = (RecyclerView) base.findViewById(R.id.recyclerViewGifts);
        GiftItemMessageRecyclerAdapter adapter = (GiftItemMessageRecyclerAdapter) recyclerViewGiftMsgs.getAdapter();
        adapter.notifyItemChanged(position + 1); // Since position 0 is of header-view
        adapter.notifyItemChanged(adapter.getItemCount() - 1); // Notify footer
    }

    public boolean useCommonMsg() {
        View base = getView();
        if (base == null) return true;
        RecyclerView recyclerViewGiftMsgs = (RecyclerView) base.findViewById(R.id.recyclerViewGifts);
        GiftItemMessageRecyclerAdapter adapter = (GiftItemMessageRecyclerAdapter) recyclerViewGiftMsgs.getAdapter();
        return adapter.isShowCommonMsg();
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
        return GiftMessageFragment.class.getName();
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "GiftMessageFragment";
    }
}
