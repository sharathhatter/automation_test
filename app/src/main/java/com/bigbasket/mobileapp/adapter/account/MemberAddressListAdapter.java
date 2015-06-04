package com.bigbasket.mobileapp.adapter.account;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.model.account.Address;

import org.xml.sax.Attributes;

import java.util.ArrayList;

public class MemberAddressListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_ADDRESS = 0;
    public static final int VIEW_TYPE_LABEL = 1;
    public static final int VIEW_TYPE_ADD_ADDRESS = 2;

    private ArrayList<Object> addressObjectList;
    private T context;
    private LayoutInflater inflater;
    private boolean fromAccount=true;
    private RadioButton selectedRadioBtn;

    public MemberAddressListAdapter(T context, ArrayList<Object> addressObjectList,
                                    boolean fromAccount) {
        this.addressObjectList = addressObjectList;
        this.context = context;
        this.fromAccount = fromAccount;
        inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || position == 2) {
            return VIEW_TYPE_LABEL;
        } else if (position == 3) {
            return VIEW_TYPE_ADD_ADDRESS;
        } else {
            return VIEW_TYPE_ADDRESS;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LABEL:
                View view = inflater.inflate(R.layout.uiv3_address_label_layout, viewGroup, false);
                return new AddressLabelViewHolder(view);
            case VIEW_TYPE_ADDRESS:
                view = inflater.inflate(R.layout.uiv3_address_layout, viewGroup, false);
                return new MemberAddressViewHolder(view);
            case VIEW_TYPE_ADD_ADDRESS:
                view = inflater.inflate(R.layout.uiv3_add_address_layout, viewGroup, false);
                ((TextView)view.findViewById(R.id.txtAddNewAddress)).setTypeface(BaseActivity.faceRobotoMedium);
                return new AddAddressViewHolder(view);
        }
        return null;
    }

    private String getCompleteAddress(Address address) {
        String addressStr = "";
        if (!TextUtils.isEmpty(address.getHouseNumber())) {
            addressStr += address.getHouseNumber() + " ";
        }
        if (!TextUtils.isEmpty(address.getLandmark())) {
            addressStr += address.getLandmark() + " ";
        }
        if (!TextUtils.isEmpty(address.getResidentialComplex())) {
            addressStr += address.getResidentialComplex() + " ";
        }

        if (!TextUtils.isEmpty(address.getStreet())) {
            addressStr += address.getStreet() + " ";
        }

        if (!TextUtils.isEmpty(address.getArea())) {
            addressStr += address.getArea() + " ";
        }
        return addressStr;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        int viewType = getItemViewType(position);
        if (viewType == VIEW_TYPE_LABEL) {
            AddressLabelViewHolder addressLabelViewHolder = (AddressLabelViewHolder) viewHolder;
            TextView txtAddressLabel = addressLabelViewHolder.getTxtAddressLabel();
            String labelTxt = (String) addressObjectList.get(position);
            txtAddressLabel.setText(labelTxt);

        } else if (viewType == VIEW_TYPE_ADDRESS) {
            final Address address = (Address) addressObjectList.get(position);
            MemberAddressViewHolder memberAddressViewHolder = (MemberAddressViewHolder) viewHolder;

            if(position>1){
                View view = memberAddressViewHolder.getItemView();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
                params.bottomMargin = (int)((ActivityAware)context).getCurrentActivity().getResources().getDimension(R.dimen.margin_small);
                view.setLayoutParams(params);
            }


            RadioButton radioBtnSelectedAddress = memberAddressViewHolder.getRadioBtnSelectedAddress();
            if(fromAccount){
                radioBtnSelectedAddress.setVisibility(View.GONE);
            }else {
                if (address.isDefault()) {
                    radioBtnSelectedAddress.setChecked(true);
                    selectedRadioBtn = radioBtnSelectedAddress;
                }
                else
                    radioBtnSelectedAddress.setChecked(false);
            }

            TextView txtAddress = memberAddressViewHolder.getTxtAddress();
            String completeAdd = getCompleteAddress(address);
            txtAddress.setText(completeAdd);

            ImageView imgEditIcon = memberAddressViewHolder.getImgEditIcon();
            imgEditIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((AddressSelectionAware) context).onEditAddressClicked(address);
                }
            });
            if (!fromAccount)
                imgEditIcon.setVisibility(View.INVISIBLE);
            else
                imgEditIcon.setVisibility(View.VISIBLE);

            TextView txtExpressDelivery = memberAddressViewHolder.getTxtExpressDelivery();
            if (!address.isExpress()) {
                txtExpressDelivery.setText(((ActivityAware) context).getCurrentActivity().getString(R.string.expressAvailable));
                txtExpressDelivery.setVisibility(View.VISIBLE);
            } else
                txtExpressDelivery.setVisibility(View.GONE);
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

    public class AddAddressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public AddAddressViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            ((AddressSelectionAware) context).onAddNewAddressClicked();
        }
    }

    public class AddressLabelViewHolder extends RecyclerView.ViewHolder {

        private TextView txtAddressLabel;

        public AddressLabelViewHolder(View itemView) {
            super(itemView);
        }

        public TextView getTxtAddressLabel() {
            if (txtAddressLabel == null) {
                txtAddressLabel = (TextView) itemView.findViewById(R.id.txtAddressLabel);
                txtAddressLabel.setTypeface(BaseActivity.faceRobotoRegular);
            }
            return txtAddressLabel;
        }
    }

    public class MemberAddressViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RadioButton radioBtnSelectedAddress;
        private ImageView imgEditIcon;
        private TextView txtAddress;
        private TextView txtExpressDelivery;
        private View itemView;

        public MemberAddressViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            itemView.setOnClickListener(this);
        }

        public View getItemView(){
            return this.itemView;
        }

        public RadioButton getRadioBtnSelectedAddress() {
            if (radioBtnSelectedAddress == null)
                radioBtnSelectedAddress = (RadioButton) itemView.findViewById(R.id.radioBtnSelectedAddress);
            return radioBtnSelectedAddress;
        }

        public ImageView getImgEditIcon() {
            if (imgEditIcon == null)
                imgEditIcon = (ImageView) itemView.findViewById(R.id.imgEditIcon);
            return imgEditIcon;
        }

        public TextView getTxtAddress() {
            if (txtAddress == null) {
                txtAddress = (TextView) itemView.findViewById(R.id.txtAddress);
                txtAddress.setTypeface(BaseActivity.faceRobotoMedium);
            }

            return txtAddress;
        }

        public TextView getTxtExpressDelivery() {
            if (txtExpressDelivery == null) {
                txtExpressDelivery = (TextView) itemView.findViewById(R.id.txtExpressDelivery);
                txtExpressDelivery.setTypeface(BaseActivity.faceRobotoRegular);
            }
            return txtExpressDelivery;
        }

        @Override
        public void onClick(View v) {
            if(selectedRadioBtn!=radioBtnSelectedAddress){
                radioBtnSelectedAddress.setChecked(true);
                selectedRadioBtn.setChecked(false);
                selectedRadioBtn = radioBtnSelectedAddress;
                Address address = (Address) addressObjectList.get(getPosition());
                ((AddressSelectionAware) context).onAddressSelected(address);
            }
        }
    }
}
