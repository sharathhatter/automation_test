package com.bigbasket.mobileapp.model.promo;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class PromoCategory implements Parcelable {

    public static final Parcelable.Creator<PromoCategory> CREATOR = new Parcelable.Creator<PromoCategory>() {
        @Override
        public PromoCategory createFromParcel(Parcel source) {
            return new PromoCategory(source);
        }

        @Override
        public PromoCategory[] newArray(int size) {
            return new PromoCategory[size];
        }
    };
    @SerializedName("desc")
    private String description;
    @SerializedName("id")
    private int id;
    @SerializedName("name")
    private String name;
    @SerializedName("icon")
    private String icon;
    @SerializedName("promos")
    private ArrayList<Promo> promos;

    public PromoCategory(Parcel source) {
        description = source.readString();
        id = source.readInt();
        name = source.readString();
        boolean isIconNull = source.readByte() == (byte) 1;
        if (!isIconNull) {
            icon = source.readString();
        }
        promos = new ArrayList<>();
        source.readTypedList(promos, Promo.CREATOR);
    }

    /**
     * Copy Constructor for PromoCategory. This won't assign "promos",
     * use "setPromos" to manually set it
     */
    public PromoCategory(PromoCategory promoCategory) {
        this.description = promoCategory.getDescription();
        this.id = promoCategory.getId();
        this.name = promoCategory.getName();
        this.icon = promoCategory.getIcon();
    }

    public PromoCategory(String description, int id, String name,
                         String icon, ArrayList<Promo> promos) {

        this.description = description;
        this.id = id;
        this.name = name;
        this.icon = icon;
        this.promos = promos;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(description);
        dest.writeInt(id);
        dest.writeString(name);
        boolean isIconNull = icon == null;
        dest.writeByte(isIconNull ? (byte) 1 : (byte) 0);
        if (!isIconNull) {
            dest.writeString(icon);
        }
        dest.writeTypedList(promos);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public ArrayList<Promo> getPromos() {
        return promos;
    }

    public void setPromos(ArrayList<Promo> promos) {
        this.promos = promos;
    }
}
