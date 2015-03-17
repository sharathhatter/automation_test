package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.Serializable;

public class BaseSectionTextItem implements Parcelable, Serializable {

    private SectionTextItem title;
    private SectionTextItem description;

    public BaseSectionTextItem(SectionTextItem title, SectionTextItem description) {
        this.title = title;
        this.description = description;
    }

    @Nullable
    public SectionTextItem getTitle() {
        return title;
    }

    @Nullable
    public SectionTextItem getDescription() {
        return description;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasTitleNull = title == null;
        dest.writeByte(wasTitleNull ? (byte) 1 : (byte) 0);
        if (!wasTitleNull) {
            dest.writeParcelable(title, flags);
        }
        boolean wasDescNull = description == null;
        dest.writeByte(wasDescNull ? (byte) 1 : (byte) 0);
        if (!wasDescNull) {
            dest.writeParcelable(description, flags);
        }
    }

    public BaseSectionTextItem(Parcel source) {
        boolean wasTitleNull = source.readByte() == (byte) 1;
        if (!wasTitleNull) {
            title = source.readParcelable(BaseSectionTextItem.class.getClassLoader());
        }
        boolean wasDescNull = source.readByte() == (byte) 1;
        if (!wasDescNull) {
            description = source.readParcelable(BaseSectionTextItem.class.getClassLoader());
        }
    }

    public static final Parcelable.Creator<BaseSectionTextItem> CREATOR = new Parcelable.Creator<BaseSectionTextItem>() {
        @Override
        public BaseSectionTextItem createFromParcel(Parcel source) {
            return new BaseSectionTextItem(source);
        }

        @Override
        public BaseSectionTextItem[] newArray(int size) {
            return new BaseSectionTextItem[size];
        }
    };
}
