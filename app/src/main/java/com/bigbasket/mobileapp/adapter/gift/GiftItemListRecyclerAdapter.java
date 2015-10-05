package com.bigbasket.mobileapp.adapter.gift;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.gift.GiftOperationAware;
import com.bigbasket.mobileapp.model.product.gift.GiftItem;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.List;


public class GiftItemListRecyclerAdapter<T extends GiftOperationAware> extends RecyclerView.Adapter<GiftItemListRecyclerAdapter.GiftItemViewHolder> {

    private String baseImgUrl;
    private List<GiftItem> giftItems;
    private T context;
    private Typeface faceRupee;

    public GiftItemListRecyclerAdapter(T context, List<GiftItem> giftItems, String baseImgUrl) {
        this.context = context;
        this.giftItems = giftItems;
        this.baseImgUrl = baseImgUrl;
        this.faceRupee = FontHolder.getInstance(((ActivityAware) context).getCurrentActivity()).getFaceRupee();
    }

    @Override
    public GiftItemListRecyclerAdapter.GiftItemViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = ((ActivityAware) this.context).getCurrentActivity();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.uiv3_gift_item_row, viewGroup, false);
        return new GiftItemViewHolder(row, FontHolder.getInstance(context).getFaceRobotoRegular());
    }

    @Override
    public void onBindViewHolder(GiftItemListRecyclerAdapter.GiftItemViewHolder holder, final int position) {
        Context context = ((ActivityAware) this.context).getCurrentActivity();
        ImageView imgProduct = holder.getImgProduct();
        TextView txtProductDesc = holder.getTxtProductDesc();
        TextView txtProductQuantity = holder.getTxtProductQty();
        TextView txtProductBrand = holder.getTxtProductBrand();
        TextView txtProductGiftWrapPrice = holder.getTxtProductGiftWrapPrice();
        TextView txtDecreaseQuantity = holder.getTxtDecreaseQuantity();
        TextView txtIncreaseQuantity = holder.getTxtIncreaseQuantity();
        TextView txtProductGiftWrapTotalPrice = holder.getTxtProductGiftWrapTotalPrice();
        TextView txtProductGiftWrapQuantityStatus = holder.getTxtProductGiftWrapQuantityStatus();
        GiftItem giftItem = giftItems.get(position);
        final int reservedQty = giftItem.getReservedQty();
        int allowedQty = giftItem.getQuantity();

        if (giftItem.isReadOnly()) {
            txtIncreaseQuantity.setVisibility(View.GONE);
            txtDecreaseQuantity.setVisibility(View.GONE);
        } else {
            if (reservedQty == allowedQty) {
                txtIncreaseQuantity.setVisibility(View.GONE);
                txtDecreaseQuantity.setVisibility(View.VISIBLE);
            } else if (reservedQty == 0) {
                txtIncreaseQuantity.setVisibility(View.VISIBLE);
                txtDecreaseQuantity.setVisibility(View.GONE);
            } else {
                txtIncreaseQuantity.setVisibility(View.VISIBLE);
                txtDecreaseQuantity.setVisibility(View.VISIBLE);
            }
            if (txtDecreaseQuantity.getVisibility() == View.VISIBLE) {
                txtDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GiftItemListRecyclerAdapter.this.context.updateGiftItemQty(position, reservedQty - 1);
                    }
                });
            } else {
                txtDecreaseQuantity.setOnClickListener(null);
            }
            if (txtIncreaseQuantity.getVisibility() == View.VISIBLE) {
                txtIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GiftItemListRecyclerAdapter.this.context.updateGiftItemQty(position, reservedQty + 1);
                    }
                });
            } else {
                txtIncreaseQuantity.setOnClickListener(null);
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
    }

    public static class GiftItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView txtProductDesc;
        private TextView txtProductQuantity;
        private TextView txtProductBrand;
        private TextView txtProductGiftWrapPrice;
        private TextView txtDecreaseQuantity;
        private TextView txtIncreaseQuantity;
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

        public TextView getTxtDecreaseQuantity() {
            if (txtDecreaseQuantity == null) {
                txtDecreaseQuantity = (TextView) itemView.findViewById(R.id.txtDecreaseQuantity);
            }
            return txtDecreaseQuantity;
        }

        public TextView getTxtIncreaseQuantity() {
            if (txtIncreaseQuantity == null) {
                txtIncreaseQuantity = (TextView) itemView.findViewById(R.id.txtIncreaseQuantity);
            }
            return txtIncreaseQuantity;
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

    @Override
    public int getItemCount() {
        return giftItems.size();
    }
}
