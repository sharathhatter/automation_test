package com.bigbasket.mobileapp.apiservice.models.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by manu on 14/9/15.
 */
public class PaytmPostParams {

    @SerializedName("CHANNEL_ID")
    @Expose
    private String CHANNELID;
    @SerializedName("CUST_ID")
    @Expose
    private String CUSTID;
    @Expose
    private String EMAIL;
    @SerializedName("INDUSTRY_TYPE_ID")
    @Expose
    private String INDUSTRYTYPEID;
    @Expose
    private String MID;
    @SerializedName("MOBILE_NO")
    @Expose
    private String MOBILENO;
    @SerializedName("ORDER_ID")
    @Expose
    private String ORDERID;
    @Expose
    private String THEME;
    @Expose
    private String WEBSITE;
    @SerializedName("TXN_AMOUNT")
    @Expose
    private String TXNAMOUNT;

    /**
     *
     * @return
     * The CHANNELID
     */
    public String getCHANNELID() {
        return CHANNELID;
    }

    /**
     *
     * @param CHANNELID
     * The CHANNEL_ID
     */
    public void setCHANNELID(String CHANNELID) {
        this.CHANNELID = CHANNELID;
    }

    /**
     *
     * @return
     * The CUSTID
     */
    public String getCUSTID() {
        return CUSTID;
    }

    /**
     *
     * @param CUSTID
     * The CUST_ID
     */
    public void setCUSTID(String CUSTID) {
        this.CUSTID = CUSTID;
    }

    /**
     *
     * @return
     * The EMAIL
     */
    public String getEMAIL() {
        return EMAIL;
    }

    /**
     *
     * @param EMAIL
     * The EMAIL
     */
    public void setEMAIL(String EMAIL) {
        this.EMAIL = EMAIL;
    }

    /**
     *
     * @return
     * The INDUSTRYTYPEID
     */
    public String getINDUSTRYTYPEID() {
        return INDUSTRYTYPEID;
    }

    /**
     *
     * @param INDUSTRYTYPEID
     * The INDUSTRY_TYPE_ID
     */
    public void setINDUSTRYTYPEID(String INDUSTRYTYPEID) {
        this.INDUSTRYTYPEID = INDUSTRYTYPEID;
    }

    /**
     *
     * @return
     * The MID
     */
    public String getMID() {
        return MID;
    }

    /**
     *
     * @param MID
     * The MID
     */
    public void setMID(String MID) {
        this.MID = MID;
    }

    /**
     *
     * @return
     * The MOBILENO
     */
    public String getMOBILENO() {
        return MOBILENO;
    }

    /**
     *
     * @param MOBILENO
     * The MOBILE_NO
     */
    public void setMOBILENO(String MOBILENO) {
        this.MOBILENO = MOBILENO;
    }

    /**
     *
     * @return
     * The ORDERID
     */
    public String getORDERID() {
        return ORDERID;
    }

    /**
     *
     * @param ORDERID
     * The ORDER_ID
     */
    public void setORDERID(String ORDERID) {
        this.ORDERID = ORDERID;
    }

    /**
     *
     * @return
     * The THEME
     */
    public String getTHEME() {
        return THEME;
    }

    /**
     *
     * @param THEME
     * The THEME
     */
    public void setTHEME(String THEME) {
        this.THEME = THEME;
    }

    /**
     *
     * @return
     * The WEBSITE
     */
    public String getWEBSITE() {
        return WEBSITE;
    }

    /**
     *
     * @param WEBSITE
     * The WEBSITE
     */
    public void setWEBSITE(String WEBSITE) {
        this.WEBSITE = WEBSITE;
    }

    /**
     *
     * @return
     * The TXNAMOUNT
     */
    public String getTXNAMOUNT() {
        return TXNAMOUNT;
    }

    /**
     *
     * @param TXNAMOUNT
     * The TXN_AMOUNT
     */
    public void setTXNAMOUNT(String TXNAMOUNT) {
        this.TXNAMOUNT = TXNAMOUNT;
    }

}