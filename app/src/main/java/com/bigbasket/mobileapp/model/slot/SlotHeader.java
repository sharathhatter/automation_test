package com.bigbasket.mobileapp.model.slot;


import android.os.Parcel;
import android.os.Parcelable;

public class SlotHeader extends BaseSlot implements Parcelable {
    public static final Parcelable.Creator<SlotHeader> CREATOR = new Parcelable.Creator<SlotHeader>() {
        @Override
        public SlotHeader createFromParcel(Parcel source) {
            return new SlotHeader(source);
        }

        @Override
        public SlotHeader[] newArray(int size) {
            return new SlotHeader[size];
        }
    };

    public SlotHeader(String displayName) {
        super(displayName);
    }

    public SlotHeader(Parcel source) {
        this(source.readString());
    }

    public String getFormattedDisplayName() {
        String displayName = this.displayName.replaceAll("-", "/");
        displayName = displayName.replaceAll(",", " ");
        return displayName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
    }
}
