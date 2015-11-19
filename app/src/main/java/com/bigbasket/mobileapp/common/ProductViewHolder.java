package com.bigbasket.mobileapp.common;


import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;

public class ProductViewHolder extends RecyclerView.ViewHolder {
    private ImageView imgProduct;
    private Button btnMorePackSizes;
    private TextView txtPackageDesc;
    private TextView txtProductDesc;
    private TextView txtProductBrand;
    private TextView txtSalePrice;
    private TextView txtMrp;
    private ImageView imgPromoStar;
    private TextView txtPromoDesc;
    private TextView txtPromoAddSavings;
    private ImageView imgAddToBasket;
    private View viewDecBasketQty;
    private TextView txtInBasket;
    private View viewIncBasketQty;
    private TextView txtOutOfStockORNotForSale;
    private ImageView imgProductOverflowAction;
    private EditText editTextQty;
    private RadioGroup radioGroupExpress;
    private TextView txtExpressMsg;
    private ViewGroup layoutExpressMsg;
    private TextView txtGiftMsg;
    private ImageView imgGiftIcon;
    private ImageView imgStoreIcon;
    private RelativeLayout storeIconLayout;
    private View.OnClickListener specialityShopIconClickListener;
    private View.OnClickListener promoClickListener;
    private View.OnClickListener productOnClickListener;
    private View.OnClickListener brandPageListener;

    public ProductViewHolder(View itemView, View.OnClickListener specialityShopIconClickListener,
                             View.OnClickListener promoClickListener,
                             View.OnClickListener productOnClickListener,
                             View.OnClickListener brandPageListener) {
        super(itemView);
        this.specialityShopIconClickListener = specialityShopIconClickListener;
        this.promoClickListener = promoClickListener;
        this.productOnClickListener = productOnClickListener;
        this.brandPageListener = brandPageListener;
    }

    public ImageView getImgProduct() {
        if (imgProduct == null) {
            imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
            imgProduct.setOnClickListener(productOnClickListener);
        }
        return imgProduct;
    }

    public TextView getTxtProductBrand() {
        if (txtProductBrand == null) {
            txtProductBrand = (TextView) itemView.findViewById(R.id.txtProductBrand);
            txtProductBrand.setOnClickListener(brandPageListener);
        }
        return txtProductBrand;
    }

    public ImageView getImgPromoStar() {
        if (imgPromoStar == null) {
            imgPromoStar = (ImageView) itemView.findViewById(R.id.imgPromoStar);
            imgPromoStar.setOnClickListener(promoClickListener);
        }
        return imgPromoStar;
    }

    public Button getBtnMorePackSizes() {
        if (btnMorePackSizes == null) {
            btnMorePackSizes = (Button) itemView.findViewById(R.id.btnMorePackSizes);
        }
        return btnMorePackSizes;
    }

    public TextView getTxtPackageDesc() {
        if (txtPackageDesc == null) {
            txtPackageDesc = (TextView) itemView.findViewById(R.id.txtPackageDesc);
        }
        return txtPackageDesc;
    }

    public TextView getTxtProductDesc() {
        if (txtProductDesc == null) {
            txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
            txtProductDesc.setOnClickListener(productOnClickListener);
        }
        return txtProductDesc;
    }

    public TextView getTxtSalePrice() {
        if (txtSalePrice == null) {
            txtSalePrice = (TextView) itemView.findViewById(R.id.txtSalePrice);
        }
        return txtSalePrice;
    }

    public TextView getTxtMrp() {
        if (txtMrp == null) {
            txtMrp = (TextView) itemView.findViewById(R.id.txtMrp);
        }
        return txtMrp;
    }

    public TextView getTxtPromoDesc() {
        if (txtPromoDesc == null) {
            txtPromoDesc = (TextView) itemView.findViewById(R.id.txtPromoName);
            txtPromoDesc.setOnClickListener(promoClickListener);
        }
        return txtPromoDesc;
    }

    public TextView getTxtPromoAddSavings() {
        if (txtPromoAddSavings == null) {
            txtPromoAddSavings = (TextView) itemView.findViewById(R.id.txtPromoAddSavings);
        }
        return txtPromoAddSavings;
    }

    public ImageView getImgAddToBasket() {
        if (imgAddToBasket == null) {
            imgAddToBasket = (ImageView) itemView.findViewById(R.id.imgAddToBasket);
        }
        return imgAddToBasket;
    }

    public View getViewDecBasketQty() {
        if (viewDecBasketQty == null) {
            viewDecBasketQty = itemView.findViewById(R.id.viewDecBasketQty);
        }
        return viewDecBasketQty;
    }

    public TextView getTxtInBasket() {
        if (txtInBasket == null) {
            txtInBasket = (TextView) itemView.findViewById(R.id.txtInBasket);
        }
        return txtInBasket;
    }

    public View getViewIncBasketQty() {
        if (viewIncBasketQty == null) {
            viewIncBasketQty = itemView.findViewById(R.id.viewIncBasketQty);
        }
        return viewIncBasketQty;
    }

    public TextView getTxtOutOfStockORNotForSale() {
        if (txtOutOfStockORNotForSale == null) {
            txtOutOfStockORNotForSale = (TextView) itemView.findViewById(R.id.txtOutOfStockORNotForSale);
        }
        return txtOutOfStockORNotForSale;
    }

    public ImageView getImgProductOverflowAction() {
        if (imgProductOverflowAction == null) {
            imgProductOverflowAction = (ImageView) itemView.findViewById(R.id.imgProductOverflowAction);
        }
        return imgProductOverflowAction;
    }

    public EditText getEditTextQty() {
        if (editTextQty == null) {
            editTextQty = (EditText) itemView.findViewById(R.id.editTextQty);
        }
        return editTextQty;
    }


    public ViewGroup getLayoutExpressMsg() {
        if (layoutExpressMsg == null) {
            layoutExpressMsg = (ViewGroup) itemView.findViewById(R.id.layoutExpressMsg);
        }
        return layoutExpressMsg;
    }


    public RadioGroup getRadioGroupExpress() {
        if (radioGroupExpress == null) {
            radioGroupExpress = (RadioGroup) itemView.findViewById(R.id.radioGroupExpress);
        }
        return radioGroupExpress;
    }

    public TextView getTxtExpressMsg() {
        if (txtExpressMsg == null) {
            txtExpressMsg = (TextView) itemView.findViewById(R.id.txtExpressMsg);
        }
        return txtExpressMsg;
    }

    public TextView getTxtGiftMsg() {
        if (txtGiftMsg == null) {
            txtGiftMsg = (TextView) itemView.findViewById(R.id.txtGiftMsg);
        }
        return txtGiftMsg;
    }

    public ImageView getImgGiftIcon() {
        // Present only on PD page
        if (imgGiftIcon == null) {
            imgGiftIcon = (ImageView) itemView.findViewById(R.id.imgGiftIcon);
        }
        return imgGiftIcon;
    }

    public ImageView getImgStoreIcon() {
        if (imgStoreIcon == null) {
            imgStoreIcon = (ImageView) itemView.findViewById(R.id.imgStoreIcon);
            imgStoreIcon.setOnClickListener(specialityShopIconClickListener);
        }
        return imgStoreIcon;
    }

    public RelativeLayout getStoreIconLayout() {
        if (storeIconLayout == null) {
            storeIconLayout = (RelativeLayout) itemView.findViewById(R.id.storeIconLayout);
        }
        return storeIconLayout;
    }
}
