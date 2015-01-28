package com.bigbasket.mobileapp.view;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.util.MessageFormatUtil;
import com.nostra13.universalimageloader.core.ImageLoader;


public class ShowAnnotationInfo {
    private Activity context;
    private AnnotationInfo annotationInfo;

    public ShowAnnotationInfo(AnnotationInfo annotationInfo, Activity context) {
        this.context = context;
        this.annotationInfo = annotationInfo;
    }


    public View showAnnotationInfo() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.fulfillment_info, null);

        ImageView imgAnnotationIcon = (ImageView) base.findViewById(R.id.imgLiquorIcon);
        if (annotationInfo.getIconUrl() != null) {
            imgAnnotationIcon.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(annotationInfo.getIconUrl(), imgAnnotationIcon);
        } else {
            imgAnnotationIcon.setVisibility(View.GONE);
        }

        final TextView txtFulfilledBy = (TextView) base.findViewById(R.id.txtFulfilledBy);

        if (annotationInfo.getMsgInfo().getParams() != null && annotationInfo.getMsgInfo().getMessageStr() != null) {
            MessageFormatUtil messageFormatUtil = new MessageFormatUtil();
            SpannableStringBuilder msgContent = messageFormatUtil.replaceStringArgWithDisplayNameAndLink(((BaseActivity) context),
                    " " + annotationInfo.getMsgInfo().getMessageStr(),
                    annotationInfo.getMsgInfo().getParams(), null, null);
            txtFulfilledBy.setMovementMethod(LinkMovementMethod.getInstance());
            if (msgContent != null) {
                txtFulfilledBy.setText(msgContent, TextView.BufferType.SPANNABLE);
                txtFulfilledBy.setSelected(true);
            }
        }

        /*

        if (!TextUtils.isEmpty(annotationInfo.getDescription())) {
            txtFulfilledBy.setVisibility(View.VISIBLE);
            txtFulfilledBy.setText(" - " +annotationInfo.getDescription());
            txtFulfilledBy.setTextSize(13);
        } else {
            txtFulfilledBy.setVisibility(View.GONE);
        }

        if(annotationInfo.getDisplayName() != null && annotationInfo.getInfoPage() !=null){
            String prefix = " ";
            String postFix = annotationInfo.getDisplayName();
            SpannableString content = new SpannableString(prefix + postFix);
            content.setSpan(new ForegroundColorSpan(context.getResources().getColor(R.color.link_color)),
                    0, content.length(), 0);
            txtFulfilledBy.append(content);
            txtFulfilledBy.setTextSize(13);
            txtFulfilledBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAnnotationInfoPage(annotationInfo.getInfoPage());
                }
            });
        }

        */

        return base;
    }

    /*
    private void showAnnotationInfoPage(String fulfillmentInfoPageUrl) {
        Intent intent = new Intent(activity, FlatPageWebViewActivity.class);
        intent.putExtra(Constants.WEBVIEW_URL, fulfillmentInfoPageUrl);
        activity.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }
    */
}
