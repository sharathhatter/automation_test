package com.bigbasket.mobileapp.fragment.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SpendTrendsActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.order.MemberAddressListFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
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

        ListView lstMyAccount = new ListView(getActivity());
        lstMyAccount.setDivider(null);
        lstMyAccount.setDividerHeight(0);

        MyAccountListAdapter myAccountListAdapter = new MyAccountListAdapter();
        lstMyAccount.setAdapter(myAccountListAdapter);
        lstMyAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
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
                        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_UPDATE_PROFILE);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        break;
                    case 3:
                        intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CHANGE_PASSWD);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        break;
                    case 4:
                        intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WALLET_FRAGMENT);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        break;
                    case 5:
                        MemberAddressListFragment memberAddressListFragment = new MemberAddressListFragment();
                        Bundle addressbundle = new Bundle();
                        addressbundle.putBoolean(Constants.FROM_ACCOUNT_PAGE, true);
                        memberAddressListFragment.setArguments(addressbundle);
                        changeFragment(memberAddressListFragment);
                        break;
                    case 6:
                        intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CHANGE_PIN);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        break;
                    case 7:
                        intent = new Intent(getActivity(), SpendTrendsActivity.class);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        break;
                }
            }
        });

        contentView.addView(lstMyAccount);
    }

    private class MyAccountListAdapter extends BaseAdapter {

        String[] itemDetails = {
                getResources().getString(R.string.active_order_label),
                getResources().getString(R.string.past_order_label),
                getResources().getString(R.string.update_my_profile),
                getResources().getString(R.string.change_password),
                getResources().getString(R.string.wallet_activity),
                getResources().getString(R.string.delivery_address),
                getResources().getString(R.string.view_edit_pin_label),
                getString(R.string.spendTrends)};
        int[] imageArray = {
                R.drawable.ic_local_shipping_grey600_36dp,
                R.drawable.history_dark,
                R.drawable.user_modify_dark,
                R.drawable.ic_lock_grey600_36dp,
                R.drawable.ic_account_balance_wallet_grey600_36dp,
                R.drawable.ic_place_grey600_36dp,
                R.drawable.ic_edit_grey600_36dp,
                R.drawable.ic_trending_up_grey600_36dp};

        @Override
        public int getCount() {
            return itemDetails.length;
        }

        @Override
        public Object getItem(int position) {
            return itemDetails[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder viewHolder;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.uiv3_list_icon_and_text_row, parent, false);
                viewHolder = new ViewHolder(row);
                row.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) row.getTag();
            }
            ImageView accountImageView = viewHolder.getItemImg();
            accountImageView.setBackgroundResource(imageArray[position]);

            final TextView accountTxtView = viewHolder.getItemTitle();
            accountTxtView.setText(itemDetails[position]);
            return row;
        }

        private class ViewHolder {

            private ImageView itemImg;
            private TextView itemTitle;
            private View itemView;

            public ViewHolder(View itemView) {
                this.itemView = itemView;
            }

            public ImageView getItemImg() {
                if (itemImg == null) {
                    itemImg = (ImageView) itemView.findViewById(R.id.itemImg);
                }
                return itemImg;
            }

            public TextView getItemTitle() {
                if (itemTitle == null) {
                    itemTitle = (TextView) itemView.findViewById(R.id.itemTitle);
                    itemTitle.setTypeface(faceRobotoRegular);
                }
                return itemTitle;
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
