package com.bigbasket.mobileapp.adapter.account;

import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.AddressSelectionAware;
import com.bigbasket.mobileapp.model.account.Address;

import java.util.ArrayList;

public class MemberAddressListAdapter<T> extends RecyclerView.Adapter<MemberAddressListAdapter.MemberAddressViewHolder> {
    private ArrayList<Address> addressArrayList;
    private T context;
    private Typeface robotoRegularTypeface;
    private boolean showEditIcon;

    public MemberAddressListAdapter(T context, ArrayList<Address> addressArrayList,
                                    Typeface robotoRegularTypeface) {
        this.addressArrayList = addressArrayList;
        this.context = context;
        this.robotoRegularTypeface = robotoRegularTypeface;
    }

    public MemberAddressListAdapter(T context, ArrayList<Address> addressArrayList, Typeface robotoRegularTypeface,
                                    boolean showEditIcon) {
        this(context, addressArrayList, robotoRegularTypeface);
        this.showEditIcon = showEditIcon;
    }

    @Override
    public MemberAddressListAdapter.MemberAddressViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_member_address_row, viewGroup, false);
        return new MemberAddressListAdapter.MemberAddressViewHolder(base, (AddressSelectionAware) context);
    }

    @Override
    public void onBindViewHolder(MemberAddressListAdapter.MemberAddressViewHolder memberAddressViewHolder, int position) {
        final Address address = addressArrayList.get(position);
        TextView txtMemberAddress = memberAddressViewHolder.getTxtMemberAddress();
        TextView txtMemberName = memberAddressViewHolder.getTxtMemberName();
        TextView txtMemberContactNum = memberAddressViewHolder.getTxtMemberContactNum();
        CheckBox chkIsAddrSelected = memberAddressViewHolder.getChkIsAddrSelected();
        ImageView imgAddressAdditionalAction = memberAddressViewHolder.getImgAddressAdditionalAction();

        txtMemberAddress.setText(address.toString());
        txtMemberName.setText(address.getName());
        txtMemberContactNum.setText(address.getContactNum());
        if (address.isDefault()) {
            chkIsAddrSelected.setChecked(true);
            chkIsAddrSelected.setVisibility(View.VISIBLE);
        } else {
            chkIsAddrSelected.setVisibility(View.GONE);
        }

        if (showEditIcon) {
            imgAddressAdditionalAction.setVisibility(View.VISIBLE);
            imgAddressAdditionalAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu addressPopupMenu = new PopupMenu(((ActivityAware) context).getCurrentActivity(), v);
                    addressPopupMenu.getMenuInflater().inflate(address.isMapped() ? R.menu.address_mapped_menu :
                            R.menu.address_not_mapped_menu, addressPopupMenu.getMenu());

                    addressPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.menuEditAddress:
                                    ((AddressSelectionAware) context).onAddressSelected(address);
                                    return true;
                                case R.id.menuLocateOnMap:
                                    // Defensive check
                                    if (!address.isMapped()) {
                                        ((AddressSelectionAware) context).onLocateOnMapClicked(address);
                                    }
                                    return true;
                                default:
                                    return false;
                            }
                        }
                    });
                    addressPopupMenu.show();
                }
            });

        } else {
            imgAddressAdditionalAction.setVisibility(View.GONE);
        }
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
        private ImageView imgAddressAdditionalAction;
        private AddressSelectionAware addressSelectionAware;

        public MemberAddressViewHolder(View itemView, AddressSelectionAware addressSelectionAware) {
            super(itemView);
            this.addressSelectionAware = addressSelectionAware;
            if (!showEditIcon) {
                itemView.setOnClickListener(this);
            }
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

        public ImageView getImgAddressAdditionalAction() {
            if (imgAddressAdditionalAction == null) {
                imgAddressAdditionalAction = (ImageView) itemView.findViewById(R.id.imgAddressAdditionalAction);
            }
            return imgAddressAdditionalAction;
        }

        @Override
        public void onClick(View v) {
            if (!showEditIcon) {
                Address address = addressArrayList.get(getPosition());
                addressSelectionAware.onAddressSelected(address);
            }
        }
    }
}
