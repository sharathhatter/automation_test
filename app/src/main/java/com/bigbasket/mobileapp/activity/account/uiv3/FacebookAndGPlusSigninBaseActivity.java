package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.os.Bundle;

import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import java.util.Arrays;
import java.util.List;

public abstract class FacebookAndGPlusSigninBaseActivity extends PlusBaseActivity {

    private UiLifecycleHelper mFacebookUiLifeCycleHelper;
    private Session.StatusCallback mFacebookSessionCallback;

    public void initializeFacebookLogin() {
        if (mFacebookSessionCallback != null || mFacebookUiLifeCycleHelper != null) return;
        mFacebookSessionCallback = new Session.StatusCallback() {
            @Override
            public void call(Session session, SessionState state, Exception exception) {
                onFacebookSessionStateChanged(session, state, exception);
            }
        };
        openActiveSession(true, Arrays.asList("public_profile", "email"), mFacebookSessionCallback);

        mFacebookUiLifeCycleHelper = new UiLifecycleHelper(this, mFacebookSessionCallback);
    }

    private Session openActiveSession(boolean allowLoginUi, List<String> permissions, Session.StatusCallback callback) {
        Session.OpenRequest openRequest = new Session.OpenRequest(getCurrentActivity()).
                setPermissions(permissions).setCallback(callback);
        Session session = new Session.Builder(getCurrentActivity()).build();
        if (SessionState.CREATED_TOKEN_LOADED.equals(session.getState()) || allowLoginUi) {
            Session.setActiveSession(session);
            session.openForRead(openRequest);
            return session;
        }
        return null;
    }

    private void onFacebookSessionStateChanged(Session session, SessionState sessionState,
                                               Exception exception) {
        if (exception != null) {
            showToast(exception.getMessage());
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
        setSuspended(false);
        Session activeSession = Session.getActiveSession();
        if (activeSession != null) {
            activeSession.onActivityResult(getCurrentActivity(), requestCode, resultCode, data);
        }
        if (mFacebookUiLifeCycleHelper != null) {
            mFacebookUiLifeCycleHelper.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
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
