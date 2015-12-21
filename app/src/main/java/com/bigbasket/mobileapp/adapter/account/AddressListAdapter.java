package com.bigbasket.mobileapp.adapter.account;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.AddressChangeAware;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.account.Address;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.ArrayList;

public class AddressListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ADDRESS = 0;

    private ArrayList<Address> addressObjectList;
    private T context;
    private LayoutInflater inflater;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoMedium;

    public AddressListAdapter(T context, ArrayList<Address> addressObjectList) {
        this.addressObjectList = addressObjectList;
        this.context = context;
        Context ctx = ((AppOperationAware) context).getCurrentActivity();
        this.faceRobotoMedium = FontHolder.getInstance(ctx).getFaceRobotoMedium();
        this.faceRobotoRegular = FontHolder.getInstance(ctx).getFaceRobotoRegular();
        inflater = ((AppOperationAware) context).getCurrentActivity().getLayoutInflater();
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ADDRESS;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.uiv3_address_layout, viewGroup, false);
        return new MemberAddressViewHolder(view, faceRobotoMedium, faceRobotoRegular);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Address address = addressObjectList.get(position);
        MemberAddressViewHolder memberAddressViewHolder = (MemberAddressViewHolder) viewHolder;

        View view = memberAddressViewHolder.getItemView();
        view.setTag(R.id.address_id, addressObjectList.get(position));
        view.setOnClickListener(new OnAddressViewClickListener<>(context));

        TextView txtAddress = memberAddressViewHolder.getTxtAddress();
        txtAddress.setText(address.toString().trim());

        ImageView imgEditIcon = memberAddressViewHolder.getImgEditIcon();
        imgEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddressChangeAware) context).onEditAddressClicked(address);
            }
        });

        TextView txtExpressDelivery = memberAddressViewHolder.getTxtExpressDelivery();
        if (address.isExpress()) {
            txtExpressDelivery.setText(((AppOperationAware) context).getCurrentActivity().getString(R.string.expressAvailable));
            txtExpressDelivery.setVisibility(View.VISIBLE);
        } else
            txtExpressDelivery.setVisibility(View.GONE);

        TextView txtPartialAddress = memberAddressViewHolder.getPartialAddress();
        TextView txtName = memberAddressViewHolder.getTxtName();
        TextView txtPh = memberAddressViewHolder.getTxtPh();
        if (address.isPartial()) {
            txtPartialAddress.setText(((AppOperationAware) context).getCurrentActivity().getString(R.string.incomplete));
            txtPartialAddress.setVisibility(View.VISIBLE);
        } else {
            txtPartialAddress.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(address.getContactNum())) {
            txtPh.setVisibility(View.VISIBLE);
            txtPh.setText(address.getContactNum());
        } else txtPh.setVisibility(View.GONE);
        if (!TextUtils.isEmpty(address.getName())) {
            txtName.setVisibility(View.VISIBLE);
            txtName.setText(address.getName());
        } else txtName.setVisibility(View.GONE);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return addressObjectList.size();
    }

    private static class OnAddressViewClickListener<T> implements View.OnClickListener {

        private T context;

        public OnAddressViewClickListener(T context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            if (context == null) return;
            Address selectedAddress = (Address) v.getTag(R.id.address_id);
            ((AddressSelectionAware) context).onAddressSelected(selectedAddress);
        }
    }

    private static class MemberAddressViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgEditIcon;
        private TextView txtAddress;
        private TextView txtExpressDelivery;
        private View itemView;
        private TextView partial_address_textview;
        private TextView txtName;
        private TextView txtPh;
        private Typeface faceRobotoRegular;
        private Typeface faceRobotoMedium;

        public MemberAddressViewHolder(View itemView, Typeface faceRobotoRegular,
                                       Typeface faceRobotoMedium) {
            super(itemView);
            this.faceRobotoRegular = faceRobotoRegular;
            this.faceRobotoMedium = faceRobotoMedium;
            this.itemView = itemView;
        }

        public View getItemView() {
            return this.itemView;
        }

        public ImageView getImgEditIcon() {
            if (imgEditIcon == null)
                imgEditIcon = (ImageView) itemView.findViewById(R.id.imgEditIcon);
            return imgEditIcon;
        }

        public TextView getTxtName() {
            if (txtName == null) {
                txtName = (TextView) itemView.findViewById(R.id.txtName);
                txtName.setTypeface(faceRobotoMedium);
            }
            return txtName;
        }

        public TextView getTxtPh() {
            if (txtPh == null) {
                txtPh = (TextView) itemView.findViewById(R.id.txtPh);
                txtPh.setTypeface(faceRobotoRegular);
            }
            return txtPh;
        }

        public TextView getTxtAddress() {
            if (txtAddress == null) {
                txtAddress = (TextView) itemView.findViewById(R.id.txtAddress);
                txtAddress.setTypeface(faceRobotoRegular);
            }
            return txtAddress;
        }

        public TextView getTxtExpressDelivery() {
            if (txtExpressDelivery == null) {
                txtExpressDelivery = (TextView) itemView.findViewById(R.id.txtExpressDelivery);
                txtExpressDelivery.setTypeface(faceRobotoRegular);
            }
            return txtExpressDelivery;
        }

        public TextView getPartialAddress() {
            if (partial_address_textview == null) {
                partial_address_textview = (TextView) itemView.findViewById(R.id.txtPartialAddress);
                partial_address_textview.setTypeface(faceRobotoRegular);
            }
            return partial_address_textview;
        }
    }
}