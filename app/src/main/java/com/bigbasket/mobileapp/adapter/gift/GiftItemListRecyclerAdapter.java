package com.bigbasket.mobileapp.adapter.gift;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.handler.OnCompoundDrawableClickListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.gift.GiftOperationAware;
import com.bigbasket.mobileapp.model.product.gift.Gift;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;


public class GiftItemListRecyclerAdapter<T extends GiftOperationAware> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_FOOTER = 0;
    private static final int VIEW_TYPE_GIFT = 1;
    private String baseImgUrl;
    private Gift gift;
    private T context;
    private Typeface faceRupee;
    private Typeface faceRobotoRegular;
    private Typeface faceRobotoBold;

    public GiftItemListRecyclerAdapter(T context, Gift gift, String baseImgUrl) {
        this.context = context;
        this.gift = gift;
        this.baseImgUrl = baseImgUrl;
        Context ctx = ((ActivityAware) context).getCurrentActivity();
        this.faceRupee = FontHolder.getInstance(ctx).getFaceRupee();
        this.faceRobotoBold = FontHolder.getInstance(ctx).getFaceRobotoBold();
        this.faceRobotoRegular = FontHolder.getInstance(ctx).getFaceRobotoRegular();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = ((ActivityAware) this.context).getCurrentActivity();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType) {
            case VIEW_TYPE_FOOTER:
                View row = inflater.inflate(R.layout.uiv3_gift_list_footer, viewGroup, false);
                return new GiftItemFooterViewHolder(row, faceRobotoRegular, faceRobotoBold);
            default:
                row = inflater.inflate(R.layout.uiv3_gift_item_row, viewGroup, false);
                return new GiftItemViewHolder(row, FontHolder.getInstance(context).getFaceRobotoRegular());
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == gift.getGiftItems().size()) {
            return VIEW_TYPE_FOOTER;
        }
        return VIEW_TYPE_GIFT;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder vholder, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_GIFT) {
            GiftItemViewHolder holder = (GiftItemViewHolder) vholder;
            Context context = ((ActivityAware) this.context).getCurrentActivity();
            ImageView imgProduct = holder.getImgProduct();
            TextView txtProductDesc = holder.getTxtProductDesc();
            TextView txtProductQuantity = holder.getTxtProductQty();
            TextView txtProductBrand = holder.getTxtProductBrand();
            TextView txtProductGiftWrapPrice = holder.getTxtProductGiftWrapPrice();
            TextView txtProductGiftWrapTotalPrice = holder.getTxtProductGiftWrapTotalPrice();
            TextView txtProductGiftWrapQuantityStatus = holder.getTxtProductGiftWrapQuantityStatus();
            GiftItem giftItem = gift.getGiftItems().get(position);
            final int reservedQty = giftItem.getReservedQty();
            int allowedQty = giftItem.getQuantity();

            if (giftItem.isReadOnly()) {
                txtProductGiftWrapQuantityStatus.setCompoundDrawablesWithIntrinsicBounds(
                        null, null, null, null);
                txtProductGiftWrapQuantityStatus.setOnTouchListener(null);
            } else {
                if (reservedQty == allowedQty) {
                    txtProductGiftWrapQuantityStatus.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.minus_red), null, null, null);
                } else if (reservedQty == 0) {
                    txtProductGiftWrapQuantityStatus.setCompoundDrawablesWithIntrinsicBounds(
                            null, null, ContextCompat.getDrawable(context, R.drawable.plus_red), null);
                } else {
                    txtProductGiftWrapQuantityStatus.setCompoundDrawablesWithIntrinsicBounds(
                            ContextCompat.getDrawable(context, R.drawable.minus_red), null,
                            ContextCompat.getDrawable(context, R.drawable.plus_red), null);
                }
                Integer[] drawableTypes = new Integer[2];
                if (txtProductGiftWrapQuantityStatus.getCompoundDrawables()[OnCompoundDrawableClickListener.DRAWABLE_LEFT] != null) {
                    drawableTypes[0] = OnCompoundDrawableClickListener.DRAWABLE_LEFT;
                }
                if (txtProductGiftWrapQuantityStatus.getCompoundDrawables()[OnCompoundDrawableClickListener.DRAWABLE_RIGHT] != null) {
                    drawableTypes[drawableTypes[0] != null ? 1 : 0] = OnCompoundDrawableClickListener.DRAWABLE_RIGHT;
                }
                if (drawableTypes[0] != null) {
                    txtProductGiftWrapQuantityStatus.setOnTouchListener(new OnCompoundDrawableClickListener(drawableTypes) {
                        @Override
                        public void onRightDrawableClicked() {
                            GiftItemListRecyclerAdapter.this.context.updateGiftItemQty(position, reservedQty + 1);
                        }

                        @Override
                        public void onLeftDrawableClicked() {
                            GiftItemListRecyclerAdapter.this.context.updateGiftItemQty(position, reservedQty - 1);
                        }
                    });
                }
            }

            UIUtil.displayProductImage(baseImgUrl, giftItem.getImageUrl(), imgProduct);
            txtProductDesc.setText(giftItem.getDescription());
            txtProductBrand.setText(giftItem.getBrand());
            txtProductQuantity.setText(context.getString(R.string.qtyInBasket) + " " + reservedQty);

            SpannableString giftWrapChargeSpannable = UIUtil.asRupeeSpannable(context.getString(R.string.giftWrapCharge) + " ",
                    UIUtil.formatAsMoney(giftItem.getGiftWrapCharge()) +
                            context.getString(R.string.rupeeTerminator) + " " + context.getString(R.string.perItem),
                    faceRupee);
            txtProductGiftWrapPrice.setText(giftWrapChargeSpannable);

            txtProductGiftWrapQuantityStatus.setText(giftItem.getReservedQty() + " " + context.getString(R.string.qtyToGiftWrapOrMsg));
            SpannableString giftWrapTotalChargeSpannable = UIUtil.asRupeeSpannable(context.getString(R.string.giftWrapChargeTotal) + " ",
                    UIUtil.formatAsMoney(giftItem.getGiftWrapCharge() * reservedQty) +
                            context.getString(R.string.rupeeTerminator), faceRupee);
            txtProductGiftWrapTotalPrice.setText(giftWrapTotalChargeSpannable);
        } else {
            UIUtil.setUpGiftItemListFooter(gift, (GiftItemFooterViewHolder) vholder,
                    ((ActivityAware) context).getCurrentActivity());
        }
    }

    public static class GiftItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView txtProductDesc;
        private TextView txtProductQuantity;
        private TextView txtProductBrand;
        private TextView txtProductGiftWrapPrice;
        private TextView txtProductGiftWrapTotalPrice;
        private TextView txtProductGiftWrapQuantityStatus;
        private Typeface typeface;

        public GiftItemViewHolder(View itemView, Typeface typeface) {
            super(itemView);
            this.typeface = typeface;
        }

        public ImageView getImgProduct() {
            if (imgProduct == null) {
                imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
            }
            return imgProduct;
        }

        public TextView getTxtProductDesc() {
            if (txtProductDesc == null) {
                txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
                txtProductDesc.setTypeface(typeface);
            }
            return txtProductDesc;
        }

        public TextView getTxtProductQty() {
            if (txtProductQuantity == null) {
                txtProductQuantity = (TextView) itemView.findViewById(R.id.txtProductQuantity);
                txtProductQuantity.setTypeface(typeface);
            }
            return txtProductQuantity;
        }

        public TextView getTxtProductBrand() {
            if (txtProductBrand == null) {
                txtProductBrand = (TextView) itemView.findViewById(R.id.txtProductBrand);
                txtProductBrand.setTypeface(typeface);
            }
            return txtProductBrand;
        }

        public TextView getTxtProductGiftWrapPrice() {
            if (txtProductGiftWrapPrice == null) {
                txtProductGiftWrapPrice = (TextView) itemView.findViewById(R.id.txtProductGiftWrapPrice);
                txtProductGiftWrapPrice.setTypeface(typeface);
            }
            return txtProductGiftWrapPrice;
        }

        public TextView getTxtProductGiftWrapTotalPrice() {
            if (txtProductGiftWrapTotalPrice == null) {
                txtProductGiftWrapTotalPrice = (TextView) itemView.findViewById(R.id.txtProductGiftWrapTotalPrice);
                txtProductGiftWrapTotalPrice.setTypeface(typeface);
            }
            return txtProductGiftWrapTotalPrice;
        }

        public TextView getTxtProductGiftWrapQuantityStatus() {
            if (txtProductGiftWrapQuantityStatus == null) {
                txtProductGiftWrapQuantityStatus = (TextView) itemView.findViewById(R.id.txtProductGiftWrapQuantityStatus);
                txtProductGiftWrapQuantityStatus.setTypeface(typeface);
            }
            return txtProductGiftWrapQuantityStatus;
        }
    }

    public static class GiftItemFooterViewHolder extends RecyclerView.ViewHolder {
        private TextView lblTotalGiftItems;
        private TextView txtCountGiftItems;
        private TextView lblGiftItemTotalPrice;
        private TextView txtGiftItemTotalPrice;
        private Typeface typefaceText;
        private Typeface typefacePrice;

        public GiftItemFooterViewHolder(View itemView, Typeface typefaceText,
                                        Typeface typefacePrice) {
            super(itemView);
            this.typefaceText = typefaceText;
            this.typefacePrice = typefacePrice;
        }

        public TextView getLblTotalGiftItems() {
            if (lblTotalGiftItems == null) {
                lblTotalGiftItems = (TextView) itemView.findViewById(R.id.lblTotalGiftItems);
                lblTotalGiftItems.setTypeface(typefaceText);
            }
            return lblTotalGiftItems;
        }

        public TextView getTxtCountGiftItems() {
            if (txtCountGiftItems == null) {
                txtCountGiftItems = (TextView) itemView.findViewById(R.id.txtCountGiftItems);
                txtCountGiftItems.setTypeface(typefacePrice);
            }
            return txtCountGiftItems;
        }

        public TextView getLblGiftItemTotalPrice() {
            if (lblGiftItemTotalPrice == null) {
                lblGiftItemTotalPrice = (TextView) itemView.findViewById(R.id.lblGiftItemTotalPrice);
                lblGiftItemTotalPrice.setTypeface(typefaceText);
            }
            return lblGiftItemTotalPrice;
        }

        public TextView getTxtGiftItemTotalPrice() {
            if (txtGiftItemTotalPrice == null) {
                txtGiftItemTotalPrice = (TextView) itemView.findViewById(R.id.txtGiftItemTotalPrice);
                txtGiftItemTotalPrice.setTypeface(typefacePrice);
            }
            return txtGiftItemTotalPrice;
        }
    }

    @Override
    public int getItemCount() {
        return gift.getGiftItems().size() + 1;
    }
}
