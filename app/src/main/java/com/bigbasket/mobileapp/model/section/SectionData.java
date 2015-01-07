package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SectionData implements Parcelable {

    private ArrayList<Section> sections;

    @SerializedName(Constants.DESTINATIONS_INFO)
    private HashMap<Integer, DestinationInfo> destinationInfoMap;

    @SerializedName(Constants.RENDERERS)
    private HashMap<Integer, Renderer> renderersMap;

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
        boolean wasDestMapNull = destinationInfoMap == null;
        dest.writeByte(wasDestMapNull ? (byte) 1 : (byte) 0);
        if (!wasDestMapNull) {
            dest.writeInt(destinationInfoMap.size());
            for (Map.Entry<Integer, DestinationInfo> destinationInfoEntry : destinationInfoMap.entrySet()) {
                dest.writeInt(destinationInfoEntry.getKey());
                dest.writeParcelable(destinationInfoEntry.getValue(), flags);
            }
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
    }

    public SectionData(Parcel source) {
        boolean wasSectionsNull = source.readByte() == (byte) 1;
        if (!wasSectionsNull) {
            sections = new ArrayList<>();
            source.readTypedList(sections, Section.CREATOR);
        }
        boolean wasDestMapNull = source.readByte() == (byte) 1;
        if (!wasDestMapNull) {
            destinationInfoMap = new HashMap<>();
            int size = source.readInt();
            for (int i = 0; i < size; i++) {
                int destinationInfoId = source.readInt();
                DestinationInfo destinationInfo = source.readParcelable(Section.class.getClassLoader());
                destinationInfoMap.put(destinationInfoId, destinationInfo);
            }
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
    }

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

    public ArrayList<Section> getSections() {
        return sections;
    }

    public HashMap<Integer, DestinationInfo> getDestinationInfoMap() {
        return destinationInfoMap;
    }

    public HashMap<Integer, Renderer> getRenderersMap() {
        return renderersMap;
    }
}
