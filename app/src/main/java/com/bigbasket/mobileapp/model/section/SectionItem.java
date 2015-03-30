package com.bigbasket.mobileapp.model.section;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.order.PayuResponse;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public class SectionItem extends BaseSectionTextItem implements Parcelable, Serializable {

    public static final int VIEW_TITLE_DESC_IMG_VERTICAL = 0;
    public static final int VIEW_TITLE_IMG_DESC_VERTICAL = 1;
    public static final int VIEW_IMG_TITLE_DESC_VERTICAL = 2;
    public static final int VIEW_TITLE_DESC_VERTICAL = 3;
    public static final int VIEW_TITLE_DESC_IMG_HORIZONTAL = 4;
    public static final int VIEW_TITLE_IMG_DESC_HORIZONTAL = 5;
    public static final int VIEW_IMG_TITLE_DESC_HORIZONTAL = 6;
    public static final int VIEW_TITLE_DESC_HORIZONTAL = 7;
    public static final int VIEW_IMG = 8;
    public static final int VIEW_UNKNOWN = 9;

    private String image;

    @SerializedName(Constants.RENDERING_ID)
    private int renderingId;

    @SerializedName(Constants.DESTINATION)
    private DestinationInfo destinationInfo;

    public SectionItem(SectionTextItem title, SectionTextItem description, String image, int renderingId, DestinationInfo destinationInfo) {
        super(title, description);
        this.image = image;
        this.renderingId = renderingId;
        this.destinationInfo = destinationInfo;
    }

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

    public void displayImage(ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(image)) {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
            return;
        }
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
                imageView.setImageDrawable(null);
                imageView.setVisibility(View.GONE);
            }
        } else {
            UIUtil.displayAsyncImage(imageView, image);
        }
    }

    public int getItemViewType(Renderer renderer) {
        if (renderer != null) {
            if (renderer.getOrientation() == Renderer.VERTICAL) {
                if (TextUtils.isEmpty(image)) {
                    return VIEW_TITLE_DESC_VERTICAL;
                } else if ((getTitle() == null || TextUtils.isEmpty(getTitle().getText())) &&
                        (getDescription() == null || TextUtils.isEmpty(getDescription().getText()))) {
                    return VIEW_IMG;
                }
                if (TextUtils.isEmpty(renderer.getOrdering())) {
                    return VIEW_TITLE_IMG_DESC_VERTICAL;
                }
                switch (renderer.getOrdering()) {
                    case Renderer.IMG_TITLE_DESC:
                        return VIEW_IMG_TITLE_DESC_VERTICAL;
                    case Renderer.TITLE_DESC_IMG:
                        return VIEW_TITLE_DESC_IMG_VERTICAL;
                    case Renderer.TITLE_IMG_DESC:
                        return VIEW_TITLE_IMG_DESC_VERTICAL;
                }
            } else if (renderer.getOrientation() == Renderer.HORIZONTAL) {
                if (TextUtils.isEmpty(image)) {
                    return VIEW_TITLE_DESC_HORIZONTAL;
                } else if ((getTitle() == null || TextUtils.isEmpty(getTitle().getText())) &&
                        (getDescription() == null || TextUtils.isEmpty(getDescription().getText()))) {
                    return VIEW_IMG;
                }
                if (TextUtils.isEmpty(renderer.getOrdering())) {
                    return VIEW_TITLE_IMG_DESC_HORIZONTAL;
                }
                switch (renderer.getOrdering()) {
                    case Renderer.IMG_TITLE_DESC:
                        return VIEW_IMG_TITLE_DESC_HORIZONTAL;
                    case Renderer.TITLE_DESC_IMG:
                        return VIEW_TITLE_DESC_IMG_HORIZONTAL;
                    case Renderer.TITLE_IMG_DESC:
                        return VIEW_TITLE_IMG_DESC_HORIZONTAL;
                }
            }
        } else {
            if (TextUtils.isEmpty(image)) {
                return VIEW_TITLE_DESC_VERTICAL;
            } else if ((getTitle() == null || TextUtils.isEmpty(getTitle().getText())) &&
                    (getDescription() == null || TextUtils.isEmpty(getDescription().getText()))) {
                return VIEW_IMG;
            }
            return VIEW_TITLE_IMG_DESC_VERTICAL;
        }
        return VIEW_UNKNOWN;
    }

    @LayoutRes
    public static int getLayoutResId(int viewType) {
        switch (viewType) {
            case VIEW_TITLE_DESC_VERTICAL:
                return R.layout.section_text_desc;
            case VIEW_TITLE_DESC_IMG_VERTICAL:
                return R.layout.section_text_desc_img;
            case VIEW_TITLE_IMG_DESC_VERTICAL:
                return R.layout.section_text_img_desc;
            case VIEW_IMG_TITLE_DESC_VERTICAL:
                return R.layout.section_img_text_desc;
            case VIEW_TITLE_DESC_HORIZONTAL:
                return R.layout.section_text_desc_horizontal;
            case VIEW_TITLE_DESC_IMG_HORIZONTAL:
                return R.layout.section_text_desc_img_horizontal;
            case VIEW_TITLE_IMG_DESC_HORIZONTAL:
                return R.layout.section_text_img_desc_horizontal;
            case VIEW_IMG_TITLE_DESC_HORIZONTAL:
                return R.layout.section_img_text_desc_horizontal;
            case VIEW_IMG:
                return R.layout.section_img;
            default:
                return 0;
        }
    }

    public int getHeight(Context context, Renderer renderer) {
        if (renderer != null) {
            if (renderer.getOrientation() == Renderer.VERTICAL) {
                if (TextUtils.isEmpty(image)) {
                    return (int) context.getResources().getDimension(R.dimen.vertical_tile_min_height);
                } else {
                    return (int) context.getResources().getDimension(R.dimen.vertical_tile_with_img_min_height);
                }
            } else if (renderer.getOrientation() == Renderer.HORIZONTAL) {
                if (TextUtils.isEmpty(image)) {
                    return (int) context.getResources().getDimension(R.dimen.horizontal_tile_min_height);
                } else {
                    return (int) context.getResources().getDimension(R.dimen.horizontal_tile_min_height);
                }
            }
        } else {
            if (TextUtils.isEmpty(image)) {
                return (int) context.getResources().getDimension(R.dimen.vertical_tile_min_height);
            } else {
                return (int) context.getResources().getDimension(R.dimen.vertical_tile_with_img_min_height);
            }
        }
        return 0;
    }
}
