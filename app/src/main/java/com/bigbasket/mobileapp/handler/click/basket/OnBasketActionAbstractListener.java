package com.bigbasket.mobileapp.handler.click.basket;

import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.task.BasketOperationTask;

import java.util.HashMap;
import java.util.Map;

public abstract class OnBasketActionAbstractListener implements View.OnClickListener {

    protected
    @BasketOperation.Mode
    int basketOperation;

    public OnBasketActionAbstractListener(@BasketOperation.Mode int basketOperation) {
        this.basketOperation = basketOperation;
    }

    @Override
    public void onClick(View v) {
        doBasketOperation(v);
    }

    protected abstract Product getProduct(View v);

    @SuppressWarnings("unchecked")
    protected void doBasketOperation(View v) {
        Product product = getProduct(v);
        String qty = (String) v.getTag(R.id.basket_op_qty_tag_id);
        String eventName = (String) v.getTag(R.id.basket_op_event_name_tag_id);
        String nc = (String) v.getTag(R.id.basket_op_nc_tag_id);
        String tabName = (String) v.getTag(R.id.basket_op_tabname_tag_id);
        ProductViewHolder productViewHolder = (ProductViewHolder) v.getTag(R.id.basket_op_product_view_holder_tag_id);
        Map<String, String> basketQueryMap = (Map<String, String>) v.getTag(R.id.basket_op_additional_query_map_tag_id);
        HashMap<String, String> cartInfo = (HashMap<String, String>) v.getTag(R.id.basket_op_cart_info_map_tag_id);

        boolean showQtyInput = false;  // For kirana users we show a input field, and this flag is used to indicate the same
        if (v.getTag(R.id.basket_op_read_input_qty_tag_id) != null) {
            showQtyInput = (boolean) v.getTag(R.id.basket_op_read_input_qty_tag_id);
        }
        if (showQtyInput && productViewHolder != null) {
            qty = productViewHolder.getEditTextQty().getText().toString();
            if (TextUtils.isEmpty(qty)) {
                Toast.makeText(v.getContext(),
                        v.getContext().getString(R.string.quantity_missing), Toast.LENGTH_SHORT).show();
            }
            BaseActivity.hideKeyboard(v.getContext(), productViewHolder.getEditTextQty());
            return;
        }
        BasketOperationTask.Builder builder =
                new BasketOperationTask.Builder<>((AppOperationAware) v.getContext(), basketOperation, product)
                        .withEventName(eventName)
                        .withNavigationCtx(nc)
                        .withProductView(v)
                        .withQty(qty)
                        .withEventName(eventName)
                        .withCartInfo(cartInfo)
                        .withTabName(tabName)
                        .withBasketQueryMap(basketQueryMap);
        if (productViewHolder != null) {
            builder.withBasketCountTextView(productViewHolder.getTxtInBasket())
                    .withViewDecQty(productViewHolder.getViewDecBasketQty())
                    .withViewIncQty(productViewHolder.getViewIncBasketQty())
                    .withViewAddToBasket(productViewHolder.getImgAddToBasket())
                    .withEditTextQty(productViewHolder.getEditTextQty());
        }
        builder.build().startTask();
    }
}
