package com.bigbasket.mobileapp.interfaces;

import com.bigbasket.mobileapp.model.product.Product;

import java.util.ArrayList;

public interface ProductListDialogAware {
    public void showDialog(ArrayList<Product> products, int productCount, String baseImgUrl,
                           boolean asDialog, String tagName);
}
