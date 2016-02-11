package com.bigbasket.mobileapp.task;

import android.Manifest;
import android.os.Handler;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.fragment.account.OTPValidationFragment;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.OtpDialogAware;
import com.bigbasket.mobileapp.util.Constants;

public final class OtpValidationHelper {
    private static OTPValidationFragment otpValidationFragment;

    private OtpValidationHelper() {
    }

    public static <T extends AppOperationAware & OtpDialogAware> void requestOtpUI(T ctx) {
        if (ctx.getCurrentActivity().handlePermission(Manifest.permission.RECEIVE_SMS,
                ctx.getCurrentActivity().getString(R.string.sms_permission_rationale),
                Constants.PERMISSION_REQUEST_CODE_RECEIVE_SMS)) {
            validateMobileNumber(ctx, false, null);
        }
    }

    public static <T extends AppOperationAware & OtpDialogAware> void reportError(T ctx, String errMsg) {
        validateMobileNumber(ctx, true, errMsg);
    }

    public static <T extends AppOperationAware & OtpDialogAware>
    boolean onRequestPermissionsResult(T ctx, int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION_REQUEST_CODE_RECEIVE_SMS:
                if (grantResults.length > 0 && permissions.length > 0
                        && permissions[0].equals(Manifest.permission.RECEIVE_SMS)) {
                    validateMobileNumber(ctx, false, null);
                }
                return true;
        }
        return false;
    }

    private static <T extends AppOperationAware & OtpDialogAware>
    void validateMobileNumber(final T ctx, boolean hasError, String errorMsg) {
        if (otpValidationFragment == null) {
            otpValidationFragment = OTPValidationFragment.newInstance();
        }
        if (otpValidationFragment.isVisible()) {
            if (hasError) {
                otpValidationFragment.showErrorText(errorMsg);
            }
        } else {
            // When permission dialog in shown, activity's onSaveInstance is called, hence,
            // if the fragment is added directly it can cause IllegalStateException.
            // Hence adding the Fragment after a delay
            if (ctx.isSuspended()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ctx.getCurrentActivity().getSupportFragmentManager().beginTransaction()
                                .add(R.id.content_frame,
                                        otpValidationFragment, otpValidationFragment.getFragmentTxnTag())
                                .addToBackStack(null)
                                .commit();
                    }
                }, 200);
            } else {
                ctx.getCurrentActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.content_frame, otpValidationFragment, otpValidationFragment.getFragmentTxnTag())
                        .addToBackStack(null)
                        .commit();
            }
        }
    }

    public static void dismiss() {
        if (otpValidationFragment != null && otpValidationFragment.isVisible()) {
            otpValidationFragment.dismiss();
        }
        onDestroy();
    }

    public static void onDestroy() {
        otpValidationFragment = null;
    }
}
