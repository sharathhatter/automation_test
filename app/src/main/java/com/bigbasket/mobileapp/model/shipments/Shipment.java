package com.bigbasket.mobileapp.model.shipments;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Shipment implements Parcelable {
    @SerializedName(Constants.SHIPMENT_ID)
    private String shipmentId;
    private int count;
    private String value;
    private String type;
    @SerializedName(Constants.SHIPMENT_NAME)
    private String shipmentName;
    @SerializedName(Constants.FULFILLMENT_NAME)
    private String fulfillmentName;
    @SerializedName(Constants.FULFILLMENT_ID)
    private String fulfillmentId;
    @SerializedName(Constants.DELIVERY_CHARGE)
    private String deliveryCharge;
    @SerializedName(Constants.ACTION)
    private ShipmentAction shipmentAction;
    @SerializedName(Constants.LINKED_SHIPMENTS)
    private LinkedShipments linkedShipments;
    private ArrayList<Slot> slots;
    private Slot selectedSlot;
    @SerializedName(Constants.HELP_PAGE)
    private String helpPage;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(shipmentId);
        dest.writeInt(count);
        dest.writeString(value);
        dest.writeString(type);
        dest.writeString(shipmentName);
        dest.writeString(fulfillmentName);
        dest.writeString(fulfillmentId);
        dest.writeString(deliveryCharge);
        boolean wasShipmentActionNull = shipmentAction == null;
        dest.writeByte(wasShipmentActionNull ? (byte) 1 : (byte) 0);
        if (!wasShipmentActionNull) {
            dest.writeParcelable(shipmentAction, flags);
        }
        boolean wasHelpPageNull = helpPage == null;
        dest.writeByte(wasHelpPageNull ? (byte) 1 : (byte) 0);
        if (!wasHelpPageNull) {
            dest.writeString(helpPage);
        }
        dest.writeParcelable(linkedShipments, flags);
        dest.writeTypedList(slots);
        boolean wasSelectedSlotNull = selectedSlot == null;
        dest.writeByte(wasSelectedSlotNull ? (byte) 1 : (byte) 0);
        if (!wasSelectedSlotNull) {
            dest.writeParcelable(selectedSlot, flags);
        }
    }

    public Shipment(Parcel source) {
        shipmentId = source.readString();
        count = source.readInt();
        value = source.readString();
        type = source.readString();
        shipmentName = source.readString();
        fulfillmentName = source.readString();
        fulfillmentId = source.readString();
        deliveryCharge = source.readString();
        boolean wasShipmentActionNull = source.readByte() == (byte) 1;
        if (!wasShipmentActionNull) {
            shipmentAction = source.readParcelable(Shipment.class.getClassLoader());
        }
        boolean wasHelpPageNull = source.readByte() == (byte) 1;
        if (!wasHelpPageNull) {
            helpPage = source.readString();
        }
        linkedShipments = source.readParcelable(Shipment.class.getClassLoader());
        slots = new ArrayList<>();
        source.readTypedList(slots, Slot.CREATOR);
        boolean wasSelectedSlotNull = source.readByte() == (byte) 1;
        if (!wasSelectedSlotNull) {
            selectedSlot = source.readParcelable(Shipment.class.getClassLoader());
        }
    }

    public static final Parcelable.Creator<Shipment> CREATOR = new Parcelable.Creator<Shipment>() {
        @Override
        public Shipment createFromParcel(Parcel source) {
            return new Shipment(source);
        }

        @Override
        public Shipment[] newArray(int size) {
            return new Shipment[size];
        }
    };

    public String getShipmentId() {
        return shipmentId;
    }

    public int getCount() {
        return count;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getShipmentName() {
        return shipmentName;
    }

    public String getFulfillmentName() {
        return fulfillmentName;
    }

    public String getDeliveryCharge() {
        return deliveryCharge;
    }

    public ShipmentAction getShipmentAction() {
        return shipmentAction;
    }

    public LinkedShipments getLinkedShipments() {
        return linkedShipments;
    }

    public ArrayList<Slot> getSlots() {
        return slots;
    }

    public String getHelpPage() {
        return helpPage;
    }

    public String getFulfillmentId() {
        return fulfillmentId;
    }

    public Slot getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(Slot selectedSlot) {
        this.selectedSlot = selectedSlot;
    }
}
