package com.bigbasket.mobileapp.model.slot;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class SlotGroup implements Parcelable {

    public static final Parcelable.Creator<SlotGroup> CREATOR = new Parcelable.Creator<SlotGroup>() {
        @Override
        public SlotGroup createFromParcel(Parcel source) {
            return new SlotGroup(source);
        }

        @Override
        public SlotGroup[] newArray(int size) {
            return new SlotGroup[size];
        }
    };
    @SerializedName(Constants.FULFILLMENT_INFO)
    private FulfillmentInfo fulfillmentInfo;
    @SerializedName(Constants.SLOTS)
    private List<Slot> slotList;
    @SerializedName(Constants.SLOT)
    private Slot selectedSlot;
    @SerializedName(Constants.NEXT_AVAILABLE_SLOT)
    private Slot nextAvailableSlot;
    private boolean _wasSlotListNull;
    private boolean _wasSlotNull;
    private boolean _wasNextSlotNull;

    public SlotGroup() {
        slotList = new ArrayList<>();
    }

    public SlotGroup(FulfillmentInfo fulfillmentInfo, List<Slot> slotList) {
        this.fulfillmentInfo = fulfillmentInfo;
        this.slotList = slotList;
    }

    public SlotGroup(FulfillmentInfo fulfillmentInfo, List<Slot> slotList, Slot selectedSlot,
                     Slot nextAvailableSlot) {
        this(fulfillmentInfo, slotList);
        this.selectedSlot = selectedSlot;
        this.nextAvailableSlot = nextAvailableSlot;
    }

    public SlotGroup(Parcel source) {
        fulfillmentInfo = source.readParcelable(SlotGroup.class.getClassLoader());
        _wasSlotNull = source.readByte() == (byte) 1;
        if (!_wasSlotNull) {
            selectedSlot = source.readParcelable(SlotGroup.class.getClassLoader());
        }
        _wasSlotListNull = source.readByte() == (byte) 1;
        if (!_wasSlotListNull) {
            slotList = new ArrayList<>();
            source.readTypedList(slotList, Slot.CREATOR);
        }
        _wasNextSlotNull = source.readByte() == (byte) 1;
        if (!_wasNextSlotNull) {
            nextAvailableSlot = source.readParcelable(SlotGroup.class.getClassLoader());
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(fulfillmentInfo, flags);
        this._wasSlotNull = selectedSlot == null;
        dest.writeByte(_wasSlotNull ? (byte) 1 : (byte) 0);
        if (selectedSlot != null) {
            dest.writeParcelable(selectedSlot, flags);
        }
        this._wasSlotListNull = slotList == null;
        dest.writeByte(_wasSlotListNull ? (byte) 1 : (byte) 0);
        if (slotList != null) {
            dest.writeTypedList(slotList);
        }
        this._wasNextSlotNull = nextAvailableSlot == null;
        dest.writeByte(_wasNextSlotNull ? (byte) 1 : (byte) 0);
        if (nextAvailableSlot != null) {
            dest.writeParcelable(nextAvailableSlot, flags);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public Slot getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(Slot selectedSlot) {
        this.selectedSlot = selectedSlot;
    }

    public FulfillmentInfo getFulfillmentInfo() {
        return fulfillmentInfo;
    }

    public Slot getNextAvailableSlot() {
        return nextAvailableSlot;
    }

    public List<Slot> getSlotList() {
        return slotList;
    }

    public boolean isSelected() {
        return selectedSlot != null;
    }
}
