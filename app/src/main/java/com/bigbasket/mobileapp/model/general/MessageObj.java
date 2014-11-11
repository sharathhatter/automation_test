package com.bigbasket.mobileapp.model.general;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;

/**
 * Created by jugal on 12/9/14.
 */
public class MessageObj implements Parcelable {

    @SerializedName("message_obj")
    private MessageInfo msgInfo;

    public MessageObj(MessageInfo msgInfo) {
        this.msgInfo = msgInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public MessageObj(Parcel parcel) {
        boolean isMsgInfoNull = parcel.readByte() == (byte) 1;
        if (!isMsgInfoNull) {
            msgInfo = parcel.readParcelable(MessageObj.class.getClassLoader());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean isMsgInfoNull = msgInfo == null;
        dest.writeByte(isMsgInfoNull ? (byte) 1 : (byte) 0);
        if (!isMsgInfoNull) {
            dest.writeParcelable(msgInfo, flags);
        }
    }

    public static final Parcelable.Creator<MessageObj> CREATOR = new Parcelable.Creator<MessageObj>() {
        @Override
        public MessageObj createFromParcel(Parcel source) {
            return new MessageObj(source);
        }

        @Override
        public MessageObj[] newArray(int size) {
            return new MessageObj[size];
        }
    };

    public MessageInfo getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(MessageInfo msgInfo) {
        this.msgInfo = msgInfo;
    }
}
