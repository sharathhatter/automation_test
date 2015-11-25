package com.bigbasket.mobileapp.fragment.account;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.DoWalletActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.MyAccountActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;

public class AccountView<T> {

    private ListView lstMyAccount;
    private T context;

    public AccountView(T context, ListView lstMyAccount) {
        this.context = context;
        this.lstMyAccount = lstMyAccount;
        setListView();
    }

    private void setListView() {
        final BaseActivity ctx = ((AppOperationAware) context).getCurrentActivity();
        if (AuthParameters.getInstance(ctx).isAuthTokenEmpty()) {
            // Not logged in
            final String[] itemDetails = {
                    ctx.getResources().getString(R.string.action_sign_in),
                    ctx.getResources().getString(R.string.bbCommHub),
                    ctx.getResources().getString(R.string.changeCityMenuTxt),
                    ctx.getResources().getString(R.string.rateTheApp)};
            MyAccountListAdapter myAccountListAdapter = new MyAccountListAdapter(itemDetails);
            lstMyAccount.setAdapter(myAccountListAdapter);
            lstMyAccount.setOnItemClickListener(new onListItemClickedWhenUserIsNOTLoggedIn());
        } else {
            final String[] itemDetails = {
                    ctx.getResources().getString(R.string.myAccount),
                    ctx.getResources().getString(R.string.my_orders),
                    ctx.getResources().getString(R.string.change_password),
                    ctx.getResources().getString(R.string.bbCommHub),
                    ctx.getResources().getString(R.string.wallet_activity),
                    ctx.getResources().getString(R.string.rateTheApp),
                    ctx.getResources().getString(R.string.signOut)};

            MyAccountListAdapter myAccountListAdapter = new MyAccountListAdapter(itemDetails);
            lstMyAccount.setAdapter(myAccountListAdapter);
            lstMyAccount.setOnItemClickListener(new onListItemClickedWhenUserIsLoggedIn());
        }
    }

    private class onListItemClickedWhenUserIsLoggedIn implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BaseActivity ctx = ((AppOperationAware) context).getCurrentActivity();
            ctx.setNextScreenNavigationContext(TrackEventkeys.ACCOUNT_MENU);
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.ACCOUNT_MENU);
            switch (position) {
                case 0:
                    ctx.trackEvent(TrackingAware.MY_ACCOUNT_CLICKED, map);
                    Intent intent = new Intent(ctx, MyAccountActivity.class);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 1:
                    ctx.trackEvent(TrackingAware.MY_ORDER_CLICKED, map);
                    intent = new Intent(ctx, OrderListActivity.class);
                    intent.putExtra(Constants.ORDER, "all");
                    intent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.ACCOUNT_MENU);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 2:
                    ctx.trackEvent(TrackingAware.CHANGE_PASSWORD_CLICKED, map);
                    intent = new Intent(ctx, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CHANGE_PASSWD);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 3:
                    ctx.launchMoEngageCommunicationHub();
                    break;
                case 4:
                    ctx.trackEvent(TrackingAware.MY_ACCOUNT_WALLET_CLICKED, map);
                    intent = new Intent(ctx, DoWalletActivity.class);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 5:
                    ctx.trackEvent(TrackingAware.RATE_APP_CLICKED, map);
                    try {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + Constants.BASE_PKG_NAME)));
                    } catch (ActivityNotFoundException e) {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + Constants.BASE_PKG_NAME)));
                    }
                    break;
                case 6:
                    ctx.trackEvent(TrackingAware.LOG_OUT_ICON_CLICKED, map);
                    if (ctx instanceof SocialLoginActivity) {
                        ((SocialLoginActivity) ctx).onLogoutRequested();
                    }
                    break;
            }
        }
    }

    private class onListItemClickedWhenUserIsNOTLoggedIn implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BaseActivity ctx = ((AppOperationAware) context).getCurrentActivity();
            HashMap<String, String> map = new HashMap<>();
            map.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.ACCOUNT_MENU);
            switch (position) {
                case 0:
                    ctx.trackEvent(TrackingAware.LOGIN_CLICKED, map);
                    ctx.launchLogin(TrackEventkeys.ACCOUNT_MENU, false);
                    break;
                case 1:
                    ctx.launchMoEngageCommunicationHub();
                    break;
                case 2:
                    ctx.trackEvent(TrackingAware.HOME_CHANGE_CITY, map);
                    ctx.showChangeCity(false, TrackEventkeys.ACCOUNT_MENU, false);
                    break;
                case 3:
                    ctx.trackEvent(TrackingAware.RATE_APP_CLICKED, map);
                    try {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + Constants.BASE_PKG_NAME)));
                    } catch (ActivityNotFoundException e) {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + Constants.BASE_PKG_NAME)));
                    }
                    break;
            }
        }
    }

    private class MyAccountListAdapter extends BaseAdapter {
        private String[] itemDetails;

        public MyAccountListAdapter(String[] itemDetails) {
            this.itemDetails = itemDetails;
        }

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
        @SuppressWarnings("unchecked")
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder viewHolder;
            if (row == null) {
                LayoutInflater inflater = ((AppOperationAware) context).getCurrentActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.uiv3_main_nav_list_row, parent, false);
                row.findViewById(R.id.txtNavListRowSubTitle).setVisibility(View.GONE);
                row.findViewById(R.id.imgNavItem).setVisibility(View.GONE);
                row.findViewById(R.id.imgNavItemExpand).setVisibility(View.GONE);
                viewHolder = new ViewHolder(row);
                row.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) row.getTag();
            }

            final TextView accountTxtView = viewHolder.getItemTitle();
            accountTxtView.setText(itemDetails[position]);
            return row;
        }

        private class ViewHolder {

            private TextView itemTitle;
            private View itemView;

            public ViewHolder(View itemView) {
                this.itemView = itemView;
            }

            public TextView getItemTitle() {
                if (itemTitle == null) {
                    itemTitle = (TextView) itemView.findViewById(R.id.txtNavListRow);
                    itemTitle.setTypeface(FontHolder.getInstance(((AppOperationAware) context)
                            .getCurrentActivity()).getFaceRobotoMedium());
                }
                return itemTitle;
            }
        }
    }
}
