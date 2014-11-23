package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

import java.util.Arrays;

public abstract class FacebookAndGPlusSigninBaseActivity extends PlusBaseActivity {

    private UiLifecycleHelper mFacebookUiLifeCycleHelper;

    public void initializeFacebookLogin(LoginButton btnFBLogin) {

        btnFBLogin = (LoginButton) findViewById(R.id.btnFBLogin);
        btnFBLogin.setReadPermissions(Arrays.asList("public_profile", "email"));

        Session.StatusCallback facebookSessionCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onFacebookSessionStateChanged(session, state, exception);
            }
        };

        mFacebookUiLifeCycleHelper = new UiLifecycleHelper(this, facebookSessionCallback);
    }

    private void onFacebookSessionStateChanged(Session session, SessionState sessionState,
                                               Exception exception) {
        if (exception != null) {
            showToast(exception.getMessage());
            return;
        }

        if (session.isOpened()) {
            onFacebookSignIn(session);
        } else if (session.isClosed()) {
            if (isInLogoutMode()) {
                onFacebookSignOut();
            }
            session.closeAndClearTokenInformation();
        }
    }

    public abstract boolean isInLogoutMode();

    public abstract void onFacebookSignIn(Session facebookSession);

    public abstract void onFacebookSignOut();

    @Override
    protected void onResume() {
        super.onResume();
        Session facebookSession = Session.getActiveSession();
        if (facebookSession != null && (facebookSession.isOpened() || facebookSession.isClosed())) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFacebookUiLifeCycleHelper != null) {
            mFacebookUiLifeCycleHelper.onSaveInstanceState(outState);
        }
    }
}
