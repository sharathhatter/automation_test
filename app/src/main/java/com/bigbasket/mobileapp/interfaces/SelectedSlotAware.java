package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.slot.SelectedSlotType;

import java.util.ArrayList;

public interface SelectedSlotAware {
    public void setSelectedSlotType(ArrayList<SelectedSlotType> selectedSlotType);
}
