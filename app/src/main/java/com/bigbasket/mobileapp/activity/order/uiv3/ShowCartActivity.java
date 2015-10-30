package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.CartGetApiResponseContent;
import com.bigbasket.mobileapp.fragment.ShowCartFragment;
import com.bigbasket.mobileapp.interfaces.BasketChangeQtyAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.cart.AnnotationInfo;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.CartItem;
import com.bigbasket.mobileapp.model.cart.CartItemList;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.cart.FulfillmentInfo;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShowCartActivity extends BackButtonActivity implements BasketChangeQtyAware {

    private ArrayList<CartItemList> cartItemLists;
    private ArrayList<FulfillmentInfo> fulfillmentInfos;
    private ArrayList<AnnotationInfo> annotationInfoArrayList;
    @Nullable
    private MenuItem basketMenuItem;
    private int currentItemPosition;
    private int currentTabIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.my_basket_header));
        setNextScreenNavigationContext(TrackEventkeys.CO_BASKET);
    }

    @Override
    public void onResume() {
        super.onResume();
        getCartItems(null, false);
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        super.setOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.empty_basket, menu);
        basketMenuItem = menu.getItem(0);
        basketMenuItem.setVisible(false);
    }

    @Override
    public int getMainLayout() {
        return R.layout.uiv3_show_cart_layout;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_empty_basket:
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
                String numItems = preferences.getString(Constants.GET_CART, "0");
                if (!TextUtils.isEmpty(numItems) && !numItems.equals("0")) {
                    showAlertDialog(null, getString(R.string.removeAllProducts), DialogButton.YES,
                            DialogButton.NO, Constants.EMPTY_BASKET, null, getString(R.string.emptyBasket));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void renderCheckoutLayout(CartSummary cartSummary, boolean isCurrentPageRequest) {
        Map<String, String> eventAttribs = new HashMap<>();
        int numItems = 0;
        for (CartItemList cartItemInfoArray : cartItemLists) {
            eventAttribs.put(cartItemInfoArray.getTopCatName() + " Items", String.valueOf(cartItemInfoArray.getTopCatItems()));
            eventAttribs.put(cartItemInfoArray.getTopCatName() + " Value", String.valueOf(cartItemInfoArray.getTopCatTotal()));
            int cartItemsSize = cartItemInfoArray.getCartItems().size();
            numItems += cartItemsSize;
            ArrayList<CartItem> cartItems = cartItemInfoArray.getCartItems();
            for (int i = 0; i < cartItemsSize; i++) {
                if (cartItems.get(i).getPromoAppliedType() == 2 ||
                        cartItems.get(i).getPromoAppliedType() == 3) {
                    HashMap<String, String> map = new HashMap<>();
                    if (isCurrentPageRequest) {
                        map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                    } else {
                        map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentNavigationContext());
                    }
                    trackEvent(TrackingAware.PROMO_REDEEMED, map);
                }
            }
        }

        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        if (!layoutCheckoutFooter.isShown()) layoutCheckoutFooter.setVisibility(View.VISIBLE);
        final String cartTotal = UIUtil.formatAsMoney(cartSummary.getTotal());
        TextView txtNumOfItems = (TextView) layoutCheckoutFooter.findViewById(R.id.txtNumOfItems);
        if (txtNumOfItems != null) {
            txtNumOfItems.setVisibility(View.VISIBLE);
            txtNumOfItems.setTypeface(faceRobotoRegular);
            if (numItems > 0) {
                String itemString = numItems > 1 ? " Items" : " Item";
                txtNumOfItems.setText(numItems + itemString);
            }
            if (basketMenuItem != null) basketMenuItem.setVisible(numItems > 0);
        }

        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, cartTotal,
                getString(R.string.checkOut), true);
        int dp16 = (int) getResources().getDimension(R.dimen.margin_normal);
        int dp8 = (int) getResources().getDimension(R.dimen.margin_small);
        layoutCheckoutFooter.setPadding(dp16, dp8, dp16, dp8);

        layoutCheckoutFooter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, String> map = new HashMap<>();
                map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                trackEvent(TrackingAware.BASKET_CHECKOUT_CLICKED, map);
                if (getCartSummary() != null && getCartSummary().getNoOfItems() > 0) {
                    if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                        launchLogin(TrackEventkeys.NAVIGATION_CTX_SHOW_BASKET, NavigationCodes.GO_TO_BASKET);
                    } else {
                        startCheckout(cartTotal);
                    }
                }
            }
        });

        if (!isCurrentPageRequest)
            logViewBasketEvent(cartSummary, eventAttribs);
    }

    private void logViewBasketEvent(CartSummary cartSummary, Map<String, String> eventAttribs) {
        if (cartSummary == null) return;
        eventAttribs.put(TrackEventkeys.TOTAL_ITEMS_IN_BASKET, String.valueOf(cartSummary.getNoOfItems()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_VALUE, String.valueOf(cartSummary.getTotal()));
        eventAttribs.put(TrackEventkeys.TOTAL_BASKET_SAVING, String.valueOf(cartSummary.getSavings()));
        trackEvent(TrackingAware.BASKET_VIEW_SHOWN, eventAttribs, null, null, false, true);
    }

    private void startCheckout(String cartTotal) {
        Intent intent = new Intent(this, BackButtonActivity.class);
        setNextScreenNavigationContext(TrackEventkeys.CO_BASKET);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_DELIVERY_ADDRESS);
        intent.putExtra(Constants.TOTAL_BASKET_VALUE, cartTotal);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName)) {
            switch (sourceName) {
                case Constants.EMPTY_BASKET:
                    if (cartItemLists != null)
                        emptyCart();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, null, valuePassed);
        }
    }

    private void setBasketNumItemsDisplay() {
        markBasketDirty();
        if (getCartSummary() == null) return;
        updateUIForCartInfo();
    }

    private void emptyCart() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
        trackEvent(TrackingAware.BASKET_EMPTY_CLICKED, map);
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.emptyCart(new Callback<BaseApiResponse>() {
            @Override
            public void success(BaseApiResponse cartEmptyApiResponseCallback, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                markBasketChanged(null);
                if (cartEmptyApiResponseCallback.status == 0) {
                    editor.putString(Constants.GET_CART, "0");
                    showBasketEmptyMessage();
                    CartSummary cartSummary = new CartSummary(0, 0, 0);
                    setCartSummary(cartSummary);
                    setBasketNumItemsDisplay();
                } else if (cartEmptyApiResponseCallback.status == ApiErrorCodes.CART_NOT_EXISTS) {
                    showAlertDialog("Cart is already empty");
                } else {
                    handler.sendEmptyMessage(cartEmptyApiResponseCallback.status,
                            cartEmptyApiResponseCallback.message, true);
                }
                editor.apply();
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void getCartItems(String fulfillmentIds, final boolean isCurrentPageRequest) {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError(true);
            return;
        }
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        final SharedPreferences.Editor editor = prefer.edit();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.cartGet(isCurrentPageRequest ?
                        getNextScreenNavigationContext() : getCurrentNavigationContext(),
                fulfillmentIds, new Callback<ApiResponse<CartGetApiResponseContent>>() {
                    @Override
                    public void success(ApiResponse<CartGetApiResponseContent> cartGetApiResponseContentApiResponse, Response response) {
                        if (isSuspended()) return;
                        hideProgressView();
                        if (cartGetApiResponseContentApiResponse.status == 0) {
                            CartSummary cartSummary = cartGetApiResponseContentApiResponse.apiResponseContent.cartSummary;
                            setCartSummary(cartSummary);
                            setBasketNumItemsDisplay();
                            editor.putString(Constants.GET_CART,
                                    String.valueOf(cartSummary.getNoOfItems()));
                            fulfillmentInfos = cartGetApiResponseContentApiResponse.apiResponseContent.fulfillmentInfos;
                            annotationInfoArrayList = cartGetApiResponseContentApiResponse.apiResponseContent.annotationInfos;

                            if (cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent != null
                                    && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists != null
                                    && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists.size() > 0) {
                                cartItemLists = cartGetApiResponseContentApiResponse.apiResponseContent.
                                        cartGetApiCartItemsContent.cartItemLists;

                                addTabsToPager(AppDataDynamic.getInstance(getCurrentActivity()).isContextualMode(),
                                        cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.baseImgUrl,
                                        fulfillmentInfos, annotationInfoArrayList);

                                renderCheckoutLayout(cartSummary, isCurrentPageRequest);
                            } else {
                                showBasketEmptyMessage();
                                editor.putString(Constants.GET_CART, "0");
                            }
                        } else {
                            handler.sendEmptyMessage(cartGetApiResponseContentApiResponse.status,
                                    cartGetApiResponseContentApiResponse.message, true);
                        }
                        editor.apply();
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        hideProgressView();
                        handler.handleRetrofitError(error, true);
                    }
                });
    }

    private void addTabsToPager(boolean showTabs, String baseImgUrl, ArrayList<FulfillmentInfo> fulfillmentInfos,
                                ArrayList<AnnotationInfo> annotationInfoArrayList) {
        if (showTabs) {
            GenerateTabDataAsync generateTabDataAsync = new GenerateTabDataAsync(baseImgUrl);
            generateTabDataAsync.execute();
        } else {
            ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
            TabLayout pageTitleStrip = (TabLayout) findViewById(R.id.slidingTabs);
            pageTitleStrip.setVisibility(View.GONE);
            if (!mViewPager.isShown()) {
                mViewPager.setVisibility(View.VISIBLE);
            }
            ViewGroup base = (ViewGroup) findViewById(R.id.empty_cart);
            if (base.isShown()) base.setVisibility(View.GONE);

            ArrayList<BBTab> bbTabs = new ArrayList<>();

            createTabFragment(getString(R.string.stnd_delivery), baseImgUrl,
                    cartItemLists, fulfillmentInfos, annotationInfoArrayList, bbTabs, Constants.DEFAULT_TAB_INDEX);

            TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(),
                    bbTabs);
            mViewPager.setAdapter(tabPagerAdapter);
            mViewPager.setCurrentItem(currentTabIndex);
        }
    }

    private ArrayList<BBTab> createTabFragment(String tab_name, String baseUrl, ArrayList<CartItemList>
            cartItemList, ArrayList<FulfillmentInfo> fulfillmentInfos, ArrayList<AnnotationInfo>
                                                       annotationInfoArrayList, ArrayList<BBTab> bbTabs,
                                               int currentTabIndex) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.CART_ITEMS, cartItemList);
        bundle.putParcelableArrayList(Constants.FULFILLMENT_INFO, fulfillmentInfos);
        bundle.putParcelableArrayList(Constants.ANNOTATION_INFO, annotationInfoArrayList);
        bundle.putString(Constants.BASE_IMG_URL, baseUrl);
        if (this.currentTabIndex == currentTabIndex) {
            bundle.putInt(Constants.ITEM_SCROLL_POSITION, currentItemPosition);
        }
        bundle.putInt(Constants.CURRENT_TAB_INDEX, currentTabIndex);
        bbTabs.add(new BBTab<>(tab_name, ShowCartFragment.class, bundle));
        return bbTabs;
    }


    @Override
    public void onBasketQtyChanged(int itemPosition, int currentTabIndex) {
        this.currentItemPosition = itemPosition;
        this.currentTabIndex = currentTabIndex;
    }

    private void showBasketEmptyMessage() {
        TabLayout pageTitleStrip = (TabLayout) findViewById(R.id.slidingTabs);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutCheckoutFooter);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);

        ViewGroup base = (ViewGroup) findViewById(R.id.empty_cart);

        ImageView imgEmptyPage = (ImageView) base.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_basket);
        TextView txtEmptyMsg1 = (TextView) base.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.empty_basket_txt1);
        TextView txtEmptyMsg2 = (TextView) base.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setText(R.string.empty_basket_txt2);
        Button btnBlankPage = (Button) base.findViewById(R.id.btnBlankPage);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToHome(false);
            }
        });

        Toolbar toolbar = getToolbar();
        toolbar.setTitle(getString(R.string.my_basket_header));
        if (basketMenuItem != null) basketMenuItem.setVisible(false);

        pageTitleStrip.setVisibility(View.GONE);
        mViewPager.setVisibility(View.GONE);
        linearLayout.setVisibility(View.GONE);
        base.setVisibility(View.VISIBLE);

        markBasketChanged(null);
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(@BasketOperation.Mode int basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket,
                                                    Product product, String qty,
                                                    @Nullable View productView, @Nullable HashMap<String, Integer> cartInfo,
                                                    @Nullable EditText editTextQty) {
        super.updateUIAfterBasketOperationSuccess(basketOperation, basketCountTextView, viewDecQty, viewIncQty,
                btnAddToBasket, product, qty, productView, cartInfo, editTextQty);
        getCartItems(null, true);
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.VIEW_BASKET_SCREEN;
    }

    private void renderTabsDataToView(ArrayList<CartItemList> cartItemListsStnd, ArrayList<CartItemList> cartItemListsExp, String baseImgUrl) {
        final ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        if (!mViewPager.isShown()) mViewPager.setVisibility(View.VISIBLE);
        TabLayout pageTitleStrip = (TabLayout) findViewById(R.id.slidingTabs);
        ViewGroup base = (ViewGroup) findViewById(R.id.empty_cart);
        if (base.isShown()) base.setVisibility(View.GONE);

        ArrayList<BBTab> bbTabs = new ArrayList<>();

        if (cartItemListsStnd.size() > 0 && cartItemListsExp.size() > 0) {
            if (!pageTitleStrip.isShown()) pageTitleStrip.setVisibility(View.VISIBLE);
            createTabFragment(getString(R.string.stnd_delivery), baseImgUrl,
                    cartItemListsStnd, fulfillmentInfos, annotationInfoArrayList, bbTabs, Constants.STANDARD_TAB_INDEX);
            createTabFragment(getString(R.string.exp_delivery), baseImgUrl,
                    cartItemListsExp, fulfillmentInfos, annotationInfoArrayList, bbTabs, Constants.EXPRESS_TAB_INDEX);
        } else if (cartItemListsStnd.size() > 0 || cartItemListsExp.size() > 0) {
            pageTitleStrip.setVisibility(View.GONE);
            cartItemListsExp = cartItemListsExp.size() > 0 ? cartItemListsExp : cartItemListsStnd;
            String title = cartItemListsExp.size() > 0 ? getString(R.string.exp_delivery) :
                    getString(R.string.stnd_delivery);
            createTabFragment(title, baseImgUrl,
                    cartItemListsExp, fulfillmentInfos, annotationInfoArrayList, bbTabs, Constants.DEFAULT_TAB_INDEX);
        } else {
            throw new AssertionError("Both Standard and Express can't be Empty");
        }

        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(),
                bbTabs);
        mViewPager.setAdapter(tabPagerAdapter);

        if (pageTitleStrip.isShown()) {
            pageTitleStrip.setupWithViewPager(mViewPager);
            mViewPager.setCurrentItem(currentTabIndex);
            pageTitleStrip.getSelectedTabPosition();
        }
        /*
        mViewPager.postDelayed(new Runnable() {

            @Override
            public void run() {
                mViewPager.setCurrentItem(currentTabIndex);
            }
        }, 100);
        */
    }

    @Override
    public void markBasketChanged(@Nullable Intent data) {
        super.markBasketChanged(null);
    }

    private class GenerateTabDataAsync extends AsyncTask<Void, Void, Void> {

        ArrayList<CartItemList> cartItemListsStnd, cartItemListsExp;
        String baseImgUrl;

        public GenerateTabDataAsync(String baseImgUrl) {
            this.baseImgUrl = baseImgUrl;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog(getString(R.string.preparingBasket));
        }

        @Override
        protected Void doInBackground(Void... params) {
            Pair<ArrayList<CartItemList>, ArrayList<CartItemList>> expressStandardListPair =
                    getGroupedSkuTabs(cartItemLists);
            cartItemListsExp = expressStandardListPair.first;
            cartItemListsStnd = expressStandardListPair.second;
            return null;
        }

        private Pair<ArrayList<CartItemList>, ArrayList<CartItemList>> getGroupedSkuTabs(ArrayList<CartItemList> totalList) {
            ArrayList<CartItemList> expressTabList = new ArrayList<>();
            ArrayList<CartItemList> standardTabList = new ArrayList<>();

            for (CartItemList cartItemList : totalList) {
                double standardTotalPerCat = 0, expressTotalPerCat = 0;
                int standardItemCountPerCat = 0, expressItemCountPerCat = 0;

                ArrayList<CartItem> listExpress = new ArrayList<>();
                ArrayList<CartItem> listStandard = new ArrayList<>();
                for (CartItem cartItem : cartItemList.getCartItems()) {
                    String sku_type = cartItem.getSkuType();
                    if (!TextUtils.isEmpty(sku_type) &&
                            (sku_type.equals(Constants.SKU_TYPE_EXPRESS) ||
                                    sku_type.equals(Constants.SKU_TYPE_JIT) ||
                                    sku_type.equals(Constants.SKU_TYPE_KIRANA))) {
                        listExpress.add(cartItem);
                        expressItemCountPerCat++;
                        expressTotalPerCat += cartItem.getTotalPrice();
                    } else {
                        listStandard.add(cartItem);
                        standardItemCountPerCat++;
                        standardTotalPerCat += cartItem.getTotalPrice();
                    }
                }
                if (listExpress.size() > 0) {
                    CartItemList returnCartItemList = new CartItemList(listExpress, cartItemList.getTopCatName(),
                            expressTotalPerCat, expressItemCountPerCat);
                    expressTabList.add(returnCartItemList);
                }
                if (listStandard.size() > 0) {
                    CartItemList returnCartItemList = new CartItemList(listStandard, cartItemList.getTopCatName(),
                            standardTotalPerCat, standardItemCountPerCat);
                    standardTabList.add(returnCartItemList);
                }
            }
            return new Pair<>(expressTabList, standardTabList);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            renderTabsDataToView(cartItemListsStnd, cartItemListsExp, baseImgUrl);
            hideProgressDialog();
        }
    }

}