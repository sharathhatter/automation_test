package com.bigbasket.mobileapp.model.section;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
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
import java.util.ArrayList;
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
    public static final int VIEW_TITLE_DESC_IMG_VERTICAL_OVERLAY = 9;
    public static final int VIEW_TITLE_IMG_DESC_VERTICAL_OVERLAY = 10;
    public static final int VIEW_IMG_TITLE_DESC_VERTICAL_OVERLAY = 11;
    public static final int VIEW_UNKNOWN = 12;
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
    private String image;

    @SerializedName(Constants.IMAGE_NAME)
    private String imageName;

    @SerializedName(Constants.RENDERING_ID)
    private int renderingId;

    @SerializedName(Constants.DESTINATION)
    private DestinationInfo destinationInfo;

    @SerializedName(Constants.SUB_ITEMS)
    private ArrayList<SectionItem> subSectionItems;

    @SerializedName(Constants.IMAGE_PARAMS)
    private ImageParams imageParams;

    public SectionItem(SectionTextItem title, SectionTextItem description, String image,
                       int renderingId, DestinationInfo destinationInfo) {
        super(title, description);
        this.image = image;
        this.renderingId = renderingId;
        this.destinationInfo = destinationInfo;
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
        boolean wasSubSectionItemNull = source.readByte() == (byte) 1;
        if (!wasSubSectionItemNull) {
            subSectionItems = new ArrayList<>();
            source.readTypedList(subSectionItems, SectionItem.CREATOR);
        }
        boolean wasImgNameNull = source.readByte() == (byte) 1;
        if (!wasImgNameNull) {
            imageName = source.readString();
        }
        boolean wasImgParamsNull = source.readByte() == (byte) 1;
        if (!wasImgParamsNull) {
            imageParams = source.readParcelable(SectionItem.class.getClassLoader());
        }
    }

    public static int getViewTypeCount() {
        // Update this number once you add/remove any of the above view-types
        return 13;
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
            case VIEW_IMG_TITLE_DESC_VERTICAL_OVERLAY:
                return R.layout.section_img_text_desc_overlay;
            case VIEW_TITLE_DESC_IMG_VERTICAL_OVERLAY:
                return R.layout.section_text_desc_img_overlay;
            case VIEW_TITLE_IMG_DESC_VERTICAL_OVERLAY:
                return R.layout.section_text_img_desc_overlay;
            default:
                return 0;
        }
    }

    public String getImage() {
        return image;
    }

    public String getImageName() {
        return imageName;
    }

    public boolean hasImage() {
        return !TextUtils.isEmpty(imageName) || !TextUtils.isEmpty(image);
    }

    public int getRenderingId() {
        return renderingId;
    }

    public DestinationInfo getDestinationInfo() {
        return destinationInfo;
    }

    public ArrayList<SectionItem> getSubSectionItems() {
        return subSectionItems;
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
        boolean wasSubSectionItemNull = subSectionItems == null;
        dest.writeByte(wasSubSectionItemNull ? (byte) 1 : (byte) 0);
        if (!wasSubSectionItemNull) {
            dest.writeTypedList(subSectionItems);
        }
        boolean wasImageNameNull = imageName == null;
        dest.writeByte(wasImageNameNull ? (byte) 1 : (byte) 0);
        if (!wasImageNameNull) {
            dest.writeString(imageName);
        }
        boolean wasImageParamsNull = imageParams == null;
        dest.writeByte(wasImageParamsNull ? (byte) 1 : (byte) 0);
        if (!wasImageParamsNull) {
            dest.writeParcelable(imageParams, flags);
        }
    }

    public void displayImage(Context context, @Nullable String baseImgUrl, ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(image)) {
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
        } else if (!TextUtils.isEmpty(imageName) && !TextUtils.isEmpty(baseImgUrl)) {
            UIUtil.displayAsyncImage(imageView,
                    constructImageUrl(context, baseImgUrl));
        } else {
            imageView.setImageDrawable(null);
            imageView.setVisibility(View.GONE);
        }
    }

    public String constructImageUrl(Context context, String baseImgUrl) {
        return baseImgUrl + UIUtil.getScreenDensity(context) + "/" + imageName;
    }

    public int getItemViewType(Renderer renderer, String sectionType) {
        if (renderer != null) {
            if (renderer.getOrientation() == Renderer.VERTICAL) {
                if (!hasImage()) {
                    return VIEW_TITLE_DESC_VERTICAL;
                } else if ((getTitle() == null || TextUtils.isEmpty(getTitle().getText())) &&
                        (getDescription() == null || TextUtils.isEmpty(getDescription().getText()))) {
                    return VIEW_IMG;
                }
                if (TextUtils.isEmpty(renderer.getOrdering())) {
                    return sectionType.equals(Section.PRODUCT_CAROUSEL) ?
                            VIEW_IMG_TITLE_DESC_VERTICAL : VIEW_IMG_TITLE_DESC_VERTICAL_OVERLAY;
                }
                switch (renderer.getOrdering()) {
                    case Renderer.IMG_TITLE_DESC:
                        return sectionType.equals(Section.PRODUCT_CAROUSEL) ?
                                VIEW_IMG_TITLE_DESC_VERTICAL : VIEW_IMG_TITLE_DESC_VERTICAL_OVERLAY;
                    case Renderer.TITLE_DESC_IMG:
                        return sectionType.equals(Section.PRODUCT_CAROUSEL) ?
                                VIEW_TITLE_DESC_IMG_VERTICAL : VIEW_TITLE_DESC_IMG_VERTICAL_OVERLAY;
                    case Renderer.TITLE_IMG_DESC:
                        return sectionType.equals(Section.PRODUCT_CAROUSEL) ?
                                VIEW_TITLE_IMG_DESC_VERTICAL : VIEW_TITLE_IMG_DESC_VERTICAL_OVERLAY;
                }
            } else if (renderer.getOrientation() == Renderer.HORIZONTAL) {
                if (!hasImage()) {
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
            if (!hasImage()) {
                return VIEW_TITLE_DESC_VERTICAL;
            } else if ((getTitle() == null || TextUtils.isEmpty(getTitle().getText())) &&
                    (getDescription() == null || TextUtils.isEmpty(getDescription().getText()))) {
                return VIEW_IMG;
            }
            return sectionType.equals(Section.PRODUCT_CAROUSEL) ?
                    VIEW_IMG_TITLE_DESC_VERTICAL : VIEW_IMG_TITLE_DESC_VERTICAL_OVERLAY;
        }
        return VIEW_UNKNOWN;
    }

    public boolean isOverlayWithAdjacentTitleDesc(int viewType) {
        return viewType == VIEW_IMG_TITLE_DESC_VERTICAL_OVERLAY ||
                viewType == VIEW_TITLE_DESC_IMG_VERTICAL_OVERLAY;
    }

    private int getActualHeight(Context context) {
        if (imageParams != null) {
            return (int) (imageParams.getHeight() * UIUtil.getDpiCoefficient(context));
        }
        return 0;
    }

    private int getEstimatedHeight(Context context, @Nullable Renderer renderer) {
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

    public int getHeight(Context context, @Nullable Renderer renderer) {
        int height = getActualHeight(context);
        if (height <= 0) {
            height = getEstimatedHeight(context, renderer);
        }
        return height;
    }

    public boolean doesViewRequireMinHeight(int viewType) {
        return viewType != VIEW_IMG;
    }

    public boolean hasTitle() {
        return getTitle() != null && !TextUtils.isEmpty(getTitle().getText());
    }

    public boolean hasDescription() {
        return getDescription() != null && !TextUtils.isEmpty(getDescription().getText());
    }

    @Override
    public String toString() {
        return getTitle() != null ? (TextUtils.isEmpty(getTitle().getText()) ? "" : getTitle().getText()) : "";
    }
}
