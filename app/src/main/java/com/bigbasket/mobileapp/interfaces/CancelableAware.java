package com.bigbasket.mobileapp.interfaces;


public interface CancelableAware {
    boolean isSuspended();

    void setSuspended(boolean state);
}
