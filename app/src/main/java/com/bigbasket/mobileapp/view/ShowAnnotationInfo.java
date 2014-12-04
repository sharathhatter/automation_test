package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MessageFormatUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * Created by jugal on 4/9/14.
 */
public class ShowAnnotationInfo {

    private BaseActivity activity;
    private AnnotationInfo annotationInfo;

    public ShowAnnotationInfo(AnnotationInfo annotationInfo, BaseActivity activity) {
        this.activity = activity;
        this.annotationInfo = annotationInfo;
    }


    public View showAnnotationInfo() {
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.fulfillment_info, null);

        ImageView imgLiquorIcon = (ImageView) base.findViewById(R.id.imgLiquorIcon);
        if (annotationInfo.getIconUrl() != null) {
            imgLiquorIcon.setVisibility(View.VISIBLE);
            ImageLoader.getInstance().displayImage(annotationInfo.getIconUrl(), imgLiquorIcon);
        } else {
            imgLiquorIcon.setVisibility(View.GONE);
        }

        final TextView txtFulfilledBy = (TextView) base.findViewById(R.id.txtFulfilledBy);

//        ArrayList<Class<?>> activitiesList = new ArrayList<>();
//        activitiesList.add(ShowCartActivity.class);
        if (annotationInfo.getMsgInfo().getParams() != null && annotationInfo.getMsgInfo().getMessageStr() != null) {
            MessageFormatUtil messageFormatUtil = new MessageFormatUtil();
            SpannableStringBuilder msgContent = messageFormatUtil.replaceStringArgWithDisplayNameAndLink(activity,
                    " " + annotationInfo.getMsgInfo().getMessageStr(),
                    annotationInfo.getMsgInfo().getParams(), null, null);
            txtFulfilledBy.setMovementMethod(LinkMovementMethod.getInstance());
            if (msgContent != null) {
                txtFulfilledBy.setText(msgContent, TextView.BufferType.SPANNABLE);
                txtFulfilledBy.setSelected(true);
            }
            //txtFulfilledBy.setText(" "+msgContent);
        }
        /*
        if (!TextUtils.isEmpty(annotationInfo.getDescription())) {
            txtFulfilledBy.setVisibility(View.VISIBLE);
            txtFulfilledBy.setText("- " +annotationInfo.getDescription());
            txtFulfilledBy.setTextSize(13);
        } else {
            txtFulfilledBy.setVisibility(View.GONE);
        }
        if(annotationInfo.getDisplayName() != null && annotationInfo.getInfoPage() !=null){
            String prefix = " ";
            String postFix = annotationInfo.getDisplayName();
            SpannableString content = new SpannableString(prefix + postFix);
            content.setSpan(new ForegroundColorSpan(activity.getResources().getColor(R.color.link_color)),
                    0, content.length(), 0);
            txtFulfilledBy.append(content);
            txtFulfilledBy.setTextSize(13);
            txtFulfilledBy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAnnotationInfoPage(annotationInfo.getInfoPage());
                }
            });
        }   */
        // todo annotation with T&C


        return base;
    }

    private void showAnnotationInfoPage(String fulfillmentInfoPageUrl) {
        Intent intent = new Intent(activity, FlatPageWebViewActivity.class);
        intent.putExtra(Constants.FULFILLED_BY_INFO_PAGE_URL, fulfillmentInfoPageUrl);
        activity.startActivityForResult(intent, Constants.GO_TO_HOME);
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
    }


}
