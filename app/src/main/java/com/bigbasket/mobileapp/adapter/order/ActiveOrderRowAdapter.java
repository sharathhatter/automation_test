package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BBActivity;
import com.bigbasket.mobileapp.activity.order.uiv3.ShowCartActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
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
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.ShowAnnotationInfo;
import com.bigbasket.mobileapp.view.ShowFulfillmentInfo;

import java.util.HashMap;
import java.util.List;


public class ActiveOrderRowAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CART_ITEM = 0;
    private static final int VIEW_TYPE_CART_HEADER = 1;
    private static final int VIEW_TYPE_CART_ANNOTATION = 2;
    private static final int VIEW_TYPE_FULFILLMENT_INFO = 3;
    private List<Object> orderList;
    private LayoutInflater inflater;
    private
    @OrderItemDisplaySource.Type
    int orderItemDisplaySource;
    private boolean isReadOnlyBasket;
    private HashMap<String, String> fulfillmentInfoIdAndIconHashMap;
    private HashMap<String, AnnotationInfo> annotationHashMap;
    private String baseImgUrl;
    private Typeface faceRobotoRegular, faceRupee;
    private String navigationCtx;
    private T context;

    public ActiveOrderRowAdapter(List<Object> orderList, T context, Typeface faceRupee,
                                 Typeface faceRobotoRegular, @OrderItemDisplaySource.Type int orderItemDisplaySource,
                                 boolean isReadOnly,
                                 HashMap<String, String> fulfillmentInfoIdAndIconHashMap,
                                 HashMap<String, AnnotationInfo> annotationHashMap,
                                 String baseImageUrl, String navigationCtx) {
        this.context = context;
        this.orderList = orderList;
        this.faceRupee = faceRupee;
        this.faceRobotoRegular = faceRobotoRegular;
        this.orderItemDisplaySource = orderItemDisplaySource;
        this.fulfillmentInfoIdAndIconHashMap = fulfillmentInfoIdAndIconHashMap;
        this.annotationHashMap = annotationHashMap;
        this.isReadOnlyBasket = isReadOnly;
        this.baseImgUrl = baseImageUrl;
        this.navigationCtx = navigationCtx;
        this.inflater = (LayoutInflater) ((ActivityAware) context).getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CART_ITEM:
                View row = inflater.inflate(R.layout.uiv3_cart_item_row, parent, false);
                return new RowHolder(row);
            case VIEW_TYPE_FULFILLMENT_INFO:
                row = inflater.inflate(R.layout.fulfillment_info, parent, false);
                return new FulfillmentInfoViewHolder(row);
            case VIEW_TYPE_CART_ANNOTATION:
                row = inflater.inflate(R.layout.fulfillment_info, parent, false);
                return new FulfillmentInfoViewHolder(row);
            default:
                row = inflater.inflate(R.layout.uiv3_category_row, parent, false);
                return new HeaderTitleHolder(row);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        final Object obj = orderList.get(position);

        if (viewType == VIEW_TYPE_CART_ITEM) {
            renderBasicView((RowHolder) holder, (CartItem) obj);

            CartItem cartItem = (CartItem) obj;
            switch (cartItem.getPromoAppliedType()) {
                case CartItem.REGULAR_PRICE_AND_NO_PROMO:
                    getRegularPriceAndNoPromoView((RowHolder) holder);
                    break;
                case CartItem.REGULAR_PRICE_AND_PROMO_NOT_APPLIED:
                    getRegularPriceAndPromoNotAppliedView((RowHolder) holder, cartItem);
                    break;
                case CartItem.PROMO_APPLIED_AND_PROMO_PRICING:
                    getPromoAppliedAndPromoPricingView((RowHolder) holder, cartItem);
                    break;
                case CartItem.PROMO_APPLIED_AND_MIXED_PRICING:
                    getPromoAppliedAndMixedPromoPricingView((RowHolder) holder, cartItem);
                    break;

            }
        } else if (viewType == VIEW_TYPE_FULFILLMENT_INFO) {
            showFulfillmentInfo(obj, (FulfillmentInfoViewHolder) holder);
        } else if (viewType == VIEW_TYPE_CART_ANNOTATION) {
            showAnnotationInfo(obj, (FulfillmentInfoViewHolder) holder);
        } else {
            renderHeaderView((HeaderTitleHolder) holder, (CartItemHeader) obj);
        }
    }

    @Override
    public int getItemViewType(int position) {
        Object obj = orderList.get(position);
        if (obj instanceof CartItem) {
            return VIEW_TYPE_CART_ITEM;
        } else if (obj instanceof FulfillmentInfo) {
            return VIEW_TYPE_FULFILLMENT_INFO;
        } else if (obj instanceof AnnotationInfo) {
            return VIEW_TYPE_CART_ANNOTATION;
        }
        return VIEW_TYPE_CART_HEADER;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    private void renderHeaderView(HeaderTitleHolder headerTitleHolder, CartItemHeader cartItemList) {

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
            topCatTotalItems.setVisibility(View.INVISIBLE);
        }

        String separator = "  |  ";
        String topCatTotalAmount = UIUtil.formatAsMoney(cartItemList.getTopCatTotal());
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
            topCatTotal.setVisibility(View.INVISIBLE);
        }


    }

    private void showFulfillmentInfo(Object obj, FulfillmentInfoViewHolder holder) {
        ShowFulfillmentInfo showFulfillmentInfo = new ShowFulfillmentInfo<>((FulfillmentInfo) obj,
                ((ActivityAware) context).getCurrentActivity(), faceRobotoRegular, holder);
        showFulfillmentInfo.showFulfillmentInfo(true, true);
    }

    private void showAnnotationInfo(Object obj, FulfillmentInfoViewHolder holder) {
        ShowAnnotationInfo showAnnotationInfo = new ShowAnnotationInfo<>((AnnotationInfo) obj,
                ((ActivityAware) context).getCurrentActivity(), holder);
        showAnnotationInfo.showAnnotationInfo();
    }

    private void renderBasicView(RowHolder rowHolder, final CartItem cartItem) {
        ImageView imgProduct = rowHolder.getImgProduct();
        if (imgProduct != null && !TextUtils.isEmpty(cartItem.getProductImgUrl())) {
            UIUtil.displayAsyncImage(imgProduct, baseImgUrl != null ? baseImgUrl +
                    cartItem.getProductImgUrl() : cartItem.getProductImgUrl());
        }

        ImageView imgMarketPlaceIcon = rowHolder.getImgLiquorIcon();
        if (!TextUtils.isEmpty(cartItem.getFulfillmentId())) {
            String fulfillmentId = cartItem.getFulfillmentId();
            if (!TextUtils.isEmpty(fulfillmentId) && fulfillmentInfoIdAndIconHashMap != null &&
                    fulfillmentInfoIdAndIconHashMap.containsKey(fulfillmentId)) {
                String icon = fulfillmentInfoIdAndIconHashMap.get(fulfillmentId);
                if (!TextUtils.isEmpty(icon) && !icon.equalsIgnoreCase("null")) {
                    UIUtil.displayAsyncImage(imgMarketPlaceIcon, icon);
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
                    UIUtil.displayAsyncImage(imgMarketPlaceIcon, iconUrl);
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

        TextView txtProductDesc = rowHolder.getTxtProductDesc();
        txtProductDesc.setText(cartItem.getProductDesc());

        TextView txtProductBrand = rowHolder.getTxtProductBrand();
        txtProductBrand.setText(cartItem.getProductBrand());


        TextView txtExpressAvailable = rowHolder.getTxtExpressAvailable();
        if (cartItem.isExpress()) {
            txtExpressAvailable.setVisibility(View.VISIBLE);
        } else {
            txtExpressAvailable.setVisibility(View.GONE);
        }

        TextView txtSalePrice = rowHolder.getTxtSalePrice();
        if (cartItem.getTotalPrice() > 0) {
            String prefix = "`";
            String salePriceStr = UIUtil.formatAsMoney(cartItem.getTotalPrice());
            int prefixLen = prefix.length();
            SpannableString spannableSalePrice = new SpannableString(prefix + salePriceStr);
            spannableSalePrice.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen - 1,
                    prefixLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtSalePrice.setText(spannableSalePrice);
            txtSalePrice.setVisibility(View.VISIBLE);
        } else {
            txtSalePrice.setText("Free!");
        }

        /*
        TextView txtSaving = rowHolder.getTxtSaving();
        if (cartItem.getSaving() > 0 && (cartItem.getPromoAppliedType() >CartItem.REGULAR_PRICE_AND_PROMO_NOT_APPLIED &&
                cartItem.getPromoAppliedType()<CartItem.REGULAR_PRICE_AND_NO_PROMO)) {
            txtSaving.setVisibility(View.VISIBLE);
            String prefix = "Save: `";
            Spannable savingSpannable = new SpannableString(prefix + UIUtil.formatAsMoney(cartItem.getSaving()));
            savingSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), prefix.length()-1, prefix.length(),
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtSaving.setText(savingSpannable);
        }else {
            txtSaving.setVisibility(View.GONE);
        }
        */


        final TextView txtInBasket = rowHolder.getTxtInBasket();
        final View imgDecBasketQty = rowHolder.getViewDecBasketQty();
        final View imgIncBasketQty = rowHolder.getViewIncBasketQty();
        final ImageView imgRemove = rowHolder.getImgRemove();
        TextView txtPackDesc = rowHolder.getTxtPackDesc();
        String packType = "";
        if (!TextUtils.isEmpty(cartItem.getProductWeight()))
            packType = cartItem.getProductWeight();
        if (!TextUtils.isEmpty(cartItem.getPackDesc()))
            packType += " - " + cartItem.getPackDesc();

        if (!TextUtils.isEmpty(packType)) {
            txtPackDesc.setText(packType);
            txtPackDesc.setVisibility(View.VISIBLE);
        } else {
            txtPackDesc.setVisibility(View.GONE);
        }

        if (imgDecBasketQty != null && imgIncBasketQty != null && imgRemove != null) {
            if (orderItemDisplaySource == OrderItemDisplaySource.BASKET && !isReadOnlyBasket && cartItem.getTotalPrice() > 0) {
                txtInBasket.setVisibility(View.VISIBLE);
                imgIncBasketQty.setVisibility(View.VISIBLE);
                imgDecBasketQty.setVisibility(View.VISIBLE);
                imgRemove.setVisibility(View.VISIBLE);

                if (cartItem.getTotalQty() > 0) {
                    txtInBasket.setVisibility(View.VISIBLE);
                    int itemCount = (int) cartItem.getTotalQty();
                    txtInBasket.setText(String.valueOf(itemCount));
                } else {
                    txtInBasket.setVisibility(View.GONE);
                }

                imgDecBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(((ActivityAware) context).getCurrentActivity())) {
                            Product product = new Product(cartItem.getProductBrand(),
                                    cartItem.getProductDesc(), String.valueOf(cartItem.getSkuId()),
                                    cartItem.getTopCategoryName(), cartItem.getProductCategoryName());
                            BasketOperationTask basketOperationTask = new BasketOperationTask<>(context,
                                    BasketOperation.DEC, product,
                                    null, null, null, null, null, TrackingAware.BASKET_DECREMENT,
                                    navigationCtx, null, null, null, TrackEventkeys.SINGLE_TAB_NAME);
                            basketOperationTask.startTask();
                        } else {
                            Toast toast = Toast.makeText(((ActivityAware) context).getCurrentActivity(), "Unable to connect to Internet", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }
                });
                imgIncBasketQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(((ActivityAware) context).getCurrentActivity())) {
                            Product product = new Product(cartItem.getProductBrand(),
                                    cartItem.getProductDesc(), String.valueOf(cartItem.getSkuId()),
                                    cartItem.getTopCategoryName(), cartItem.getProductCategoryName());
                            BasketOperationTask basketOperationTask = new BasketOperationTask<>(context,
                                    BasketOperation.INC, product,
                                    null, null, null, null, null, TrackingAware.BASKET_INCREMENT,
                                    navigationCtx, null, null, null, TrackEventkeys.SINGLE_TAB_NAME);
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
                                    product, txtInBasket, null, null, null, "0",
                                    TrackingAware.BASKET_REMOVE, navigationCtx, null, null, null,
                                    TrackEventkeys.SINGLE_TAB_NAME);
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
                imgIncBasketQty.setVisibility(View.GONE);
                imgDecBasketQty.setVisibility(View.GONE);
                imgRemove.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void getRegularPriceAndNoPromoView(RowHolder rowHolder) {
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
        txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.red_color));

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
        if (context instanceof ShowCartActivity) {
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
        String regularSalePriceStr = "`" + UIUtil.formatAsMoney(cartItem.getCartItemPromoInfo().getRegularInfo().getSalePrice());
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
            String promoSalePriceStr = "`" + UIUtil.formatAsMoney(cartItem.getCartItemPromoInfo().getPromoInfo().getSalePrice());
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
        if (context instanceof ShowCartActivity) {
            txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.promo_txt_green_color));
        } else {
            txtPromoNameDesc.setTextColor(((ActivityAware) context).getCurrentActivity().getResources().getColor(R.color.link_color));
        }
        if (orderItemDisplaySource == OrderItemDisplaySource.BASKET) {
            txtPromoNameDesc.setOnClickListener(new PromoListener(cartItem.getCartItemPromoInfo().getPromoInfo().getPromoId()));
        }
    }

    private class RowHolder extends RecyclerView.ViewHolder {
        private ImageView imgProduct;
        private TextView txtProductDesc;
        private TextView txtSalePrice;
        private ImageView imgRegularImg;
        private TextView txtProductBrand;
        private TextView txtRegularPriceAndQty;
        private TextView lblRegularPrice;
        private ImageView imgPromoUsed;
        private TextView txtPromoPriceAndQty;
        private TextView txtPromoNameDesc;
        private TextView txtInBasket;
        private View viewDecBasketQty;
        private View viewIncBasketQty;
        private ImageView imgRemove;
        private ImageView imgLiquorIcon;
        private TextView txtExpressAvailable;
        private TextView txtPackDesc;

        public RowHolder(View base) {
            super(base);
        }

        public ImageView getImgProduct() {
            if (imgProduct == null)
                imgProduct = (ImageView) itemView.findViewById(R.id.imgProduct);
            return imgProduct;
        }

        public ImageView getImgLiquorIcon() {
            if (imgLiquorIcon == null)
                imgLiquorIcon = (ImageView) itemView.findViewById(R.id.imgLiquorIcon);
            return imgLiquorIcon;
        }

        public TextView getTxtProductDesc() {
            if (txtProductDesc == null) {
                txtProductDesc = (TextView) itemView.findViewById(R.id.txtProductDesc);
                txtProductDesc.setTypeface(faceRobotoRegular);
            }
            return txtProductDesc;
        }


        public TextView getTxtProductBrand() {
            if (txtProductBrand == null) {
                txtProductBrand = (TextView) itemView.findViewById(R.id.txtProductBrand);
                txtProductBrand.setTypeface(faceRobotoRegular);
            }
            return txtProductBrand;
        }

        public TextView getTxtExpressAvailable() {
            if (txtExpressAvailable == null)
                txtExpressAvailable = (TextView) itemView.findViewById(R.id.txtExpressAvailable);
            txtExpressAvailable.setTypeface(faceRobotoRegular);
            return txtExpressAvailable;
        }

        public TextView getTxtSalePrice() {
            if (txtSalePrice == null) {
                txtSalePrice = (TextView) itemView.findViewById(R.id.txtSalePrice);
                txtSalePrice.setTypeface(faceRobotoRegular);
            }
            return txtSalePrice;
        }

        public TextView getTxtInBasket() {
            if (txtInBasket == null) {
                txtInBasket = (TextView) itemView.findViewById(R.id.txtInBasket);
                txtInBasket.setTypeface(faceRobotoRegular);
            }
            return txtInBasket;
        }

        public ImageView getImgRegularImg() {
            if (imgRegularImg == null)
                imgRegularImg = (ImageView) itemView.findViewById(R.id.imgRegularImg);
            return imgRegularImg;
        }

        public TextView getTxtRegularPriceAndQty() {
            if (txtRegularPriceAndQty == null) {
                txtRegularPriceAndQty = (TextView) itemView.findViewById(R.id.txtRegularPriceAndQty);
                txtRegularPriceAndQty.setTypeface(faceRobotoRegular);
            }
            return txtRegularPriceAndQty;
        }

        public TextView getLblRegularPrice() {
            if (lblRegularPrice == null) {
                lblRegularPrice = (TextView) itemView.findViewById(R.id.lblRegularPrice);
                lblRegularPrice.setTypeface(faceRobotoRegular);
            }
            return lblRegularPrice;
        }

        public ImageView getImgPromoUsed() {
            if (imgPromoUsed == null)
                imgPromoUsed = (ImageView) itemView.findViewById(R.id.imgPromoUsed);
            return imgPromoUsed;
        }

        public TextView getTxtPromoPriceAndQty() {
            if (txtPromoPriceAndQty == null) {
                txtPromoPriceAndQty = (TextView) itemView.findViewById(R.id.txtPromoPriceAndQty);
                txtPromoPriceAndQty.setTypeface(faceRobotoRegular);
            }
            return txtPromoPriceAndQty;
        }

        public TextView getTxtPromoNameDesc() {
            if (txtPromoNameDesc == null) {
                txtPromoNameDesc = (TextView) itemView.findViewById(R.id.txtPromoNameDesc);
                txtPromoNameDesc.setTypeface(faceRobotoRegular);
            }
            return txtPromoNameDesc;
        }

        public TextView getTxtPackDesc() {
            if (txtPackDesc == null) {
                txtPackDesc = (TextView) itemView.findViewById(R.id.txtPackDesc);
                txtPackDesc.setTypeface(faceRobotoRegular);
            }
            return txtPackDesc;
        }

        public ImageView getImgRemove() {
            if (imgRemove == null)
                imgRemove = (ImageView) itemView.findViewById(R.id.imgRemove);
            return imgRemove;
        }

        public View getViewIncBasketQty() {
            if (viewIncBasketQty == null)
                viewIncBasketQty = itemView.findViewById(R.id.viewIncBasketQty);
            return viewIncBasketQty;
        }

        public View getViewDecBasketQty() {
            if (viewDecBasketQty == null)
                viewDecBasketQty = itemView.findViewById(R.id.viewDecBasketQty);
            return viewDecBasketQty;
        }
    }

    private class HeaderTitleHolder extends RecyclerView.ViewHolder {
        private TextView txtTopCategory;
        private TextView topCatTotalItems;
        private TextView topCatTotal;

        public HeaderTitleHolder(View base) {
            super(base);
        }

        public TextView getTxtTopCategory() {
            if (txtTopCategory == null) {
                txtTopCategory = (TextView) itemView.findViewById(R.id.txtTopCategory);
                txtTopCategory.setTypeface(faceRobotoRegular);
            }
            return txtTopCategory;
        }

        public TextView getTopCatTotalItems() {
            if (topCatTotalItems == null) {
                topCatTotalItems = (TextView) itemView.findViewById(R.id.topCatTotalItems);
                topCatTotalItems.setTypeface(faceRobotoRegular);
            }
            return topCatTotalItems;
        }

        public TextView getTopCatTotal() {
            if (topCatTotal == null) {
                topCatTotal = (TextView) itemView.findViewById(R.id.topCatTotal);
                topCatTotal.setTypeface(faceRobotoRegular);
            }
            return topCatTotal;
        }
    }

    public static class FulfillmentInfoViewHolder extends RecyclerView.ViewHolder {
        private RelativeLayout layoutInfoMsg;
        private ImageView imgLiquorIcon;
        private TextView txtFulfilledBy;
        private TextView txtTC1;
        private TextView txtTC2;

        public FulfillmentInfoViewHolder(View itemView) {
            super(itemView);
        }

        public RelativeLayout getLayoutInfoMsg() {
            if (layoutInfoMsg == null) {
                layoutInfoMsg = (RelativeLayout) itemView.findViewById(R.id.layoutInfoMsg);
            }
            return layoutInfoMsg;
        }

        public ImageView getImgLiquorIcon() {
            if (imgLiquorIcon == null) {
                imgLiquorIcon = (ImageView) itemView.findViewById(R.id.imgLiquorIcon);
            }
            return imgLiquorIcon;
        }

        public TextView getTxtFulfilledBy() {
            if (txtFulfilledBy == null) {
                txtFulfilledBy = (TextView) itemView.findViewById(R.id.txtFulfilledBy);
            }
            return txtFulfilledBy;
        }

        public TextView getTxtTC1() {
            if (txtTC1 == null) {
                txtTC1 = (TextView) itemView.findViewById(R.id.txtTC1);
            }
            return txtTC1;
        }

        public TextView getTxtTC2() {
            if (txtTC2 == null) {
                txtTC2 = (TextView) itemView.findViewById(R.id.txtTC2);
            }
            return txtTC2;
        }
    }

    private class PromoListener implements View.OnClickListener {
        private int promoId;

        PromoListener(int promoId) {
            this.promoId = promoId;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(((ActivityAware) context).getCurrentActivity(), BBActivity.class);
            intent.putExtra(Constants.PROMO_ID, promoId);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_PROMO_DETAIL);
            ((ActivityAware) context).getCurrentActivity().startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
        }
    }
}
