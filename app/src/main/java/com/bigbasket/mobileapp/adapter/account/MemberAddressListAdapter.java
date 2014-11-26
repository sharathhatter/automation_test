package com.bigbasket.mobileapp.adapter.account;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.model.account.Address;

import java.util.ArrayList;

public class MemberAddressListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<Address> addressArrayList;
    private Context context;
    private Typeface robotoRegularTypeface;
    private boolean showEditIcon;
    private AddressSelectionAware addressSelectionAware;

    public MemberAddressListAdapter(AddressSelectionAware addressSelectionAware,
                                    ArrayList<Address> addressArrayList, Context context,
                                    Typeface robotoRegularTypeface) {
        this.addressArrayList = addressArrayList;
        this.context = context;
        this.robotoRegularTypeface = robotoRegularTypeface;
        this.addressSelectionAware = addressSelectionAware;
    }

    public MemberAddressListAdapter(AddressSelectionAware addressSelectionAware,
                                    ArrayList<Address> addressArrayList, Context context, Typeface robotoRegularTypeface,
                                    boolean showEditIcon) {
        this(addressSelectionAware, addressArrayList, context, robotoRegularTypeface);
        this.showEditIcon = showEditIcon;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_member_address_row, viewGroup, false);
        return new MemberAddressViewHolder(base);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        MemberAddressViewHolder memberAddressViewHolder = (MemberAddressViewHolder) viewHolder;
        Address address = addressArrayList.get(position);
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
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return addressArrayList.size();
    }

    public class MemberAddressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtMemberAddress;
        private TextView txtMemberName;
        private TextView txtMemberContactNum;
        private CheckBox chkIsAddrSelected;
        private TextView lblEditAddress;

        public MemberAddressViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        public TextView getTxtMemberAddress() {
            if (txtMemberAddress == null) {
                txtMemberAddress = (TextView) itemView.findViewById(R.id.txtMemberAddress);
                txtMemberAddress.setTypeface(robotoRegularTypeface);
            }
            return txtMemberAddress;
        }

        public TextView getTxtMemberName() {
            if (txtMemberName == null) {
                txtMemberName = (TextView) itemView.findViewById(R.id.txtMemberName);
                txtMemberName.setTypeface(robotoRegularTypeface);
            }
            return txtMemberName;
        }

        public TextView getTxtMemberContactNum() {
            if (txtMemberContactNum == null) {
                txtMemberContactNum = (TextView) itemView.findViewById(R.id.txtMemberContactNum);
                txtMemberContactNum.setTypeface(robotoRegularTypeface);
            }
            return txtMemberContactNum;
        }

        public CheckBox getChkIsAddrSelected() {
            if (chkIsAddrSelected == null) {
                chkIsAddrSelected = (CheckBox) itemView.findViewById(R.id.chkIsAddrSelected);
            }
            return chkIsAddrSelected;
        }

        public TextView getLblEditAddress() {
            if (lblEditAddress == null) {
                lblEditAddress = (TextView) itemView.findViewById(R.id.lblEditAddress);
            }
            return lblEditAddress;
        }

        @Override
        public void onClick(View v) {
            Address address = addressArrayList.get(getPosition());
            addressSelectionAware.onAddressSelected(address);
        }
    }
}
