package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.cart.CartItemHeader;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.order.OrderItemDisplaySource;
import com.bigbasket.mobileapp.task.BasketOperationTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.ShowAnnotationInfo;
import com.bigbasket.mobileapp.view.ShowFulfillmentInfo;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.HashMap;
import java.util.List;


public class ActiveOrderRowAdapter extends android.widget.BaseAdapter {

    private BaseActivity baseActivity;
    private Context context;
    private List<Object> orderList;
    private LayoutInflater inflater;
    private Typeface faceRobotoSlabLight, faceRobotoSlabNrml, faceLatoNormal, faceLatoLight;
    private OrderItemDisplaySource orderItemDisplaySource;
    private boolean isReadOnlyBasket; //showCartOperationBtn,
    private HashMap<String, String> fulfillmentInfoIdAndIconHashMap;
    private HashMap<String, AnnotationInfo> annotationHashMap;
    private String baseImgUrl;
    private BaseFragment fragment;
    private Typeface faceRobotoRegular, faceRupee;

    public ActiveOrderRowAdapter(List<Object> orderList, BaseActivity context, Typeface faceRupee,
                                 Typeface faceRobotoSlabLight, Typeface faceRobotoSlabNrml, Typeface faceLatoNormal,
                                 Typeface faceLatoLight,
                                 OrderItemDisplaySource orderItemDisplaySource, HashMap<String, String> fulfillmentInfoIdAndIconHashMap,
                                 HashMap<String, AnnotationInfo> annotationHashMap) {
        this.baseActivity = context;
        this.orderList = orderList;
        this.faceRupee = faceRupee;
        this.faceRobotoSlabLight = faceRobotoSlabLight;
        this.faceRobotoSlabNrml = faceRobotoSlabNrml;
        this.faceLatoNormal = faceLatoNormal;
        this.faceLatoLight = faceLatoLight;
        this.orderItemDisplaySource = orderItemDisplaySource;
        this.fulfillmentInfoIdAndIconHashMap = fulfillmentInfoIdAndIconHashMap;
        this.annotationHashMap = annotationHashMap;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public ActiveOrderRowAdapter(List<Object> orderList, BaseActivity baseActivity, BaseFragment fragment, Typeface faceRupee,
                                 Typeface faceRobotoRegular, OrderItemDisplaySource orderItemDisplaySource,
                                 boolean isReadOnly,
                                 HashMap<String, String> fulfillmentInfoIdAndIconHashMap,
                                 HashMap<String, AnnotationInfo> annotationHashMap,
                                 String baseImageUrl) {
        this.baseActivity = baseActivity;
        this.fragment = fragment;
        this.orderList = orderList;
        this.faceRupee = faceRupee;
        this.faceRobotoRegular = faceRobotoRegular;
        this.orderItemDisplaySource = orderItemDisplaySource;
        this.fulfillmentInfoIdAndIconHashMap = fulfillmentInfoIdAndIconHashMap;
        this.annotationHashMap = annotationHashMap;
        this.isReadOnlyBasket = isReadOnly;
        this.baseImgUrl = baseImageUrl;
        this.inflater = (LayoutInflater) baseActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public ActiveOrderRowAdapter(List<Object> orderList, BaseActivity context, Typeface faceRupee,
                                 Typeface faceRobotoSlabLight, Typeface faceRobotoSlabNrml, Typeface faceLatoNormal,
                                 Typeface faceLatoLight,
                                 OrderItemDisplaySource orderItemDisplaySource) {
        this.baseActivity = context;
        this.orderList = orderList;
        this.faceRupee = faceRupee;
        this.faceRobotoSlabLight = faceRobotoSlabLight;
        this.faceRobotoSlabNrml = faceRobotoSlabNrml;
        this.faceLatoNormal = faceLatoNormal;
        this.faceLatoLight = faceLatoLight;
        this.orderItemDisplaySource = orderItemDisplaySource;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public ActiveOrderRowAdapter(List<Object> orderList, BaseActivity context, Typeface faceRupee,
                                 Typeface faceRobotoSlabLight, Typeface faceRobotoSlabNrml, Typeface faceLatoNormal,
                                 Typeface faceLatoLight,
                                 OrderItemDisplaySource orderItemDisplaySource, boolean showCartOperationBtn,
                                 HashMap<String, String> fulfillmentInfoIdAndIconHashMap, boolean isReadOnlyBasket,
                                 HashMap<String, AnnotationInfo> annotationHashMap, String baseImageUrl) {
        this.baseActivity = context;
        this.orderList = orderList;
        this.faceRupee = faceRupee;
        this.faceRobotoSlabLight = faceRobotoSlabLight;
        this.faceRobotoSlabNrml = faceRobotoSlabNrml;
        this.faceLatoNormal = faceLatoNormal;
        this.orderItemDisplaySource = orderItemDisplaySource;
        //this.showCartOperationBtn = showCartOperationBtn;
        this.faceLatoLight = faceLatoLight;
        this.fulfillmentInfoIdAndIconHashMap = fulfillmentInfoIdAndIconHashMap;
        this.isReadOnlyBasket = isReadOnlyBasket;
        this.annotationHashMap = annotationHashMap;
        this.baseImgUrl = baseImageUrl;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                topCatTotalItems.setText(cartItemList.getTopCatItems() + " Items: ");
            } else {
                topCatTotalItems.setText(cartItemList.getTopCatItems() + " Item: ");
            }
        } else {
            topCatTotalItems.setVisibility(View.GONE);
        }


        String topCatTotalAmount = baseActivity.getDecimalAmount(cartItemList.getTopCatTotal());
        TextView topCatTotal = headerTitleHolder.getTopCatTotal();
        if (!topCatTotalAmount.equals("0")) {
            topCatTotal.setVisibility(View.VISIBLE);
            String regularSalePriceStr = "`" + topCatTotalAmount;
            Spannable regularSpannable = new SpannableString(regularSalePriceStr);
            regularSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), 0,
                    1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
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
                row = inflater.inflate(R.layout.uiv3_cart_item_row, null);
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
            row = inflater.inflate(R.layout.uiv3_category_row, null);
            headerTitleHolder = new HeaderTitleHolder(row);
            renderHeaderView(headerTitleHolder, position, (CartItemHeader) obj);
        }
        return row;
    }

    private View getFulfillmentInfo(Object obj) {
        ShowFulfillmentInfo showFulfillmentInfo = new ShowFulfillmentInfo((FulfillmentInfo) obj, baseActivity);
        View view = showFulfillmentInfo.showFulfillmentInfo(true, true);
        if (view == null) {
            Log.e("************************************", "null coming");
        } else {
            return view;
        }
        return null;
    }

    private View showAnnotationInfo(Object obj) {
        ShowAnnotationInfo showAnnotationInfo = new ShowAnnotationInfo((AnnotationInfo) obj, baseActivity);
        View view = showAnnotationInfo.showAnnotationInfo();
        if (view != null)
            return view;
        return null;
    }

    private void renderBasicView(RowHolder rowHolder, int childPosition, CartItem cartItem) {
        ImageView imgProduct = rowHolder.getImgProduct();
        if (imgProduct != null && !TextUtils.isEmpty(cartItem.getProductImgUrl())) {
            ImageLoader.getInstance().displayImage(baseImgUrl != null ? baseImgUrl + cartItem.getProductImgUrl() : cartItem.getProductImgUrl(),
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
        //txtSalePrice.setTypeface(faceRobotoSlabNrml);
        if (cartItem.getTotalPrice() > 0) {
            String salePriceText = baseActivity.getDecimalAmount(cartItem.getTotalPrice());
            Spannable salePriceSpannable = new SpannableString("` " + salePriceText);
            salePriceSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), 0, 1,
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            salePriceSpannable.setSpan(new ForegroundColorSpan(baseActivity.getResources().getColor(R.color.medium_grey)),
                    0, 1, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            txtSalePrice.setText(salePriceSpannable);
        } else {
            txtSalePrice.setText("Free!");
        }


        TextView txtTotalPriceAndQty = rowHolder.getTxtTotalPriceAndQty();
        //txtTotalPriceAndQty.setTypeface(faceRobotoSlabNrml);
        String qtyStr = "(" + UIUtil.roundOrInt(cartItem.getTotalQty());
        String separator = " @ ";
        String rupeeSign = "`";
        String totalSalePriceStr = baseActivity.getDecimalAmount(cartItem.getSalePrice()) + ")";
        Spannable totalPriceAndQtySpannable = new SpannableString(qtyStr + separator + rupeeSign + totalSalePriceStr);
        totalPriceAndQtySpannable.setSpan(new CustomTypefaceSpan("", faceRupee), qtyStr.length() + separator.length(),
                qtyStr.length() + separator.length() + rupeeSign.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        txtTotalPriceAndQty.setText(totalPriceAndQtySpannable);

        TextView txtSaving = rowHolder.getTxtSaving();

        TextView lblSaving = rowHolder.getLblSaving();
        if (cartItem.getSaving() > 0) {
            lblSaving.setVisibility(View.VISIBLE);
            txtSaving.setVisibility(View.VISIBLE);
            Spannable savingSpannable = new SpannableString("`" + baseActivity.getDecimalAmount(cartItem.getSaving()));
            savingSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
            txtSaving.setText(savingSpannable);
        } else {
            lblSaving.setVisibility(View.GONE);
            txtSaving.setVisibility(View.GONE);
        }

        final String productId = String.valueOf(cartItem.getSkuId());

        final TextView itemCountTxtView = rowHolder.getItemCountTxtView();
        if (cartItem.getTotalQty() > 0) {
            itemCountTxtView.setVisibility(View.VISIBLE);
            int itemCount = (int) cartItem.getTotalQty();
            itemCountTxtView.setText(String.valueOf(itemCount));
        } else {
            itemCountTxtView.setVisibility(View.GONE);
        }

        final ImageView imgDec = rowHolder.getImgDec();
        final ImageView imgAdd = rowHolder.getImgAdd();
        final ImageView imgRemove = rowHolder.getImgRemove();
        final View basketOperationSeparatorLine = rowHolder.getBasketOperationSeparatorLine();
        if (imgDec != null && imgAdd != null && imgRemove != null) {
            if (orderItemDisplaySource == OrderItemDisplaySource.BASKET && !isReadOnlyBasket && cartItem.getTotalPrice() > 0) {
                itemCountTxtView.setVisibility(View.VISIBLE);
                imgAdd.setVisibility(View.VISIBLE);
                imgDec.setVisibility(View.VISIBLE);
                imgRemove.setVisibility(View.VISIBLE);
                basketOperationSeparatorLine.setVisibility(View.VISIBLE);
                imgDec.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(baseActivity)) {
                            BasketOperationTask<BaseFragment> basketOperationTask = new BasketOperationTask<>(fragment,
                                    BasketOperation.DEC, productId,
                                    null, null, null, null, null
                            );
                            basketOperationTask.startTask();
                        } else {
                            Toast toast = Toast.makeText(baseActivity, "Unable to connect to Internet", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }
                });
                imgAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(baseActivity)) {
                            BasketOperationTask<BaseFragment> basketOperationTask = new BasketOperationTask<>(fragment,
                                    BasketOperation.INC, productId,
                                    null, null, null, null, null
                            );
                            basketOperationTask.startTask();

                        } else {
                            Toast toast = Toast.makeText(baseActivity, "Unable to connect to Internet", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }
                });
                imgRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (DataUtil.isInternetAvailable(baseActivity)) {
                            BasketOperationTask<BaseFragment> basketOperationTask = new BasketOperationTask<>(fragment,
                                    BasketOperation.EMPTY,
                                    productId, itemCountTxtView, null, null, null, null, "0"
                            );
                            basketOperationTask.startTask();
                        } else {
                            Toast toast = Toast.makeText(baseActivity, "Unable to connect to Internet", Toast.LENGTH_LONG);
                            toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                            toast.show();
                        }
                    }
                });
            } else {
                itemCountTxtView.setVisibility(View.GONE);
                imgAdd.setVisibility(View.GONE);
                imgDec.setVisibility(View.GONE);
                imgRemove.setVisibility(View.GONE);
                basketOperationSeparatorLine.setVisibility(View.GONE);
            }
        }
    }

    private void getRegularPriceAndNoPromoView(RowHolder rowHolder, CartItem cartItem) {

        View promoSeparatorLine = rowHolder.getPromoSeparatorLine();
        if (promoSeparatorLine != null) {
            promoSeparatorLine.setVisibility(View.GONE);
        }

        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.GONE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        //txtRegularPriceAndQty.setTypeface(faceRobotoSlabNrml);
        txtRegularPriceAndQty.setVisibility(View.GONE);

        TextView lblRegularPrice = rowHolder.getLblRegularPrice();
        //lblRegularPrice.setTypeface(faceRobotoSlabLight);
        lblRegularPrice.setVisibility(View.GONE);

        ImageView imgPromoUsed = rowHolder.getImgPromoUsed();
        imgPromoUsed.setVisibility(View.GONE);

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        //txtPromoPriceAndQty.setTypeface(faceRobotoSlabNrml);
        txtPromoPriceAndQty.setVisibility(View.GONE);

        TextView txtPromoNameDesc = rowHolder.getTxtPromoNameDesc();
        //txtPromoNameDesc.setTypeface(faceRobotoSlabNrml);
        txtPromoNameDesc.setVisibility(View.GONE);
    }


    private void getRegularPriceAndPromoNotAppliedView(RowHolder rowHolder, CartItem cartItem) {

        View promoSeparatorLine = rowHolder.getPromoSeparatorLine();
        promoSeparatorLine.setVisibility(View.VISIBLE);

        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.GONE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        //txtRegularPriceAndQty.setTypeface(faceRobotoSlabNrml);
        txtRegularPriceAndQty.setVisibility(View.GONE);

        TextView lblRegularPrice = rowHolder.getLblRegularPrice();
        //lblRegularPrice.setTypeface(faceRobotoSlabLight);
        lblRegularPrice.setVisibility(View.GONE);

        ImageView imgPromoUsed = rowHolder.getImgPromoUsed();
        imgPromoUsed.setVisibility(View.VISIBLE);   // gone
        imgPromoUsed.setBackgroundResource(R.drawable.promo_unused);  //unused

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        txtPromoPriceAndQty.setVisibility(View.VISIBLE);  // gone
        txtPromoPriceAndQty.setText("Promo not used  ");
        //txtPromoPriceAndQty.setTypeface(faceRobotoSlabNrml);

        TextView txtPromoNameDesc = rowHolder.getTxtPromoNameDesc();
        txtPromoNameDesc.setVisibility(View.VISIBLE);
        //txtPromoNameDesc.setTypeface(faceRobotoSlabLight);
        String promoTxtName = cartItem.getCartItemPromoInfo().getPromoInfo().getPromoName();
        txtPromoNameDesc.setText(promoTxtName);
        txtPromoNameDesc.setTextColor(baseActivity.getResources().getColor(R.color.dark_red));

        if (orderItemDisplaySource == OrderItemDisplaySource.BASKET) {
            txtPromoNameDesc.setOnClickListener(new PromoListener(cartItem.getCartItemPromoInfo().getPromoInfo().getPromoId()));
        }

    }

    private void getPromoAppliedAndPromoPricingView(RowHolder rowHolder, CartItem cartItem) {

        View promoSeparatorLine = rowHolder.getPromoSeparatorLine();
        promoSeparatorLine.setVisibility(View.VISIBLE);

        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.GONE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        //txtRegularPriceAndQty.setTypeface(faceRobotoSlabNrml);
        txtRegularPriceAndQty.setVisibility(View.GONE);

        TextView lblRegularPrice = rowHolder.getLblRegularPrice();
        //lblRegularPrice.setTypeface(faceRobotoSlabLight);
        lblRegularPrice.setVisibility(View.GONE);

        ImageView imgPromoUsed = rowHolder.getImgPromoUsed();
        imgPromoUsed.setVisibility(View.VISIBLE);

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        //txtPromoPriceAndQty.setTypeface(faceRobotoSlabNrml);
        txtPromoPriceAndQty.setVisibility(View.GONE);

        TextView txtPromoNameDesc = rowHolder.getTxtPromoNameDesc();
        txtPromoNameDesc.setVisibility(View.VISIBLE);
        //txtPromoNameDesc.setTypeface(faceRobotoSlabLight);
        String promoTxtName = cartItem.getCartItemPromoInfo().getPromoInfo().getPromoName();
        txtPromoNameDesc.setText(promoTxtName);
        if (false) { // TODO : Change this with equivalent of "if (baseActivity instanceof ShowCartActivity)"
            txtPromoNameDesc.setTextColor(baseActivity.getResources().getColor(R.color.promo_txt_green_color));
        } else {
            txtPromoNameDesc.setTextColor(baseActivity.getResources().getColor(R.color.link_color));
        }
        if (orderItemDisplaySource == OrderItemDisplaySource.BASKET) {
            txtPromoNameDesc.setOnClickListener(new PromoListener(cartItem.getCartItemPromoInfo().getPromoInfo().getPromoId()));
        }

    }

    private void getPromoAppliedAndMixedPromoPricingView(RowHolder rowHolder, CartItem cartItem) {

        View promoSeparatorLine = rowHolder.getPromoSeparatorLine();
        promoSeparatorLine.setVisibility(View.VISIBLE);

        ImageView imgRegularImg = rowHolder.getImgRegularImg();
        imgRegularImg.setVisibility(View.VISIBLE);

        TextView txtRegularPriceAndQty = rowHolder.getTxtRegularPriceAndQty();
        txtRegularPriceAndQty.setVisibility(View.VISIBLE);
        //txtRegularPriceAndQty.setTypeface(faceRobotoSlabNrml);
        String regularQtyStr = UIUtil.roundOrInt(cartItem.getCartItemPromoInfo().
                getRegularInfo().getNumItemInCart());
        String separator = " @ ";
        String regularSalePriceStr = "`" + baseActivity.getDecimalAmount(cartItem.getCartItemPromoInfo().getRegularInfo().getSalePrice());
        Spannable regularSpannable = new SpannableString(regularQtyStr + separator + regularSalePriceStr);
        regularSpannable.setSpan(new CustomTypefaceSpan("", faceRupee), regularQtyStr.length()
                        + separator.length(), regularQtyStr.length() + separator.length() + 1,
                Spanned.SPAN_EXCLUSIVE_INCLUSIVE
        );
        regularSpannable.setSpan(new ForegroundColorSpan(baseActivity.getResources().getColor(R.color.tabDark)), regularSalePriceStr.length() - 1,
                regularQtyStr.length() + separator.length() + regularSalePriceStr.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        regularSpannable.setSpan(new ForegroundColorSpan(baseActivity.getResources().getColor(R.color.medium_grey)), 0,
                regularQtyStr.length() + separator.length(), Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
        txtRegularPriceAndQty.setText(regularSpannable);

        TextView txtPromoPriceAndQty = rowHolder.getTxtPromoPriceAndQty();
        txtPromoPriceAndQty.setVisibility(View.VISIBLE);
        //txtPromoPriceAndQty.setTypeface(faceRobotoSlabNrml);
        if (cartItem.getCartItemPromoInfo().getPromoInfo().getSalePrice() > 0) {
            String promoQtyStr = UIUtil.roundOrInt(cartItem.getCartItemPromoInfo().
                    getPromoInfo().getNumItemInCart());
            String promoSalePriceStr = "`" + baseActivity.getDecimalAmount(cartItem.getCartItemPromoInfo().getPromoInfo().getSalePrice());
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
        //txtPromoNameDesc.setTypeface(faceRobotoSlabLight);
        String promoTxtName = cartItem.getCartItemPromoInfo().getPromoInfo().getPromoName();
        txtPromoNameDesc.setText(promoTxtName);
        if (false) { // TODO : Change this with equivalent of "if (baseActivity instanceof ShowCartActivity)"
            txtPromoNameDesc.setTextColor(baseActivity.getResources().getColor(R.color.promo_txt_green_color));
        } else {
            txtPromoNameDesc.setTextColor(baseActivity.getResources().getColor(R.color.link_color));
        }
        if (orderItemDisplaySource == OrderItemDisplaySource.BASKET) {
            txtPromoNameDesc.setOnClickListener(new PromoListener(cartItem.getCartItemPromoInfo().getPromoInfo().getPromoId()));
        }
    }

    private class RowHolder {
        private ImageView imgProduct;
        private TextView txtProductDesc;
        private TextView txtSalePrice;
        private TextView txtTotalPriceAndQty;
        private TextView txtSaving;
        private TextView lblSaving;
        private ImageView imgRegularImg;
        private TextView txtRegularPriceAndQty;
        private TextView lblRegularPrice;
        private ImageView imgPromoUsed;
        private TextView txtPromoPriceAndQty;
        private TextView txtPromoNameDesc;
        private TextView itemCountTxtView;
        private ImageView imgDec;
        private ImageView imgAdd;
        private ImageView imgRemove;
        private ImageView imgLiquorIcon;
        private View base;
        private View promoSeparatorLine;
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

        public View getPromoSeparatorLine() {
            if (promoSeparatorLine == null)
                promoSeparatorLine = base.findViewById(R.id.promoSeparatorLine);
            return promoSeparatorLine;
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

        public TextView getTxtTotalPriceAndQty() {
            if (txtTotalPriceAndQty == null) {
                txtTotalPriceAndQty = (TextView) base.findViewById(R.id.txtTotalPriceAndQty);
                txtTotalPriceAndQty.setTypeface(faceRobotoRegular);
            }
            return txtTotalPriceAndQty;
        }

        public TextView getTxtSaving() {
            if (txtSaving == null) {
                txtSaving = (TextView) base.findViewById(R.id.txtSaving);
                txtSaving.setTypeface(faceRobotoRegular);
            }
            return txtSaving;
        }

        public TextView getLblSaving() {
            if (lblSaving == null) {
                lblSaving = (TextView) base.findViewById(R.id.lblSaving);
                lblSaving.setTypeface(faceRobotoRegular);
            }
            return lblSaving;
        }

        public TextView getItemCountTxtView() {
            if (itemCountTxtView == null) {
                itemCountTxtView = (TextView) base.findViewById(R.id.itemCountTxtView);
                itemCountTxtView.setTypeface(faceRobotoRegular);
            }
            return itemCountTxtView;
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

        public ImageView getImgAdd() {
            if (imgAdd == null)
                imgAdd = (ImageView) base.findViewById(R.id.imgAdd);
            return imgAdd;
        }

        public ImageView getImgDec() {
            if (imgDec == null)
                imgDec = (ImageView) base.findViewById(R.id.imgDec);
            return imgDec;
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
            baseActivity.onChangeFragment(promoDetailFragment);
        }
    }
}
