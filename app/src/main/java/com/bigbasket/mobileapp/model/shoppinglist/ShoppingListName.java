package com.bigbasket.mobileapp.model.shoppinglist;


import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.google.gson.annotations.SerializedName;

public class ShoppingListName implements Parcelable {

    public static final Parcelable.Creator<ShoppingListName> CREATOR = new Parcelable.Creator<ShoppingListName>() {
        @Override
        public ShoppingListName createFromParcel(Parcel source) {
            return new ShoppingListName(source);
        }

        @Override
        public ShoppingListName[] newArray(int size) {
            return new ShoppingListName[size];
        }
    };
    @SerializedName(Constants.SHOPPING_LIST_NAME)
    private String name;
    @SerializedName(Constants.SHOPPING_LIST_SLUG)
    private String slug;
    @SerializedName(Constants.SHOPPING_LIST_IS_SYSTEM)
    private int isSystem;
    @SerializedName(Constants.SHOPPING_LIST_DESC)
    private String description;

    public ShoppingListName() {
    }

    public ShoppingListName(String name, String slug, boolean system) {
        this.name = name;
        this.slug = slug;
        isSystem = system ? 1 : 0;
    }

    public ShoppingListName(Parcel source) {
        boolean wasNameNull = source.readByte() == (byte) 1;
        if (!wasNameNull) {
            this.name = source.readString();
        }
        this.slug = source.readString();
        this.isSystem = source.readInt();
        boolean wasDescNull = source.readByte() == (byte) 1;
        if (!wasDescNull) {
            this.description = source.readString();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public boolean isSystem() {
        return isSystem == 1 || 
                (slug != null && slug.equalsIgnoreCase(Constants.SMART_BASKET_SLUG));
    }

    public void setAsSystem(int isSystem) {
        this.isSystem = isSystem;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasNameNull = this.name == null;
        dest.writeByte(wasNameNull ? (byte) 1 : (byte) 0);
        if (!wasNameNull) {
            dest.writeString(this.name);
        }
        dest.writeString(this.slug);
        dest.writeInt(this.isSystem);
        boolean wasDescNull = this.description == null;
        dest.writeByte(wasDescNull ? (byte) 1 : (byte) 0);
        if (!wasDescNull) {
            dest.writeString(description);
        }
    }

    public String getNc() {
        String nc;
        if (slug.equalsIgnoreCase(Constants.SMART_BASKET_SLUG)) {
            nc = TrackEventkeys.SB;
        } else if (isSystem()) {
            nc = TrackEventkeys.SSL;
        } else {
            nc = TrackEventkeys.SL;
        }
        return nc;// + "." + slug;
    }
}
