package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Slot implements Parcelable {
    @SerializedName(Constants.DISPLAY)
    private SlotDisplay slotDisplay;
    @SerializedName(Constants.SLOT_ID)
    private String slotId;
    private boolean available;
    @SerializedName(Constants.SLOT_DATE)
    private String slotDate;
    @SerializedName(Constants.SLOT_TIME)
    private String slotTime;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(slotDisplay, flags);
        dest.writeString(slotId);
        dest.writeByte(available ? (byte) 1 : (byte) 0);

        boolean wasSlotDateNull = slotDate == null;
        dest.writeByte(wasSlotDateNull ? (byte) 1 : (byte) 0);
        if (!wasSlotDateNull) {
            dest.writeString(slotDate);
        }
        dest.writeString(slotTime);
    }

    public Slot(Parcel source) {
        slotDisplay = source.readParcelable(Slot.class.getClassLoader());
        slotId = source.readString();
        available = source.readByte() == (byte) 1;
        boolean wasSlotDateNull = source.readByte() == (byte) 1;
        if (!wasSlotDateNull) {
            slotDate = source.readString();
        }
        slotTime = source.readString();
    }

    public static final Parcelable.Creator<Slot> CREATOR = new Parcelable.Creator<Slot>() {
        @Override
        public Slot createFromParcel(Parcel source) {
            return new Slot(source);
        }

        @Override
        public Slot[] newArray(int size) {
            return new Slot[size];
        }
    };

    public SlotDisplay getSlotDisplay() {
        return slotDisplay;
    }

    public String getSlotId() {
        return slotId;
    }

    public boolean isAvailable() {
        return available;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public static List<Object> getFlattenedSlotGroupList(List<Slot> slotListToGroup) {
        List<Object> groupedSlotList = new LinkedList<>();
        HashMap<String, List<Slot>> groupedSlotMap = getGroupedSlotMap(slotListToGroup);
        for (Map.Entry<String, List<Slot>> entry : groupedSlotMap.entrySet()) {
            if (!TextUtils.isEmpty(entry.getKey())) {
                groupedSlotList.add(entry.getKey());
            }
            groupedSlotList.addAll(entry.getValue());
        }
        return groupedSlotList;
    }

    private static LinkedHashMap<String, List<Slot>> getGroupedSlotMap(List<Slot> slotListToGroup) {
        LinkedHashMap<String, List<Slot>> groupedSlotMap = new LinkedHashMap<>();
        for (Slot slot : slotListToGroup) {
            List<Slot> slotList = groupedSlotMap.get(slot.getSlotDisplay().getDate());
            if (slotList == null) {
                slotList = new LinkedList<>();
                groupedSlotMap.put(slot.getSlotDisplay().getDate(), slotList);
            }
            slotList.add(slot);
        }
        return groupedSlotMap;
    }
}
