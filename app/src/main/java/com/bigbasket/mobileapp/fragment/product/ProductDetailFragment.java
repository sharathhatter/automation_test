package com.bigbasket.mobileapp.fragment.product;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ProductDetailApiResponse;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.common.ProductViewHolder;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.handler.click.OnBrandPageListener;
import com.bigbasket.mobileapp.handler.click.OnPromoClickListener;
import com.bigbasket.mobileapp.handler.click.OnSectionItemClickListener;
import com.bigbasket.mobileapp.handler.click.OnSpecialityShopIconClickListener;
import com.bigbasket.mobileapp.handler.click.basket.OnProductBasketActionListener;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.ShoppingListNamesAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.ProductAdditionalInfo;
import com.bigbasket.mobileapp.model.product.ProductTag;
import com.bigbasket.mobileapp.model.product.ProductViewDisplayDataHolder;
import com.bigbasket.mobileapp.model.promo.Promo;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListOption;
import com.bigbasket.mobileapp.task.uiv3.CreateShoppingListTask;
import com.bigbasket.mobileapp.task.uiv3.ShoppingListDoAddDeleteTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FlatPageHelper;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.view.uiv2.ProductView;
import com.bigbasket.mobileapp.view.uiv3.FlowLayout;
import com.bigbasket.mobileapp.view.uiv3.ShoppingListNamesDialog;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;


public class ProductDetailFragment extends BaseFragment implements ShoppingListNamesAware {

    private String selectedProductId;
    private Product mProduct;
    private String mTitle;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_product_detail, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mProduct = savedInstanceState.getParcelable(Constants.PRODUCT);
            if (mProduct != null) {
                renderProductDetail();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProductDetail();
    }

    private void logProductDetailEvent() {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.PRODUCT_TOP_CAT, mProduct.getTopLevelCategoryName());
        map.put(TrackEventkeys.PRODUCT_CAT, mProduct.getProductCategoryName());
        map.put(TrackEventkeys.PRODUCT_BRAND, mProduct.getBrand());
        map.put(TrackEventkeys.PRODUCT_ID, mProduct.getSku());
        String desc = mProduct.getDescription();
        if (!TextUtils.isEmpty(mProduct.getPackageDescription()))
            desc = " " + mProduct.getWeightAndPackDesc();
        map.put(TrackEventkeys.PRODUCT_DESC, desc);
        trackEvent(TrackingAware.PRODUCT_DETAIL_SHOWN, map);
        trackEventAppsFlyer(TrackingAware.PRODUCT_DETAIL_SHOWN);
    }

    private void loadProductDetail() {
        Bundle args = getArguments();
        if (args == null) return;
        String productId = args.getString(Constants.SKU_ID);
        String eanCode = args.getString(Constants.EAN_CODE);
        if (TextUtils.isEmpty(productId) && TextUtils.isEmpty(eanCode)) return;
        setCurrentScreenName("pd." + (productId != null ? productId : eanCode));
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<ProductDetailApiResponse> call = bigBasketApiService.productDetails(getPreviousScreenName(), productId, eanCode);
        call.enqueue(new BBNetworkCallback<ProductDetailApiResponse>(this, true) {
            @Override
            public void onSuccess(ProductDetailApiResponse productDetailApiResponse) {
                switch (productDetailApiResponse.status) {
                    case Constants.OK:
                        mProduct = productDetailApiResponse.product;
                        renderProductDetail();
                        break;
                    default:
                        handler.sendEmptyMessage(productDetailApiResponse.getErrorTypeAsInt(),
                                productDetailApiResponse.message, true);
                        break;
                }
            }

            @Override
            public boolean updateProgress() {
                try {
                    hideProgressDialog();
                    return true;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            }
        });
    }

    private void renderProductDetail() {
        if (getActivity() == null || getView() == null) return;

        mTitle = mProduct != null && !TextUtils.isEmpty(mProduct.getDescription())
                ? mProduct.getDescription() : getTitle();
        if (!TextUtils.isEmpty(mTitle)) {
            setTitle(mTitle);
        }
        ProductViewDisplayDataHolder productViewDisplayDataHolder = new ProductViewDisplayDataHolder.Builder()
                .setCommonTypeface(faceRobotoRegular)
                .setSansSerifMediumTypeface(faceRobotoMedium)
                .setRupeeTypeface(faceRupee)
                .setHandler(handler)
                .setLoggedInMember(!AuthParameters.getInstance(getActivity()).isAuthTokenEmpty())
                .setShowShoppingListBtn(true)
                .setShowBasketBtn(true)
                .setShowShopListDeleteBtn(false)
                .disableInBasketChildSwap(true)
                .useRadioButtonsForContextual(true)
                .showQtyInput(AuthParameters.getInstance(getActivity()).isKirana())
                .build();

        LinearLayout layoutProductDetail = (LinearLayout) getView().findViewById(R.id.layoutProductDetail);
        layoutProductDetail.removeAllViews();
        layoutProductDetail.setPadding(0, 0, 0, 36);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View productRow = inflater.inflate(R.layout.uiv3_product_detail_row, layoutProductDetail, false);

        AppDataDynamic appDataDynamic = AppDataDynamic.getInstance(getActivity());
        ProductViewHolder productViewHolder = new ProductViewHolder(productRow);
        productViewHolder.setSpecialityShopIconClickListener(
                new OnSpecialityShopIconClickListener<>(this, appDataDynamic.getSpecialityStoreDetailList()));
        productViewHolder.setPromoClickListener(new OnPromoClickListener<>(this));
        productViewHolder.setBrandPageListener(new OnBrandPageListener<>(this));
        productViewHolder.setBasketIncActionListener(new OnProductBasketActionListener(BasketOperation.INC, this));
        productViewHolder.setBasketDecActionListener(new OnProductBasketActionListener(BasketOperation.DEC, this));
        ProductView.setProductView(productViewHolder,
                mProduct, null, productViewDisplayDataHolder,
                false, this, getCurrentScreenName(), null, "none",
                appDataDynamic.getStoreAvailabilityMap(),
                appDataDynamic.getSpecialityStoreDetailList());

        if (mProduct.getProductPromoInfo() == null ||
                !Promo.getAllTypes().contains(mProduct.getProductPromoInfo().getPromoType())) {
            productRow.findViewById(R.id.layoutPromoInfoBox).setVisibility(View.GONE);
        }
        layoutProductDetail.addView(productRow);

        TextView txtVariableWeightMsg = (TextView) productRow.findViewById(R.id.txtVariableWeightMsg);
        String variableWeightMsg = null, knowMoreLink = null;
        SpannableStringBuilder spannableBuilder = null;
        int start;
        boolean hasMsg = false;
        if (mProduct.getStoreAvailability() != null && appDataDynamic.getStoreAvailabilityMap() != null) {
            for (int i = 0; i < mProduct.getStoreAvailability().size(); i++) {
                if (appDataDynamic.getStoreAvailabilityMap().containsKey(mProduct.getStoreAvailability().get(i).get(Constants.AVAILABILITY_INFO_ID))) {
                    if (mProduct.getStoreAvailability().get(i).containsKey(Constants.VARIABLE_WEIGHT_MSG)) {
                        hasMsg = true;
                        variableWeightMsg = mProduct.getStoreAvailability().get(i).get(Constants.VARIABLE_WEIGHT_MSG);
                        if (mProduct.getStoreAvailability().get(i).containsKey(Constants.VARIABLE_WEIGHT_LINK)) {
                            knowMoreLink = mProduct.getStoreAvailability().get(i).get(Constants.VARIABLE_WEIGHT_LINK);
                        }
                        break;
                    }
                }
            }
        }
        if (!hasMsg) {
            if (!TextUtils.isEmpty(mProduct.getVariableWeightMsg())) {
                variableWeightMsg = mProduct.getVariableWeightMsg();
                knowMoreLink = mProduct.getKnowMoreLink();
            }
        }
        if (!TextUtils.isEmpty(variableWeightMsg)) {
            spannableBuilder = new SpannableStringBuilder(variableWeightMsg).append(" ");
            start = spannableBuilder.length();
            spannableBuilder.setSpan(new CustomTypefaceSpan(faceRobotoRegular), 0, start,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (!TextUtils.isEmpty(knowMoreLink)) {
                final UnderlineSpan underLineSpan = new UnderlineSpan();
                CustomClickableSpan customClickableSpan = new CustomClickableSpan(getActivity(), knowMoreLink);
                spannableBuilder.append(getString(R.string.know_more));
                int end = spannableBuilder.length();
                spannableBuilder.setSpan(customClickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableBuilder.setSpan(new CustomTypefaceSpan(faceRobotoMedium), start, end,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableBuilder.setSpan(underLineSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannableBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(getActivity(),
                        R.color.uiv3_link_color)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                txtVariableWeightMsg.setHighlightColor(Color.TRANSPARENT);
                txtVariableWeightMsg.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

        if (!TextUtils.isEmpty(spannableBuilder)) {
            txtVariableWeightMsg.setText(spannableBuilder);
            txtVariableWeightMsg.setVisibility(View.VISIBLE);
        } else {
            txtVariableWeightMsg.setVisibility(View.GONE);
        }

        //adding the food type to the detail view of a product
        if (mProduct.getProductTag() != null && mProduct.getProductTag().size() > 0) {
//            on clicklistener for the food type
            View.OnClickListener onFoodTypeClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new OnSectionItemClickListener<>((AppOperationAware) getActivity())
                            .handleDestinationClick((DestinationInfo) v.getTag(R.id.destination_info));
                }
            };

            for (ProductTag productTag : mProduct.getProductTag()) {
                if (productTag.getType() == 1) {
                    View productFoodType = inflater.inflate(R.layout.uiv3_heading_textview, layoutProductDetail, false);
                    TextView txtFoodTypeHeading = (TextView) productFoodType.findViewById(R.id.txtFoodTypeHeading);
                    txtFoodTypeHeading.setTypeface(faceRobotoMedium);
                    txtFoodTypeHeading.setText(productTag.getHeader());
                    layoutProductDetail.addView(productFoodType);
                    FlowLayout tagsFlowLayout = (FlowLayout) productFoodType.findViewById(R.id.tagsFlowLayout);
                    List<HashMap<String, DestinationInfo>> values = productTag.getValues();
                    for (HashMap<String, DestinationInfo> v : values) {
                        for (Map.Entry<String, DestinationInfo> e : v.entrySet()) {
                            TextView txtFoodType = (TextView) inflater.inflate(R.layout.uiv3_foodtype_tag, layoutProductDetail, false);
                            txtFoodType.setTypeface(faceRobotoRegular);
                            txtFoodType.setText(e.getKey());
                            /**
                             * adding tags to the textview food-type
                             * values of tags are used to launch the product list passing these values as parameters
                             */
                            DestinationInfo destinationInfo = e.getValue();
                            if (destinationInfo != null) {
                                txtFoodType.setTag(R.id.destination_info, destinationInfo);
                                txtFoodType.setOnClickListener(onFoodTypeClickListener);
                            }
                            //adding view to the flowlayout
                            tagsFlowLayout.addView(txtFoodType);
                        }
                    }
                }
            }
        }

        ArrayList<ProductAdditionalInfo> productAdditionalInfos = mProduct.getProductAdditionalInfos();
        if (productAdditionalInfos != null && productAdditionalInfos.size() > 0) {
            for (ProductAdditionalInfo productAdditionalInfo : productAdditionalInfos) {
                View additionalInfoView = inflater.inflate(R.layout.uiv3_product_add_desc, layoutProductDetail, false);
                TextView txtProductAddDescTitle = (TextView) additionalInfoView.findViewById(R.id.txtProductAddDescTitle);
                TextView txtProductAddDescContent = (TextView) additionalInfoView.findViewById(R.id.txtProductAddDescContent);
                txtProductAddDescContent.setTypeface(faceRobotoRegular);
                txtProductAddDescTitle.setTypeface(faceRobotoMedium);
                txtProductAddDescContent.setText(Html.fromHtml(productAdditionalInfo.getContent()));
                if (!TextUtils.isEmpty(productAdditionalInfo.getTitle())) {
                    txtProductAddDescTitle.setText(Html.fromHtml(productAdditionalInfo.getTitle()));
                } else {
                    txtProductAddDescTitle.setVisibility(View.GONE);
                }
                layoutProductDetail.addView(additionalInfoView);
            }
        }
        //logging the product detail events
        logProductDetailEvent();
    }

    @Override
    public String getTitle() {
        return TextUtils.isEmpty(mTitle) ? "" : mTitle;
    }

    @Override
    public ViewGroup getContentView() {
        return null;
    }

    @Override
    public void onShoppingListFetched(ArrayList<ShoppingListName> shoppingListNames) {
        if (shoppingListNames == null) {
            shoppingListNames = new ArrayList<>();
        }
        ShoppingListNamesDialog shoppingListNamesDialog = ShoppingListNamesDialog.newInstance(shoppingListNames);
        shoppingListNamesDialog.setTargetFragment(this, 0);
        shoppingListNamesDialog.show(getFragmentManager(), Constants.SHOP_LST);
    }

    @Override
    public String getSelectedProductId() {
        return selectedProductId;
    }

    @Override
    public void setSelectedProductId(String selectedProductId) {
        this.selectedProductId = selectedProductId;
    }

    @Override
    public void postShoppingListItemDeleteOperation() {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }

    @Override
    public void addToShoppingList(List<ShoppingListName> selectedShoppingListNames) {
        if (getActivity() == null) return;
        if (selectedShoppingListNames == null || selectedShoppingListNames.size() == 0) {
            Toast.makeText(getActivity(), getString(R.string.chooseShopList), Toast.LENGTH_SHORT).show();
            return;
        }
        ShoppingListDoAddDeleteTask shoppingListDoAddDeleteTask =
                new ShoppingListDoAddDeleteTask<>(this, selectedShoppingListNames,
                        ShoppingListOption.ADD_TO_LIST);
        shoppingListDoAddDeleteTask.startTask();
    }

    @Override
    public void postAddToShoppingListOperation() {
        if (getCurrentActivity() == null) return;
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }

    @Override
    public void createNewShoppingList() {
        new CreateShoppingListTask<>(this).showDialog();
    }

    @Override
    public void onNewShoppingListCreated(String listName) {
        if (getCurrentActivity() == null) return;
        Toast.makeText(getCurrentActivity(),
                "List \"" + listName
                        + "\" was created successfully", Toast.LENGTH_LONG).show();
        getCurrentActivity().setResult(NavigationCodes.SHOPPING_LIST_MODIFIED);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mProduct != null) {
            outState.putParcelable(Constants.PRODUCT, mProduct);
        }
        super.onSaveInstanceState(outState);
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return ProductDetailFragment.class.getName();
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.PRODUCT_DETAIL_SCREEN;
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation,
                                                    @Nullable WeakReference<TextView> basketCountTextViewRef,
                                                    @Nullable WeakReference<View> viewDecQtyRef,
                                                    @Nullable WeakReference<View> viewIncQtyRef,
                                                    @Nullable WeakReference<View> btnAddToBasketRef,
                                                    Product product, String qty,
                                                    @Nullable WeakReference<View> productViewRef,
                                                    @Nullable WeakReference<HashMap<String, Integer>> cartInfoMapRef,
                                                    @Nullable WeakReference<EditText> editTextQtyRef) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextViewRef, viewDecQtyRef,
                viewIncQtyRef, btnAddToBasketRef, product, qty, productViewRef, cartInfoMapRef, editTextQtyRef);
        int productQtyInBasket = 0;
        if (basketOperationResponse.getBasketResponseProductInfo() != null) {
            productQtyInBasket = basketOperationResponse.getBasketResponseProductInfo().getTotalQty();
        }
        if (product != null && getActivity() != null) {
            Intent data = new Intent();
            data.putExtra(Constants.SKU_ID, product.getSku());
            data.putExtra(Constants.NO_ITEM_IN_CART, productQtyInBasket);
            data.putExtra(Constants.PRODUCT_NO_ITEM_IN_CART, product.getNoOfItemsInCart());
            getActivity().setResult(NavigationCodes.BASKET_CHANGED, data);
        }
    }

    @NonNull
    @Override
    public String getInteractionName() {
        return "ProductDetailFragment";
    }

    public static class CustomClickableSpan extends android.text.style.ClickableSpan {
        private Activity activity;
        private String knowMoreLink;

        public CustomClickableSpan(Activity activity, String knowMoreLink) {
            this.activity = activity;
            this.knowMoreLink = knowMoreLink;
        }

        @Override
        public void onClick(View widget) {
            FlatPageHelper.openFlatPage(activity, knowMoreLink, null);
        }
    }
}