package com.bigbasket.mobileapp.fragment.account;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ChangeCityActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.request.AuthParameters;
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
            int[] imageArray = {
                    R.drawable.nav_user,
                    R.drawable.nav_communication_hub,
                    R.drawable.nav_place,
                    R.drawable.nav_ratethisapp};
            MyAccountListAdapter myAccountListAdapter = new MyAccountListAdapter(itemDetails, imageArray);
            lstMyAccount.setAdapter(myAccountListAdapter);
            lstMyAccount.setOnItemClickListener(new onListItemClickedWhenUserIsNOTLoggedIn());
        } else {
            final String[] itemDetails = {
                    ctx.getResources().getString(R.string.myAccount),
                    ctx.getResources().getString(R.string.bbCommHub),
                    ctx.getResources().getString(R.string.shoppingList),
                    ctx.getResources().getString(R.string.smartBasket),
                    ctx.getResources().getString(R.string.rateTheApp),
                    ctx.getResources().getString(R.string.signOut)};
            int[] imageArray = {
                    R.drawable.nav_user,
                    R.drawable.nav_communication_hub,
                    R.drawable.nav_shoppinglist,
                    R.drawable.nav_smartbasket,
                    R.drawable.nav_ratethisapp,
                    R.drawable.nav_logout,};
            MyAccountListAdapter myAccountListAdapter = new MyAccountListAdapter(itemDetails, imageArray);
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
                    Intent intent = new Intent(ctx, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ACCOUNT_SETTING);
                    ctx.startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                    ((TrackingAware) context).trackEvent(TrackingAware.MY_ACCOUNT_CLICKED, null);
                    break;
                case 1:
                    ctx.launchKonotor();
                    break;
                case 2:
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
                case 3:
                    eventAttribs = new HashMap<>();
                    eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_LEFTNAV);
                    ctx.trackEvent(TrackingAware.LOG_OUT_ICON_CLICKED, eventAttribs);
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
        private int[] imageArray;

        public MyAccountListAdapter(String[] itemDetails, int[] imageArray) {
            this.itemDetails = itemDetails;
            this.imageArray = imageArray;
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
                    itemImg = (ImageView) itemView.findViewById(R.id.imgNavItem);
                }
                return itemImg;
            }

            public TextView getItemTitle() {
                if (itemTitle == null) {
                    itemTitle = (TextView) itemView.findViewById(R.id.txtNavListRow);
                    itemTitle.setTypeface(FontHolder.getInstance(((ActivityAware) context)
                            .getCurrentActivity()).getFaceRobotoLight());
                }
                return itemTitle;
            }
        }
    }
}
