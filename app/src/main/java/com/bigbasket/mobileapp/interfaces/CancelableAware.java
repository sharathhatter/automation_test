package com.bigbasket.mobileapp.interfaces;


public interface CancelableAware {
    public boolean isSuspended();

    public void setSuspended(boolean state);
}
