package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class Renderer implements Parcelable {
    @SerializedName(Constants.TEXT_COLOR)
    private String textColor;

    @SerializedName(Constants.BACKGROUND_COLOR)
    private String backgroundColor;

    @SerializedName(Constants.PADDING)
    private int padding;

    @SerializedName(Constants.MARGIN)
    private int margin;

    @SerializedName(Constants.ALIGNMENT)
    private int alignment;

    public String getTextColor() {
        return !TextUtils.isEmpty(textColor) ? textColor : "77000000";
    }

    public String getBackgroundColor() {
        return !TextUtils.isEmpty(backgroundColor) ? backgroundColor : "FFFFFFFF";
    }

    public int getPadding() {
        return padding;
    }

    public int getMargin() {
        return margin;
    }

    public int getAlignment() {
        return alignment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasTextColorNull = textColor == null;
        dest.writeByte(wasTextColorNull ? (byte) 1 : (byte) 0);
        if (!wasTextColorNull) {
            dest.writeString(textColor);
        }
        boolean wasBkgColorNull = backgroundColor == null;
        dest.writeByte(wasBkgColorNull ? (byte) 1 : (byte) 0);
        if (!wasBkgColorNull) {
            dest.writeString(backgroundColor);
        }
        dest.writeInt(padding);
        dest.writeInt(margin);
        dest.writeInt(alignment);
    }

    public Renderer(Parcel source) {
        boolean wasTextColorNull = source.readByte() == (byte) 1;
        if (!wasTextColorNull) {
            textColor = source.readString();
        }
        boolean wasBkgColorNull = source.readByte() == (byte) 1;
        if (!wasBkgColorNull) {
            backgroundColor = source.readString();
        }
        padding = source.readInt();
        margin = source.readInt();
        alignment = source.readInt();
    }

    public static final Parcelable.Creator<Renderer> CREATOR = new Parcelable.Creator<Renderer>() {
        @Override
        public Renderer createFromParcel(Parcel source) {
            return new Renderer(source);
        }

        @Override
        public Renderer[] newArray(int size) {
            return new Renderer[size];
        }
    };
}
