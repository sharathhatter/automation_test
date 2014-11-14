package com.bigbasket.mobileapp.model.order;

import android.os.Parcel;
import android.os.Parcelable;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by jugal on 16/7/14.
 */
public class MarketPlaceRuleValidator implements Parcelable { //implements Parcelable

    @SerializedName(Constants.RULE_NAME)
    private String ruleName;

    @SerializedName(Constants.RULE_DESC)
    private String ruleDesc;

    @SerializedName(Constants.WEIGHT_LABEL)
    private String weightLabel;

    @SerializedName(Constants.RULE_TOTAL_QTY)
    private float ruleTotalQty;

    @SerializedName(Constants.RULE_TOTAL_PRICE)
    private float ruleTotalPrice;

    @SerializedName(Constants.ITEMS)
    private ArrayList<MarketPlaceItems> items;

    @SerializedName(Constants.RULE_MAX_WEIGHT_LIMIT)
    private float ruleMaxWeightLimit;

    //private int totalRuleNoOfItems;
    private boolean noRuleError;

    public MarketPlaceRuleValidator(String ruleName, String ruleDesc, String weightLabel, float ruleTotalQty,
                                    float ruleTotalPrice,
                                    ArrayList<MarketPlaceItems> items, float ruleMaxWeightLimit) {
        this.ruleName = ruleName;
        this.ruleDesc = ruleDesc;
        this.weightLabel = weightLabel;
        this.ruleTotalQty = ruleTotalQty;
        this.ruleTotalPrice = ruleTotalPrice;
        this.items = items;
        this.ruleMaxWeightLimit = ruleMaxWeightLimit;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    public MarketPlaceRuleValidator(Parcel source) {
        ruleName = source.readString();
        ruleDesc = source.readString();
        weightLabel = source.readString();
        ruleTotalQty = source.readFloat();
        ruleTotalPrice = source.readFloat();
        boolean _wasMarketPlaceItemsNull = source.readByte() == (byte) 1;
        if (!_wasMarketPlaceItemsNull) {
            items = new ArrayList<>();
            source.readTypedList(items, MarketPlaceItems.CREATOR);
        }
        ruleMaxWeightLimit = source.readFloat();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(ruleName);
        dest.writeString(ruleDesc);
        dest.writeString(weightLabel);
        dest.writeFloat(ruleTotalQty);
        dest.writeFloat(ruleTotalPrice);
        boolean _wasMarketPlaceItemsNull = false;
        if (items == null) {
            _wasMarketPlaceItemsNull = true;
        }
        dest.writeByte(_wasMarketPlaceItemsNull ? (byte) 1 : (byte) 0);
        if (items != null) {
            dest.writeTypedList(items);
        }
        dest.writeFloat(ruleMaxWeightLimit);

    }

    public static final Parcelable.Creator<MarketPlaceRuleValidator> CREATOR = new Parcelable.Creator<MarketPlaceRuleValidator>() {
        @Override
        public MarketPlaceRuleValidator createFromParcel(Parcel source) {
            return new MarketPlaceRuleValidator(source);
        }

        @Override
        public MarketPlaceRuleValidator[] newArray(int size) {
            return new MarketPlaceRuleValidator[size];
        }
    };

    public float getRuleTotalPrice() {
        return ruleTotalPrice;
    }

    public boolean isNoRuleError() {
        return noRuleError;
    }

    public float getRuleMaxWeightLimit() {
        return ruleMaxWeightLimit;
    }

    public float getRuleTotalQty() {
        return ruleTotalQty;
    }

    public String getWeightLabel() {
        return weightLabel;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getRuleDesc() {
        return ruleDesc;
    }

    public ArrayList<MarketPlaceItems> getItems() {
        return items;
    }

    public void setItems(ArrayList<MarketPlaceItems> items) {
        this.items = items;
    }
}
