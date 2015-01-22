package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class SectionTextItem implements Parcelable {
    private String text;

    @SerializedName(Constants.RENDERING_ID)
    private int renderingId;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasTextNull = text == null;
        dest.writeByte(wasTextNull ? (byte) 1 : (byte) 0);
        if (!wasTextNull) {
            dest.writeString(text);
        }
        dest.writeInt(renderingId);
    }

    public SectionTextItem(Parcel source) {
        boolean wasTextNull = source.readByte() == (byte) 1;
        if (!wasTextNull) {
            text = source.readString();
        }
        renderingId = source.readInt();
    }

    public String getText() {
        return text;
    }

    public int getRenderingId() {
        return renderingId;
    }

    public static final Parcelable.Creator<SectionTextItem> CREATOR = new Parcelable.Creator<SectionTextItem>() {
        @Override
        public SectionTextItem createFromParcel(Parcel source) {
            return new SectionTextItem(source);
        }

        @Override
        public SectionTextItem[] newArray(int size) {
            return new SectionTextItem[size];
        }
    };
}
