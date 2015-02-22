package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.annotations.SerializedName;

public class Renderer implements Parcelable {

    public static final int PADDING = 4;
    public static final int MARGIN = 4;
    public static final int MAX_MARGIN = 32;
    public static final int MAX_PADDING = 32;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int CENTER = 3;
    public static final int CENTER_HORIZONTAL = 4;
    public static final int CENTER_VERTICAL = 5;

    @SerializedName(Constants.TEXT_COLOR)
    private String textColor;

    private int nativeTextColor;

    @SerializedName(Constants.BACKGROUND_COLOR)
    private String backgroundColor;

    private int nativeBkgColor;

    @SerializedName(Constants.PADDING)
    private int padding;

    @SerializedName(Constants.MARGIN)
    private int margin;

    @SerializedName(Constants.ALIGNMENT)
    private int alignment;

    public String getTextColor() {
        return textColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public int getNativeTextColor() {
        return nativeTextColor;
    }

    public void setNativeTextColor(int nativeTextColor) {
        this.nativeTextColor = nativeTextColor;
    }

    public int getNativeBkgColor() {
        return nativeBkgColor;
    }

    public void setNativeBkgColor(int nativeBkgColor) {
        this.nativeBkgColor = nativeBkgColor;
    }

    public int getAlignment() {
        return alignment;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean wasTextColorNull = textColor == null;
        dest.writeByte(wasTextColorNull ? (byte) 1 : (byte) 0);
        if (!wasTextColorNull) {
            dest.writeString(textColor);
        }
        boolean wasBkgColorNull = backgroundColor == null;
        dest.writeByte(wasBkgColorNull ? (byte) 1 : (byte) 0);
        if (!wasBkgColorNull) {
            dest.writeString(backgroundColor);
        }
        dest.writeInt(padding);
        dest.writeInt(margin);
        dest.writeInt(alignment);
    }

    public Renderer(Parcel source) {
        boolean wasTextColorNull = source.readByte() == (byte) 1;
        if (!wasTextColorNull) {
            textColor = source.readString();
        }
        boolean wasBkgColorNull = source.readByte() == (byte) 1;
        if (!wasBkgColorNull) {
            backgroundColor = source.readString();
        }
        nativeBkgColor = UIUtil.parseAsNativeColor(backgroundColor);
        nativeTextColor = UIUtil.parseAsNativeColor(textColor);
        padding = source.readInt();
        margin = source.readInt();
        alignment = source.readInt();
    }

    public static final Parcelable.Creator<Renderer> CREATOR = new Parcelable.Creator<Renderer>() {
        @Override
        public Renderer createFromParcel(Parcel source) {
            return new Renderer(source);
        }

        @Override
        public Renderer[] newArray(int size) {
            return new Renderer[size];
        }
    };


    public int getSafeMargin(int defaultValue) {
        return getSafeUnit(margin, Renderer.MARGIN, defaultValue, Renderer.MAX_MARGIN);
    }

    public int getSafePadding(int defaultValue) {
        return getSafeUnit(padding, Renderer.PADDING, defaultValue, Renderer.MAX_PADDING);
    }

    public static int getSafeUnit(int coefficient, int unitValue, int defaultValue, int maxValue) {
        int value = coefficient > 0 ? coefficient * unitValue : Math.max(defaultValue, 0);
        return value > maxValue ? defaultValue : value;
    }

    public int getAsNativeAlignment() {
        switch (alignment) {
            case LEFT:
                return Gravity.LEFT;
            case RIGHT:
                return Gravity.RIGHT;
            case CENTER:
                return Gravity.CENTER;
            case CENTER_HORIZONTAL:
                return Gravity.CENTER_HORIZONTAL;
            case CENTER_VERTICAL:
                return Gravity.CENTER_HORIZONTAL;
            default:
                return Gravity.NO_GRAVITY;
        }
    }

    public void setRendering(View view, int defaultMargin, int defaultPadding) {
        setRendering(view, defaultMargin, defaultPadding, true, true, true, true);
    }

    public void setRendering(View view, int defaultMargin, int defaultPadding,
                             boolean applyLeft, boolean applyTop, boolean applyRight,
                             boolean applyBottom) {
        setRendering(view, defaultMargin, defaultPadding, applyLeft, applyTop, applyRight, applyBottom, -1);
    }

    public void setRendering(View view, int defaultMargin, int defaultPadding,
                             boolean applyLeft, boolean applyTop, boolean applyRight,
                             boolean applyBottom, int width) {
        int margin = this.getSafeMargin(defaultMargin);
        int padding = this.getSafePadding(defaultPadding);
        if (margin >= 0) {
            int widthToUse = width >= 0 ? width : ViewGroup.LayoutParams.MATCH_PARENT;
            ViewParent viewParent = view.getParent();
            if (viewParent != null) {
                if (viewParent instanceof LinearLayout) {
                    LinearLayout.LayoutParams txtLayoutParams = new LinearLayout.LayoutParams(widthToUse,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    txtLayoutParams.setMargins(applyLeft ? margin : 0, applyTop ? margin : 0,
                            applyRight ? margin : 0, applyBottom ? margin : 0);
                    view.setLayoutParams(txtLayoutParams);
                } else if (viewParent instanceof RelativeLayout) {
                    RelativeLayout.LayoutParams txtLayoutParams = new RelativeLayout.LayoutParams(widthToUse,
                            ViewGroup.LayoutParams.WRAP_CONTENT);
                    txtLayoutParams.setMargins(applyLeft ? margin : 0, applyTop ? margin : 0,
                            applyRight ? margin : 0, applyBottom ? margin : 0);
                    view.setLayoutParams(txtLayoutParams);
                }
            }
        }
        if (padding >= 0 && !(view instanceof ImageView)) {
            view.setPadding(applyLeft ? padding : 0, applyTop ? padding : 0, applyRight ? padding : 0,
                    applyBottom ? padding : 0);
        }
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(this.getNativeTextColor());
            ((TextView) view).setGravity(this.getAsNativeAlignment());
        }
        view.setBackgroundColor(this.getNativeBkgColor());
    }
}
