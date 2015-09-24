package com.bigbasket.mobileapp.fragment.order;

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
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

/**
 * Created by mrudula on 24/9/15.
 */
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
            int numOfGifts = args.getInt(Constants.NUM_GIFTS);
            renderGiftLayout(numOfGifts);
        }
    }

    private void renderCheckOutProgressView(LinearLayout layout) {
        String[] array_txtValues = new String[]{"Address", "Gift", "Slots", "Order"};
        Integer[] array_compPos = new Integer[]{0};
        int selectedPos = 1;
        View giftView = UIUtil.getCheckoutProgressView(getActivity(), null, array_txtValues, array_compPos, selectedPos);
        if (giftView != null) layout.addView(giftView, 0);
    }

    private void renderGiftLayout(int count) {
        ViewGroup contentView = getContentView();
        if (contentView == null) return;
        LinearLayout layout = (LinearLayout) contentView.findViewById(R.id.layoutGiftScroll);
        renderCheckOutProgressView(layout);
        TextView textViewCount = (TextView) contentView.findViewById(R.id.textViewNumGifts);
        textViewCount.setText("You have " + count + " gift items in your basket!");
        textViewCount.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoMedium());
        Button btnSkipAndProceed = (Button) contentView.findViewById(R.id.buttonSkipPro);
        btnSkipAndProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        Button btnGiftOptions = (Button) contentView.findViewById(R.id.buttonGiftWrapOpt);
        btnGiftOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
