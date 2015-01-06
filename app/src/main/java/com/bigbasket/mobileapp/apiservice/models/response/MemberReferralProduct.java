package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by jugal on 29/12/14.
 */
public class MemberReferralProduct {

    @SerializedName(Constants.SKU_ID)
    private String skuId;

    @SerializedName(Constants.REF_IMAGE_URL)
    private String refImageUrl;

    @SerializedName(Constants.INCENTIVE_TYPE)
    private String incentiveType;

    @SerializedName(Constants.MEMBER_CREDIT_AMOUNT)
    private String memberCreditAmount;

    @SerializedName(Constants.VOUCHER_CODE)
    private String voucherCode;

    @SerializedName(Constants.VOUCHER_CODE_DESC)
    private String voucherCodeDesc;

    @SerializedName(Constants.INCENTIVE_DESC)
    private String incentiveDesc;

    @SerializedName(Constants.MIN_ORDER_VAL)
    private int minorderVal;

    @SerializedName(Constants.TC)
    private ArrayList<String> termAndCondition;

    @SerializedName(Constants.REF_LINK_FB)
    private String refLinkFb;

    @SerializedName(Constants.REF_LINK)
    private String refLink;

    @SerializedName(Constants.MAX_EMAIL_LEN)
    private String maxEmailLen;

    @SerializedName(Constants.MAX_MSG_LEN)
    private String maxMsgLen;

    @SerializedName(Constants.REFERRAL_MSG)
    private String referralMsg;

    public String getSkuId() {
        return skuId;
    }

    public String getIncentiveType() {
        return incentiveType;
    }

    public String getMemberCreditAmount() {
        return memberCreditAmount;
    }

    public String getVoucherCode() {
        return voucherCode;
    }

    public String getVoucherCodeDesc() {
        return voucherCodeDesc;
    }

    public int getMinorderVal() {
        return minorderVal;
    }

    public String getRefLinkFb() {
        return refLinkFb;
    }

    public String getRefLink() {
        return refLink;
    }

    public String getMaxEmailLen() {
        return maxEmailLen;
    }

    public String getMaxMsgLen() {
        return maxMsgLen;
    }

    public String getReferralMsg() {
        return referralMsg;
    }

    public String getRefImageUrl() {
        return refImageUrl;
    }

    public ArrayList<String> getTermAndCondition() {
        return termAndCondition;
    }

    public String getIncentiveDesc() {
        return incentiveDesc;
    }
}
