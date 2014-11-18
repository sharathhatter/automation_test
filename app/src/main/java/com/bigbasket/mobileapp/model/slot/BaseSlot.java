package com.bigbasket.mobileapp.model.slot;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseSlot {
    @SerializedName(Constants.DISPLAY_NAME)
    protected String displayName;

    public BaseSlot(String displayName) {
        this.displayName = displayName;
    }

    public BaseSlot(JSONObject jsonObject) throws JSONException {
        this.displayName = jsonObject.getString(Constants.DISPLAY_NAME);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFormattedDisplayName() {
        String displayName = this.displayName.replaceAll("-", " to ");
        displayName = displayName.replaceAll(",", " ");
        return displayName;
    }

}
