package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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
import com.bigbasket.mobileapp.interfaces.TrackingAware;
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

public class ShowCartActivity extends BackButtonActivity {

    private ArrayList<CartItemList> cartItemLists;
    private ArrayList<FulfillmentInfo> fulfillmentInfos;
    private ArrayList<AnnotationInfo> annotationInfoArrayList;
    @Nullable
    private MenuItem basketMenuItem;
    private TextView txtBasketSubTitle;
    private boolean showMenu = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.CO_BASKET);
    }

    @Override
    public void onResume() {
        super.onResume();
        getCartItems(null, false);
    }

    private Toolbar setToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarMain);
        toolbar.setTitle(getString(R.string.my_basket_header));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        return toolbar;
    }

    private void renderHeaderView(int totalItemCount) {
        Toolbar toolbar = setToolbar();
        if (txtBasketSubTitle != null) toolbar.removeView(txtBasketSubTitle);
        txtBasketSubTitle = (TextView) getLayoutInflater().inflate(R.layout.basket_header_layout, toolbar, false);
        txtBasketSubTitle.setTypeface(faceRobotoRegular);
        toolbar.addView(txtBasketSubTitle);
        if (totalItemCount > 0) {
            String itemString = totalItemCount > 1 ? " Items" : " Item";
            txtBasketSubTitle.setText(totalItemCount + itemString);
            showMenu = true;
            this.invalidateOptionsMenu();
        } else {
            txtBasketSubTitle.setVisibility(View.GONE);
            showMenu = false;
            this.invalidateOptionsMenu();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.empty_basket, menu);
        basketMenuItem = menu.findItem(R.id.action_empty_basket);
        if (!showMenu) {
            if (basketMenuItem != null) {
                basketMenuItem.setVisible(false);
            }
        } else {
            if (basketMenuItem != null) {
                basketMenuItem.setVisible(true);
            }
        }
        return true;
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

        renderHeaderView(numItems);

        ViewGroup layoutCheckoutFooter = (ViewGroup) findViewById(R.id.layoutCheckoutFooter);
        if (!layoutCheckoutFooter.isShown()) layoutCheckoutFooter.setVisibility(View.VISIBLE);
        final String cartTotal = UIUtil.formatAsMoney(cartSummary.getTotal());
        UIUtil.setUpFooterButton(this, layoutCheckoutFooter, cartTotal,
                getString(R.string.checkOut), true);

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
        if (sourceName != null) {
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

    public final void setBasketNumItemsDisplay() {
        if (getCartSummary() == null) return;
        updateUIForCartInfo();
        markBasketDirty();
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
                handler.handleRetrofitError(error);
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

                            if (cartGetApiResponseContentApiResponse.apiResponseContent.
                                    cartGetApiCartItemsContent != null
                                    && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists != null
                                    && cartGetApiResponseContentApiResponse.apiResponseContent.cartGetApiCartItemsContent.cartItemLists.size() > 0) {
                                cartItemLists = cartGetApiResponseContentApiResponse.apiResponseContent.
                                        cartGetApiCartItemsContent.cartItemLists;

                                addTabsToPager(cartGetApiResponseContentApiResponse
                                        .apiResponseContent.cartGetApiCartItemsContent.baseImgUrl, fulfillmentInfos, annotationInfoArrayList);

                                renderCheckoutLayout(cartSummary, isCurrentPageRequest);

                            } else {
                                showBasketEmptyMessage();
                                editor.putString(Constants.GET_CART, "0");
                            }
                        } else {
                            handler.sendEmptyMessage(cartGetApiResponseContentApiResponse.status,
                                    cartGetApiResponseContentApiResponse.message);
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

    private ArrayList<CartItemList> getSkuForTabs(ArrayList<CartItemList> totalList, int tabType) {
        ArrayList<CartItemList> tabList = new ArrayList<CartItemList>();

        for (int i = 0; i < totalList.size(); i++) {
            ArrayList<CartItem> lists = new ArrayList<CartItem>();
            for (int j = 0; j < totalList.get(i).getCartItems().size(); j++) {
                String sku_type = totalList.get(i).getCartItems().get(j).getSkuType();
                if (tabType == Constants.TAB_TYPE_EXPRESS) {
                    if (sku_type != null && sku_type.equals(Constants.SKU_TYPE_EXPRESS) || sku_type.equals(Constants.SKU_TYPE_JIT) ||
                            sku_type.equals(Constants.SKU_TYPE_KIRANA)) {
                        lists.add(totalList.get(i).getCartItems().get(j));
                    }
                } else if (tabType == Constants.TAB_TYPE_STANDARD) {
                    if (sku_type != null && sku_type.equals(Constants.SKU_TYPE_STANDARD)) {
                        lists.add(totalList.get(i).getCartItems().get(j));
                    }
                }
            }
            if (lists.size() > 0) {
                CartItemList cartItemList = new CartItemList(lists, totalList.get(i).getTopCatName(),
                        totalList.get(i).getTopCatTotal(), totalList.get(i).getTopCatItems());
                tabList.add(cartItemList);
            }
        }
        return tabList;
    }

    private void addTabsToPager(String baseImgUrl, ArrayList<FulfillmentInfo> fulfillmentInfos,
                                ArrayList<AnnotationInfo> annotationInfoArrayList) {

        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);

        ArrayList<CartItemList> cartItemLists_stnd = getSkuForTabs(cartItemLists, Constants.TAB_TYPE_STANDARD);
        ArrayList<CartItemList> cartItemLists_exp = getSkuForTabs(cartItemLists, Constants.TAB_TYPE_EXPRESS);

        ArrayList<BBTab> bbTabs = new ArrayList<>();

        TabLayout pageTitleStrip = (TabLayout) findViewById(R.id.slidingTabs);

        if (cartItemLists_stnd != null && cartItemLists_exp != null &&
                cartItemLists_stnd.size() > 0 && cartItemLists_exp.size() > 0) {
            pageTitleStrip.setVisibility(View.VISIBLE);
            createTabFragment(getString(R.string.stnd_delivery), baseImgUrl,
                    cartItemLists_stnd, fulfillmentInfos, annotationInfoArrayList, bbTabs);
            createTabFragment(getString(R.string.exp_delivery), baseImgUrl,
                    cartItemLists_exp, fulfillmentInfos, annotationInfoArrayList, bbTabs);
        } else if (cartItemLists_stnd != null && cartItemLists_exp != null &&
                cartItemLists_stnd.size() > 0 || cartItemLists_exp.size() > 0) {
            pageTitleStrip.setVisibility(View.GONE);
            cartItemLists_exp = cartItemLists_exp.size() > 0 ? cartItemLists_exp : cartItemLists_stnd;
            String title = cartItemLists_exp.size() > 0 ? getString(R.string.exp_delivery) :
                    getString(R.string.stnd_delivery);
            createTabFragment(title, baseImgUrl,
                    cartItemLists_exp, fulfillmentInfos, annotationInfoArrayList, bbTabs);
        }

        TabPagerAdapter tabPagerAdapter = new TabPagerAdapter(getCurrentActivity(), getSupportFragmentManager(),
                bbTabs);
        mViewPager.setAdapter(tabPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        pageTitleStrip.setupWithViewPager(mViewPager);
    }

    private ArrayList<BBTab> createTabFragment(String tab_name, String baseUrl, ArrayList<CartItemList> cartItemList, ArrayList<FulfillmentInfo> fulfillmentInfos, ArrayList<AnnotationInfo> annotationInfoArrayList, ArrayList<BBTab> bbTabs) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.CART_ITEMS, cartItemList);
        bundle.putParcelableArrayList(Constants.FULFILLMENT_INFO, fulfillmentInfos);
        bundle.putParcelableArrayList(Constants.ANNOTATION_INFO, annotationInfoArrayList);
        bundle.putString(Constants.BASE_IMG_URL, baseUrl);
        bbTabs.add(new BBTab<>(tab_name, ShowCartFragment.class, bundle));
        return bbTabs;
    }

    private void showBasketEmptyMessage() {

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.realative_show_cart);
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout);
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        TabLayout pageTitleStrip = (TabLayout) findViewById(R.id.slidingTabs);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frame_show_cart);
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layoutCheckoutFooter);
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);

        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_empty_data_text, relativeLayout, false);

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

        Toolbar toolbar = setToolbar();
        toolbar.setTitle(getString(R.string.my_basket_header));
        if (txtBasketSubTitle != null) txtBasketSubTitle.setVisibility(View.GONE);
        showMenu = false;
        this.invalidateOptionsMenu();

        appBarLayout.removeView(pageTitleStrip);
        coordinatorLayout.removeView(mViewPager);
        relativeLayout.removeView(linearLayout);
        frameLayout.addView(base);
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
}
