package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.facebook.AccessToken;
import com.facebook.login.widget.ProfilePictureView;
import com.google.android.gms.plus.model.people.Person;

public class MyAccountActivity extends BackButtonActivity {

    private ViewGroup mLayoutProfilePicture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.myAccount));
        mLayoutProfilePicture = (ViewGroup) findViewById(R.id.layoutProfilePicture);

        loadMyAccount();
    }

    private void loadMyAccount() {

        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);

        MyAccountListAdapter myAccountListAdapter = new MyAccountListAdapter();
        lstMyAccount.setAdapter(myAccountListAdapter);
        lstMyAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        Intent orderListIntent = new Intent(getCurrentActivity(), OrderListActivity.class);
                        orderListIntent.putExtra(Constants.ORDER, getString(R.string.active_label));
                        orderListIntent.putExtra(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_MY_ACCOUNT);
                        startActivityForResult(orderListIntent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.MY_ACCOUNT_ACTIVE_ORDER_CLICKED, null);
                        break;
                    case 1:
                        orderListIntent = new Intent(getCurrentActivity(), OrderListActivity.class);
                        orderListIntent.putExtra(Constants.ORDER, getString(R.string.past_label));
                        startActivityForResult(orderListIntent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.MY_ACCOUNT_PAST_ORDER_CLICKED, null);
                        break;
                    case 2:
                        Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_UPDATE_PROFILE);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.MY_ACCOUNT_UPDATE_PROFILE_CLICKED, null);
                        break;
                    case 3:
                        intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CHANGE_PASSWD);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.CHANGE_PASSWORD_CLICKED, null);
                        break;
                    case 4:
                        intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_WALLET_FRAGMENT);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.MY_ACCOUNT_WALLET_CLICKED, null);
                        break;
                    case 5:
                        intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_DELIVERY_ADDRESS);
                        intent.putExtra(Constants.FROM_ACCOUNT_PAGE, true);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.DELIVERY_ADDRESS_CLICKED, null);
                        break;
                    case 6:
                        intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_CHANGE_PIN);
                        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                        trackEvent(TrackingAware.CHANGE_PIN_CLICKED, null);
                        break;
                }
            }
        });

        TextView txtMemberName = (TextView) findViewById(R.id.txtMemberName);
        txtMemberName.setTypeface(faceRobotoRegular);
        txtMemberName.setText(AuthParameters.getInstance(this).getMemberFullName());
        loadProfileImage();

        trackEvent(TrackingAware.MY_ACCOUNT_SHOWN, null);
    }

    @Override
    public void onFacebookSignIn(AccessToken accessToken) {
        loadFbImage(accessToken);
    }

    @Override
    protected void onPlusClientSignIn(String email, Person person) {
        loadGPlusImage(person);
    }

    private void loadProfileImage() {
        if (getCurrentActivity() == null) return;

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String socialAccountType = preferences.getString(Constants.SOCIAL_ACCOUNT_TYPE, "");
        if (!TextUtils.isEmpty(socialAccountType) && SocialAccount.getSocialLoginTypes().contains(socialAccountType)
                && checkInternetConnection()) {
            switch (socialAccountType) {
                case SocialAccount.GP:
                    initializeGooglePlusSignIn();
                    initiatePlusClientConnect();
                    break;
                case SocialAccount.FB:
                    onFacebookSignIn(AccessToken.getCurrentAccessToken());
                    break;
            }
        } else {
            loadDefaultPic();
        }
    }

    private void loadGPlusImage(Person person) {
        if (person != null && person.getImage() != null &&
                person.getImage().hasUrl()) {
            ImageView imgProfile = new ImageView(this);
            mLayoutProfilePicture.removeAllViews();
            mLayoutProfilePicture.addView(imgProfile);
            UIUtil.displayAsyncImage(imgProfile, person.getImage().getUrl());
        } else {
            loadDefaultPic();
        }
    }

    private void loadFbImage(AccessToken accessToken) {
        if (accessToken == null) {
            loadDefaultPic();
            return;
        }
        ProfilePictureView profilePictureView = new ProfilePictureView(this);
        profilePictureView.setPresetSize(ProfilePictureView.SMALL);
        ViewGroup.LayoutParams layoutParams = new
                ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        profilePictureView.setLayoutParams(layoutParams);
        mLayoutProfilePicture.removeAllViews();
        mLayoutProfilePicture.addView(profilePictureView);
        profilePictureView.setProfileId(AccessToken.getCurrentAccessToken().getUserId());
    }

    private void loadDefaultPic() {
        ImageView imgProfile = new ImageView(this);
        imgProfile.setImageDrawable(ContextCompat.getDrawable(this,
                R.mipmap.ic_launcher));
        mLayoutProfilePicture.removeAllViews();
        mLayoutProfilePicture.addView(imgProfile);
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
                row = getLayoutInflater().inflate(R.layout.uiv3_list_icon_and_text_row, parent, false);
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

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_my_account;
    }
}
