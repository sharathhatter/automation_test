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
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.ArrayList;

public class AddressSummaryDropdownAdapter<T extends AddressSummary> extends ArrayAdapter<T> {

    // Spinner in API 21 and onwards doesn't support getItemViewType(),
    // hence using a dirty implementation in getView(),
    // given that that VIEW_TYPE_CHANGE will only occur once.

    public static final int VIEW_TYPE_ADDRESS = 0;
    public static final int VIEW_TYPE_CHANGE = 1;

    private ArrayList<T> addressSummaries;
    private String changeAddressTxt;
    private Typeface faceRobotLight;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoBold;

    public AddressSummaryDropdownAdapter(Context context, int resource, ArrayList<T> addressSummaries,
                                         String changeAddressTxt) {
        super(context, resource, addressSummaries);
        this.addressSummaries = addressSummaries;
        this.changeAddressTxt = changeAddressTxt;
        FontHolder fontHolder = FontHolder.getInstance(context);
        this.faceRobotLight = fontHolder.getFaceRobotoLight();
        this.faceRobotoRegular = fontHolder.getFaceRobotoRegular();
        this.faceRobotoBold = fontHolder.getFaceRobotoBold();
    }

    @Override
    public int getCount() {
        return addressSummaries.size() + 1;
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
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent) {
        if (getSpinnerViewType(position) == VIEW_TYPE_ADDRESS) {
            convertView = getAddressRow(false, position, convertView, parent);
        } else {
            convertView = getChangeAddressRow(parent);
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (getSpinnerViewType(position) == VIEW_TYPE_ADDRESS) {
            convertView = getAddressRow(true, position, convertView, parent);
        } else {
            convertView = getChangeAddressRow(parent);
        }
        return convertView;
    }

    private View getChangeAddressRow(ViewGroup parent) {
        View convertView = LayoutInflater.from(getContext())
                .inflate(R.layout.uiv3_change_city_spinner_row, parent, false);
        TextView txtChangeCity = (TextView) convertView;
        txtChangeCity.setTypeface(faceRobotoRegular);
        txtChangeCity.setText(changeAddressTxt);
        /************Setting different color for gingerbread and below******/
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            txtChangeCity.setTextColor(getContext().getResources().getColor(R.color.dark_black));
        }
        return convertView;
    }

    @SuppressWarnings("unchecked")
    private View getAddressRow(boolean showSlotTime, int position, View convertView,
                               ViewGroup parent) {
        AddressSummary addressSummary = addressSummaries.get(position);
        AddressViewHolder addressViewHolder;
        if (convertView == null || convertView.getTag() == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.uiv3_change_address_spinner_row, parent, false);
            addressViewHolder = new AddressViewHolder(convertView);
            convertView.setTag(addressViewHolder);
        } else {
            addressViewHolder = (AddressViewHolder) convertView.getTag();
        }
        String nick = addressSummary.getAddressNickName();
        /************Setting different color for gingerbread and below******/

        TextView txtCityName = addressViewHolder.getTxtCityName();
        TextView txtAreaName = addressViewHolder.getTxtAreaName();
        TextView txtSlotTime = addressViewHolder.getTxtSlotTime();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            txtAreaName.setTextColor(getContext().getResources().getColor(R.color.dark_black));
            txtCityName.setTextColor(getContext().getResources().getColor(R.color.dark_black));
            txtSlotTime.setTextColor(getContext().getResources().getColor(R.color.dark_black));
        }
        if (TextUtils.isEmpty(nick)) {
            txtAreaName.setText(addressSummary.getArea());
        } else {
            SpannableString spannableString = new SpannableString(nick + " - " +
                    addressSummary.getArea());
            spannableString.setSpan(new CustomTypefaceSpan("", faceRobotoBold), 0,
                    nick.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            txtAreaName.setText(spannableString);
        }
        txtCityName.setText(addressSummary.getCityName());
        String slot = addressSummary.getSlot();
        if (!TextUtils.isEmpty(slot) && showSlotTime) {
            txtSlotTime.setText(slot);
            txtSlotTime.setVisibility(View.VISIBLE);
        } else {
            txtSlotTime.setVisibility(View.GONE);
        }
        return convertView;
    }

    private class AddressViewHolder {
        private View itemView;
        private TextView txtAreaName;
        private TextView txtCityName;
        private TextView txtSlotTime;

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

        public TextView getTxtSlotTime() {
            if (txtSlotTime == null) {
                txtSlotTime = (TextView) itemView.findViewById(R.id.txtSlotTime);
            }
            return txtSlotTime;
        }
    }
}
