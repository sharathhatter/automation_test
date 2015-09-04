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
import android.util.Log;
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
import java.util.HashSet;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ShowCartActivity extends BackButtonActivity {

    private ArrayList<CartItemList> cartItemLists;
    private ArrayList<FulfillmentInfo> fulfillmentInfos;
    private ArrayList<AnnotationInfo> annotationInfoArrayList;
    private ArrayList<CartItemList> cartItemLists_exp;
    private ArrayList<CartItemList> cartItemLists_stnd;
    @Nullable
    private MenuItem basketMenuItem;
    private TextView txtBasketSubTitle;
    private ViewPager mViewPager;
    private boolean showMenu = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNextScreenNavigationContext(TrackEventkeys.CO_BASKET);
        setContentView(getMainLayout());
        setTitle(getString(R.string.my_basket_header));
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

    private void renderCheckoutLayout(CartSummary cartSummary) {
        int numItems = 0;
        for (CartItemList cartItemInfoArray : cartItemLists) {
            int cartItemsSize = cartItemInfoArray.getCartItems().size();
            numItems += cartItemsSize;
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
                Log.e("CLICK", "INSIDE CLICK EVENT");
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
                                        .apiResponseContent.cartGetApiCartItemsContent.baseImgUrl, isCurrentPageRequest, cartSummary);

                                renderCheckoutLayout(cartSummary);

                            } else {
                                Log.d("ELSE","Cart is empty..");
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

    private void addTabsToPager(String baseImgUrl, boolean isCurrentPageRequest, CartSummary cartSummary) {

        mViewPager = (ViewPager) findViewById(R.id.pager);

        cartItemLists_exp = new ArrayList<CartItemList>();
        cartItemLists_stnd = new ArrayList<CartItemList>();

        for (int i = 0; i < cartItemLists.size(); i++) {
            for (int j = 0; j < cartItemLists.get(i).getCartItems().size(); j++) {
                if (cartItemLists.get(i).getCartItems().get(j).getSkuType() != null) {
                    cartItemLists_exp.add(cartItemLists.get(i));
                } else {
                    cartItemLists_stnd.add(cartItemLists.get(i));
                }
            }
        }

        ArrayList<BBTab> bbTabs = new ArrayList<>();

        HashSet hs = new HashSet();
        hs.addAll(cartItemLists_stnd);
        cartItemLists_stnd.clear();

        cartItemLists_stnd.addAll(hs);

        cartItemLists_exp.addAll(cartItemLists_stnd);

        TabLayout pageTitleStrip = (TabLayout) findViewById(R.id.slidingTabs);
        if (cartItemLists_exp.size() > 0 && cartItemLists_stnd.size() > 0) {
            pageTitleStrip.setVisibility(View.VISIBLE);
            createTabFragment(getString(R.string.exp_delivery), baseImgUrl, ShowCartFragment.class, cartItemLists_exp, bbTabs, isCurrentPageRequest, cartSummary);
            createTabFragment(getString(R.string.stnd_delivery), baseImgUrl, ShowCartFragment.class, cartItemLists_stnd, bbTabs, isCurrentPageRequest, cartSummary);
        } else if (cartItemLists_exp.size() > 0) {
            pageTitleStrip.setVisibility(View.GONE);
            createTabFragment(getString(R.string.exp_delivery), baseImgUrl, ShowCartFragment.class, cartItemLists_exp, bbTabs, isCurrentPageRequest, cartSummary);
        } else if (cartItemLists_stnd.size() > 0) {
            pageTitleStrip.setVisibility(View.GONE);
            createTabFragment(getString(R.string.stnd_delivery), baseImgUrl, ShowCartFragment.class, cartItemLists_stnd, bbTabs, isCurrentPageRequest, cartSummary);
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

//        if(pageTitleStrip.isShown()){
        pageTitleStrip.setupWithViewPager(mViewPager);
//        }
    }

    private ArrayList<BBTab> createTabFragment(String tab_name, String baseUrl, Class<ShowCartFragment> showCartFragmentClass, ArrayList<CartItemList> cartItemList, ArrayList<BBTab> bbTabs, boolean isCurrentPageRequest, CartSummary cartSummary) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.CART_ITEMS, cartItemList);
        bundle.putString(Constants.BASE_IMG_URL, baseUrl);
        bundle.putBoolean(Constants.CURRENT_PAGE, isCurrentPageRequest);
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

        if (relativeLayout == null) return;
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
