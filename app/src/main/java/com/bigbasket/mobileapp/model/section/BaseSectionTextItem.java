package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.io.Serializable;

public class BaseSectionTextItem implements Parcelable, Serializable {

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
    private SectionTextItem title;
    private SectionTextItem description;

    public BaseSectionTextItem(SectionTextItem title, SectionTextItem description) {
        this.title = title;
        this.description = description;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BaseSectionTextItem that = (BaseSectionTextItem) o;

        if (title != null ? !title.equals(that.title) : that.title != null) return false;
        return !(description != null ? !description.equals(that.description) : that.description != null);

    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }
}
