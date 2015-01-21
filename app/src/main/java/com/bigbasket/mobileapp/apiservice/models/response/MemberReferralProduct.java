package com.bigbasket.mobileapp.apiservice.models.response;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by jugal on 29/12/14.
 */
public class MemberReferralProduct {

    @SerializedName(Constants.SKU_ID)
    public String skuId;

    @SerializedName(Constants.REF_IMAGE_URL)
    public String refImageUrl;

    @SerializedName(Constants.INCENTIVE_TYPE)
    public String incentiveType;

    @SerializedName(Constants.MEMBER_CREDIT_AMOUNT)
    public String memberCreditAmount;

    @SerializedName(Constants.VOUCHER_CODE)
    public String voucherCode;

    @SerializedName(Constants.VOUCHER_CODE_DESC)
    public String voucherCodeDesc;

    @SerializedName(Constants.INCENTIVE_DESC)
    public String incentiveDesc;

    @SerializedName(Constants.MIN_ORDER_VAL)
    public int minOrderVal;

    @SerializedName(Constants.TC)
    public ArrayList<String> termAndCondition;

    @SerializedName(Constants.REF_LINK_FB)
    public String refLinkFb;

    @SerializedName(Constants.REF_LINK)
    public String refLink;

    @SerializedName(Constants.MAX_EMAIL_LEN)
    public int maxEmailLen;

    @SerializedName(Constants.MAX_MSG_LEN)
    public int maxMsgLen;

    @SerializedName(Constants.MAX_MSG_CHAR_LEN)
    public int maxMsgCharLen;

    @SerializedName(Constants.REFERRAL_MSG)
    public String referralMsg;

    @SerializedName(Constants.P_DESC)
    public String productDesc;
}
