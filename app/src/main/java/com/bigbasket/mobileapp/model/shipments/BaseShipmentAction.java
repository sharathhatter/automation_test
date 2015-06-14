package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class BaseShipmentAction implements Parcelable {

    @SerializedName(Constants.ACTION_MSG)
    private String actionMsg;
    @SerializedName(Constants.VIEW_STATE)
    private String viewState;
    @SerializedName(Constants.ACTION_STATE)
    private String actionState;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasActionMsgNull = actionMsg == null;
        dest.writeByte(wasActionMsgNull ? (byte) 1: (byte) 0);
        if (!wasActionMsgNull) {
            dest.writeString(actionMsg);
        }

        boolean wasViewStateNull = viewState == null;
        dest.writeByte(wasViewStateNull ? (byte) 1: (byte) 0);
        if (!wasViewStateNull) {
            dest.writeString(viewState);
        }

        boolean wasActionStateNull = actionState == null;
        dest.writeByte(wasActionStateNull ? (byte) 1: (byte) 0);
        if (!wasActionStateNull) {
            dest.writeString(actionState);
        }
    }

    public BaseShipmentAction(String actionMsg, String viewState, String actionState) {
        this.actionMsg = actionMsg;
        this.viewState = viewState;
        this.actionState = actionState;
    }

    public BaseShipmentAction(Parcel source) {
        boolean wasActionMsgNull = source.readByte() == (byte) 1;
        if (!wasActionMsgNull) {
            actionMsg = source.readString();
        }
        boolean wasViewStateNull = source.readByte() == (byte) 1;
        if (!wasViewStateNull) {
            viewState = source.readString();
        }

        boolean wasActionStateNull = source.readByte() == (byte) 1;
        if (!wasActionStateNull) {
            actionState = source.readString();
        }
    }

    public static final Parcelable.Creator<BaseShipmentAction> CREATOR = new Parcelable.Creator<BaseShipmentAction>() {
        @Override
        public BaseShipmentAction createFromParcel(Parcel source) {
            return new BaseShipmentAction(source);
        }

        @Override
        public BaseShipmentAction[] newArray(int size) {
            return new BaseShipmentAction[size];
        }
    };

    public String getActionMsg() {
        return actionMsg;
    }

    public String getViewState() {
        return viewState;
    }

    public String getActionState() {
        return actionState;
    }
}
