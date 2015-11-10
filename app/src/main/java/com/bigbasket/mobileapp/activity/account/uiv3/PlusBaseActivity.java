package com.bigbasket.mobileapp.activity.account.uiv3;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.util.Log;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.Plus;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A base class to wrap communication with the Google Play Services PlusClient.
 */
public abstract class PlusBaseActivity extends BaseActivity {

    private static final String TAG = "PlusBaseActivity";

    /* RequestCode for resolutions involving sign-in */
    private static final int RC_RESOLVE_CONNECT_ERROR = 49404;
    private static final int RC_RESOLVE_AUTH_ERROR = 49405;

    // An integer error argument for play services error dialog
    private static final String DIALOG_ERROR = "dialog_error";


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, SIGN_IN, SIGN_OUT, REVOKE})
    private @interface ConnectionMode {
    }

    private static final int NONE = 0;
    private static final int SIGN_IN = 1;
    private static final int REVOKE = 2;
    private static final int SIGN_OUT = 3;

    private
    @ConnectionMode
    int mConnectionMode = NONE;

    /* Keys for persisting instance variables in savedInstanceState */
    private static final String KEY_IS_RESOLVING = "is_resolving";
    private static final String KEY_CONNECTION_MODE = "connection_mode";

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    // [START resolution_variables]
    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;
    // [END resolution_variables]

    /**
     * Email scope for Google APIs,
     * replace this with Scopes.EMAIL after updating play services lib version to 8.x.x
     */
    private static final String SCOPE_EMAIL = "https://www.googleapis.com/auth/userinfo.email";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore from saved instance state
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING);
            @ConnectionMode int mode = savedInstanceState.getInt(KEY_CONNECTION_MODE, NONE);
            mConnectionMode = mode;
            if (mConnectionMode != NONE) {
                initializeGoogleApiClient();
            }
        }
        // [END restore_saved_instance_state]
    }

    // [START on_start_on_stop]
    @Override
    protected void onStart() {
        super.onStart();
        if (mConnectionMode != NONE && mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
    }
    // [END on_start_on_stop]

    // [START on_save_instance_state]
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        outState.putInt(KEY_CONNECTION_MODE, mConnectionMode);
    }
    // [END on_save_instance_state]

    /**
     * Called when the PlusClient revokes access to this app.
     */
    protected void onPlusClientRevokeAccess() {
        hideProgressView();
    }

    /**
     * Failed to revoke the client
     */
    protected void onPlusClientRevokeFailed() {
        hideProgressView();
    }

    /**
     * Called when the PlusClient is successfully connected.
     */
    protected abstract void onPlusClientSignIn(String authToken);

    /**
     * Called when failed to obtain auth token
     */
    protected void onPlusClientSignInFailed() {
        hideProgressView();
    }

    /**
     * Called when the PlusClient is disconnected.
     */
    protected abstract void onPlusClientSignOut();

    /**
     * Called when failed to signout
     */
    protected void onPlusClientSignOutFailed() {
        hideProgressView();
    }

    protected void onPlusClientConnected() {

    }

    protected void onPlusClientConnectFailed() {
        hideProgressView();

    }

    private void initializeGoogleApiClient() {
        // [START create_google_api_client]
        // Build GoogleApiClient with access to basic profile
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API)
                .addScope(new Scope(Scopes.PROFILE))
                .addScope(new Scope(SCOPE_EMAIL))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        if (!isFinishing() && !isSuspended()) {
                            onGoogleClientConnected(bundle);
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        // The connection to Google Play services was lost.
                        // The GoogleApiClient will automatically attempt to re-connect.
                        // Any UI elements that depend on connection to Google APIs should
                        // be hidden or disabled until onConnected is called again.
                        Log.w(TAG, "onConnectionSuspended:" + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        // Could not connect to Google Play Services.
                        // The user needs to select an account, grant permissions or resolve
                        // an error in order to sign in. Refer to the javadoc for
                        // ConnectionResult to see possible error codes.
                        Log.d(TAG, "onConnectionFailed:" + connectionResult);
                        if (mIsResolving) {
                            // Already attempting to resolve an error.
                            return;
                        } else if (connectionResult.hasResolution()) {
                            try {
                                mIsResolving = true;
                                connectionResult.startResolutionForResult(PlusBaseActivity.this,
                                        RC_RESOLVE_CONNECT_ERROR);
                            } catch (IntentSender.SendIntentException e) {
                                // There was an error with the resolution intent. Try again.
                                mGoogleApiClient.connect();
                            }
                        } else {
                            showGoogleClientConnectionErrorDialog(connectionResult.getErrorCode());
                            mIsResolving = false;
                        }
                    }
                })
                .build();
        // [END create_google_api_client]
    }

    /**
     * Try to sign in the user. Automatically resolve any errors and display erros
     */
    protected void signInViaGPlus() {
        // User clicked the sign-in button, so begin the sign-in process and automatically
        // attempt to resolve any errors that occur.
        Log.d(TAG, "signInViaGPlus");
        mConnectionMode = SIGN_IN;
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        //Always start with a fresh client for signin
        initializeGoogleApiClient();
        mGoogleApiClient.connect();
    }

    /**
     * Sign out the user (so they can switch to another account).
     */
    protected void signOutFromGplus() {
        Log.d(TAG, "signOutFromGplus");
        mConnectionMode = SIGN_OUT;
        if (mGoogleApiClient == null) {
            initializeGoogleApiClient();
        }

        // We only want to sign out if we're connected.
        if (mGoogleApiClient.isConnected()) {
            signOut();
        } else {
            mGoogleApiClient.connect();
        }
    }

    private void signIn() {
        try {
            //TODO: Requires GET_ACCOUNTS permission, Resolve this permission on "M"
            String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
            fetchAuthToken(accountName);
        } catch (Exception e) {
            Crashlytics.logException(e);
            showToast(getString(R.string.unknownError));
        }
    }

    private void signOut() {
        clearAndRevokeAccount();
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        onPlusClientSignOut();
    }

    private void fetchAuthToken(String accountName) {
        new GplusAuthTokenFetcher(this).execute(accountName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);

        if (requestCode == RC_RESOLVE_CONNECT_ERROR) {
            mIsResolving = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            } else {
                //User cancelled
                try {
                    //Clear the selected account
                    mGoogleApiClient.clearDefaultAccountAndReconnect();
                } catch (IllegalStateException ex) {
                    //Will throw exception as client not connected,
                    //but there is no other API to clear the selected account, ignore the exception
                }
                onGoogleClientConnectCancelled();
            }
        } else if (requestCode == RC_RESOLVE_AUTH_ERROR) {
            if (resultCode == RESULT_OK) {
                String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                if (!TextUtils.isEmpty(accountName)) {
                    fetchAuthToken(accountName);
                } else {
                    if (mGoogleApiClient.isConnected()) {
                        onGoogleClientConnected(null);
                    } else if (!mGoogleApiClient.isConnecting()) {
                        mConnectionMode = SIGN_IN;
                        mGoogleApiClient.connect();
                    }
                }
            } else {
                //User cancelled
                onAuthCancelled();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void onGoogleClientConnected(Bundle bundle) {
        if (mGoogleApiClient == null || isSuspended()) return;
        onPlusClientConnected();
        switch (mConnectionMode) {
            case SIGN_IN:
                signIn();
                break;
            case SIGN_OUT:
                signOut();
                break;
            case REVOKE:
                clearAndRevokeAccount();
                break;
        }
        resetConnectionMode();

    }

    private void onGoogleClientConnectFailed() {
        onPlusClientConnectFailed();
        invokeFailedCallBack();
        resetConnectionMode();
    }

    private void onGoogleClientConnectCancelled() {
        onPlusClientConnectFailed();
        invokeFailedCallBack();
        resetConnectionMode();
    }

    private void invokeFailedCallBack() {
        switch (mConnectionMode) {
            case SIGN_IN:
                //TODO: Pass the error code
                onPlusClientSignInFailed();
                break;
            case SIGN_OUT:
                onPlusClientSignOutFailed();
                break;
            case REVOKE:
                onPlusClientRevokeFailed();
                break;
        }
    }

    public GoogleApiClient getPlusClient() {
        return mGoogleApiClient;
    }

    private void clearAndRevokeAccount() {
        try {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
        } catch (IllegalStateException ex) {
            //Ignore, google client is not connected
        }
        try {
            PendingResult<Status> pr = Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
            pr.setResultCallback(new ResultCallback<Status>() {
                @Override
                public void onResult(Status status) {
                    onPlusClientRevokeAccess();
                }
            });
            //TODO: wait for pr to complete
        } catch (IllegalStateException ex) {
            //Ignore for now
        }
    }

    private void onAuthFailed(Exception authException) {
        if (authException != null) {
            if (authException instanceof UserRecoverableAuthException) {
                startActivityForResult(((UserRecoverableAuthException) authException).getIntent(),
                        RC_RESOLVE_AUTH_ERROR);
            } else {
                //TODO: Show error and retry if is IOException
                onPlusClientSignInFailed();
                clearAndRevokeAccount();
                hideProgressView();
            }
        }
    }

    private void onAuthCancelled() {
        clearAndRevokeAccount();
        onPlusClientSignInFailed();
    }


    private void onAuthComplete(String authToken) {
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        onPlusClientSignIn(authToken);
    }

    /* Creates a dialog for an error message */
    private void showGoogleClientConnectionErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        GooglePlayServicesErrorDialogFragment dialogFragment = new GooglePlayServicesErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from GooglePlayServicesErrorDialogFragment when the dialog is dismissed. */
    private void onDialogDismissed() {
        mIsResolving = false;
        onGoogleClientConnectFailed();
    }

    private void resetConnectionMode() {
        mConnectionMode = NONE;
    }


    protected void revokeGPlusAccess() {
        Log.d(TAG, "revokeGPlusAccess");
        mConnectionMode = REVOKE;
        if (mGoogleApiClient == null) {
            initializeGoogleApiClient();
        }

        // We only want to revoke if we're connected.
        if (mGoogleApiClient.isConnected()) {
            clearAndRevokeAccount();
            mGoogleApiClient = null;
        } else {
            mGoogleApiClient.connect();
        }
    }

    /* A fragment to display an error dialog */
    public static class GooglePlayServicesErrorDialogFragment extends DialogFragment {
        public GooglePlayServicesErrorDialogFragment() {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(),
                    RC_RESOLVE_CONNECT_ERROR);

        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (getActivity() instanceof PlusBaseActivity) {
                ((PlusBaseActivity) getActivity()).onDialogDismissed();
            }
        }
    }

    private class GplusAuthTokenFetcher extends AsyncTask<String, Void, AuthTokenResult> {

        private final Activity activity;

        public GplusAuthTokenFetcher(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected AuthTokenResult doInBackground(String... params) {
            AuthTokenResult result = new AuthTokenResult();
            try {
                result.setAuthToken(GoogleAuthUtil.getToken(activity, params[0],
                        "oauth2:" + Scopes.PROFILE + " " + SCOPE_EMAIL));
            } catch (Exception e) {
                result.setAuthException(e);
            }
            return result;
        }

        @Override
        protected void onPostExecute(AuthTokenResult result) {
            if (isCancelled()) {
                onAuthCancelled();
                return;
            }
            if (TextUtils.isEmpty(result.getAuthToken())) {
                onAuthFailed(result.getAuthException());
            } else {
                onAuthComplete(result.getAuthToken());
            }
        }
    }

    private static class AuthTokenResult {
        String authToken;
        Exception authException;

        public String getAuthToken() {
            return authToken;
        }

        public void setAuthToken(String authToken) {
            this.authToken = authToken;
        }

        public Exception getAuthException() {
            return authException;
        }

        public void setAuthException(Exception authException) {
            this.authException = authException;
        }
    }

}
