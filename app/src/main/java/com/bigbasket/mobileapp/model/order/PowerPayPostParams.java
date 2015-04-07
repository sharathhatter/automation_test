package com.bigbasket.mobileapp.model.order;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PowerPayPostParams {
    @SerializedName(Constants.MERCHANT_ID)
    private String merchantId;

    @SerializedName(Constants.COUNTRY_CODE)
    private String countryCode;

    @SerializedName(Constants.MERCHANT_APP_ID)
    private String merchantAppId;

    private String email;
    private String name;
    private String dob;
    private String amount;
    private String currency;
    private String mobile;


    @SerializedName(Constants.FORMATTED_AMOUNT)
    private String formattedAmount;

    @SerializedName(Constants.PAYMENT_CHOICES)
    private String[] paymentChoices;

    @SerializedName(Constants.TXN_ID)
    private String txnId;

    @SerializedName(Constants.TXN_DESC)
    private String txnDesc;

    @SerializedName(Constants.MSG_HASH)
    private String msgHash;

    @SerializedName(Constants.APP_DATA)
    private String appData;

    @SerializedName(Constants.PG_SERVER_URL)
    private String serverUrl;

    @SerializedName(Constants.PG_PKG_NAME)
    private String pkgName;

    public String getServerUrl() {
        return serverUrl;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public String getMobile() {
        return mobile;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getMerchantAppId() {
        return merchantAppId;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getDob() {
        return dob;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String[] getPaymentChoices() {
        return paymentChoices;
    }

    public String getTxnId() {
        return txnId;
    }

    public String getTxnDesc() {
        return txnDesc;
    }

    public String getMsgHash() {
        return msgHash;
    }

    public String getAppData() {
        return appData;
    }

    public String getFormattedAmount() {
        return formattedAmount;
    }
}
