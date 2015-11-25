package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.util.Arrays;

public abstract class FacebookAndGPlusSigninBaseActivity extends PlusBaseActivity {

    private CallbackManager mCallbackManager;

    public void initializeFacebookLogin() {

        mCallbackManager = CallbackManager.Factory.create();
        LoginManager loginManager = LoginManager.getInstance();
        loginManager.logInWithReadPermissions(getCurrentActivity(), Arrays.asList("public_profile", "email"));
        loginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                onFacebookSignIn(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                hideKeyboard(getCurrentActivity(), getCurrentFocus());
            }

            @Override
            public void onError(FacebookException e) {

            }
        });
    }

    public abstract void onFacebookSignIn(AccessToken accessToken);

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        super.onActivityResult(requestCode, resultCode, data);
        if (mCallbackManager != null) {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void revokeFbAccess() {
        LoginManager.getInstance().logOut();
    }
}
