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

    private ArrayList<Section> sections;
    @SerializedName(Constants.RENDERERS)
    private HashMap<Integer, Renderer> renderersMap;
    @SerializedName(Constants.SCREEN_NAME)
    private String screenName;
    @SerializedName(Constants.BASE_IMG_URL)
    private String baseImgUrl;
    @SerializedName(Constants.ANALYTICS_ATTRS)
    private HashMap<String, Map<String, String>> analyticsAttrs;

    public SectionData() {

    }

    public SectionData(Parcel source) {
        boolean wasSectionsNull = source.readByte() == (byte) 1;
        if (!wasSectionsNull) {
            sections = source.createTypedArrayList(Section.CREATOR);
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
        int analyticsAttrSize = source.readInt();
        analyticsAttrs = new HashMap<>(analyticsAttrSize);

        for (int i = 0; i < analyticsAttrSize; i++) {
            String key = source.readString();
            int valMapLen = source.readInt();
            if (valMapLen <= 0) {
                analyticsAttrs.put(key, null);
            } else {
                Map<String, String> valueMap = new HashMap<>(valMapLen);
                for (int j = 0; j < valMapLen; j++) {
                    String valKey = source.readString();
                    boolean wasValueNull = source.readByte() == (byte) 1;
                    if (!wasValueNull) {
                        valueMap.put(valKey, source.readString());
                    } else {
                        valueMap.put(valKey, null);
                    }
                }
                analyticsAttrs.put(key, valueMap);
            }
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

        if (analyticsAttrs != null && !analyticsAttrs.isEmpty()) {
            dest.writeInt(analyticsAttrs.size());
            for (Map.Entry<String, Map<String, String>> entry : analyticsAttrs.entrySet()) {
                dest.writeString(entry.getKey()); //FIXME: Assumption key is not null
                Map<String, String> value = entry.getValue();
                if (value != null && !value.isEmpty()) {
                    dest.writeInt(value.size());
                    for (Map.Entry<String, String> valEntry : value.entrySet()) {
                        dest.writeString(valEntry.getKey());
                        boolean wasValueNull = valEntry.getValue() == null;
                        dest.writeByte(wasValueNull ? (byte) 1 : (byte) 0);
                        if (!wasValueNull) {
                            dest.writeString(valEntry.getValue());
                        }
                    }
                } else {
                    dest.writeInt(0);
                }
            }
        } else {
            dest.writeInt(0);
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

    public Map<String, String> getAnalyticsAttrs(String sectionItemId) {
        if (analyticsAttrs != null) {
            return analyticsAttrs.get(sectionItemId);
        }
        return null;
    }

    public static final Creator<SectionData> CREATOR = new Creator<SectionData>() {
        public SectionData createFromParcel(Parcel source) {
            return new SectionData(source);
        }

        public SectionData[] newArray(int size) {
            return new SectionData[size];
        }
    };
}
