package com.bigbasket.mobileapp.activity.account.uiv3;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

/**
 * A base class to wrap communication with the Google Play Services PlusClient.
 */
public abstract class PlusBaseActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // A magic number we will use to know that our sign-in error resolution activity has completed
    private static final int RC_SIGN_IN = 49404;
    // A flag to track when a connection is already in progress
    public boolean mPlusClientIsConnecting = false;
    protected boolean mIsInLogoutMode;
    // A flag to stop multiple dialogues appearing for the user
    private boolean mAutoResolveOnFail;
    // This is the helper object that connects to Google Play Services.
    private GoogleApiClient mGoogleApiClient;
    // The saved result from {@link #onConnectionFailed(ConnectionResult)}.  If a connection
    // attempt has been made, this is non-null.
    // If this IS null, then the connect method is still running.
    private ConnectionResult mConnectionResult;
    // A flag to track if connection is being established to revoke access
    private boolean mRevokeAccess;

    /**
     * Called when the PlusClient revokes access to this app.
     */
    protected abstract void onPlusClientRevokeAccess();

    /**
     * Called when the PlusClient is successfully connected.
     */
    protected abstract void onPlusClientSignIn(String authToken);

    /**
     * Called when the PlusClient is disconnected.
     */
    protected abstract void onPlusClientSignOut();

    /**
     * Called when the PlusClient is blocking the UI.  If you have a progress bar widget,
     * this tells you when to show or hide it.
     */
    protected abstract void onPlusClientBlockingUI(boolean show);

    /**
     * Called when there is a change in connection state.  If you have "Sign in"/ "Connect",
     * "Sign out"/ "Disconnect", or "Revoke access" buttons, this lets you know when their states
     * need to be updated.
     */
    protected abstract void updatePlusConnectedButtonState();

    public void initializeGooglePlusSignIn() {
        // Initialize the PlusClient connection.
        // Scopes indicate the information about the user your application will be able to access.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();
    }

    /**
     * Try to sign in the user.
     */
    public void signInViaGPlus() {
        if (mGoogleApiClient == null) return;
        if (!mGoogleApiClient.isConnected()) {
            // Show the dialog as we are now signing in.
            setProgressBarVisible(true);
            // Make sure that we will start the resolution (e.g. fire the intent and pop up a
            // dialog for the user) for any errors that come in.
            mAutoResolveOnFail = true;
            // We should always have a connection result ready to resolve,
            // so we can start that process.
            if (mConnectionResult != null) {
                startResolution();
            } else {
                // If we don't have one though, we can start connect in
                // order to retrieve one.
                initiatePlusClientConnect();
            }
        }

        updatePlusConnectedButtonState();
    }

    /**
     * Connect the PlusClient only if a connection isn't already in progress.  This will
     * call back to {@link #onConnected(android.os.Bundle)} or
     * {@link #onConnectionFailed(com.google.android.gms.common.ConnectionResult)}.
     */
    public void initiatePlusClientConnect() {
        if (mGoogleApiClient == null) return;
        if (!mGoogleApiClient.isConnecting()) {
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            } else {
                onConnected(null);
            }
        }
    }

    public void initiatePlusClientDisconnect() {
        if (mGoogleApiClient == null) return;
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Sign out the user (so they can switch to another account).
     */
    public void signOutFromGplus() {
        if (mGoogleApiClient == null) return;

        // We only want to sign out if we're connected.
        if (mGoogleApiClient.isConnected()) {
            // Clear the default account in order to allow the user to potentially choose a
            // different account from the account chooser.
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();

            // Disconnect from Google Play Services, then reconnect in order to restart the
            // process from scratch.
            initiatePlusClientDisconnect();
            onPlusClientSignOut();
        }

        updatePlusConnectedButtonState();
    }

    /**
     * Revoke Google+ authorization completely.
     */
    public void revokeGPlusAccess() {
        if (mGoogleApiClient == null) return;

        if (mGoogleApiClient.isConnected()) {
            // Clear the default account as in the Sign Out.
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {

                    // Revoke access to this entire application. This will call back to
                    // onAccessRevoked when it is complete, as it needs to reach the Google
                    // authentication servers to revoke all tokens.
                    updatePlusConnectedButtonState();
                    onPlusClientRevokeAccess();
                }
            });
        } else {
            mRevokeAccess = true;
            initiatePlusClientConnect();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        initiatePlusClientDisconnect();
    }

    private void setProgressBarVisible(boolean flag) {
        mPlusClientIsConnecting = flag;
        onPlusClientBlockingUI(flag);
    }

    /**
     * A helper method to flip the mResolveOnFail flag and start the resolution
     * of the ConnectionResult from the failed connect() call.
     */
    private void startResolution() {
        if (mGoogleApiClient == null) return;
        try {
            // Don't start another resolution now until we have a result from the activity we're
            // about to start.
            mAutoResolveOnFail = false;
            // If we can resolve the error, then call start resolution and pass it an integer tag
            // we can use to track.
            // This means that when we get the onActivityResult callback we'll know it's from
            // being started here.
            mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
        } catch (IntentSender.SendIntentException e) {
            // Any problems, just try to connect() again so we get a new ConnectionResult.
            mConnectionResult = null;
            initiatePlusClientConnect();
        }
    }

    /**
     * An earlier connection failed, and we're now receiving the result of the resolution attempt
     * by PlusClient.
     *
     * @see #onConnectionFailed(ConnectionResult)
     */
    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        setSuspended(false);
        if (mGoogleApiClient != null) {
            updatePlusConnectedButtonState();
        }
        if (requestCode == RC_SIGN_IN) {
            if (responseCode == RESULT_OK) {
                // If we have a successful result, we will want to be able to resolve any further
                // errors, so turn on resolution with our flag.
                mAutoResolveOnFail = true;
                // If we have a successful result, let's call connect() again. If there are any more
                // errors to resolve we'll get our onConnectionFailed, but if not,
                // we'll get onConnected.
                initiatePlusClientConnect();
            } else {
                // If we've got an error we can't resolve, we're no longer in the midst of signing
                // in, so we can stop the progress spinner.
                setProgressBarVisible(false);
            }
        } else {
            super.onActivityResult(requestCode, responseCode, intent);
        }
    }

    /**
     * Successfully connected (called by PlusClient)
     */
    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        if (mGoogleApiClient == null) return;

        if (mRevokeAccess) {
            mRevokeAccess = false;
            revokeGPlusAccess();
            return;
        }

        updatePlusConnectedButtonState();
        setProgressBarVisible(false);
        try {
            new GplusAuthTokenFetcher().execute(Plus.AccountApi.getAccountName(mGoogleApiClient));
        } catch (Exception e) {
            e.printStackTrace();
            showToast(getString(R.string.unknownError));
        }
    }

    private class GplusAuthTokenFetcher extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
//            setProgressBarVisible(true);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                return GoogleAuthUtil.getToken(getCurrentActivity(), params[0],
                        "oauth2:" + Scopes.PLUS_LOGIN
                                + " https://www.googleapis.com/auth/userinfo.email");
            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), RC_SIGN_IN);
            } catch (GoogleAuthException | IOException e) {
                showToast(getString(R.string.unknownError));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (isCancelled() || isSuspended() || TextUtils.isEmpty(s)) return;
//            setProgressBarVisible(false);
            onPlusClientSignIn(s);
        }
    }

    /**
     * Connection failed for some reason (called by PlusClient)
     * Try and resolve the result.  Failure here is usually not an indication of a serious error,
     * just that the user's input is needed.
     *
     * @see #onActivityResult(int, int, Intent)
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (mGoogleApiClient == null) return;

        updatePlusConnectedButtonState();

        // Most of the time, the connection will fail with a user resolvable result. We can store
        // that in our mConnectionResult property ready to be used when the user clicks the
        // sign-in button.
        if (result.hasResolution()) {
            mConnectionResult = result;
            if (mAutoResolveOnFail) {
                // This is a local helper function that starts the resolution of the problem,
                // which may be showing the user an account chooser or similar.
                startResolution();
            }
        }
    }

    public GoogleApiClient getPlusClient() {
        return mGoogleApiClient;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (mGoogleApiClient == null) return;

        mGoogleApiClient.connect();
    }
}
