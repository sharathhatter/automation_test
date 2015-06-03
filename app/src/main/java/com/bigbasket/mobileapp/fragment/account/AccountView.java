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
import com.bigbasket.mobileapp.activity.account.uiv3.ChangeCityActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.MyAccountActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.UpdatePinActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListActivity;
import com.bigbasket.mobileapp.activity.shoppinglist.ShoppingListSummaryActivity;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateInfo;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.HashMap;
import java.util.Map;

public class AccountView<T> {

    private ListView lstMyAccount;
    private T context;

    public AccountView(T context, ListView lstMyAccount) {
        this.context = context;
        this.lstMyAccount = lstMyAccount;
        setListView();
    }

    public void setListView() {
        final BaseActivity ctx = ((ActivityAware) context).getCurrentActivity();
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
                    ctx.getResources().getString(R.string.view_edit_pin_label),
                    ctx.getResources().getString(R.string.bbCommHub),
                    ctx.getResources().getString(R.string.moEngageCommHub),
                    ctx.getResources().getString(R.string.shoppingList),
                    ctx.getResources().getString(R.string.smartBasket),
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
            final BaseActivity ctx = ((ActivityAware) context).getCurrentActivity();
            switch (position) {
                case 0:
                    ctx.trackEvent(TrackingAware.MY_ACCOUNT_CLICKED, null);
                    Intent intent = new Intent(ctx, MyAccountActivity.class);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 1:
                    ctx.trackEvent(TrackingAware.MY_ORDER_CLICKED, null);
                    intent = new Intent(ctx, OrderListActivity.class);
                    intent.putExtra(Constants.ORDER, "all");
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 2:
                    ctx.trackEvent(TrackingAware.DELIVERY_ADDRESS_CLICKED, null);
                    intent = new Intent(ctx, UpdatePinActivity.class);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 3:
                    ctx.launchKonotor();
                    break;
                case 4:
                    ctx.launchMoEngageCommunicationHub();
                    break;
                case 5:
                    intent = new Intent(ctx, ShoppingListActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    ctx.onChangeFragment(new ShoppingListFragment());
                    break;
                case 6:
                    ShoppingListName shoppingListName = new ShoppingListName(Constants.SMART_BASKET,
                            Constants.SMART_BASKET_SLUG, true);
                    intent = new Intent(ctx, ShoppingListSummaryActivity.class);
                    intent.putExtra(Constants.SHOPPING_LIST_NAME, shoppingListName);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    break;
                case 7:
                    Map<String, String> eventAttribs = new HashMap<>();
                    eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LEFTNAV);
                    ctx.trackEvent(TrackingAware.RATE_APP_CLICKED, eventAttribs);
                    try {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=" + Constants.BASE_PKG_NAME)));
                    } catch (ActivityNotFoundException e) {
                        ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + Constants.BASE_PKG_NAME)));
                    }
                    break;
                case 8:
                    eventAttribs = new HashMap<>();
                    eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LEFTNAV);
                    ctx.trackEvent(TrackingAware.LOG_OUT_ICON_CLICKED, eventAttribs);
                    if (ctx instanceof SocialLoginActivity) {
                        ((SocialLoginActivity) ctx).onLogoutRequested();
                    }
                    break;
                case 9:
                    ctx.trackEvent(TrackingAware.DELIVERY_ADDRESS_CLICKED, null);
                    intent = new Intent(ctx, UpdatePinActivity.class);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
            }
        }
    }

    private class onListItemClickedWhenUserIsNOTLoggedIn implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final BaseActivity ctx = ((ActivityAware) context).getCurrentActivity();
            switch (position) {
                case 0:
                    ctx.launchLogin(TrackEventkeys.NAVIGATION_CTX_TOPNAV);
                    break;
                case 1:
                    ctx.launchKonotor();
                    break;
                case 2:
                    Map<String, String> eventAttribs = new HashMap<>();
                    eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LEFTNAV);
                    ctx.trackEvent(TrackingAware.HOME_CHANGE_CITY, eventAttribs);
                    Intent intent = new Intent(ctx, ChangeCityActivity.class);
                    ctx.startActivityForResult(intent, NavigationCodes.CITY_CHANGED);
                    break;
                case 3:
                    eventAttribs = new HashMap<>();
                    eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LEFTNAV);
                    ctx.trackEvent(TrackingAware.RATE_APP_CLICKED, eventAttribs);
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ViewHolder viewHolder;
            if (row == null) {
                LayoutInflater inflater = ((ActivityAware) context).getCurrentActivity().getLayoutInflater();
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
                    itemTitle.setTypeface(FontHolder.getInstance(((ActivityAware) context)
                            .getCurrentActivity()).getFaceRobotoRegular());
                }
                return itemTitle;
            }
        }
    }
}
