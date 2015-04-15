package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Renderer implements Parcelable, Serializable {

    public static final int PADDING = 4;
    public static final int MARGIN = 4;
    public static final int MAX_MARGIN = 32;
    public static final int MAX_PADDING = 32;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;
    public static final int CENTER = 3;
    public static final int CENTER_HORIZONTAL = 4;
    public static final int CENTER_VERTICAL = 5;

    public static final int VERTICAL = 0;
    public static final int HORIZONTAL = 1;

    public static final String TITLE_IMG_DESC = "t,i,d";
    public static final String TITLE_DESC_IMG = "t,d,i";
    public static final String IMG_TITLE_DESC = "i,t,d";

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

    private int orientation;

    private String ordering;

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

    public int getOrientation() {
        return orientation;
    }

    public String getOrdering() {
        return ordering;
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
        dest.writeInt(orientation);

        boolean wasOrderingNull = ordering == null;
        dest.writeByte(wasOrderingNull ? (byte) 1 : (byte) 0);
        if (!wasOrderingNull) {
            dest.writeString(ordering);
        }
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
        boolean wasOrderingNull = source.readByte() == (byte) 1;
        if (!wasOrderingNull) {
            ordering = source.readString();
        }
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

    public int getMargin() {
        return margin;
    }

    public int getPadding() {
        return padding;
    }

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
        setRendering(view, defaultMargin, defaultPadding, applyLeft, applyTop, applyRight,
                applyBottom, applyLeft, applyTop, applyRight, applyBottom);
    }

    public void setRendering(View view, int defaultMargin, int defaultPadding,
                             boolean applyLeft, boolean applyTop, boolean applyRight,
                             boolean applyBottom, boolean applyLeftPadding,
                             boolean applyTopPadding, boolean applyRightPadding,
                             boolean applyBottomPadding) {
        int margin = this.getSafeMargin(defaultMargin);
        int padding = this.getSafePadding(defaultPadding);
        if (margin >= 0 && this.margin > 0) {
            ViewParent viewParent = view.getParent();
            if (viewParent instanceof ViewGroup) {
                ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                    ((ViewGroup.MarginLayoutParams) layoutParams).
                            setMargins(applyLeft ? margin : 0, applyTop ? margin : 0,
                                    applyRight ? margin : 0, applyBottom ? margin : 0);
                }
            }
        }
        if (padding >= 0 && !(view instanceof ImageView) && this.padding > 0) {
            view.setPadding(applyLeftPadding ? padding : 0, applyTopPadding ? padding : 0,
                    applyRightPadding ? padding : 0, applyBottomPadding ? padding : 0);
        }
        if (view instanceof TextView) {
            if (!TextUtils.isEmpty(textColor)) {
                ((TextView) view).setTextColor(this.getNativeTextColor());
            }
            ((TextView) view).setGravity(this.getAsNativeAlignment());
        }
        if (!TextUtils.isEmpty(backgroundColor)) {
            view.setBackgroundColor(this.getNativeBkgColor());
        }
    }

    public void adjustTitlePaddingForOverlayWithAdjacentTitleAndDesc(int paddingWhenNoDesc, int paddingWhenDesc,
                                                                     TextView txtTitle, TextView txtDescription,
                                                                     SectionItem sectionItem) {
        txtTitle.setPadding(txtTitle.getPaddingLeft(), txtTitle.getPaddingTop(),
                txtTitle.getPaddingRight(),
                txtDescription != null && sectionItem.hasDescription() ? paddingWhenDesc : paddingWhenNoDesc);
    }

    public void adjustDescPaddingForOverlayWithAdjacentTitleAndDesc(int paddingWhenNoTitle, int paddingWhenTitle,
                                                                    TextView txtTitle, TextView txtDescription,
                                                                    SectionItem sectionItem) {
        txtDescription.setPadding(txtDescription.getPaddingLeft(),
                txtTitle != null && sectionItem.hasTitle() ? paddingWhenTitle : paddingWhenNoTitle,
                txtDescription.getPaddingRight(), txtDescription.getPaddingBottom());
    }
}
