package com.bigbasket.mobileapp.activity.account.uiv3;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.SignInUtils;
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

import static com.bigbasket.mobileapp.util.SignInUtils.ConnectionMode;
import static com.bigbasket.mobileapp.util.SignInUtils.NONE;
import static com.bigbasket.mobileapp.util.SignInUtils.SIGN_IN;
import static com.bigbasket.mobileapp.util.SignInUtils.SIGN_OUT;
import static com.bigbasket.mobileapp.util.SignInUtils.REVOKE;
import static com.bigbasket.mobileapp.util.SignInUtils.DIALOG_ERROR;
import static com.bigbasket.mobileapp.util.SignInUtils.KEY_IS_RESOLVING;
import static com.bigbasket.mobileapp.util.SignInUtils.KEY_CONNECTION_MODE;
import static com.bigbasket.mobileapp.util.SignInUtils.KEY_SHOW_INITIAL_INFO;

/**
 * A base class to wrap communication with the Google Play Services PlusClient.
 */
public abstract class PlusBaseActivity extends BaseActivity {
    
    private boolean mShowInitialPermissionInfo = true;

    private @ConnectionMode int mConnectionMode = NONE;

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;

    // [START resolution_variables]
    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;
    // [END resolution_variables]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Restore from saved instance state
        // [START restore_saved_instance_state]
        if (savedInstanceState != null) {
            mIsResolving = savedInstanceState.getBoolean(KEY_IS_RESOLVING, false);
            mShowInitialPermissionInfo = savedInstanceState.getBoolean(KEY_SHOW_INITIAL_INFO, true);
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
        if(mIsResolving) {
            outState.putBoolean(KEY_IS_RESOLVING, mIsResolving);
        }
        if(mConnectionMode != NONE) {
            outState.putInt(KEY_CONNECTION_MODE, mConnectionMode);
        }
        if(!mShowInitialPermissionInfo) {
            outState.putBoolean(KEY_SHOW_INITIAL_INFO, mShowInitialPermissionInfo);
        }
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
                .addScope(new Scope(Scopes.EMAIL))
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
                        Log.w(SignInUtils.PLUS_BASE_TAG, "onConnectionSuspended:" + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        // Could not connect to Google Play Services.
                        // The user needs to select an account, grant permissions or resolve
                        // an error in order to sign in. Refer to the javadoc for
                        // ConnectionResult to see possible error codes.
                        Log.d(SignInUtils.PLUS_BASE_TAG, "onConnectionFailed:" + connectionResult);
                        if (mIsResolving) {
                            // Already attempting to resolve an error.
                            Log.w(SignInUtils.PLUS_BASE_TAG, "Already resolving");
                            return;
                        } else if (mConnectionMode != SIGN_IN
                                && connectionResult.getErrorCode() == ConnectionResult.SIGN_IN_REQUIRED){
                            if (mConnectionMode == SIGN_OUT) {
                                onPlusClientSignOut();
                            } else {
                                onPlusClientRevokeAccess();
                            }
                            mGoogleApiClient = null;
                            resetConnectionMode();
                            mIsResolving = false;
                        } else if (connectionResult.hasResolution()) {
                            try {
                                mIsResolving = true;
                                connectionResult.startResolutionForResult(PlusBaseActivity.this,
                                        NavigationCodes.RC_RESOLVE_CONNECT_ERROR);
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
        Log.d(SignInUtils.PLUS_BASE_TAG, "signInViaGPlus");
        mConnectionMode = SIGN_IN;
        mShowInitialPermissionInfo = true;
        if (mGoogleApiClient != null) {
            mGoogleApiClient.reconnect();
        } else {
            initializeGoogleApiClient();
            mGoogleApiClient.connect();
        }
    }

    /**
     * Sign out the user (so they can switch to another account).
     */
    protected void signOutFromGplus() {
        Log.d(SignInUtils.PLUS_BASE_TAG, "signOutFromGplus");
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
        Log.d(SignInUtils.PLUS_BASE_TAG, "signIn :" + mShowInitialPermissionInfo);
        if (mShowInitialPermissionInfo && !hasPermissionGranted(Manifest.permission.GET_ACCOUNTS)){
            showAlertDialog("", getString(R.string.contacts_permission_reason),
                    getString(R.string.ok), getString(R.string.cancel),
                    Constants.CONTACT_PERMISSION_INFO_DIALOG, null, true);
            mShowInitialPermissionInfo = false;
        } else if(handlePermission(Manifest.permission.GET_ACCOUNTS,
                null, Constants.PERMISSION_REQUEST_CODE_GET_ACCOUNTS)) {
            signinUser();
            resetConnectionMode();
        }
    }

    private void signinUser() {
        try {
            if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                fetchAuthToken(accountName);
            } else {
                if(mGoogleApiClient == null){
                    initializeGoogleApiClient();
                    mGoogleApiClient.connect();
                } else if(!mGoogleApiClient.isConnecting()){
                    mGoogleApiClient.reconnect();
                }
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            showToast(getString(R.string.unknownError));
            onAuthCancelled();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_GET_ACCOUNTS:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.GET_ACCOUNTS)
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    signinUser();
                } else {
                    onStateNotSaved();
                    onAuthCancelled();
                    View contentView = findViewById(android.R.id.content);
                    if(contentView == null) {
                        contentView = getWindow().getDecorView();
                    }
                    Snackbar snackbar = Snackbar.make(contentView,
                            R.string.contacts_permission_rationale, Snackbar.LENGTH_LONG);
                    snackbar.setAction(R.string.action_settings, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startPermissionsSettingsActivity(getCurrentActivity());
                        }
                    });
                    snackbar.show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void signOut() {
        clearAndRevokeAccount();
        mGoogleApiClient.disconnect();
        mGoogleApiClient = null;
        onPlusClientSignOut();
        resetConnectionMode();
    }

    private void fetchAuthToken(String accountName) {
        new GplusAuthTokenFetcher(this).execute(accountName);
    }

    @Override
    protected void onNegativeButtonClicked(int requestCode, Bundle data) {
        if (requestCode == Constants.CONTACT_PERMISSION_INFO_DIALOG){
            onAuthCancelled();
        } else if (requestCode == NavigationCodes.RC_PERMISSIONS_SETTINGS &&
                data != null &&
                data.getInt(Constants.KEY_PERMISSION_RC) ==
                        Constants.PERMISSION_REQUEST_CODE_GET_ACCOUNTS ) {
            onAuthCancelled();
        } else {
            super.onNegativeButtonClicked(requestCode, data);
        }
    }

    @Override
    public void onDialogCancelled(int requestCode, Bundle data) {
        if (requestCode == Constants.CONTACT_PERMISSION_INFO_DIALOG){
            onAuthCancelled();
        } else if(requestCode == NavigationCodes.RC_PERMISSIONS_SETTINGS &&
                data != null &&
                data.getInt(Constants.KEY_PERMISSION_RC) ==
                        Constants.PERMISSION_REQUEST_CODE_GET_ACCOUNTS ) {
            onAuthCancelled();
        } else {
            super.onDialogCancelled(requestCode, data);
        }
    }

    @Override
    protected void onPositiveButtonClicked(int requestCode, Bundle valuePassed) {
        if (requestCode == Constants.CONTACT_PERMISSION_INFO_DIALOG){
            signIn();
        } else {
            super.onPositiveButtonClicked(requestCode, valuePassed);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        onStateNotSaved();
        if (requestCode == NavigationCodes.RC_RESOLVE_CONNECT_ERROR) {
            mIsResolving = false;
            if(mGoogleApiClient == null) {
                initializeGoogleApiClient();
            }
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()) {
                    Log.d(SignInUtils.PLUS_BASE_TAG, "Connecting again, mode:" + mConnectionMode);
                    mGoogleApiClient.connect();
                }
            } else {
                //User cancelled
                try {
                    mConnectionMode = REVOKE;
                    //Clear the selected account
                    revokeGPlusAccess();
                } catch (IllegalStateException ex) {
                    //Will throw exception as client not connected,
                    //but there is no other API to clear the selected account, ignore the exception
                    Crashlytics.logException(ex);
                }
                onGoogleClientConnectCancelled();
            }
        } else if (requestCode == NavigationCodes.RC_RESOLVE_AUTH_ERROR) {
            if (mGoogleApiClient == null) {
                initializeGoogleApiClient();
            }
            if (resultCode == RESULT_OK) {
                String accountName = Plus.AccountApi.getAccountName(mGoogleApiClient);
                if (!TextUtils.isEmpty(accountName)) {
                    fetchAuthToken(accountName);
                } else {
                    if (mGoogleApiClient.isConnected()) {
                        onGoogleClientConnected(null);
                    } else if (!mGoogleApiClient.isConnecting()) {
                        mConnectionMode = SIGN_IN;
                        initializeGoogleApiClient();
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
            Crashlytics.logException(ex);
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
            Crashlytics.logException(ex);
        }
        resetConnectionMode();
    }

    private void onAuthFailed(Exception authException) {
        if (authException != null) {
            if (authException instanceof UserRecoverableAuthException) {
                startActivityForResult(((UserRecoverableAuthException) authException).getIntent(),
                        NavigationCodes.RC_RESOLVE_AUTH_ERROR);
            } else {
                //TODO: Show error and retry if is IOException
                onPlusClientSignInFailed();
                revokeGPlusAccess();
                hideProgressView();
            }
        }
    }

    private void onAuthCancelled() {
        revokeGPlusAccess();
        onPlusClientSignInFailed();
    }


    private void onAuthComplete(String authToken) {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
            mGoogleApiClient = null;
        }
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
        Log.d(SignInUtils.PLUS_BASE_TAG, "Reset connection mode");
        mConnectionMode = NONE;
        mIsResolving = false;
    }


    protected void revokeGPlusAccess() {
        Log.d(SignInUtils.PLUS_BASE_TAG, "revokeGPlusAccess");
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
    public static class GooglePlayServicesErrorDialogFragment extends AppCompatDialogFragment {
        public GooglePlayServicesErrorDialogFragment() {
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode, this.getActivity(),
                    NavigationCodes.RC_RESOLVE_CONNECT_ERROR);

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
                        "oauth2:" + Scopes.PROFILE + " " + Scopes.EMAIL));
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
