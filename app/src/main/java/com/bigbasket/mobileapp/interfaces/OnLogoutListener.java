package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.apiservice.models.ErrorResponse;

public interface OnLogoutListener {
    void onLogoutSuccess();

    void onLogoutFailure(ErrorResponse errorResponse);
}
