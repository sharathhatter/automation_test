package com.bigbasket.mobileapp.apiservice.models.request;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class SelectedShipment {
    @SerializedName(Constants.ID)
    private String shipmentId;
    @SerializedName(Constants.FULFILLMENT_ID)
    private String fulfillmentId;
    @SerializedName(Constants.SLOT_ID)
    private String slotId;
    @SerializedName(Constants.SLOT_DATE)
    private String slotDate;

    public SelectedShipment(String shipmentId, String fulfillmentId, String slotId, String slotDate) {
        this.shipmentId = shipmentId;
        this.fulfillmentId = fulfillmentId;
        this.slotId = slotId;
        this.slotDate = slotDate;
    }
}
