package com.bigbasket.mobileapp.adapter.account;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.StyleSpan;
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
        txtChangeCity.setTypeface(faceRobotLight);
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
        /************Setting different color for gingerbread and below******/
        TextView txtAddress = addressViewHolder.getTxtAddress();
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1) {
            txtAddress.setTextColor(getContext().getResources().getColor(R.color.dark_black));
        }

        String nick = addressSummary.getAddressNickName();
        if (TextUtils.isEmpty(nick)) {
            nick = "";
        }
        String area = TextUtils.isEmpty(addressSummary.getArea()) ? "" :
                " - " + addressSummary.getArea() + "\n";
        String cityName = addressSummary.getCityName();
        String slot = !showSlotTime || TextUtils.isEmpty(addressSummary.getSlot()) ? "" :
                "\n" + addressSummary.getSlot();
        if (showSlotTime) {
            txtAddress.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }

        SpannableString spannableString = new SpannableString(nick + area + cityName + slot);
        if (!TextUtils.isEmpty(nick)) {
            spannableString.setSpan(new CustomTypefaceSpan("", faceRobotoBold), 0,
                    nick.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        spannableString.setSpan(new AbsoluteSizeSpan(12, true),
                nick.length() + area.length(), spannableString.length(),
                Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        if (!TextUtils.isEmpty(slot)) {
            spannableString.setSpan(new StyleSpan(Typeface.ITALIC),
                    nick.length() + area.length() + cityName.length(),
                    spannableString.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        txtAddress.setText(spannableString);
        return convertView;
    }

    private class AddressViewHolder {
        private View itemView;
        private TextView txtAddress;

        public AddressViewHolder(View itemView) {
            this.itemView = itemView;
        }

        public TextView getTxtAddress() {
            if (txtAddress == null) {
                txtAddress = (TextView) itemView.findViewById(R.id.txtAddress);
                txtAddress.setTypeface(faceRobotoRegular);
            }
            return txtAddress;
        }
    }
}
