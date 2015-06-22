package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.SearchableActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.UpdateProfileApiResponse;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.model.account.UpdateProfileModel;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.ImageViewCircular;
import com.facebook.AccessToken;
import com.google.android.gms.plus.model.people.Person;

import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MyAccountActivity extends BackButtonActivity {

    private UpdateProfileModel updateProfileModel;
    private ImageViewCircular circularImageView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.myAccount));
        getMemberDetails();
    }

    private void getMemberDetails() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.getMemberProfileData(new Callback<ApiResponse<UpdateProfileApiResponse>>() {
            @Override
            public void success(ApiResponse<UpdateProfileApiResponse> memberProfileDataCallback, Response response) {
                hideProgressDialog();
                if (memberProfileDataCallback.status == 0) {
                    updateProfileModel = memberProfileDataCallback.apiResponseContent.memberDetails;
                    renderProfileData(updateProfileModel);
                } else {
                    handler.sendEmptyMessage(memberProfileDataCallback.status, memberProfileDataCallback.message, true);
                    Map<String, String> eventAttribs = new HashMap<>();
                    eventAttribs.put(TrackEventkeys.FAILURE_REASON, memberProfileDataCallback.message);
                    trackEvent(TrackingAware.UPDATE_PROFILE_GET_FAILED, eventAttribs);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                handler.handleRetrofitError(error, true);
                Map<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(TrackEventkeys.FAILURE_REASON, error.toString());
                trackEvent(TrackingAware.UPDATE_PROFILE_GET_FAILED, eventAttribs);
            }
        });
    }


    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.my_account, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_edit_detail:
                Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_UPDATE_PROFILE);
                intent.putExtra(Constants.UPDATE_PROFILE_OBJ, updateProfileModel);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                trackEvent(TrackingAware.MY_ACCOUNT_UPDATE_PROFILE_CLICKED, null);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.ACCOUNT_UPDATED) {
            getMemberDetails();
            setResult(NavigationCodes.ACCOUNT_UPDATED);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void renderProfileData(UpdateProfileModel updateProfileModel) {
        if (updateProfileModel == null) return;

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        contentLayout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getCurrentActivity());
        View view = inflater.inflate(R.layout.uiv3_my_account, contentLayout, false);

        circularImageView = (ImageViewCircular) view.findViewById(R.id.circularImageView);
        TextView txtMemberName = (TextView) view.findViewById(R.id.txtMemberName);
        txtMemberName.setTypeface(faceRobotoMedium);
        txtMemberName.setText(updateProfileModel.getFirstName() + " " + updateProfileModel.getLastName());
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String profileImageUrl = sharedPreferences.getString(Constants.UPDATE_PROFILE_IMG_URL, null);
        if (TextUtils.isEmpty(sharedPreferences.getString(Constants.UPDATE_PROFILE_IMG_URL, null)))
            loadProfileImage();
        else
            renderProfileImage(profileImageUrl);


        TextView txtEmailId = (TextView) view.findViewById(R.id.txtEmailId);
        //txtEmailId.setPaintFlags(txtEmailId.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        txtEmailId.setText(updateProfileModel.getEmail());


        RelativeLayout layoutPhoneNumber = (RelativeLayout) view.findViewById(R.id.layoutPhoneNumber);
        if (!TextUtils.isEmpty(updateProfileModel.getMobileNumber())) {
            TextView txtPhoneLabel = (TextView) view.findViewById(R.id.txtPhoneLabel);
            txtPhoneLabel.setTypeface(faceRobotoRegular);

            TextView txtPhone = (TextView) view.findViewById(R.id.txtPhone);
            txtPhone.setTypeface(faceRobotoMedium);
            txtPhone.setText(updateProfileModel.getMobileNumber());
        } else {
            layoutPhoneNumber.setVisibility(View.GONE);
        }

        RelativeLayout layoutBirthday = (RelativeLayout) view.findViewById(R.id.layoutBirthday);
        if (!TextUtils.isEmpty(updateProfileModel.getDateOfBirth())) {
            TextView txtBirthdayLabel = (TextView) view.findViewById(R.id.txtBirthdayLabel);
            txtBirthdayLabel.setTypeface(faceRobotoRegular);

            TextView txtBirthday = (TextView) view.findViewById(R.id.txtBirthday);
            txtBirthday.setTypeface(faceRobotoMedium);
            txtBirthday.setText(updateProfileModel.getDateOfBirth());
        } else {
            layoutBirthday.setVisibility(View.GONE);
        }

        RelativeLayout layoutAddress = (RelativeLayout) view.findViewById(R.id.layoutAddress);
        String address = getAddress(updateProfileModel);
        if (!TextUtils.isEmpty(address)) {
            TextView txtAddressLabel = (TextView) view.findViewById(R.id.txtAddressLabel);
            txtAddressLabel.setTypeface(faceRobotoRegular);

            address += updateProfileModel.getCityName();
            if (!TextUtils.isEmpty(updateProfileModel.getPincode()))
                address += " - " + updateProfileModel.getPincode();
            TextView txtAddress = (TextView) view.findViewById(R.id.txtAddress);
            txtAddress.setTypeface(faceRobotoMedium);
            txtAddress.setText(address);
        } else {
            layoutAddress.setVisibility(View.GONE);
            RelativeLayout layoutEmptyAddress = (RelativeLayout) view.findViewById(R.id.layoutEmptyAddress);
            layoutEmptyAddress.setVisibility(View.VISIBLE);
        }

        contentLayout.addView(view);
        trackEvent(TrackingAware.MY_ACCOUNT_SHOWN, null);
    }

    public void renderAddressActivity(View view) {
        Intent intent = new Intent(getCurrentActivity(), BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_DELIVERY_ADDRESS);
        intent.putExtra(Constants.FROM_ACCOUNT_PAGE, true);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        trackEvent(TrackingAware.DELIVERY_ADDRESS_CLICKED, null);
    }

    private String getAddress(UpdateProfileModel updateProfileModel) {
        String address = "";
        if (!TextUtils.isEmpty(updateProfileModel.getHouseNumber())) {
            address += updateProfileModel.getHouseNumber() + " ";
        }
        if (!TextUtils.isEmpty(updateProfileModel.getLandmark())) {
            address += updateProfileModel.getLandmark() + " ";
        }
        if (!TextUtils.isEmpty(updateProfileModel.getResidentialComplex())) {
            address += updateProfileModel.getResidentialComplex() + " ";
        }

        if (!TextUtils.isEmpty(updateProfileModel.getStreet())) {
            address += updateProfileModel.getStreet() + " ";
        }

        if (!TextUtils.isEmpty(updateProfileModel.getArea())) {
            address += updateProfileModel.getArea() + " ";
        }

        return address;
    }


    @Override
    public void onFacebookSignIn(final AccessToken accessToken) {
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

    private void renderProfileImage(String url) {
        UIUtil.displayAsyncImage(circularImageView, url);
    }

    private void loadGPlusImage(Person person) {
        if (person != null && person.getImage() != null &&
                person.getImage().hasUrl()) {

            String profilePicUrl = person.getImage().getUrl();
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
            editor.putString(Constants.UPDATE_PROFILE_IMG_URL, profilePicUrl);
            editor.commit();
            renderProfileImage(profilePicUrl);
        } else {
            loadDefaultPic();
        }
    }

    private void loadFbImage(AccessToken accessToken) {
        if (accessToken == null) {
            loadDefaultPic();
            return;
        }

        String profilePicUrl = "https://graph.facebook.com/" +
                accessToken.getUserId() + "/picture?type=normal";
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.UPDATE_PROFILE_IMG_URL, profilePicUrl);
        editor.commit();
        renderProfileImage(profilePicUrl);
    }

    private void loadDefaultPic() {
        circularImageView.setImageDrawable(ContextCompat.getDrawable(this,
                R.mipmap.ic_launcher));
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.ACCOUNT_SCREEN;
    }

}
