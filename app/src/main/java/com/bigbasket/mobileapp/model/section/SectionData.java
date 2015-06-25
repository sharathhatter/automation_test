package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SectionData implements Parcelable, Serializable {

    public static final Parcelable.Creator<SectionData> CREATOR = new Parcelable.Creator<SectionData>() {
        @Override
        public SectionData createFromParcel(Parcel source) {
            return new SectionData(source);
        }

        @Override
        public SectionData[] newArray(int size) {
            return new SectionData[size];
        }
    };
    private ArrayList<Section> sections;
    @SerializedName(Constants.RENDERERS)
    private HashMap<Integer, Renderer> renderersMap;
    @SerializedName(Constants.SCREEN_NAME)
    private String screenName;
    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;

    public SectionData(Parcel source) {
        boolean wasSectionsNull = source.readByte() == (byte) 1;
        if (!wasSectionsNull) {
            sections = new ArrayList<>();
            source.readTypedList(sections, Section.CREATOR);
        }
        boolean wasRendererMapNull = source.readByte() == (byte) 1;
        if (!wasRendererMapNull) {
            renderersMap = new HashMap<>();
            int size = source.readInt();
            for (int i = 0; i < size; i++) {
                int rendererId = source.readInt();
                Renderer renderer = source.readParcelable(Section.class.getClassLoader());
                renderersMap.put(rendererId, renderer);
            }
        }
        boolean wasScreenNameNull = source.readByte() == (byte) 1;
        if (!wasScreenNameNull) {
            screenName = source.readString();
        }
        boolean wasBaseImgUrlNull = source.readByte() == (byte) 1;
        if (!wasBaseImgUrlNull) {
            baseImgUrl = source.readString();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasSectionsNull = sections == null;
        dest.writeByte(wasSectionsNull ? (byte) 1 : (byte) 0);
        if (!wasSectionsNull) {
            dest.writeTypedList(sections);
        }
        boolean wasRendererMapNull = renderersMap == null;
        dest.writeByte(wasRendererMapNull ? (byte) 1 : (byte) 0);
        if (!wasRendererMapNull) {
            dest.writeInt(renderersMap.size());
            for (Map.Entry<Integer, Renderer> rendererEntry : renderersMap.entrySet()) {
                dest.writeInt(rendererEntry.getKey());
                dest.writeParcelable(rendererEntry.getValue(), flags);
            }
        }
        boolean wasScreenNameNull = screenName == null;
        dest.writeByte(wasScreenNameNull ? (byte) 1 : (byte) 0);
        if (!wasScreenNameNull) {
            dest.writeString(screenName);
        }
        boolean wasBaseImgUrl = baseImgUrl == null;
        dest.writeByte(wasBaseImgUrl ? (byte) 1 : (byte) 0);
        if (!wasBaseImgUrl) {
            dest.writeString(baseImgUrl);
        }
    }

    public ArrayList<Section> getSections() {
        return sections;
    }

    public HashMap<Integer, Renderer> getRenderersMap() {
        return renderersMap;
    }

    public String getScreenName() {
        return screenName;
    }

    public String getBaseImgUrl() {
        return baseImgUrl;
    }

    public void setSections(ArrayList<Section> sections) {
        this.sections = sections;
    }
}
