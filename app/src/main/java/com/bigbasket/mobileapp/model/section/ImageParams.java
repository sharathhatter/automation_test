package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class ImageParams implements Parcelable, Serializable {
    private int width;
    private int height;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public ImageParams(Parcel source) {
        width = source.readInt();
        height = source.readInt();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(width);
        dest.writeInt(height);
    }

    public static final Parcelable.Creator<ImageParams> CREATOR = new Parcelable.Creator<ImageParams>() {
        @Override
        public ImageParams createFromParcel(Parcel source) {
            return new ImageParams(source);
        }

        @Override
        public ImageParams[] newArray(int size) {
            return new ImageParams[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageParams that = (ImageParams) o;

        if (width != that.width) return false;
        return height == that.height;

    }

    @Override
    public int hashCode() {
        int result = width;
        result = 31 * result + height;
        return result;
    }
}
