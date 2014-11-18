package com.bigbasket.mobileapp.adapter.account;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.account.Address;

import java.util.ArrayList;

public class MemberAddressListAdapter extends BaseAdapter {
    private ArrayList<Address> addressArrayList;
    private Context context;
    private Typeface robotoRegularTypeface;
    private boolean showEditIcon;

    public MemberAddressListAdapter(ArrayList<Address> addressArrayList, Context context) {
        this.addressArrayList = addressArrayList;
        this.context = context;
        this.robotoRegularTypeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Regular.ttf");
    }

    public MemberAddressListAdapter(ArrayList<Address> addressArrayList, Context context, boolean showEditIcon) {
        this(addressArrayList, context);
        this.showEditIcon = showEditIcon;
    }

    @Override
    public int getCount() {
        return addressArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return addressArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class MemberAddressViewHolder {
        private TextView txtMemberAddress;
        private TextView txtMemberName;
        private TextView txtMemberContactNum;
        private CheckBox chkIsAddrSelected;
        private TextView lblEditAddress;
        private View base;

        public MemberAddressViewHolder(View base) {
            this.base = base;
        }

        public TextView getTxtMemberAddress() {
            if (txtMemberAddress == null) {
                txtMemberAddress = (TextView) base.findViewById(R.id.txtMemberAddress);
                txtMemberAddress.setTypeface(robotoRegularTypeface);
            }
            return txtMemberAddress;
        }

        public TextView getTxtMemberName() {
            if (txtMemberName == null) {
                txtMemberName = (TextView) base.findViewById(R.id.txtMemberName);
                txtMemberName.setTypeface(robotoRegularTypeface);
            }
            return txtMemberName;
        }

        public TextView getTxtMemberContactNum() {
            if (txtMemberContactNum == null) {
                txtMemberContactNum = (TextView) base.findViewById(R.id.txtMemberContactNum);
                txtMemberContactNum.setTypeface(robotoRegularTypeface);
            }
            return txtMemberContactNum;
        }

        public CheckBox getChkIsAddrSelected() {
            if (chkIsAddrSelected == null) {
                chkIsAddrSelected = (CheckBox) base.findViewById(R.id.chkIsAddrSelected);
            }
            return chkIsAddrSelected;
        }

        public TextView getLblEditAddress() {
            if (lblEditAddress == null) {
                lblEditAddress = (TextView) base.findViewById(R.id.lblEditAddress);
            }
            return lblEditAddress;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        final Address address = addressArrayList.get(position);
        MemberAddressViewHolder memberAddressViewHolder;
        if (row == null) {
            row = getInflatedView();
            memberAddressViewHolder = new MemberAddressViewHolder(row);
            row.setTag(memberAddressViewHolder);
        } else {
            memberAddressViewHolder = (MemberAddressViewHolder) row.getTag();
        }

        TextView txtMemberAddress = memberAddressViewHolder.getTxtMemberAddress();
        TextView txtMemberName = memberAddressViewHolder.getTxtMemberName();
        TextView txtMemberContactNum = memberAddressViewHolder.getTxtMemberContactNum();
        CheckBox chkIsAddrSelected = memberAddressViewHolder.getChkIsAddrSelected();
        TextView lblEditAddress = memberAddressViewHolder.getLblEditAddress();

        txtMemberAddress.setText(address.toString());
        txtMemberName.setText(address.getName());
        txtMemberContactNum.setText(address.getContactNum());
        if (address.isDefault()) {
            chkIsAddrSelected.setChecked(true);
            chkIsAddrSelected.setVisibility(View.VISIBLE);
        } else {
            chkIsAddrSelected.setVisibility(View.GONE);
        }
        lblEditAddress.setVisibility(showEditIcon ? View.VISIBLE : View.GONE);
        return row;
    }

    private View getInflatedView() {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        return inflater.inflate(R.layout.uiv3_member_address_row, null);
    }
}
