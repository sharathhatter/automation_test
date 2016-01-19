package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SectionTextItem implements Parcelable, Serializable {
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
    private String text;
    @SerializedName(Constants.RENDERING_ID)
    private int renderingId;

    public SectionTextItem(String text, int renderingId) {
        this.text = text;
        this.renderingId = renderingId;
    }

    public SectionTextItem(Parcel source) {
        boolean wasTextNull = source.readByte() == (byte) 1;
        if (!wasTextNull) {
            text = source.readString();
        }
        renderingId = source.readInt();
    }

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

    public String getText() {
        return text;
    }

    public int getRenderingId() {
        return renderingId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SectionTextItem that = (SectionTextItem) o;

        if (renderingId != that.renderingId) return false;
        return !(text != null ? !text.equals(that.text) : that.text != null);

    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + renderingId;
        return result;
    }
}
