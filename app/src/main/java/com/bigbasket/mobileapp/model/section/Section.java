package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Section implements Parcelable {
    public static final String BANNER = "banner";
    public static final String SALUTATION = "salutation";
    public static final String PRODUCT_CAROUSEL = "product_carousel";
    public static final String INFO_WIDGET = "info-widget";
    public static final String AD = "ad";
    public static final String IMAGE = "image";
    public static final String TILE = "tile";

    public static Set<String> getSupportedSectionTypes() {
        Set<String> sectionTypeSets = new HashSet<>();
        sectionTypeSets.add(BANNER);
        sectionTypeSets.add(SALUTATION);
        sectionTypeSets.add(PRODUCT_CAROUSEL);
        sectionTypeSets.add(INFO_WIDGET);
        sectionTypeSets.add(AD);
        sectionTypeSets.add(IMAGE);
        sectionTypeSets.add(TILE);
        return sectionTypeSets;
    }

    @SerializedName(Constants.SECTION_TYPE)
    private String sectionType;

    @SerializedName(Constants.DISPLAY_NAME)
    private String displayName;

    @SerializedName(Constants.ITEMS)
    private ArrayList<SectionItem> sectionItems;

    @SerializedName(Constants.MORE)
    private SectionItem moreSectionItem;

    public String getSectionType() {
        return sectionType;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ArrayList<SectionItem> getSectionItems() {
        return sectionItems;
    }

    public SectionItem getMoreSectionItem() {
        return moreSectionItem;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sectionType);
        boolean _wasDisplayNameNull = displayName == null;
        dest.writeByte(_wasDisplayNameNull ? (byte) 1 : (byte) 0);
        if (!_wasDisplayNameNull) {
            dest.writeString(displayName);
        }
        boolean _wasSectionItemsNull = sectionItems == null;
        dest.writeByte(_wasSectionItemsNull ? (byte) 1 : (byte) 0);
        if (!_wasSectionItemsNull) {
            dest.writeTypedList(sectionItems);
        }
        boolean _wasMoreSectionItemNull = moreSectionItem == null;
        dest.writeByte(_wasMoreSectionItemNull ? (byte) 1 : (byte) 0);
        if (!_wasMoreSectionItemNull) {
            dest.writeParcelable(moreSectionItem, flags);
        }
    }

    public Section(Parcel source) {
        sectionType = source.readString();
        boolean _wasDisplayNameNull = source.readByte() == (byte) 1;
        if (!_wasDisplayNameNull) {
            displayName = source.readString();
        }
        boolean _wasSectionItemsNull = source.readByte() == (byte) 1;
        if (!_wasSectionItemsNull) {
            sectionItems = new ArrayList<>();
            source.readTypedList(sectionItems, SectionItem.CREATOR);
        }
        boolean _wasMoreSectionItemNull = source.readByte() == (byte) 1;
        if (!_wasMoreSectionItemNull) {
            moreSectionItem = source.readParcelable(Section.class.getClassLoader());
        }
    }


    public static final Parcelable.Creator<Section> CREATOR = new Parcelable.Creator<Section>() {
        @Override
        public Section createFromParcel(Parcel source) {
            return new Section(source);
        }

        @Override
        public Section[] newArray(int size) {
            return new Section[size];
        }
    };
}
