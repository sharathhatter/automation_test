package com.bigbasket.mobileapp.interfaces;

public interface OtpDialogAware {
    void validateOtp(String otpCode, boolean isResendOtpRequested);
}
