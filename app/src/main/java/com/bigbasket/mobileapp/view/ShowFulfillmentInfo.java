package com.bigbasket.mobileapp.view;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;


public class ShowFulfillmentInfo<T> {

    private FulfillmentInfo fulfillmentInfo;
    private T ctx;
    private Typeface faceRobotoSlabNrml;
    private ActiveOrderRowAdapter.FulfillmentInfoViewHolder holder;

    public ShowFulfillmentInfo(FulfillmentInfo fulfillmentInfo, T ctx,
                               Typeface faceRobotoSlabNrml,
                               ActiveOrderRowAdapter.FulfillmentInfoViewHolder holder) {
        this.fulfillmentInfo = fulfillmentInfo;
        this.ctx = ctx;
        this.faceRobotoSlabNrml = faceRobotoSlabNrml;
        this.holder = holder;
    }

    public void showFulfillmentInfo(boolean showImgLiquorIcon, boolean showTC) {
        RelativeLayout layoutInfoMsg = holder.getLayoutInfoMsg();
        layoutInfoMsg.setBackgroundResource(R.drawable.background);

        if (showImgLiquorIcon) {
            ImageView imgLiquorIcon = holder.getImgLiquorIcon();
            if (fulfillmentInfo.getIcon() != null && !fulfillmentInfo.getIcon().equalsIgnoreCase("null")) {
                imgLiquorIcon.setVisibility(View.VISIBLE);
                UIUtil.displayAsyncImage(imgLiquorIcon, fulfillmentInfo.getIcon());
            } else {
                imgLiquorIcon.setVisibility(View.VISIBLE);
            }
        }

        final TextView txtFulfilledBy = holder.getTxtFulfilledBy();
        if (!TextUtils.isEmpty(fulfillmentInfo.getFulfilledBy()) && !fulfillmentInfo.getFulfilledBy().equalsIgnoreCase("null")) {
            if (showImgLiquorIcon) { // show basket page
                String prefix = " - Indicates " + fulfillmentInfo.getDisplayName() + " products fulfilled by ";
                String postFix = fulfillmentInfo.getFulfilledBy();
                if (!TextUtils.isEmpty(fulfillmentInfo.getFulfilledByInfoPage())) {
                    SpannableString content = new SpannableString(prefix + postFix);
                    int prefixLen = prefix.length();
                    content.setSpan(new ForegroundColorSpan(ContextCompat.getColor(((AppOperationAware) ctx).getCurrentActivity()
                                    , R.color.link_color)),
                            prefixLen - 1, content.length(), 0);
                    txtFulfilledBy.setVisibility(View.VISIBLE);
                    txtFulfilledBy.setText(content);
                    txtFulfilledBy.setTextSize(13);
                    txtFulfilledBy.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showFulfillmentInfoPage(fulfillmentInfo.getFulfilledByInfoPage());
                        }
                    });
                } else {
                    txtFulfilledBy.setVisibility(View.VISIBLE);
                    txtFulfilledBy.setText(prefix + postFix);
                    txtFulfilledBy.setTextSize(13);
                    txtFulfilledBy.setOnClickListener(null);
                }
            } else { // for past order review page
                txtFulfilledBy.setTypeface(faceRobotoSlabNrml);
                txtFulfilledBy.setVisibility(View.VISIBLE);
                txtFulfilledBy.setText(fulfillmentInfo.getFulfilledBy());
            }

        } else {
            txtFulfilledBy.setTextSize(15);
            txtFulfilledBy.setVisibility(View.GONE);
        }

        if (showTC) {
            //TextView txtTCLabel = (TextView) base.findViewById(R.id.txtTCLabel);
            TextView txtTC1 = holder.getTxtTC1();
            if (fulfillmentInfo.getTc1() != null && fulfillmentInfo.getTc1().length() > 0) {
                txtTC1.setVisibility(View.VISIBLE);
                txtTC1.setText(fulfillmentInfo.getTc1());
            } else {
                txtTC1.setVisibility(View.GONE);
            }

            TextView txtTC2 = holder.getTxtTC2();
            if (fulfillmentInfo.getTc2() != null && fulfillmentInfo.getTc2().length() > 0) {
                txtTC2.setVisibility(View.VISIBLE);
                txtTC2.setText(fulfillmentInfo.getTc2());
            } else {
                txtTC2.setVisibility(View.GONE);
            }

        }

    }

    public void showFulfillmentInfoPage(String fulfillmentInfoPageUrl) {
        Intent intent = new Intent(((AppOperationAware) ctx).getCurrentActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
        intent.putExtra(Constants.WEBVIEW_URL, fulfillmentInfoPageUrl);
        ((AppOperationAware) ctx).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }
}
