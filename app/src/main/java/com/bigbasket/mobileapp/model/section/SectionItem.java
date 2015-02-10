package com.bigbasket.mobileapp.model.section;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.util.Constants;
import com.google.gson.annotations.SerializedName;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class SectionItem extends BaseSectionTextItem implements Parcelable {

    public static final int VIEW_TYPE_TEXT_IMG = 0;
    public static final int VIEW_TYPE_TEXT_DESC = 1;
    public static final int VIEW_TYPE_TEXT_ONLY = 2;

    private String image;

    @SerializedName(Constants.RENDERING_ID)
    private int renderingId;

    @SerializedName(Constants.DESTINATION)
    private DestinationInfo destinationInfo;

    public String getImage() {
        return image;
    }

    public int getRenderingId() {
        return renderingId;
    }

    public DestinationInfo getDestinationInfo() {
        return destinationInfo;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        boolean _wasImageNull = image == null;
        dest.writeByte(_wasImageNull ? (byte) 1 : (byte) 0);
        if (!_wasImageNull) {
            dest.writeString(image);
        }
        dest.writeInt(renderingId);
        boolean wasDestNull = destinationInfo == null;
        dest.writeByte(wasDestNull ? (byte) 1 : (byte) 0);
        if (!wasDestNull) {
            dest.writeParcelable(destinationInfo, flags);
        }
    }

    public SectionItem(Parcel source) {
        super(source);
        boolean _wasImageNull = source.readByte() == (byte) 1;
        if (!_wasImageNull) {
            image = source.readString();
        }
        renderingId = source.readInt();
        boolean wasDestNull = source.readByte() == (byte) 1;
        if (!wasDestNull) {
            destinationInfo = source.readParcelable(SectionItem.class.getClassLoader());
        }
    }

    public static final Parcelable.Creator<SectionItem> CREATOR = new Parcelable.Creator<SectionItem>() {
        @Override
        public SectionItem createFromParcel(Parcel source) {
            return new SectionItem(source);
        }

        @Override
        public SectionItem[] newArray(int size) {
            return new SectionItem[size];
        }
    };

    public int getViewType() {
        SectionTextItem titleTextItem = getTitle();
        SectionTextItem descTextItem = getDescription();

        boolean isTitlePresent = titleTextItem != null && !TextUtils.isEmpty(titleTextItem.getText());
        boolean isDescPresent = descTextItem != null && !TextUtils.isEmpty(descTextItem.getText());
        boolean isImgPresent = !TextUtils.isEmpty(image);

        if (isImgPresent) {
            return VIEW_TYPE_TEXT_IMG;
        } else if (isTitlePresent && isDescPresent) {
            return VIEW_TYPE_TEXT_DESC;
        }
        return VIEW_TYPE_TEXT_ONLY;
    }

    public void displayImage(ImageView imageView) {
        if (TextUtils.isEmpty(image)) return;
        if (image.startsWith(Constants.LOCAL_RES_PREFIX)) {
            try {
                URI uri = new URI(image);
                Map<String, String> queryMap = PayuResponse.getQueryMap(uri.getQuery());
                String name = queryMap.get(Constants.NAME);
                if (!TextUtils.isEmpty(name)) {
                    Class res = R.drawable.class;
                    Field field = res.getField(name);
                    int drawableId = field.getInt(null);
                    imageView.setImageResource(drawableId);
                }
            } catch (URISyntaxException | NoSuchFieldException | IllegalAccessException e) {
                imageView.setImageResource(R.drawable.image_404);
            }
        } else {
            ImageLoader.getInstance().displayImage(image, imageView);
        }
    }
}
