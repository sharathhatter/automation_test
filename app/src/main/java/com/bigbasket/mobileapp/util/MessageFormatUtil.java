package com.bigbasket.mobileapp.util;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.general.MessageParamInfo;

import java.util.ArrayList;


public class MessageFormatUtil<T> {

    private static SpannableStringBuilder addClickablePart(String str, final Activity activity,
                                                           final ArrayList<MessageParamInfo> messageParamInfoList,
                                                           final ArrayList<Class<?>> activityArrayList,
                                                           final ArrayList<Integer> arrayListIndex,
                                                           final ArrayList<Integer> fragmentCodeArrayList) {
        str = str.replaceAll("\\s*\\]\\s*", "]");
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        int idx1 = str.indexOf("[");
        int idx2 = 0;
        int index = 0;


        while (idx1 != -1) {
            idx2 = str.indexOf("]", idx1) + 1;
            final MessageParamInfo messageParamInfoArrayList = messageParamInfoList.get(arrayListIndex.get(index));
            final Class<?> callingActivity = activityArrayList.get(index);
            final int fragmentCode = fragmentCodeArrayList.get(index);
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (messageParamInfoArrayList.getType().equals(Constants.APP_LINK)) {
                        if (callingActivity != null && messageParamInfoArrayList.getInternalValue() != null) {
                            Intent intent = new Intent(activity, callingActivity);
                            intent.putExtra(Constants.FRAGMENT_CODE, fragmentCode);
                            intent.putExtra(Constants.INTERNAL_VALUE, messageParamInfoArrayList.getInternalValue());
                            activity.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }
                    } else if (messageParamInfoArrayList.getType().equals(Constants.WEB_LINK)) {
                        if (messageParamInfoArrayList.getInternalValue() != null) {
                            Intent intent = new Intent(activity, BackButtonActivity.class);
                            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                            intent.putExtra(Constants.WEBVIEW_URL, messageParamInfoArrayList.getInternalValue());
                            activity.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint textPaint) {
                    textPaint.setColor(textPaint.linkColor);
                    textPaint.setUnderlineText(false); // set to false to remove underline
                }
            }, idx1, idx2, 0);

            spannableStringBuilder.replace(idx1, idx1 + 1, " ");
            spannableStringBuilder.replace(idx2 - 1, idx2, " ");
            idx1 = str.indexOf("[", idx2);
            index++;
        }
        return spannableStringBuilder;
    }

    private static SpannableStringBuilder addClickablePart(String str, final Activity activity,
                                                           final ArrayList<MessageParamInfo> messageParamInfoList,
                                                           final ArrayList<Integer> arrayListIndex) {
        str = str.replaceAll("\\s*\\]\\s*", "]");
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(str);
        int idx1 = str.indexOf("[");
        int idx2 = 0;
        int index = 0;
        while (idx1 != -1) {
            idx2 = str.indexOf("]", idx1) + 1;
            final MessageParamInfo messageParamInfoArrayList = messageParamInfoList.get(arrayListIndex.get(index));
            spannableStringBuilder.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    if (messageParamInfoArrayList.getType().equals(Constants.WEB_LINK)) {
                        if (messageParamInfoArrayList.getInternalValue() != null) {
                            Intent intent = new Intent(activity, BackButtonActivity.class);
                            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WEBVIEW);
                            intent.putExtra(Constants.WEBVIEW_URL, messageParamInfoArrayList.getInternalValue());
                            activity.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        }
                    }
                }

                @Override
                public void updateDrawState(@NonNull TextPaint textPaint) {
                    textPaint.setColor(textPaint.linkColor);
                    textPaint.setUnderlineText(false); // set to false to remove underline
                }
            }, idx1, idx2, 0);

            spannableStringBuilder.replace(idx1, idx1 + 1, " ");
            spannableStringBuilder.replace(idx2 - 1, idx2, " ");
            idx1 = str.indexOf("[", idx2);
            index++;
        }
        return spannableStringBuilder;
    }

    public SpannableStringBuilder replaceStringArgWithDisplayNameAndLink(final T ctx, String msgStr,
                                                                         final ArrayList<MessageParamInfo> messageParamInfoList,
                                                                         final ArrayList<Class<?>> activityArrayList,
                                                                         final ArrayList<Integer> fragmentCodeArrayList) {
        SpannableString spannableString = null;
        int replacedStringIndex = 0;
        ArrayList<Integer> arrayListIndex = new ArrayList<>();

        while (msgStr.contains("{") && msgStr.contains("}")) {
            int preIndexOfFormatStr = msgStr.indexOf("{");
            int postIndexFormatStr = msgStr.indexOf("}");

            String replacedString = (msgStr.substring(preIndexOfFormatStr + 1, postIndexFormatStr));
            replacedStringIndex = Integer.valueOf(replacedString); //.replaceAll("\\s", "")
            arrayListIndex.add(replacedStringIndex);
            msgStr = msgStr.replaceFirst("\\{ *\\d+ *\\}",
                    "[" + messageParamInfoList.get(replacedStringIndex).getDisplayName() + "]");

            spannableString = new SpannableString(msgStr);
            // change str to link color
            int postIndex = preIndexOfFormatStr + messageParamInfoList.get(replacedStringIndex).getDisplayName().length();
            spannableString.setSpan(new ForegroundColorSpan(((AppOperationAware) ctx).getCurrentActivity().getResources().getColor(R.color.link_color)),
                    preIndexOfFormatStr, postIndex, 0);
        }
        if (activityArrayList != null && activityArrayList.size() > 0) {
            return addClickablePart(spannableString.toString().replaceAll("\\s*\\[\\s*", "["),
                    ((AppOperationAware) ctx).getCurrentActivity(), messageParamInfoList, activityArrayList,
                    arrayListIndex, fragmentCodeArrayList);
        } else {
            return addClickablePart(spannableString.toString().replaceAll("\\s*\\[\\s*", "["),
                    ((AppOperationAware) ctx).getCurrentActivity(), messageParamInfoList,
                    arrayListIndex);
        }

    }

}
