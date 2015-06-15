package com.bigbasket.mobileapp.fragment.base;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.util.UIUtil;

public abstract class AbstractOrderSummaryFragment extends BaseFragment {


    public View getOrderSummaryRow(LayoutInflater inflater, String label, String text,
                                   int textColor) {
        return getOrderSummaryRow(inflater, label, text, textColor, textColor);
    }

    public View getOrderSummaryRow(LayoutInflater inflater, String label, Spannable text,
                                   int textColor) {
        return getOrderSummaryRow(inflater, label, text, textColor, textColor);
    }

    public View getOrderSummaryRow(LayoutInflater inflater, String label, String text,
                                   int labelColor, int valueColor) {
        View row = inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) row.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(faceRobotoRegular);
        txtLabel.setTextColor(labelColor);

        TextView txtValue = (TextView) row.findViewById(R.id.txtValue);
        txtValue.setTypeface(faceRobotoRegular);
        txtValue.setTextColor(valueColor);

        txtLabel.setText(label);
        txtValue.setText(text);
        return row;
    }

    public View getOrderSummaryRow(LayoutInflater inflater, String label, Spannable text,
                                   int labelColor, int valueColor) {
        View row = inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) row.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(faceRobotoRegular);
        txtLabel.setTextColor(labelColor);

        TextView txtValue = (TextView) row.findViewById(R.id.txtValue);
        txtValue.setTypeface(faceRobotoRegular);
        txtValue.setTextColor(valueColor);

        txtLabel.setText(label);
        txtValue.setText(text);
        return row;
    }

    public View getOrderMaxPayableByVoucher(LayoutInflater inflater, String label,
                                            double foodValue, int labelColor){
        View row = inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) row.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(txtLabel.getTypeface(), Typeface.ITALIC);
        txtLabel.setTextColor(labelColor);
        String foodValueFix = "("+label+ " `";
        String foodValueStr = UIUtil.formatAsMoney(foodValue)+")";
        int prefixLen = foodValueFix.length();
        SpannableString spannableFoodValue = new SpannableString(foodValueFix + foodValueStr);
        spannableFoodValue.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

        txtLabel.setText(spannableFoodValue);
        return row;
    }

    public void renderSlotInfoRow(View row, String slotDate, String slotTime,
                                  String fulfilledBy, boolean hasMultipleSlots) {
        TextView txtNumItems = (TextView) row.findViewById(R.id.txtNumItems);
        TextView txtBasketVal = (TextView) row.findViewById(R.id.txtBasketVal);
        TextView txtSlotDate = (TextView) row.findViewById(R.id.txtSlotDate);
        TextView txtSlotTime = (TextView) row.findViewById(R.id.txtSlotTime);
        TextView txtFulfilledBy = (TextView) row.findViewById(R.id.txtFulfilledBy);
        txtSlotDate.setTypeface(faceRobotoRegular);
        txtSlotTime.setTypeface(faceRobotoRegular);

        txtSlotDate.setText(slotDate);
        txtSlotTime.setText(slotTime);
        txtNumItems.setVisibility(View.GONE);
        txtBasketVal.setVisibility(View.GONE);
        if (!hasMultipleSlots) {
            txtFulfilledBy.setVisibility(View.GONE);
        } else {
            txtFulfilledBy.setTypeface(faceRobotoRegular);
            txtFulfilledBy.setText(fulfilledBy);
        }
    }
}
