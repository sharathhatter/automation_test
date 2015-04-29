package com.bigbasket.mobileapp.model.slot;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class Slot extends BaseSlot implements Parcelable {
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
    @SerializedName(Constants.AVAILABLE)
    private boolean available;
    @SerializedName(Constants.SLOT_DATE)
    private String slotDate;
    @SerializedName(Constants.SLOT_ID)
    private String slotId;

    public Slot(Parcel source) {
        super(source.readString());
        this.available = source.readByte() == (byte) 1;
        this.slotDate = source.readString();
        this.slotId = source.readString();
    }

    public static List<BaseSlot> getFlattenedSlotGroupList(List<Slot> slotListToGroup) {
        List<BaseSlot> groupedSlotList = new LinkedList<>();
        HashMap<String, List<Slot>> groupedSlotMap = getGroupedSlotMap(slotListToGroup);
        for (Map.Entry<String, List<Slot>> entry : groupedSlotMap.entrySet()) {
            groupedSlotList.add(new SlotHeader(entry.getKey()));
            groupedSlotList.addAll(entry.getValue());
        }
        return groupedSlotList;
    }

    private static LinkedHashMap<String, List<Slot>> getGroupedSlotMap(List<Slot> slotListToGroup) {
        LinkedHashMap<String, List<Slot>> groupedSlotMap = new LinkedHashMap<>();
        for (Slot slot : slotListToGroup) {
            List<Slot> slotList = groupedSlotMap.get(slot.getSlotDate());
            if (slotList == null) {
                slotList = new LinkedList<>();
                groupedSlotMap.put(slot.getSlotDate(), slotList);
            }
            slotList.add(slot);
        }
        return groupedSlotMap;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.displayName);
        dest.writeByte(this.available ? (byte) 1 : (byte) 0);
        dest.writeString(this.slotDate);
        dest.writeString(this.slotId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getSlotDate() {
        return slotDate;
    }

    public String getSlotId() {
        return slotId;
    }

    public String getFormattedSlotDate() {
        String slotDate = this.slotDate;
        slotDate = slotDate.replaceAll(",", " ");
        slotDate = slotDate.replaceAll("-", "/");
        return slotDate;
    }
}
