package com.bigbasket.mobileapp.model.location;

public class AutoCompletePlace {
    private String placeId;
    private String description;

    public AutoCompletePlace(String placeId, String description) {
        this.placeId = placeId;
        this.description = description;
    }

    public String getPlaceId() {
        return placeId;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return description;
    }
}
