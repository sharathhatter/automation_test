package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

public abstract class FacebookAndGPlusSigninBaseActivity extends PlusBaseActivity {

    private UiLifecycleHelper mFacebookUiLifeCycleHelper;
    private Session.StatusCallback mFacebookSessionCallback;

    public void initializeFacebookLogin(LoginButton btnFBLogin) {
        if (mFacebookSessionCallback != null || mFacebookUiLifeCycleHelper != null) return;
        btnFBLogin.setReadPermissions(Arrays.asList("public_profile", "email"));

        mFacebookSessionCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onFacebookSessionStateChanged(session, state, exception);
            }
        };

        mFacebookUiLifeCycleHelper = new UiLifecycleHelper(this, mFacebookSessionCallback);
    }

    private void onFacebookSessionStateChanged(Session session, SessionState sessionState,
                                               Exception exception) {
        if (exception != null) {
            return;
        }

        AuthParameters authParameters = AuthParameters.getInstance(this);
        if (session.isOpened()) {
            if (authParameters.isAuthTokenEmpty()) {
                onFacebookSignIn(session);
            }
        } else if (session.isClosed()) {
            if (!authParameters.isAuthTokenEmpty()) {
                onFacebookSignOut();
            }
        }
    }

    public abstract void onFacebookSignIn(Session facebookSession);

    public abstract void onFacebookSignOut();

    @Override
    protected void onResume() {
        super.onResume();
        Session facebookSession = Session.getActiveSession();
        if (mFacebookSessionCallback == null &&
                facebookSession != null && (facebookSession.isOpened() || facebookSession.isClosed())) {
            onFacebookSessionStateChanged(facebookSession, facebookSession.getState(), null);
        }
        if (mFacebookUiLifeCycleHelper != null) {
            mFacebookUiLifeCycleHelper.onResume();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mFacebookUiLifeCycleHelper != null) {
            mFacebookUiLifeCycleHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFacebookUiLifeCycleHelper != null) {
            mFacebookUiLifeCycleHelper.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFacebookUiLifeCycleHelper != null) {
            mFacebookUiLifeCycleHelper.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFacebookUiLifeCycleHelper != null) {
            mFacebookUiLifeCycleHelper.onSaveInstanceState(outState);
        }
    }

    public void revokeFbAccess() {
        Session facebookSession = Session.getActiveSession();
        if (facebookSession != null && facebookSession.isOpened()) {
            facebookSession.closeAndClearTokenInformation();
        }
    }
}
