package com.bigbasket.mobileapp.fragment.account;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.order.MemberAddressListFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;

public class AccountSettingFragment extends BaseFragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.uiv3_list_container, container, false);
        view.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_light_color));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadMyAccount();
    }


    private void loadMyAccount() {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        String[] ItemDetails = {
                getResources().getString(R.string.active_order_label),
                getResources().getString(R.string.past_order_label),
                getResources().getString(R.string.update_my_profile),
                getResources().getString(R.string.change_password),
                getResources().getString(R.string.wallet_activity),
                getResources().getString(R.string.delivery_address),
                getResources().getString(R.string.view_edit_pin_label)};
        int[] imageArray = {
                R.drawable.active_orders_dark,
                R.drawable.history_dark,
                R.drawable.user_modify_dark,
                R.drawable.lock_large,
                R.drawable.safety_box_large,
                R.drawable.place_large,
                R.drawable.edit_large_dark};
        int imageArrayLen = imageArray.length;
        contentView.removeAllViews();
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i = 0; i < imageArrayLen; i++) {
            View base = inflater.inflate(R.layout.uiv3_list_icon_and_text_row, null);

            RelativeLayout accountMainLayout = (RelativeLayout) base.findViewById(R.id.layoutRow);
            accountMainLayout.setId(i);

            accountMainLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (view.getId()) {
                        case 0:
                            Intent orderListIntent = new Intent(getActivity(), OrderListActivity.class);
                            orderListIntent.putExtra(Constants.ORDER, getString(R.string.active_label));
                            startActivityForResult(orderListIntent, NavigationCodes.GO_TO_HOME);
                            break;
                        case 1:
                            orderListIntent = new Intent(getActivity(), OrderListActivity.class);
                            orderListIntent.putExtra(Constants.ORDER, getString(R.string.past_label));
                            startActivityForResult(orderListIntent, NavigationCodes.GO_TO_HOME);
                            break;
                        case 2:
                            changeFragment(new UpdateProfileFragment());
                            break;
                        case 3:
                            changeFragment(new ChangePasswordFragment());
                            break;
                        case 4:
                            changeFragment(new DoWalletFragment());
                            break;
                        case 5:
                            MemberAddressListFragment memberAddressListFragment = new MemberAddressListFragment();
                            Bundle addressbundle = new Bundle();
                            addressbundle.putBoolean(Constants.FROM_ACCOUNT_PAGE, true);
                            memberAddressListFragment.setArguments(addressbundle);
                            changeFragment(memberAddressListFragment);
                            break;
                        case 6:
                            changeFragment(new UpdatePinFragment());
                            break;
                    }
                }
            });

            ImageView accountImageView = (ImageView) base.findViewById(R.id.itemImg);
            accountImageView.setBackgroundResource(imageArray[i]);

            final TextView accountTxtView = (TextView) base.findViewById(R.id.itemTitle);
            accountTxtView.setTypeface(faceRobotoRegular);
            accountTxtView.setText(ItemDetails[i]);

            if (i != imageArrayLen - 1) {
                View dividerLine = base.findViewById(R.id.dividerLine);
                dividerLine.setVisibility(View.VISIBLE);
                contentView.addView(base);
            }
        }
    }


    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Account Settings";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return AccountSettingFragment.class.getName();
    }
}
