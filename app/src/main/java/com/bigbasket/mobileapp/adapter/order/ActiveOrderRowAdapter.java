package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.order.ShowCartFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.cart.CartItemHeader;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.order.OrderItemDisplaySource;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.task.BasketOperationTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.ShowAnnotationInfo;
import com.bigbasket.mobileapp.view.ShowFulfillmentInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;


public class ActiveOrderRowAdapter<T> extends android.widget.BaseAdapter {

    private List<Object> orderList;
    private LayoutInflater inflater;
    private OrderItemDisplaySource orderItemDisplaySource;
    private boolean isReadOnlyBasket;
    private HashMap<String, String> fulfillmentInfoIdAndIconHashMap;
    private HashMap<String, AnnotationInfo> annotationHashMap;
    private String baseImgUrl;
    private Typeface faceRobotoRegular, faceRupee;
    private String sourceName;
    private T context;


    public ActiveOrderRowAdapter(List<Object> orderList, T context, Typeface faceRupee,
                                 Typeface faceRobotoRegular, OrderItemDisplaySource orderItemDisplaySource,
                                 boolean isReadOnly,
                                 HashMap<String, String> fulfillmentInfoIdAndIconHashMap,
                                 HashMap<String, AnnotationInfo> annotationHashMap,
                                 String baseImageUrl, String sourceName) {
        this.context = context;
        this.orderList = orderList;
        this.faceRupee = faceRupee;
        this.faceRobotoRegular = faceRobotoRegular;
        this.orderItemDisplaySource = orderItemDisplaySource;
        this.fulfillmentInfoIdAndIconHashMap = fulfillmentInfoIdAndIconHashMap;
        this.annotationHashMap = annotationHashMap;
        this.isReadOnlyBasket = isReadOnly;
        this.baseImgUrl = baseImageUrl;
        this.sourceName = sourceName;
        this.inflater = (LayoutInflater) ((ActivityAware) context).getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int position) {
        return orderList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    private void renderHeaderView(HeaderTitleHolder headerTitleHolder, int position, CartItemHeader cartItemList) {

        TextView txtTopCategory = headerTitleHolder.getTxtTopCategory();
        txtTopCategory.setText(cartItemList.getTopCatName());

        TextView topCatTotalItems = headerTitleHolder.getTopCatTotalItems();
        if (cartItemList.getTopCatItems() != 0) {
            topCatTotalItems.setVisibility(View.VISIBLE);
            if (cartItemList.getTopCatItems() > 1) {
                topCatTotalItems.setText(cartItemList.getTopCatItems() + " Items");
            } else {
                topCatTotalItems.setText(cartItemList.getTopCatItems() + " Item");
            }
        } else {
            topCatTotalItems.setVisibility(View.GONE);
        }

        String separator = " | Total: ";
        String topCatTotalAmount = ((ActivityAware) context).getCurrentActivity().getDecimalAmount(cartItemList.getTopCatTotal());
        TextView topCatTotal = headerTitleHolder.getTopCatTotal();
        if (!topCatTotalAmount.equals("0")) {
            topCatTotal.setVisibility(View.VISIBLE);
            String regularSalePriceStr = "`" + topCatTotalAmount;
            Spannable regularSpannable = new SpannableString(separator + regularSalePriceStr);
            regularSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), separator.length(),
                    separator.length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            topCatTotal.setText(regularSpannable);
        } else {
            topCatTotal.setText("");
            topCatTotal.setVisibility(View.GONE);
        }


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Object obj = orderList.get(position);
        View row = convertView;
        RowHolder rowHolder;
        if (obj instanceof CartItem) {
            if (row == null || row.getTag() == null ||
                    !row.getTag().toString().equalsIgnoreCase(Constants.IS_PRODUCT)) {
                row = inflater.inflate(R.layout.uiv3_cart_item_row, parent, false);
                row.setTag(Constants.IS_PRODUCT);
                rowHolder = new RowHolder(row);
                row.setTag(rowHolder);
            } else {
                rowHolder = (RowHolder) row.getTag();
            }
            renderBasicView(rowHolder, position, (CartItem) obj);

            CartItem cartItem = (CartItem) obj;
            switch (cartItem.getPromoAppliedType()) {
                case CartItem.REGULAR_PRICE_AND_NO_PROMO:
                    getRegularPriceAndNoPromoView(rowHolder, cartItem);
                    break;
                case CartItem.REGULAR_PRICE_AND_PROMO_NOT_APPLIED:
                    getRegularPriceAndPromoNotAppliedView(rowHolder, cartItem);
                    break;
                case CartItem.PROMO_APPLIED_AND_PROMO_PRICING:
                    getPromoAppliedAndPromoPricingView(rowHolder, cartItem);
                    break;
                case CartItem.PROMO_APPLIED_AND_MIXED_PRICING:
                    getPromoAppliedAndMixedPromoPricingView(rowHolder, cartItem);
                    break;

            }
        } else if (obj instanceof FulfillmentInfo) {
            return getFulfillmentInfo(obj);
        } else if (obj instanceof AnnotationInfo) {
            return showAnnotationInfo(obj);
        } else {
            HeaderTitleHolder headerTitleHolder;
            row = inflater.inflate(R.layout.uiv3_category_row, parent, false);
            headerTitleHolder = new HeaderTitleHolder(row);
            renderHeaderView(headerTitleHolder, position, (CartItemHeader) obj);
        }
        return row;
    }

    private View getFulfillmentInfo(Object obj) {
        ShowFulfillmentInfo showFulfillmentInfo = new ShowFulfillmentInfo((FulfillmentInfo) obj,
                ((ActivityAware) context).getCurrentActivity(), faceRobotoRegular);
        return showFulfillmentInfo.showFulfillmentInfo(true, true);
    }

    private View showAnnotationInfo(Object obj) {
        ShowAnnotationInfo showAnnotationInfo = new ShowAnnotationInfo((AnnotationInfo) obj,
                ((ActivityAware) context).getCurrentActivity());
        View view = showAnnotationInfo.showAnnotationInfo();
        if (view != null)
            return view;
        return null;
    }

    private void renderBasicView(RowHolder rowHolder, int childPosition, final CartItem cartItem) {
        ImageView imgProduct = rowHolder.getImgProduct();
        if (imgProduct != null && !TextUtils.isEmpty(cartItem.getProductImgUrl())) {
            ImageLoader.getInstance().displayImage(baseImgUrl != null ? baseImgUrl +
                            cartItem.getProductImgUrl() : cartItem.getProductImgUrl(),
                    imgProduct);
        }

        ImageView imgMarketPlaceIcon = rowHolder.getImgLiquorIcon();
        if (cartItem.getFulfillmentId() != null && !cartItem.getFulfillmentId().equals("")) {
            String fulfillmentId = cartItem.getFulfillmentId();
            if (!TextUtils.isEmpty(fulfillmentId) && fulfillmentInfoIdAndIconHashMap != null &&
                    fulfillmentInfoIdAndIconHashMap.containsKey(fulfillmentId)) {
                String icon = fulfillmentInfoIdAndIconHashMap.get(fulfillmentId);
                if (!TextUtils.isEmpty(icon) && !icon.equalsIgnoreCase("null")) {
                    ImageLoader.getInstance().displayImage(icon, imgMarketPlaceIcon);
                    imgMarketPlaceIcon.setVisibility(View.VISIBLE);
                } else {
                    imgMarketPlaceIcon.setVisibility(View.GONE);
                }
            } else {
                imgMarketPlaceIcon.setVisibility(View.GONE);
            }
        } else if (cartItem.getAnnotationId() != null && !cartItem.getAnnotationId().equals("")) {
            String annotationId = cartItem.getAnnotationId();
            if (!TextUtils.isEmpty(annotationId) && annotationHashMap != null &&
                    annotationHashMap.containsKey(annotationId)) {
                String iconUrl = annotationHashMap.get(annotationId).getIconUrl();
                if (!TextUtils.isEmpty(iconUrl)) {
                    ImageLoader.getInstance().displayImage(iconUrl, imgMarketPlaceIcon);
                    imgMarketPlaceIcon.setVisibility(View.VISIBLE);
                } else {
                    imgMarketPlaceIcon.setVisibility(View.GONE);
                }
            } else {
                imgMarketPlaceIcon.setVisibility(View.GONE);
            }
        } else {
            imgMarketPlaceIcon.setVisibility(View.GONE);
        }

        TextView txtProductBrand = rowHolder.getTxtProductBrand();
        if (txtProductBrand != null) {
            txtProductBrand.setText(cartItem.getProductBrand());
        }

        TextView txtProductDesc = rowHolder.getTxtProductDesc();
        txtProductDesc.setTypeface(faceRobotoRegular);
        txtProductDesc.setText(cartItem.getProductDesc());

        TextView txtSalePrice = rowHolder.getTxtSalePrice();
        if (cartItem.getTotalPrice() > 0) {
            txtSalePrice.setText(UIUtil.asRupeeSpannable(cartItem.getTotalPrice(), faceRupee));
        } else {
            txtSalePrice.setText("Free!");
        }

        TextView txtSaving = rowHolder.getTxtSaving();

        if (cartItem.getSaving() > 0) {
            txtSaving.setVisibility(View.VISIBLE);
            txtSaving.setText(UIUtil.asRupeeSpannable(cartItem.getSaving(), faceRupee));
        } else {
            txtSaving.setVisibility(View.GONE);
        }

        final TextView txtInBasket = rowHolder.getTxtInBasket();
        if (cartItem.getTotalQty() > 0) {
            txtInBasket.setVisibility(View.VISIBLE);
            int itemCount = (int) cartItem.getTotalQty();
            txtInBasket.setText(String.valueOf(itemCount) + " in ");
        } else {
            txtInBasket.setVisibility(View.GONE);
        }

        final TextView txtDecBasketQty = rowHolder.getTxtDecBasketQty();
        final TextView txtIncBasketQty = rowHolder.getTxtIncBasketQty();
        final ImageView imgRemove = rowHolder.getImgRemove();
        final View basketOperationSeparatorLine = rowHolder.getBasketOperationSeparatorLine();
        if (txtDecBasketQty != null && txtIncBasketQty != null && imgRemove != null) {
            if (orderItemDisplaySource == OrderItemDisplaySource.BASKET && !isReadOnlyBasket && cartItem.getTotalPrice() > 0) {
                txtInBasket.setVisibility(View.VISIBLE);
                txtIncBasketQty.setVisibility(View.VISIBLE);
                txtDecBasketQty.setVisibility(View.VISIBLE);
                imgRemove.setVisibility(View.VISIBLE);
                basketOperationSeparatorLine.setVisibility(View.VISIBLE);
                txtDecBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(((ActivityAware) context).getCurrentActivity())) {
                            Product product = new Product(cartItem.getProductBrand(),
                                    cartItem.getProductDesc(), String.valueOf(cartItem.getSkuId()),
                                    cartItem.getTopCategoryName(), cartItem.getProductCategoryName());
                            BasketOperationTask basketOperationTask = new BasketOperationTask<>(context,
                                    BasketOperation.DEC, product,
                                    null, null, null, null, null, TrackingAware.BASKET_DECREMENT, sourceName
                            );
                            basketOperationTask.startTask();
                        } else {
                            Toast toast = Toast.makeText(((ActivityAware) context).getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }
                });
                txtIncBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(((ActivityAware) context).getCurrentActivity())) {
                            Product product = new Product(cartItem.getProductBrand(),
                                    cartItem.getProductDesc(), String.valueOf(cartItem.getSkuId()),
                                    cartItem.getTopCategoryName(), cartItem.getProductCategoryName());
                            BasketOperationTask basketOperationTask = new BasketOperationTask<>(context,
                                    BasketOperation.INC, product,
                                    null, null, null, null, null, TrackingAware.BASKET_INCREMENT, sourceName
                            );
                            basketOperationTask.startTask();

                        } else {
                            Toast toast = Toast.makeText(((ActivityAware) context).getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }
                });
                imgRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(((ActivityAware) context).getCurrentActivity())) {
                            Product product = new Product(cartItem.getProductBrand(),
                                    cartItem.getProductDesc(), String.valueOf(cartItem.getSkuId()),
                                    cartItem.getTopCategoryName(), cartItem.getProductCategoryName());
                            BasketOperationTask basketOperationTask = new BasketOperationTask<>(context,
                                    BasketOperation.EMPTY,
                                    product, txtInBasket, null, null, null, null, "0",
                                    TrackingAware.BASKET_REMOVE, sourceName
                            );
                            basketOperationTask.startTask();
                        } else {
                            Toast toast = Toast.makeText(((ActivityAware) context).getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }
                });
            } else {
                txtInBasket.setVisibility(View.GONE);
                txtIncBasketQty.setVisibility(View.GONE);
                txtDecBasketQty.setVisibility(View.GONE);
                imgRemove.setVisibility(View.GONE);
                basketOperationSeparatorLine.setVisibility(View.GONE);
            }
        }
    }

    private void getRegularPriceAndNoPromoView(RowHolder rowHolder, CartItem cartItem) {
        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.GONE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        txtRegularPriceAndQty.setVisibility(View.GONE);

        TextView lblRegularPrice = rowHolder.getLblRegularPrice();
        lblRegularPrice.setVisibility(View.GONE);

        ImageView imgPromoUsed = rowHolder.getImgPromoUsed();
        imgPromoUsed.setVisibility(View.GONE);

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        txtPromoPriceAndQty.setVisibility(View.GONE);

        TextView txtPromoNameDesc = rowHolder.getTxtPromoNameDesc();
        txtPromoNameDesc.setVisibility(View.GONE);
    }


    private void getRegularPriceAndPromoNotAppliedView(RowHolder rowHolder, CartItem cartItem) {
        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.GONE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        txtRegularPriceAndQty.setVisibility(View.GONE);

        TextView lblRegularPrice = rowHolder.getLblRegularPrice();
        lblRegularPrice.setVisibility(View.GONE);

        ImageView imgPromoUsed = rowHolder.getImgPromoUsed();
        imgPromoUsed.setVisibility(View.VISIBLE);
        imgPromoUsed.setBackgroundResource(R.drawable.promo_unused);

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        txtPromoPriceAndQty.setVisibility(View.VISIBLE);
        txtPromoPriceAndQty.setText("Promo not used  ");

        TextView txtPromoNameDesc = rowHolder.getTxtPromoNameDesc();
        txtPromoNameDesc.setVisibility(View.VISIBLE);
        String promoTxtName = cartItem.getCartItemPromoInfo().getPromoInfo().getPromoName();
        txtPromoNameDesc.setText(promoTxtName);
        txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.dark_red));

        if (orderItemDisplaySource == OrderItemDisplaySource.BASKET) {
            txtPromoNameDesc.setOnClickListener(new PromoListener(cartItem.getCartItemPromoInfo().getPromoInfo().getPromoId()));
        }

    }

    private void getPromoAppliedAndPromoPricingView(RowHolder rowHolder, CartItem cartItem) {
        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.GONE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        txtRegularPriceAndQty.setVisibility(View.GONE);

        TextView lblRegularPrice = rowHolder.getLblRegularPrice();
        lblRegularPrice.setVisibility(View.GONE);

        ImageView imgPromoUsed = rowHolder.getImgPromoUsed();
        imgPromoUsed.setVisibility(View.VISIBLE);

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        txtPromoPriceAndQty.setVisibility(View.GONE);

        TextView txtPromoNameDesc = rowHolder.getTxtPromoNameDesc();
        txtPromoNameDesc.setVisibility(View.VISIBLE);
        String promoTxtName = cartItem.getCartItemPromoInfo().getPromoInfo().getPromoName();
        txtPromoNameDesc.setText(promoTxtName);
        if (context instanceof ShowCartFragment) {
            txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.promo_txt_green_color));
        } else {
            txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.link_color));
        }
        if (orderItemDisplaySource == OrderItemDisplaySource.BASKET) {
            txtPromoNameDesc.setOnClickListener(new PromoListener(cartItem.getCartItemPromoInfo().getPromoInfo().getPromoId()));
        }

    }

    private void getPromoAppliedAndMixedPromoPricingView(RowHolder rowHolder, CartItem cartItem) {
        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.VISIBLE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        txtRegularPriceAndQty.setVisibility(View.VISIBLE);
        String regularQtyStr = UIUtil.roundOrInt(cartItem.getCartItemPromoInfo().
                getRegularInfo().getNumItemInCart());
        String separator = " @ ";
        String regularSalePriceStr = "`" + (((ActivityAware) context).getCurrentActivity()).getDecimalAmount(cartItem.getCartItemPromoInfo().getRegularInfo().getSalePrice());
        Spannable regularSpannable = new SpannableString(regularQtyStr + separator + regularSalePriceStr);
        regularSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), regularQtyStr.length()
                        + separator.length(), regularQtyStr.length() + separator.length() + 1,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        );
        regularSpannable.setSpan(new ForegroundColorSpan(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.tabDark)), regularSalePriceStr.length() - 1,
                regularQtyStr.length() + separator.length() + regularSalePriceStr.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        regularSpannable.setSpan(new ForegroundColorSpan(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.medium_grey)), 0,
                regularQtyStr.length() + separator.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        txtRegularPriceAndQty.setText(regularSpannable);

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        txtPromoPriceAndQty.setVisibility(View.VISIBLE);
        if (cartItem.getCartItemPromoInfo().getPromoInfo().getSalePrice() > 0) {
            String promoQtyStr = UIUtil.roundOrInt(cartItem.getCartItemPromoInfo().
                    getPromoInfo().getNumItemInCart());
            String promoSalePriceStr = "`" + (((ActivityAware) context).getCurrentActivity()).getDecimalAmount(cartItem.getCartItemPromoInfo().getPromoInfo().getSalePrice());
            Spannable promoSpannable = new SpannableString(promoQtyStr + separator + promoSalePriceStr);//
            promoSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), promoQtyStr.length() +
                            separator.length(), separator.length() + promoQtyStr.length() + 1, //
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE
            );
            txtPromoPriceAndQty.setText(promoSpannable);
        } else {
            txtPromoPriceAndQty.setText("Free!");
        }


        ImageView imgPromoUsed = rowHolder.getImgPromoUsed();
        imgPromoUsed.setVisibility(View.VISIBLE);

        TextView txtPromoNameDesc = rowHolder.getTxtPromoNameDesc();
        txtPromoNameDesc.setVisibility(View.VISIBLE);
        String promoTxtName = cartItem.getCartItemPromoInfo().getPromoInfo().getPromoName();
        txtPromoNameDesc.setText(promoTxtName);
        if (context instanceof ShowCartFragment) {
            txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.promo_txt_green_color));
        } else {
            txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.link_color));
        }
        if (orderItemDisplaySource == OrderItemDisplaySource.BASKET) {
            txtPromoNameDesc.setOnClickListener(new PromoListener(cartItem.getCartItemPromoInfo().getPromoInfo().getPromoId()));
        }
    }

    private class RowHolder {
        private ImageView imgProduct;
        private TextView txtProductDesc;
        private TextView txtSalePrice;
        private TextView txtSaving;
        private ImageView imgRegularImg;
        private TextView txtRegularPriceAndQty;
        private TextView lblRegularPrice;
        private ImageView imgPromoUsed;
        private TextView txtPromoPriceAndQty;
        private TextView txtPromoNameDesc;
        private TextView txtInBasket;
        private TextView txtDecBasketQty;
        private TextView txtIncBasketQty;
        private ImageView imgRemove;
        private ImageView imgLiquorIcon;
        private View base;
        private TextView txtProductBrand;
        private View basketOperationSeparatorLine;

        public RowHolder(View base) {
            this.base = base;
        }

        public TextView getTxtProductBrand() {
            if (txtProductBrand == null) {
                txtProductBrand = (TextView) base.findViewById(R.id.txtProductBrand);
                txtProductBrand.setTypeface(faceRobotoRegular);
            }
            return txtProductBrand;
        }

        public ImageView getImgProduct() {
            if (imgProduct == null)
                imgProduct = (ImageView) base.findViewById(R.id.imgProduct);
            return imgProduct;
        }

        public ImageView getImgLiquorIcon() {
            if (imgLiquorIcon == null)
                imgLiquorIcon = (ImageView) base.findViewById(R.id.imgLiquorIcon);
            return imgLiquorIcon;
        }

        public TextView getTxtProductDesc() {
            if (txtProductDesc == null) {
                txtProductDesc = (TextView) base.findViewById(R.id.txtProductDesc);
                txtProductDesc.setTypeface(faceRobotoRegular);
            }
            return txtProductDesc;
        }

        public TextView getTxtSalePrice() {
            if (txtSalePrice == null) {
                txtSalePrice = (TextView) base.findViewById(R.id.txtSalePrice);
                txtSalePrice.setTypeface(faceRobotoRegular);
            }
            return txtSalePrice;
        }

        public TextView getTxtSaving() {
            if (txtSaving == null) {
                txtSaving = (TextView) base.findViewById(R.id.txtSaving);
                txtSaving.setTypeface(faceRobotoRegular);
            }
            return txtSaving;
        }

        public TextView getTxtInBasket() {
            if (txtInBasket == null) {
                txtInBasket = (TextView) base.findViewById(R.id.txtInBasket);
                txtInBasket.setTypeface(faceRobotoRegular);
            }
            return txtInBasket;
        }

        public ImageView getImgRegularImg() {
            if (imgRegularImg == null)
                imgRegularImg = (ImageView) base.findViewById(R.id.imgRegularImg);
            return imgRegularImg;
        }

        public TextView getTxtRegularPriceAndQty() {
            if (txtRegularPriceAndQty == null) {
                txtRegularPriceAndQty = (TextView) base.findViewById(R.id.txtRegularPriceAndQty);
                txtRegularPriceAndQty.setTypeface(faceRobotoRegular);
            }
            return txtRegularPriceAndQty;
        }

        public TextView getLblRegularPrice() {
            if (lblRegularPrice == null) {
                lblRegularPrice = (TextView) base.findViewById(R.id.lblRegularPrice);
                lblRegularPrice.setTypeface(faceRobotoRegular);
            }
            return lblRegularPrice;
        }

        public ImageView getImgPromoUsed() {
            if (imgPromoUsed == null)
                imgPromoUsed = (ImageView) base.findViewById(R.id.imgPromoUsed);
            return imgPromoUsed;
        }

        public TextView getTxtPromoPriceAndQty() {
            if (txtPromoPriceAndQty == null) {
                txtPromoPriceAndQty = (TextView) base.findViewById(R.id.txtPromoPriceAndQty);
                txtPromoPriceAndQty.setTypeface(faceRobotoRegular);
            }
            return txtPromoPriceAndQty;
        }

        public TextView getTxtPromoNameDesc() {
            if (txtPromoNameDesc == null) {
                txtPromoNameDesc = (TextView) base.findViewById(R.id.txtPromoNameDesc);
                txtPromoNameDesc.setTypeface(faceRobotoRegular);
            }
            return txtPromoNameDesc;
        }

        public ImageView getImgRemove() {
            if (imgRemove == null)
                imgRemove = (ImageView) base.findViewById(R.id.imgRemove);
            return imgRemove;
        }

        public TextView getTxtIncBasketQty() {
            if (txtIncBasketQty == null)
                txtIncBasketQty = (TextView) base.findViewById(R.id.txtIncBasketQty);
            return txtIncBasketQty;
        }

        public TextView getTxtDecBasketQty() {
            if (txtDecBasketQty == null)
                txtDecBasketQty = (TextView) base.findViewById(R.id.txtDecBasketQty);
            return txtDecBasketQty;
        }

        public View getBasketOperationSeparatorLine() {
            if (basketOperationSeparatorLine == null) {
                basketOperationSeparatorLine = base.findViewById(R.id.basketOperationSeparatorLine);
            }
            return basketOperationSeparatorLine;
        }

        public View getBase() {
            return base;
        }
    }

    private class HeaderTitleHolder {
        private TextView txtTopCategory;
        private TextView topCatTotalItems;
        private TextView topCatTotal;
        private View base;

        public HeaderTitleHolder(View base) {
            this.base = base;
        }

        public TextView getTxtTopCategory() {
            if (txtTopCategory == null) {
                txtTopCategory = (TextView) base.findViewById(R.id.txtTopCategory);
                txtTopCategory.setTypeface(faceRobotoRegular);
            }
            return txtTopCategory;
        }

        public TextView getTopCatTotalItems() {
            if (topCatTotalItems == null) {
                topCatTotalItems = (TextView) base.findViewById(R.id.topCatTotalItems);
                topCatTotalItems.setTypeface(faceRobotoRegular);
            }
            return topCatTotalItems;
        }

        public TextView getTopCatTotal() {
            if (topCatTotal == null) {
                topCatTotal = (TextView) base.findViewById(R.id.topCatTotal);
                topCatTotal.setTypeface(faceRobotoRegular);
            }
            return topCatTotal;
        }
    }


    private class PromoListener implements View.OnClickListener {
        private int promoId;

        PromoListener(int promoId) {
            this.promoId = promoId;
        }

        @Override
        public void onClick(View v) {
            Bundle bundle = new Bundle();
            bundle.putInt(Constants.PROMO_ID, promoId);
            PromoDetailFragment promoDetailFragment = new PromoDetailFragment();
            promoDetailFragment.setArguments(bundle);
            ((ActivityAware) context).getCurrentActivity().onChangeFragment(promoDetailFragment);
        }
    }
}
