package com.bigbasket.mobileapp.common;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;

/**
 * Created by manu on 28/9/15.
 */
public class GiftItemViewHolder extends RecyclerView.ViewHolder {
    private ImageView imgProduct;
    private TextView txtProductDesc;
    private TextView txtProductBrand;
    private TextView txtProductQuantity;
    private TextView txtProductGiftWrapPrice;
    private ImageButton btnDecreaseQuantity;
    private TextView txtProductGiftWrapQuantityStatus;
    private ImageButton btnIncreaseQuantity;
    private TextView txtProductGiftWrapTotalPrice;

    public ImageView getImgProduct() {
        if (imgProduct == null) {
            imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
        }
        return imgProduct;
    }

    public TextView getTxtProductDesc() {
        if (txtProductDesc == null) {
            txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
        }
        return txtProductDesc;
    }

    public TextView getTxtProductBrand() {
        if (txtProductBrand == null) {
            txtProductBrand = (TextView) itemView.findViewById(R.id.txtProductBrand);
        }
        return txtProductBrand;
    }

    public TextView getTxtProductQuantity() {
        if (txtProductQuantity == null) {
            txtProductQuantity = (TextView) itemView.findViewById(R.id.txtProductQuantity);
        }
        return txtProductQuantity;

    }

    public TextView getTxtProductGiftWrapPrice() {
        if (txtProductGiftWrapPrice == null) {
            txtProductGiftWrapPrice = (TextView) itemView.findViewById(R.id.txtProductGiftWrapPrice);
        }
        return txtProductGiftWrapPrice;
    }

    public ImageButton getBtnDecreaseQuantity() {
        if (btnDecreaseQuantity == null) {
            btnDecreaseQuantity = (ImageButton) itemView.findViewById(R.id.btnDecreaseQuantity);
        }
        return btnDecreaseQuantity;
    }

    public TextView getTxtProductGiftWrapQuantityStatus() {
        if (txtProductGiftWrapQuantityStatus == null) {
            txtProductGiftWrapQuantityStatus = (TextView) itemView.findViewById(R.id.txtProductGiftWrapQuantityStatus);
        }
        return txtProductGiftWrapQuantityStatus;
    }

    public ImageButton getBtnIncreaseQuantity() {
        if (btnIncreaseQuantity == null) {
            btnIncreaseQuantity = (ImageButton) itemView.findViewById(R.id.btnIncreaseQuantity);
        }
        return btnIncreaseQuantity;
    }

    public TextView getTxtProductGiftWrapTotalPrice() {
        if (txtProductGiftWrapTotalPrice == null) {
            txtProductGiftWrapTotalPrice = (TextView) itemView.findViewById(R.id.txtProductGiftWrapTotalPrice);
        }
        return txtProductGiftWrapTotalPrice;
    }

    public GiftItemViewHolder(View itemView) {
        super(itemView);
    }
}
