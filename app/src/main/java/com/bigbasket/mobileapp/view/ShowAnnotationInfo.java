package com.bigbasket.mobileapp.view;

import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.util.MessageFormatUtil;
import com.bigbasket.mobileapp.util.UIUtil;


public class ShowAnnotationInfo<T> {

    private T ctx;
    private AnnotationInfo annotationInfo;
    private ActiveOrderRowAdapter.FulfillmentInfoViewHolder holder;

    public ShowAnnotationInfo(AnnotationInfo annotationInfo, T ctx, ActiveOrderRowAdapter.FulfillmentInfoViewHolder holder) {
        this.ctx = ctx;
        this.annotationInfo = annotationInfo;
        this.holder = holder;
    }


    public void showAnnotationInfo() {

        ImageView imgAnnotationIcon = holder.getImgLiquorIcon();
        if (annotationInfo.getIconUrl() != null) {
            imgAnnotationIcon.setVisibility(View.VISIBLE);
            UIUtil.displayAsyncImage(imgAnnotationIcon, annotationInfo.getIconUrl());
        } else {
            imgAnnotationIcon.setVisibility(View.GONE);
        }

        final TextView txtFulfilledBy = holder.getTxtFulfilledBy();

        if (annotationInfo.getMsgInfo().getParams() != null && annotationInfo.getMsgInfo().getMessageStr() != null) {
            MessageFormatUtil<T> messageFormatUtil = new MessageFormatUtil<>();
            SpannableStringBuilder msgContent = messageFormatUtil.
                    replaceStringArgWithDisplayNameAndLink(ctx,
                            " " + annotationInfo.getMsgInfo().getMessageStr(),
                            annotationInfo.getMsgInfo().getParams(), null, null);
            txtFulfilledBy.setMovementMethod(LinkMovementMethod.getInstance());
            if (msgContent != null) {
                txtFulfilledBy.setText(msgContent, TextView.BufferType.SPANNABLE);
                txtFulfilledBy.setSelected(true);
            }
        }
    }
}
