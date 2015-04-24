package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.UIUtil;


public class ShowFulfillmentInfo<T> {

    private FulfillmentInfo fulfillmentInfo;
    private T ctx;
    private Typeface faceRobotoSlabNrml;

    public ShowFulfillmentInfo(FulfillmentInfo fulfillmentInfo, T ctx,
                               Typeface faceRobotoSlabNrml) {
        this.fulfillmentInfo = fulfillmentInfo;
        this.ctx = ctx;
        this.faceRobotoSlabNrml = faceRobotoSlabNrml;
    }

    public View showFulfillmentInfo(boolean showImgLiquorIcon, boolean showTC) {
        LayoutInflater inflater = (LayoutInflater) ((ActivityAware) ctx).getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.fulfillment_info, null);
        RelativeLayout layoutInfoMsg = (RelativeLayout) base.findViewById(R.id.layoutInfoMsg);
        layoutInfoMsg.setBackgroundResource(R.drawable.background);

        if (showImgLiquorIcon) {
            ImageView imgLiquorIcon = (ImageView) base.findViewById(R.id.imgLiquorIcon);
            if (fulfillmentInfo.getIcon() != null && !fulfillmentInfo.getIcon().equalsIgnoreCase("null")) {
                imgLiquorIcon.setVisibility(View.VISIBLE);
                UIUtil.displayAsyncImage(imgLiquorIcon, fulfillmentInfo.getIcon());
            } else {
                imgLiquorIcon.setVisibility(View.VISIBLE);
            }
        }

        final TextView txtFulfilledBy = (TextView) base.findViewById(R.id.txtFulfilledBy);
        if (!TextUtils.isEmpty(fulfillmentInfo.getFulfilledBy()) && !fulfillmentInfo.getFulfilledBy().equalsIgnoreCase("null")) {
            if (showImgLiquorIcon) { // show basket page
                String prefix = " - Indicates " + fulfillmentInfo.getDisplayName() + " products fulfilled by ";
                String postFix = fulfillmentInfo.getFulfilledBy();
                if (!TextUtils.isEmpty(fulfillmentInfo.getFulfilledByInfoPage())) {
                    SpannableString content = new SpannableString(prefix + postFix);
                    int prefixLen = prefix.length();
                    content.setSpan(new ForegroundColorSpan(((ActivityAware) ctx).getCurrentActivity()
                                    .getResources().getColor(R.color.link_color)),
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
            TextView txtTC1 = (TextView) base.findViewById(R.id.txtTC1);
            if (fulfillmentInfo.getTc1() != null && fulfillmentInfo.getTc1().length() > 0) {
                txtTC1.setVisibility(View.VISIBLE);
                txtTC1.setText(fulfillmentInfo.getTc1());
            } else {
                txtTC1.setVisibility(View.GONE);
            }

            TextView txtTC2 = (TextView) base.findViewById(R.id.txtTC2);
            if (fulfillmentInfo.getTc2() != null && fulfillmentInfo.getTc2().length() > 0) {
                txtTC2.setVisibility(View.VISIBLE);
                txtTC2.setText(fulfillmentInfo.getTc2());
            } else {
                txtTC2.setVisibility(View.GONE);
            }

//            if (txtTC1.getVisibility() == View.VISIBLE || txtTC2.getVisibility() == View.VISIBLE)
//                txtTCLabel.setVisibility(View.VISIBLE);
        }

        return base;
    }

    public void showFulfillmentInfoPage(String fulfillmentInfoPageUrl) {
        Intent intent = new Intent(((ActivityAware) ctx).getCurrentActivity(), FlatPageWebViewActivity.class);
        intent.putExtra(Constants.WEBVIEW_URL, fulfillmentInfoPageUrl);
        ((ActivityAware) ctx).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }
}
