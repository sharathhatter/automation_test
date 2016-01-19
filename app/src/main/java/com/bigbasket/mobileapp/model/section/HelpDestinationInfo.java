package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;

import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;

public class HelpDestinationInfo extends DestinationInfo {
    @SerializedName(Constants.HELP_KEY)
    private String helpKey;

    public HelpDestinationInfo(Parcel source) {
        super(source);
        helpKey = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(helpKey);
    }

    public static final Parcelable.Creator<HelpDestinationInfo> CREATOR = new Parcelable.Creator<HelpDestinationInfo>() {
        @Override
        public HelpDestinationInfo createFromParcel(Parcel source) {
            return new HelpDestinationInfo(source);
        }

        @Override
        public HelpDestinationInfo[] newArray(int size) {
            return new HelpDestinationInfo[size];
        }
    };

    public String getHelpKey() {
        return helpKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        HelpDestinationInfo that = (HelpDestinationInfo) o;

        return !(helpKey != null ? !helpKey.equals(that.helpKey) : that.helpKey != null);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (helpKey != null ? helpKey.hashCode() : 0);
        return result;
    }
}
