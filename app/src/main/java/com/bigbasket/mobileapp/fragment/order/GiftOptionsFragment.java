package com.bigbasket.mobileapp.fragment.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.gift.GiftHomeActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.task.uiv3.PostGiftTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

public class GiftOptionsFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_giftoptions_fragment, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            renderGiftLayout(args);
        }
    }

    private void renderCheckOutProgressView(LinearLayout layout) {
        String[] array_txtValues = new String[]{"Address", "Gift", "Slots", "Order"};
        Integer[] array_compPos = new Integer[]{0};
        int selectedPos = 1;
        View giftView = UIUtil.getCheckoutProgressView(getActivity(), null, array_txtValues, array_compPos, selectedPos);
        if (giftView != null) layout.addView(giftView, 0);
    }

    private void renderGiftLayout(final Bundle args) {
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        LinearLayout layout = (LinearLayout) contentView.findViewById(R.id.layoutGiftScroll);
        renderCheckOutProgressView(layout);
        TextView textViewCount = (TextView) contentView.findViewById(R.id.textViewNumGifts);
        final Gift gift = args.getParcelable(Constants.GIFTS);
        if (gift != null) {
            textViewCount.setText("You have " + gift.getCount() + " gift items in your basket!");
            textViewCount.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoMedium());
        }
        final String potentialOrderId = args.getString(Constants.P_ORDER_ID);
        Button btnSkipAndProceed = (Button) contentView.findViewById(R.id.buttonSkipPro);
        btnSkipAndProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInternetConnection()) {
                    handler.sendOfflineError();
                    return;
                }
                new PostGiftTask<>(getCurrentActivity(), potentialOrderId, null,
                        TrackEventkeys.CO_DELIVERY_OPS).startTask();
            }
        });
        Button btnGiftOptions = (Button) contentView.findViewById(R.id.buttonGiftWrapOpt);
        btnGiftOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGiftSelection(gift, potentialOrderId);
            }
        });
    }

    private void openGiftSelection(Gift gift, String potentialOrderId) {
        Intent intent = new Intent(getActivity(), GiftHomeActivity.class);
        intent.putExtra(Constants.P_ORDER_ID, potentialOrderId);
        intent.putExtra(Constants.GIFTS, gift);
        startActivity(intent);
    }

    @Override
    public String getTitle() {
        return getString(R.string.giftOptions);
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.giftOptionsFragment) : null;
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.GIFT_OPTIONS_SCREEN;
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return GiftOptionsFragment.class.getName();
    }
}
