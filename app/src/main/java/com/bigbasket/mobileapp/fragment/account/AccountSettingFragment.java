package com.bigbasket.mobileapp.fragment.account;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

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
        ViewGroup contentView = getContentView();
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
                        trackEvent(TrackingAware.MY_ACCOUNT_ACTIVE_ORDER_CLICKED, null);
                        break;
                    case 1:
                        orderListIntent = new Intent(getActivity(), OrderListActivity.class);
                        orderListIntent.putExtra(Constants.ORDER, getString(R.string.past_label));
                        startActivityForResult(orderListIntent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.MY_ACCOUNT_PAST_ORDER_CLICKED, null);
                        break;
                    case 2:
                        Intent intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_UPDATE_PROFILE);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.MY_ACCOUNT_UPDATE_PROFILE_CLICKED, null);
                        break;
                    case 3:
                        intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CHANGE_PASSWD);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.CHANGE_PASSWORD_CLICKED, null);
                        break;
                    case 4:
                        intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WALLET_FRAGMENT);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.MY_ACCOUNT_WALLET_CLICKED, null);
                        break;
                    case 5:
                        intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_DELIVERY_ADDRESS);
                        intent.putExtra(Constants.FROM_ACCOUNT_PAGE, true);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.DELIVERY_ADDRESS_CLICKED, null);
                        break;
                    case 6:
                        intent = new Intent(getActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CHANGE_PIN);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.CHANGE_PIN_CLICKED, null);
                        break;
                }
            }
        });

        contentView.addView(lstMyAccount);
        trackEvent(TrackingAware.MY_ACCOUNT_SHOWN, null);
    }

    @Nullable
    public ViewGroup getContentView() {
        return getView() != null ? (ViewGroup) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return getString(R.string.myAccount);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return AccountSettingFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_SCREEN;
    }

    private class MyAccountListAdapter extends BaseAdapter {

        final String[] itemDetails = {
                getResources().getString(R.string.active_order_label),
                getResources().getString(R.string.past_order_label),
                getResources().getString(R.string.update_my_profile),
                getResources().getString(R.string.change_password),
                getResources().getString(R.string.wallet_activity),
                getResources().getString(R.string.delivery_address),
                getResources().getString(R.string.view_edit_pin_label)};
        int[] imageArray = {
                R.drawable.delivery_van,
                R.drawable.order_history_grey_list,
                R.drawable.edit_profile_grey_list,
                R.drawable.ic_lock_grey600_36dp,
                R.drawable.wallet_grey_list,
                R.drawable.delivery_grey_list,
                R.drawable.dpin};

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
}
