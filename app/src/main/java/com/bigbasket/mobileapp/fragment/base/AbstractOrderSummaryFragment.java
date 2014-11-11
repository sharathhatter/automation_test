package com.bigbasket.mobileapp.fragment.base;

import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableRow;
import android.widget.TextView;
import com.bigbasket.mobileapp.R;

public abstract class AbstractOrderSummaryFragment extends BaseFragment {


    public TableRow getOrderSummaryRow(LayoutInflater inflater, String label, String text) {
        TableRow tableRow = (TableRow) inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) tableRow.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(faceRobotoRegular);

        TextView txtValue = (TextView) tableRow.findViewById(R.id.txtValue);
        txtValue.setTypeface(faceRobotoRegular);

        txtLabel.setText(label);
        txtValue.setText(text);
        return tableRow;
    }

    public TableRow getOrderSummaryRow(LayoutInflater inflater, String label, Spannable text) {
        TableRow tableRow = (TableRow) inflater.inflate(R.layout.uiv3_label_value_table_row, null);

        TextView txtLabel = (TextView) tableRow.findViewById(R.id.txtLabel);
        txtLabel.setTypeface(faceRobotoRegular);

        TextView txtValue = (TextView) tableRow.findViewById(R.id.txtValue);
        txtValue.setTypeface(faceRobotoRegular);

        txtLabel.setText(label);
        txtValue.setText(text);
        return tableRow;
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
