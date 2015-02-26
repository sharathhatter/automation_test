package com.bigbasket.mobileapp.model.section;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Section extends BaseSectionTextItem implements Parcelable, Serializable {
    public static final String BANNER = "banner";
    public static final String SALUTATION = "salutation";
    public static final String PRODUCT_CAROUSEL = "product_carousel";
    public static final String NON_PRODUCT_CAROUSEL = "non_product_carousel";
    public static final String INFO_WIDGET = "info_widget";
    public static final String AD_IMAGE = "ad_image";
    public static final String TILE = "tile";
    public static final String MENU = "menu";
    public static final String MSG = Constants.MSG;
    public static final String GRID = "grid";

    public static final int SECTION_TIMEOUT_IN_MINUTES = 15;

    @SerializedName(Constants.SECTION_TYPE)
    private String sectionType;

    @SerializedName(Constants.ITEMS)
    private ArrayList<SectionItem> sectionItems;

    @SerializedName(Constants.MORE)
    private SectionItem moreSectionItem;


    public String getSectionType() {
        return sectionType;
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
        super.writeToParcel(dest, flags);
        dest.writeString(sectionType);
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
        super(source);
        sectionType = source.readString();
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

    public int getCarouselHeight(Context context, HashMap<Integer, Renderer> rendererHashMap) {
        if (sectionItems == null) return 0;
        int maxHeight = 0;
        for (SectionItem sectionItem: sectionItems) {
            Renderer renderer = rendererHashMap != null ? rendererHashMap.get(sectionItem.getRenderingId()): null;
            int sectionItemHeight = sectionItem.getHeight(context, renderer);
            maxHeight = Math.max(maxHeight, sectionItemHeight);
        }
        return maxHeight;
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
