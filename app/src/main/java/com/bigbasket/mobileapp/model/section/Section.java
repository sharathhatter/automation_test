package com.bigbasket.mobileapp.model.section;

import android.app.Activity;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.DisplayMetrics;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Section extends BaseSectionTextItem implements Parcelable, Serializable {
    public static final String BANNER = "banner";
    public static final String SALUTATION_TITLE = "salutation_title";
    public static final String SALUTATION = "salutation";
    public static final String PRODUCT_CAROUSEL = "product_carousel";
    public static final String NON_PRODUCT_CAROUSEL = "non_product_carousel";
    public static final String INFO_WIDGET = "info_widget";
    public static final String AD_IMAGE = "ad_image";
    public static final String TILE = "tile";
    public static final String MENU = "menu";
    public static final String MSG = Constants.MSG;
    public static final String GRID = "grid";

    public static final int DEFAULT_SECTION_TIMEOUT_IN_MINUTES = 15;
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
    @SerializedName(Constants.SECTION_TYPE)
    private String sectionType;
    @SerializedName(Constants.ITEMS)
    private ArrayList<SectionItem> sectionItems;
    @SerializedName(Constants.MORE)
    private SectionItem moreSectionItem;
    @SerializedName(Constants.RENDERING_ID)
    private int renderingId;

    private transient boolean isShown = false;

    public Section(SectionTextItem title, SectionTextItem description,
                   String sectionType, ArrayList<SectionItem> sectionItems, SectionItem moreSectionItem) {
        super(title, description);
        this.sectionType = sectionType;
        this.sectionItems = sectionItems;
        this.moreSectionItem = moreSectionItem;
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
        renderingId = source.readInt();
        isShown = source.readByte() != 0;
    }

    public String getSectionType() {
        return sectionType;
    }

    public ArrayList<SectionItem> getSectionItems() {
        return sectionItems;
    }

    public SectionItem getMoreSectionItem() {
        return moreSectionItem;
    }

    public int getRenderingId() {
        return renderingId;
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
        dest.writeInt(renderingId);
        dest.writeByte(isShown ? (byte) 1 : 0);
    }

    public int getWidgetHeight(Context context, HashMap<Integer, Renderer> rendererHashMap,
                               boolean adjustForScreen) {
        return getWidgetHeight(context, rendererHashMap, adjustForScreen, -1);
    }

    public int getWidgetHeight(Context context, HashMap<Integer, Renderer> rendererHashMap,
                               boolean adjustForScreen, int screenWidth) {
        if (sectionItems == null) return 0;
        if (screenWidth == -1) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            screenWidth = displayMetrics.widthPixels;
        }
        int maxHeight = 0;
        for (SectionItem sectionItem : sectionItems) {
            Renderer renderer = rendererHashMap != null ? rendererHashMap.get(sectionItem.getRenderingId()) : null;
            int sectionItemHeight = sectionItem.getHeight(context, renderer);
            if (adjustForScreen) {
                int actualSectionItemWidth = sectionItem.getActualWidth(context);
                if (actualSectionItemWidth > 0) {
                    double actualToScreenWidthRatio = (double) screenWidth / (double) actualSectionItemWidth;
                    sectionItemHeight = (int) (actualToScreenWidthRatio * (double) sectionItemHeight);
                }
            }
            maxHeight = Math.max(maxHeight, sectionItemHeight);
        }
        return maxHeight;
    }

    public boolean isShown() {
        return isShown;
    }

    public void setIsShown(boolean isShown) {
        this.isShown = isShown;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) {
            return false;
        }
        Section section = (Section) o;

        if (renderingId != section.renderingId) return false;
        if (sectionType != null ? !sectionType.equals(section.sectionType) : section.sectionType != null)
            return false;
        if (sectionItems != null ? !sectionItems.equals(section.sectionItems) : section.sectionItems != null)
            return false;
        return !(moreSectionItem != null ? !moreSectionItem.equals(section.moreSectionItem) : section.moreSectionItem != null);

    }

    @Override
    public int hashCode() {
        int result = sectionType != null ? sectionType.hashCode() : 0;
        result = 31 * result + (sectionItems != null ? sectionItems.hashCode() : 0);
        result = 31 * result + (moreSectionItem != null ? moreSectionItem.hashCode() : 0);
        result = 31 * result + renderingId;
        return result;
    }
}
