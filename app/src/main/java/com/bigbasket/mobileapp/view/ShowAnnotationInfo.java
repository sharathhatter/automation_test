package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.util.MessageFormatUtil;
import com.bigbasket.mobileapp.util.UIUtil;


public class ShowAnnotationInfo<T> {

    private T ctx;
    private AnnotationInfo annotationInfo;

    public ShowAnnotationInfo(AnnotationInfo annotationInfo, T ctx) {
        this.ctx = ctx;
        this.annotationInfo = annotationInfo;
    }


    public View showAnnotationInfo() {
        LayoutInflater inflater = (LayoutInflater) ((ActivityAware) ctx).getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.fulfillment_info, null);

        ImageView imgAnnotationIcon = (ImageView) base.findViewById(R.id.imgLiquorIcon);
        if (annotationInfo.getIconUrl() != null) {
            imgAnnotationIcon.setVisibility(View.VISIBLE);
            UIUtil.displayAsyncImage(imgAnnotationIcon, annotationInfo.getIconUrl());
        } else {
            imgAnnotationIcon.setVisibility(View.GONE);
        }

        final TextView txtFulfilledBy = (TextView) base.findViewById(R.id.txtFulfilledBy);

        if (annotationInfo.getMsgInfo().getParams() != null && annotationInfo.getMsgInfo().getMessageStr() != null) {
            MessageFormatUtil<T> messageFormatUtil = new MessageFormatUtil<T>();
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
        return base;
    }
}
