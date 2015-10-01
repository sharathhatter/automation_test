package com.bigbasket.mobileapp.adapter.gift;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.GiftItemViewHolder;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;

import java.util.List;

/**
 * Created by manu on 28/9/15.
 */
public class GiftItemListRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private String baseImgUrl;
    private List<GiftItem> giftItems;
    private ActivityAware activityAware;

//    public GiftItemListRecyclerAdapter(List<GiftItem> giftItems,String baseImgUrl,ActivityAware activityAware) {
//        this.giftItems=giftItems;
//        this.activityAware = activityAware;
//        this.baseImgUrl=baseImgUrl;
//    }


    public GiftItemListRecyclerAdapter(ActivityAware activityAware) {
        this.activityAware=activityAware;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) activityAware.getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.uiv3_gift_item_row, viewGroup, false);
        return new GiftItemViewHolder(row);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        System.out.println("manu------in call gift item recycle");
//        GiftItem giftItem=giftItems.get(position);
//        GiftView.setGiftView((GiftItemViewHolder) holder,giftItem,baseImgUrl);


    }

    @Override
    public int getItemCount() {
        return 10;
    }
}
