package com.bigbasket.mobileapp.handler.click.basket;

import android.view.View;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.order.ActiveOrderRowAdapter;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.BasketChangeQtyAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.product.Product;

public class OnCartBasketActionListener extends OnBasketActionAbstractListener {


    public OnCartBasketActionListener(@BasketOperation.Mode int basketOperation,
                                      AppOperationAware appOperationAware) {
        super(basketOperation, appOperationAware);
    }

    @Override
    protected Product getProduct(View v) {
        CartItem cartItem = (CartItem) v.getTag(R.id.basket_op_cart_item_tag_id);
        int qty;
        switch (basketOperation) {
            case BasketOperation.DEC:
                qty = (int) cartItem.getTotalQty() - 1;
                break;
            case BasketOperation.DELETE_ITEM:
                qty = 0;
                break;
            default:
                qty = (int) cartItem.getTotalQty() + 1;
                break;
        }

        return new Product(cartItem.getProductBrand(),
                cartItem.getProductDesc(), String.valueOf(cartItem.getSkuId()),
                cartItem.getTopCategoryName(), cartItem.getProductCategoryName(),
                qty);
    }

    @Override
    protected void doBasketOperation(View v) {
        super.doBasketOperation(v);
        CartItem cartItem = (CartItem) v.getTag(R.id.basket_op_cart_item_tag_id);
        ActiveOrderRowAdapter.RowHolder rowHolder = (ActiveOrderRowAdapter.RowHolder)
                v.getTag(R.id.basket_op_cart_view_holder_tag_id);
        if (v.getTag(R.id.basket_op_cart_page_tab_index_tag_id) != null) {
            int currentTabIndex = (int) v.getTag(R.id.basket_op_cart_page_tab_index_tag_id);
            if (appOperationAware instanceof BasketChangeQtyAware) {
                int itemPosition;
                switch (basketOperation) {
                    case BasketOperation.DEC:
                        int scrollOffset = cartItem.getTotalQty() == 1 ? 1 : 0;
                        itemPosition = rowHolder.getAdapterPosition() - scrollOffset;
                        break;
                    case BasketOperation.DELETE_ITEM:
                        itemPosition = rowHolder.getAdapterPosition() - 1;
                        break;
                    default:
                        itemPosition = rowHolder.getAdapterPosition();
                        break;
                }
                ((BasketChangeQtyAware) appOperationAware).onBasketQtyChanged(itemPosition,
                        currentTabIndex);
            }
        }
    }
}
