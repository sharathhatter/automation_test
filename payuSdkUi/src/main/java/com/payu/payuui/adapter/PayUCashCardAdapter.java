package com.payu.payuui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.payu.india.Model.PaymentDetails;
import com.payu.payuui.R;

import java.util.ArrayList;

public class PayUCashCardAdapter extends ArrayAdapter<PaymentDetails> {
    Context mContext;
    ArrayList<PaymentDetails> mCashCardList;

    public PayUCashCardAdapter(Context context, int resource, ArrayList<PaymentDetails> cashCardList) {
        super(context, resource, cashCardList);
        mContext = context;
        mCashCardList = cashCardList;
    }

    @Override
    public int getCount() {
        if (null != mCashCardList) return mCashCardList.size();
        else return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        CashCardViewHolder cashCardViewHolder = null;
        if (convertView == null) {
            LayoutInflater mInflater = (LayoutInflater) mContext.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = mInflater.inflate(R.layout.payu_list_item, parent, false);
            cashCardViewHolder = new CashCardViewHolder(convertView);
            convertView.setTag(cashCardViewHolder);
        } else {
            cashCardViewHolder = (CashCardViewHolder) convertView.getTag();
        }

        PaymentDetails paymentDetails = mCashCardList.get(position);

        // set text here
        cashCardViewHolder.cashCardTextView.setText(paymentDetails.getBankName());
        return convertView;
    }


    private class CashCardViewHolder {
        TextView cashCardTextView;

        CashCardViewHolder(View view) {
            cashCardTextView = (TextView) view.findViewById(R.id.text_view_list_item);
        }
    }
}
