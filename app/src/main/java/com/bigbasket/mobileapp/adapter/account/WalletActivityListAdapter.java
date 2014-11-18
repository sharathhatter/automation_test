package com.bigbasket.mobileapp.adapter.account;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.account.WalletDataItem;

import java.text.NumberFormat;
import java.util.ArrayList;


public class WalletActivityListAdapter extends BaseAdapter {

    private ArrayList<WalletDataItem> walletDataItems;
    Context context;
    private LayoutInflater layoutInflater;
    private Typeface faceRupee;
    SpannableString spannableCredit;
    SpannableString spannableEndingBal;
    SpannableString spannableId;
    Typeface faceRobotoRegular;


    public WalletActivityListAdapter(Context context, ArrayList listData, Typeface faceRobotoRegular) {
        this.walletDataItems = listData;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.faceRupee = Typeface.createFromAsset(context.getAssets(), "Rupee.ttf");
        this.faceRobotoRegular = faceRobotoRegular;
    }

    @Override
    public int getCount() {
        return walletDataItems.size();
    }

    @Override
    public Object getItem(int position) {
        return walletDataItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.uiv3_wallet_activity, null);
            holder = new ViewHolder();
            holder.dateHolder = (TextView) convertView.findViewById(R.id.date);
            holder.dateHolder.setTypeface(faceRobotoRegular);
            holder.creditedHolder = (TextView) convertView.findViewById(R.id.credited);
            holder.creditedHolder.setTypeface(faceRobotoRegular);
            holder.endingBalanceHolder = (TextView) convertView.findViewById(R.id.endingBalance);
            holder.endingBalanceHolder.setTypeface(faceRobotoRegular);
            holder.orderIdHolder = (TextView) convertView.findViewById(R.id.orderId);
            holder.orderIdHolder.setTypeface(faceRobotoRegular);
            holder.creditCreatedHolder = (TextView) convertView.findViewById(R.id.creditCreated);
            holder.creditCreatedHolder.setTypeface(faceRobotoRegular);
            holder.colorHolder = (LinearLayout) convertView.findViewById(R.id.backgroundLayout);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        double endingBal = walletDataItems.get(position).getEndingBalance();
        double amount = walletDataItems.get(position).getAmount();
        String orderId = walletDataItems.get(position).getOrderId();
        String secondaryReason = walletDataItems.get(position).getSecondary_reason();
        String primaryReason = walletDataItems.get(position).getPrimary_reason();

        String prefixEndBal = context.getString(R.string.endingBal) + " `";
        String mrpStrEndBal = getDecimalAmount(endingBal) + " ";
        int prefixEndBalLen = prefixEndBal.length();
        spannableEndingBal = new SpannableString(prefixEndBal + mrpStrEndBal);

        spannableEndingBal.setSpan(new CustomTypefaceSpan("", faceRupee), prefixEndBalLen - 1,
                prefixEndBalLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);


        if (walletDataItems.get(position).getType().equals("credit")) {
            String prefix = context.getString(R.string.credited) + " `";
            String mrpStr = getDecimalAmount(amount) + " ";
            int prefixLen = prefix.length();
            spannableCredit = new SpannableString(prefix + mrpStr);
            holder.creditedHolder.setBackgroundColor(context.getResources().getColor(R.color.dark_green));
            spannableCredit.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        } else {

            String prefix = context.getString(R.string.debited) + " `";
            String mrpStr = getDecimalAmount(amount) + " ";
            int prefixLen = prefix.length();
            holder.creditedHolder.setBackgroundColor(context.getResources().getColor(R.color.promo_red_color));
            spannableCredit = new SpannableString(prefix + mrpStr);
            spannableCredit.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        holder.dateHolder.setText(walletDataItems.get(position).getDate());
        holder.creditedHolder.setText(spannableCredit);
        holder.endingBalanceHolder.setText(spannableEndingBal);
        if (orderId != null && !orderId.equals("null")) {
            String prefixId = primaryReason + " ";
            String mrpStrId = orderId + " ";
            int prefixIdLen = prefixId.length();
            int mrpStrIdLen = mrpStrId.length();
            spannableId = new SpannableString(prefixId + mrpStrId);

            spannableId.setSpan(new ForegroundColorSpan(context.getResources().
                    getColor(R.color.dark_blue)), prefixIdLen - 1, prefixIdLen + mrpStrIdLen, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            holder.orderIdHolder.setText(spannableId);
        } else {
            holder.orderIdHolder.setText(primaryReason);
        }
        holder.creditCreatedHolder.setText(secondaryReason);

        return convertView;
    }

    static class ViewHolder {
        TextView dateHolder;
        TextView creditedHolder;
        TextView endingBalanceHolder;
        TextView orderIdHolder;
        TextView creditCreatedHolder;
        LinearLayout colorHolder;
    }

    public String getDecimalAmount(Double amount) {
        int amountInt = amount.intValue();
        if (amountInt == amount)
            return String.valueOf(amountInt);
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        nf.setGroupingUsed(false);
        return (nf.format(amount).equals("0.00") || nf.format(amount).equals("0.0")) ? "0" : nf.format(amount);
    }
}
