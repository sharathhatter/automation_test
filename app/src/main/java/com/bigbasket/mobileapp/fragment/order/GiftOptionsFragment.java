package com.bigbasket.mobileapp.fragment.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.gift.GiftHomeActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.task.uiv3.PostGiftTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
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

    private void renderCheckOutProgressView() {
        View base = getContentView();
        if (base == null) return;
        ViewGroup layoutCheckoutProgressContainer = (ViewGroup) base.findViewById(R.id.layoutCheckoutProgressContainer);
        layoutCheckoutProgressContainer.removeAllViews();
        String[] array_txtValues = new String[]{getString(R.string.address),
                getString(R.string.gift), getString(R.string.slots), getString(R.string.order)};
        Integer[] array_compPos = new Integer[]{0};
        int selectedPos = 1;
        View giftView = UIUtil.getCheckoutProgressView(getActivity(), null, array_txtValues,
                array_compPos, selectedPos);
        if (giftView != null) layoutCheckoutProgressContainer.addView(giftView);
    }

    private void renderGiftLayout(final Bundle args) {
        ViewGroup contentView = getContentView();
        if (contentView == null || getCurrentActivity() == null) return;
        renderCheckOutProgressView();
        TextView textViewCount = (TextView) contentView.findViewById(R.id.textViewNumGifts);
        TextView txtWouldYouLike = (TextView) contentView.findViewById(R.id.txtWouldYouLike);
        final Gift gift = args.getParcelable(Constants.GIFTS);
        if (gift == null) return;
        String pluralSuffix = gift.getCount() > 1 ? "s" : "";
        String pluralThemSuffix = gift.getCount() > 1 ? " them " : " it ";
        textViewCount.setText("You have " + gift.getCount() + " gift item" + pluralSuffix + " in your basket!");
        textViewCount.setTypeface(faceRobotoMedium);
        txtWouldYouLike.setText(getString(R.string.giftWrapMsgPrefix) + pluralThemSuffix + getString(R.string.giftWrapMsgSuffix));
        txtWouldYouLike.setTypeface(faceRobotoRegular);
        final String potentialOrderId = args.getString(Constants.P_ORDER_ID);
        Button btnSkipAndProceed = (Button) contentView.findViewById(R.id.buttonSkipPro);
        btnSkipAndProceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkInternetConnection()) {
                    handler.sendOfflineError();
                    return;
                }
                trackEvent(TrackingAware.GIFT_SKIP_AND_PROCEED, null);
                PostGiftTask task = new PostGiftTask<>(getCurrentActivity(), potentialOrderId, null,
                        TrackEventkeys.CO_DELIVERY_OPS);
                task.setHasGift(true); // For showing gift in progress-view on next page
                task.startTask();

            }
        });
        Button btnGiftOptions = (Button) contentView.findViewById(R.id.buttonGiftWrapOpt);
        btnGiftOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackEvent(TrackingAware.GIFT_VIEW_WRAP_OPTS, null);
                openGiftSelection(gift, potentialOrderId);
            }
        });

        TextView txtGiftMsg = (TextView) contentView.findViewById(R.id.txtGiftMsg);
        String giftMsg = UIUtil.strJoin(gift.getGiftSummaryMsg(), "\n\n");
        if (TextUtils.isEmpty(giftMsg)) {
            txtGiftMsg.setVisibility(View.GONE);
        } else {
            txtGiftMsg.setTypeface(faceRobotoRegular);
            txtGiftMsg.setText(giftMsg);
        }

        TextView lblKnowMore = (TextView) contentView.findViewById(R.id.lblKnowMore);
        if (TextUtils.isEmpty(gift.getGiftLink())) {
            lblKnowMore.setVisibility(View.GONE);
        } else {
            SpannableString spannableString = new SpannableString(lblKnowMore.getText());
            spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            lblKnowMore.setText(spannableString);
            lblKnowMore.setTypeface(FontHolder.getInstance(getActivity()).getFaceRobotoBold());
            lblKnowMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                    intent.putExtra(Constants.WEBVIEW_URL, gift.getGiftLink());
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
            });
        }
    }

    private void openGiftSelection(Gift gift, String potentialOrderId) {
        Intent intent = new Intent(getActivity(), GiftHomeActivity.class);
        intent.putExtra(Constants.P_ORDER_ID, potentialOrderId);
        intent.putExtra(Constants.GIFTS, gift);
        startActivityForResult(intent, NavigationCodes.GO_TO_BASKET);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.GO_TO_SLOT_SELECTION) {
            getActivity().setResult(NavigationCodes.GO_TO_SLOT_SELECTION);
            getActivity().finish();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public String getTitle() {
        return getString(R.string.giftOptions);
    }

    @Nullable
    @Override
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.giftOptionsFragmentLayout) : null;
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

    @NonNull
    @Override
    public String getInteractionName() {
        return "GiftOptionsFragment";
    }
}
