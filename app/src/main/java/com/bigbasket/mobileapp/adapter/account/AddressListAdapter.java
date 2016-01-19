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
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

public class AddressListAdapter<T extends AddressSelectionAware & AddressChangeAware & AppOperationAware> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ADDRESS = 0;

    private final OnAddressViewClickListener<T> addressViewClickListener;
    private final OnAddressEditClickListener<T> addressEditClickListener;
    private ArrayList<Address> addressObjectList;
    private T context;
    private LayoutInflater inflater;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoMedium;
    private Typeface faceRobotoLight;

    public AddressListAdapter(T context, ArrayList<Address> addressObjectList) {
        this.addressObjectList = addressObjectList;
        this.context = context;
        Context ctx = context.getCurrentActivity();
        this.faceRobotoMedium = FontHolder.getInstance(ctx).getFaceRobotoMedium();
        this.faceRobotoRegular = FontHolder.getInstance(ctx).getFaceRobotoRegular();
        this.faceRobotoLight = FontHolder.getInstance(ctx).getFaceRobotoLight();
        inflater = context.getCurrentActivity().getLayoutInflater();
        addressViewClickListener = new OnAddressViewClickListener<>(context);
        addressEditClickListener = new OnAddressEditClickListener<>(context);
    }

    @Override
    public int getItemViewType(int position) {
        return VIEW_TYPE_ADDRESS;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = inflater.inflate(R.layout.uiv3_address_layout, viewGroup, false);
        return new MemberAddressViewHolder(view, faceRobotoMedium, faceRobotoRegular, faceRobotoLight);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final Address address = addressObjectList.get(position);
        MemberAddressViewHolder memberAddressViewHolder = (MemberAddressViewHolder) viewHolder;

        View view = memberAddressViewHolder.getItemView();
        view.setTag(R.id.address_id, addressObjectList.get(position));
        view.setOnClickListener(addressViewClickListener);

        TextView txtAddress = memberAddressViewHolder.getTxtAddress();
        txtAddress.setText(address.toString().trim());

        ImageView imgEditIcon = memberAddressViewHolder.getImgEditIcon();
        imgEditIcon.setTag(R.id.address_id, addressObjectList.get(position));
        imgEditIcon.setOnClickListener(addressEditClickListener);

        Address selectedAddress = context.getSelectedAddress();
        boolean isSelected = selectedAddress == null || TextUtils.isEmpty(selectedAddress.getId()) ?
                address.isSelected() : (selectedAddress.getId().equals(address.getId()));
        if (isSelected && selectedAddress == null) {
            view.setBackgroundResource(R.drawable.grey_border);
        } else {
            view.setBackgroundResource(R.drawable.clickable_white);
        }

        TextView txtExpressDelivery = memberAddressViewHolder.getTxtExpressDelivery();
        if (address.isExpress()) {
            txtExpressDelivery.setText(context.getCurrentActivity().getString(R.string.expressAvailable));
            txtExpressDelivery.setVisibility(View.VISIBLE);
        } else
            txtExpressDelivery.setVisibility(View.GONE);

        TextView txtPartialAddress = memberAddressViewHolder.getPartialAddress();
        TextView txtName = memberAddressViewHolder.getTxtName();
        TextView txtPh = memberAddressViewHolder.getTxtPh();
        if (address.isPartial()) {
            txtPartialAddress.setText(context.getCurrentActivity().getString(R.string.incomplete));
            txtPartialAddress.setVisibility(View.VISIBLE);
        } else {
            txtPartialAddress.setVisibility(View.GONE);
        }
        txtPartialAddress.setTag(R.id.address_id, addressObjectList.get(position));
        txtPartialAddress.setOnClickListener(addressEditClickListener);

        if (!UIUtil.isEmpty(address.getContactNum())) {
            txtPh.setVisibility(View.VISIBLE);
            txtPh.setText(address.getContactNum());
        } else txtPh.setVisibility(View.GONE);
        if (!UIUtil.isEmpty(address.getAddressNickName())) {
            txtName.setVisibility(View.VISIBLE);
            txtName.setText(address.getAddressNickName().trim());
        } else if (!UIUtil.isEmpty(address.getAltAddressNickName())) {
            txtName.setVisibility(View.VISIBLE);
            txtName.setText(address.getAltAddressNickName().trim());
        } else {
            txtName.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return addressObjectList.size();
    }

    private static class OnAddressViewClickListener<T extends AddressSelectionAware> implements View.OnClickListener {

        private T context;

        public OnAddressViewClickListener(T context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            if (context == null) return;
            Address selectedAddress = (Address) v.getTag(R.id.address_id);
            context.onAddressSelected(selectedAddress);
        }
    }

    private static class OnAddressEditClickListener<T extends AddressChangeAware> implements View.OnClickListener {

        private T context;

        public OnAddressEditClickListener(T context) {
            this.context = context;
        }

        @Override
        public void onClick(View v) {
            if (context == null) return;
            Address selectedAddress = (Address) v.getTag(R.id.address_id);
            context.onEditAddressClicked(selectedAddress);
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
        private Typeface faceRobotoLight;

        public MemberAddressViewHolder(View itemView, Typeface faceRobotoRegular,
                                       Typeface faceRobotoMedium, Typeface faceRobotoLight) {
            super(itemView);
            this.faceRobotoRegular = faceRobotoRegular;
            this.faceRobotoMedium = faceRobotoMedium;
            this.faceRobotoLight = faceRobotoLight;
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
                txtPh.setTypeface(faceRobotoLight);
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