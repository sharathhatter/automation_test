package com.bigbasket.mobileapp.activity.base.uiv3;

import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginConfirmActivity;
import com.bigbasket.mobileapp.activity.promo.FlatPageWebViewActivity;
import com.bigbasket.mobileapp.apiservice.models.response.LoginApiResponse;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.account.SocialAccount;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;

public abstract class BaseSignInSignupActivity extends BackButtonActivity {

    protected AutoCompleteTextView mEmailView;

    protected void populateAutoComplete() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // Use ContactsContract.Profile (API 14+)
            getLoaderManager().initLoader(0, null, new ContactsLoader());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            // Use AccountManager (API 8+)
            new SetupEmailAutoCompleteTask().execute(null, null);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public class ContactsLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                return new CursorLoader(getApplicationContext(),
                        // Retrieve data rows for the device user's 'profile' contact.
                        Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                                ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                        // Select only email addresses.
                        ContactsContract.Contacts.Data.MIMETYPE +
                                " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                        .CONTENT_ITEM_TYPE},

                        // Show primary email addresses first. Note that there won't be
                        // a primary email address if the user hasn't specified one.
                        ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
            List<String> emails = new ArrayList<>();
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                emails.add(cursor.getString(ProfileQuery.ADDRESS));
                cursor.moveToNext();
            }

            addEmailsToAutoComplete(emails);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader) {

        }

    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     * This is used on Android API level 10 and below
     */
    public class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    public void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        int layout = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2 ?
                android.R.layout.simple_dropdown_item_1line : android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getCurrentActivity(), layout, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
    }

    public class LoginApiResponseCallback implements Callback<LoginApiResponse> {

        private String loginType;
        private String email;
        private String password;
        private boolean rememberMe;
        private SocialAccount socialAccount;

        public LoginApiResponseCallback(String email, String password, boolean rememberMe, String loginType) {
            this.email = email;
            this.password = password;
            this.rememberMe = rememberMe;
            this.loginType = loginType;
        }

        public LoginApiResponseCallback(String email, String password, boolean rememberMe, String loginType,
                                        SocialAccount socialAccount) {
            this(email, password, rememberMe, loginType);
            this.loginType = loginType;
            this.socialAccount = socialAccount;
        }

        @Override
        public void success(LoginApiResponse loginApiResponse, retrofit.client.Response response) {
            showProgress(false);
            switch (loginApiResponse.status) {
                case Constants.OK:
                    saveLoginUserDetailInPreference(loginApiResponse, loginType,
                            email, password, rememberMe);
                    switch (loginType) {
                        case SocialAccount.FB: //todo new account created (=yes/no), existing account email (if applicable)
                            trackEvent(TrackingAware.MY_ACCOUNT_FACEBOOK_LOGIN_SUCCESS, null);
                            break;
                        case SocialAccount.GP:
                            trackEvent(TrackingAware.MY_ACCOUNT_GOOGLE_LOGIN_SUCCESS, null);
                            break;
                        case Constants.SIGN_IN_ACCOUNT_TYPE:
                            trackEvent(TrackingAware.MY_ACCOUNT_LOGIN_SUCCESS, null);
                            break;
                        case Constants.REGISTER_ACCOUNT_TYPE:
                            showToast(getString(R.string.thanksForRegistering));
                            trackEvent(TrackingAware.MY_ACCOUNT_REGISTRATION_SUCCESS, null);
                            break;
                        default:
                            throw new AssertionError("Login or register type error while success(status=OK)");
                    }
                    break;
                case Constants.ERROR:
                    switch (loginApiResponse.getErrorTypeAsInt()) {
                        case ApiErrorCodes.INVALID_USER_PASSED:
                            showAlertDialog(null, getString(R.string.INVALID_USER_PASS));
                            break;
                        case ApiErrorCodes.NO_ACCOUNT:
                            Intent intent = new Intent(getCurrentActivity(), SocialLoginConfirmActivity.class);
                            intent.putExtra(Constants.SOCIAL_LOGIN_PARAMS, socialAccount);
                            intent.putExtra(Constants.SOCIAL_LOGIN_TYPE, loginType);
                            startActivityForResult(intent, Constants.SOCIAL_ACCOUNT_NOT_LINKED);
                            break;
                        default:
                            handler.sendEmptyMessage(loginApiResponse.getErrorTypeAsInt());
                            break;
                    }
                    HashMap<String, String> map = new HashMap<>();

                    switch (loginType) {
                        case SocialAccount.FB:
                            map.put(TrackEventkeys.FB_LOGIN_FAILURE_REASON, loginApiResponse.message);
                            trackEvent(TrackingAware.MY_ACCOUNT_FACEBOOK_LOGIN_FAILED, map);
                            break;
                        case SocialAccount.GP:
                            map.put(TrackEventkeys.GOOGLE_LOGIN_FAILURE_REASON, loginApiResponse.message);
                            trackEvent(TrackingAware.MY_ACCOUNT_GOOGLE_LOGIN_FAILED, map);
                            break;
                        case Constants.SIGN_IN_ACCOUNT_TYPE:
                            map.put(TrackEventkeys.LOGIN_FAILURE_REASON, loginApiResponse.message);
                            trackEvent(TrackingAware.MY_ACCOUNT_LOGIN_FAILED, map);
                            break;
                        case Constants.REGISTER_ACCOUNT_TYPE:
                            map.put(TrackEventkeys.REGISTRATION_FAILURE_REASON, loginApiResponse.message);
                            trackEvent(TrackingAware.MY_ACCOUNT_REGISTRATION_FAILED, map);
                            break;
                        default:
                            throw new AssertionError("Login or register type error while success(status=ERROR)");
                    }
                    break;
            }
        }

        @Override
        public void failure(RetrofitError error) {
            showProgress(false);
            HashMap<String, String> map = new HashMap<>();
            handler.handleRetrofitError(error);
            switch (loginType) {
                case SocialAccount.FB:
                    map.put(TrackEventkeys.FB_LOGIN_FAILURE_REASON, error.toString());
                    trackEvent(TrackingAware.MY_ACCOUNT_FACEBOOK_LOGIN_FAILED, map);
                    break;
                case SocialAccount.GP:
                    map.put(TrackEventkeys.GOOGLE_LOGIN_FAILURE_REASON, error.toString());
                    trackEvent(TrackingAware.MY_ACCOUNT_GOOGLE_LOGIN_FAILED, map);
                    break;
                case Constants.SIGN_IN_ACCOUNT_TYPE:
                    map.put(TrackEventkeys.LOGIN_FAILURE_REASON, error.toString());
                    trackEvent(TrackingAware.MY_ACCOUNT_LOGIN_FAILED, map);
                    break;
                case Constants.REGISTER_ACCOUNT_TYPE:
                    map.put(TrackEventkeys.REGISTRATION_FAILURE_REASON, error.toString());
                    trackEvent(TrackingAware.MY_ACCOUNT_REGISTRATION_FAILED, map);
                    break;
                default:
                    throw new AssertionError("Login or register type error while success(status=ERROR)");
            }
        }
    }

    public abstract void showProgress(boolean show);

    public void saveLoginUserDetailInPreference(LoginApiResponse loginApiResponse, String socialAccountType,
                                                String email, String password, boolean rememberMe) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.BBTOKEN_KEY, loginApiResponse.bbToken);
        editor.putString(Constants.MEMBER_EMAIL_KEY, email);
        if (!TextUtils.isEmpty(socialAccountType) && SocialAccount.getSocialLoginTypes().contains(socialAccountType)) {
            editor.putString(Constants.SOCIAL_ACCOUNT_TYPE, socialAccountType);
        } else {
            editor.remove(Constants.SOCIAL_ACCOUNT_TYPE);
        }
        if (rememberMe && !TextUtils.isEmpty(password)) {
            editor.putString(Constants.EMAIL_PREF, email);
            editor.putBoolean(Constants.REMEMBER_ME_PREF, true);
            editor.putString(Constants.PASSWD_PREF, password);
        } else {
            editor.remove(Constants.EMAIL_PREF);
            editor.remove(Constants.REMEMBER_ME_PREF);
            editor.remove(Constants.PASSWD_PREF);
        }
        editor.commit();

        UIUtil.updateStoredUserDetails(getApplication(), getCurrentActivity(),
                loginApiResponse.userDetails, email, loginApiResponse.mId);
        onLoginSuccess();
    }

    public void togglePasswordView(EditText passwordEditText, boolean isChecked) {
        if (!isChecked) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        }
    }

    public void setTermsAndCondition(TextView txtVw) {
        txtVw.setTypeface(faceRobotoRegular);
        SpannableString spannableString = new SpannableString(txtVw.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(),
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtVw.setText(spannableString);
        txtVw.setClickable(true);
        txtVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent flatPageWebviewActivity = new Intent(getCurrentActivity(), FlatPageWebViewActivity.class);
                flatPageWebviewActivity.putExtra(Constants.WEBVIEW_URL, MobileApiUrl.DOMAIN + "terms-and-conditions/");
                flatPageWebviewActivity.putExtra(Constants.WEBVIEW_TITLE, getString(R.string.termsAndCondHeading));
                startActivityForResult(flatPageWebviewActivity, NavigationCodes.GO_TO_HOME);
            }
        });
    }
}
