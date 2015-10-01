package com.bigbasket.mobileapp.fragment.gift;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.gift.GiftItemListRecyclerAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.GiftAddMessageButtonClickListener;
import com.bigbasket.mobileapp.util.UIUtil;

/**
 * Created by manu on 25/9/15.
 */
public class GiftItemListFragment extends BaseFragment {
   private GiftItemListRecyclerAdapter giftItemListRecyclerAdapter;
    Bundle giftBundle;

    public GiftItemListFragment() {
    }
    

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_giftitem_listfragment, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        loadGiftItems(getArguments());
        setAddMessageButton();
    }


    private void loadGiftItems(Bundle giftBundle) {

//        Gift gift = giftBundle.getParcelable(Constants.GIFTS);

        giftItemListRecyclerAdapter=new GiftItemListRecyclerAdapter(getCurrentActivity());

        RecyclerView giftRecyclerView = (RecyclerView) getView().findViewById(R.id.recyclerView);
        UIUtil.configureRecyclerView(giftRecyclerView, getActivity(), 1, 1);

        giftRecyclerView.setAdapter(giftItemListRecyclerAdapter);

    }

    private void setAddMessageButton() {
        Button btnAddMessage=(Button) getView().findViewById(R.id.btnAddMessage);
        btnAddMessage.setOnClickListener(onViewClickListener);
    }

    View.OnClickListener onViewClickListener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
      switch (v.getId()){
          case R.id.btnAddMessage:
              ((GiftAddMessageButtonClickListener) getActivity()).addGiftMessage();
              break;
      }
        }
    };


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
}
