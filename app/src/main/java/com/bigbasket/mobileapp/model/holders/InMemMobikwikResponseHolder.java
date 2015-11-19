package com.bigbasket.mobileapp.model.holders;

public class InMemMobikwikResponseHolder {
    private String mobikwikTxnId;
    public String mobikwikOrderStatus;

    public InMemMobikwikResponseHolder(String mobikwikTxnId, String mobikwikOrderStatus) {
        this.mobikwikTxnId = mobikwikTxnId;
        this.mobikwikOrderStatus = mobikwikOrderStatus;
    }

    public String getMobikwikTxnId() {
        return mobikwikTxnId;
    }

    public String getMobikwikOrderStatus() {
        return mobikwikOrderStatus;
    }
}
