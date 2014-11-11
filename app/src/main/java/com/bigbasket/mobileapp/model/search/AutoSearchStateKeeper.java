package com.bigbasket.mobileapp.model.search;

/**
 * Created with IntelliJ IDEA.
 * User: jugal
 * Date: 4/2/14
 * Time: 6:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class AutoSearchStateKeeper {


    public static final byte NOT_QUERIED = 0;
    public static final byte IN_PROGRESS = 1;
    public static final byte QUERIED = 2;

    private byte status;
    private String querytxt;

    public AutoSearchStateKeeper(String querytxt, byte status) {
        this.querytxt = querytxt;
        this.status = status;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public String getQuerytxt() {
        return querytxt;
    }

    public void setQuerytxt(String querytxt) {
        this.querytxt = querytxt;
    }
}
