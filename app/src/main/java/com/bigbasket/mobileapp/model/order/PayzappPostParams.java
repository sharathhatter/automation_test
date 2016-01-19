package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class PayzappPostParams implements Parcelable {

    public static final Parcelable.Creator<PayzappPostParams> CREATOR = new Parcelable.Creator<PayzappPostParams>() {
        @Override
        public PayzappPostParams[] newArray(int size) {
            return new PayzappPostParams[size];
        }

        @Override
        public PayzappPostParams createFromParcel(Parcel source) {
            return new PayzappPostParams(source);
        }
    };
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

    public PayzappPostParams(String merchantId, String pkgName,
                             String serverUrl, String appData,
                             String msgHash, String txnDesc,
                             String txnId, String[] paymentChoices,
                             String formattedAmount, String mobile,
                             String currency, String amount,
                             String dob, String name,
                             String email, String merchantAppId, String countryCode) {
        this.merchantId = merchantId;
        this.pkgName = pkgName;
        this.serverUrl = serverUrl;
        this.appData = appData;
        this.msgHash = msgHash;
        this.txnDesc = txnDesc;
        this.txnId = txnId;
        this.paymentChoices = paymentChoices;
        this.formattedAmount = formattedAmount;
        this.mobile = mobile;
        this.currency = currency;
        this.amount = amount;
        this.dob = dob;
        this.name = name;
        this.email = email;
        this.merchantAppId = merchantAppId;
        this.countryCode = countryCode;
    }

    public PayzappPostParams(Parcel source) {
        this.merchantId = source.readString();
        this.pkgName = source.readString();
        this.serverUrl = source.readString();
        this.appData = source.readString();
        this.msgHash = source.readString();
        this.txnDesc = source.readString();
        this.txnId = source.readString();
        this.paymentChoices = source.createStringArray();
        this.formattedAmount = source.readString();
        this.mobile = source.readString();
        this.currency = source.readString();
        this.amount = source.readString();
        this.dob = source.readString();
        this.name = source.readString();
        this.email = source.readString();
        this.merchantAppId = source.readString();
        this.countryCode = source.readString();

    }

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(merchantId);
        dest.writeString(pkgName);
        dest.writeString(serverUrl);
        dest.writeString(appData);
        dest.writeString(msgHash);
        dest.writeString(txnDesc);
        dest.writeString(txnId);
        dest.writeStringArray(paymentChoices);
        dest.writeString(formattedAmount);
        dest.writeString(currency);
        dest.writeString(amount);
        dest.writeString(dob);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(merchantAppId);
        dest.writeString(countryCode);
    }
}
