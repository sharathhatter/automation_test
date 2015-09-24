package com.bigbasket.mobileapp.adapter.account;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.ArrayList;

public class AddressSummaryDropdownAdapter extends BaseAdapter {

    // Spinner in API 21 and onwards doesn't support getItemViewType(),
    // hence using a dirty implementation in getView(),
    // given that that VIEW_TYPE_CHANGE will only occur once.

    public static final int VIEW_TYPE_ADDRESS = 0;
    public static final int VIEW_TYPE_CHANGE = 1;

    private ArrayList<AddressSummary> addressSummaries;
    private String changeAddressTxt;
    private Context context;
    private Typeface faceRobotLight;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoBold;

    public AddressSummaryDropdownAdapter(ArrayList<AddressSummary> addressSummaries,
                                         String changeAddressTxt, Context context) {
        this.addressSummaries = addressSummaries;
        this.changeAddressTxt = changeAddressTxt;
        this.context = context;
        this.faceRobotLight = FontHolder.getInstance(context).getFaceRobotoLight();
        this.faceRobotoRegular = FontHolder.getInstance(context).getFaceRobotoRegular();
        this.faceRobotoBold = FontHolder.getInstance(context).getFaceRobotoBold();
    }

    @Override
    public int getCount() {
        return addressSummaries.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        if (getSpinnerViewType(position) == VIEW_TYPE_ADDRESS) {
            return addressSummaries.get(position);
        }
        return changeAddressTxt;
    }

    public int getSpinnerViewType(int position) {
        if (position < addressSummaries.size()) {
            return VIEW_TYPE_ADDRESS;
        }
        return VIEW_TYPE_CHANGE;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getSpinnerViewType(position) == VIEW_TYPE_ADDRESS) {
            AddressSummary addressSummary = addressSummaries.get(position);
            AddressViewHolder addressViewHolder;
            if (convertView == null || convertView.getTag() == null) {
                convertView = LayoutInflater.from(context)
                        .inflate(R.layout.uiv3_change_address_spinner_row, parent, false);
                addressViewHolder = new AddressViewHolder(convertView);
                convertView.setTag(addressViewHolder);
            } else {
                addressViewHolder = (AddressViewHolder) convertView.getTag();
            }
            String nick = addressSummary.getAddressNickName();
            /************Setting different color for gingerbread and below******/
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {

                addressViewHolder.getTxtAreaName().setTextColor(context.getResources().getColor(R.color.dark_black));
                addressViewHolder.getTxtCityName().setTextColor(context.getResources().getColor(R.color.dark_black));

            }
            if (TextUtils.isEmpty(nick)) {
                addressViewHolder.getTxtAreaName().setText(addressSummary.getArea());
            } else {
                SpannableString spannableString = new SpannableString(nick + " - " +
                        addressSummary.getArea());
                spannableString.setSpan(new CustomTypefaceSpan("", faceRobotoBold), 0,
                        nick.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                addressViewHolder.getTxtAreaName().setText(spannableString);
            }
            addressViewHolder.getTxtCityName().setText(addressSummary.getCityName());
        } else {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.uiv3_change_city_spinner_row, parent, false);
            TextView txtChangeCity = (TextView) convertView;
            txtChangeCity.setTypeface(faceRobotoRegular);
            txtChangeCity.setText(changeAddressTxt);


            /************Setting different color for gingerbread and below******/
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {

                txtChangeCity.setTextColor(context.getResources().getColor(R.color.dark_black));

            }
        }



        return convertView;
    }

    private class AddressViewHolder {
        private View itemView;
        private TextView txtAreaName;
        private TextView txtCityName;

        public AddressViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public TextView getTxtAreaName() {
            if (txtAreaName == null) {
                txtAreaName = (TextView) itemView.findViewById(R.id.txtAreaName);
                txtAreaName.setTypeface(faceRobotoRegular);
            }
            return txtAreaName;
        }

        public TextView getTxtCityName() {
            if (txtCityName == null) {
                txtCityName = (TextView) itemView.findViewById(R.id.txtCityName);
                txtCityName.setTypeface(faceRobotLight);
            }
            return txtCityName;
        }
    }
}
