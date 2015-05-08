package com.bigbasket.mobileapp.activity.base.uiv3;

import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ShopFromOrderFragment;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.SearchableActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.adapter.NavigationAdapter;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.DynamicScreenFragment;
import com.bigbasket.mobileapp.fragment.HomeFragment;
import com.bigbasket.mobileapp.fragment.account.AccountSettingFragment;
import com.bigbasket.mobileapp.fragment.account.AccountView;
import com.bigbasket.mobileapp.fragment.account.ChangePasswordFragment;
import com.bigbasket.mobileapp.fragment.account.DoWalletFragment;
import com.bigbasket.mobileapp.fragment.account.UpdatePinFragment;
import com.bigbasket.mobileapp.fragment.account.UpdateProfileFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.order.MemberAddressListFragment;
import com.bigbasket.mobileapp.fragment.order.OrderThankYouFragment;
import com.bigbasket.mobileapp.fragment.order.ShowCartFragment;
import com.bigbasket.mobileapp.fragment.order.SlotSelectionFragment;
import com.bigbasket.mobileapp.fragment.product.CategoryLandingFragment;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoSetProductsFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.FloatingBasketUIAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.SubNavigationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.NameValuePair;
import com.bigbasket.mobileapp.model.SectionManager;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SectionTextItem;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.view.uiv3.AnimatedLinearLayout;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;
import com.melnykov.fab.FloatingBadgeCountView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BBActivity extends SocialLoginActivity implements BasketOperationAware,
        CartInfoAware, HandlerAware, SubNavigationAware, FloatingBasketUIAware {

    protected BigBasketMessageHandler handler;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mDrawerTitle;
    private String mTitle;
    private BasketOperationResponse basketOperationResponse;
    private CartSummary cartInfo = new CartSummary();
    private BBDrawerLayout mDrawerLayout;
    private String currentFragmentTag;
    private RecyclerView mNavRecyclerView;
    private Menu mMenu;
    private FloatingBadgeCountView mBtnViewBasket;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getMainLayout());

        mNavRecyclerView = (RecyclerView) findViewById(R.id.listNavigation);
        if (mNavRecyclerView != null) {
            mNavRecyclerView.setHasFixedSize(false);
            mNavRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        handler = new BigBasketMessageHandler<>(this);
        mTitle = mDrawerTitle = getTitle().toString();

        Toolbar toolbar = getToolbar();
        setSupportActionBar(toolbar);

        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                FragmentManager fragmentManager = getSupportFragmentManager();
                if (fragmentManager != null) {
                    int backStackEntryCount = fragmentManager.getBackStackEntryCount();
                    if (backStackEntryCount == 0) {
                        onNoFragmentsInLayout();
                    } else {
                        Fragment currFragment = fragmentManager.getFragments().get(backStackEntryCount - 1);
                        if (currFragment instanceof AbstractFragment) {
                            if (!(currFragment instanceof ShowCartFragment)) {
                                setViewBasketButtonStateOnActivityResume();
                            }
                            currentFragmentTag = ((AbstractFragment) currFragment).getFragmentTxnTag();
                            ((AbstractFragment) currFragment).onBackStateChanged();
                        }
                    }
                }
            }
        });
        setNavDrawer(toolbar, savedInstanceState);
        setViewBasketFloatingButton();
    }

    public void onNoFragmentsInLayout() {
        finish();
    }

    @LayoutRes
    public int getMainLayout() {
        return R.layout.uiv3_main_layout;
    }

    public void setViewBasketFloatingButton() {
        FloatingBadgeCountView btnViewBasket = getViewBasketFloatingButton();
        if (btnViewBasket != null) {
            btnViewBasket.setImg(R.drawable.ic_shopping_cart_white_24dp);
            btnViewBasket.setText(null);
            syncCartInfoFromPreference();
            updateCartCountHeaderTextView();
            btnViewBasket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showViewBasketFragment();
                }
            });
        }
    }

    @Nullable
    @Override
    public FloatingBadgeCountView getViewBasketFloatingButton() {
        if (mBtnViewBasket == null) {
            mBtnViewBasket = (FloatingBadgeCountView) findViewById(R.id.btnViewBasket);
        }
        return mBtnViewBasket;
    }

    @Override
    public void setViewBasketButtonStateOnActivityResume() {
        FloatingBadgeCountView btnViewBasket = getViewBasketFloatingButton();
        if (btnViewBasket != null) {
            if (btnViewBasket.getVisibility() != View.VISIBLE) {
                btnViewBasket.setVisibility(View.VISIBLE);
            }
            btnViewBasket.show();
        }
    }

    protected View getToolbarLayout() {
        return findViewById(R.id.toolbarMain);
    }

    public Toolbar getToolbar() {
        return (Toolbar) findViewById(R.id.toolbarMain);
    }

    public void setNavDrawer(final Toolbar toolbar, Bundle savedInstanceState) {
        mDrawerLayout = (BBDrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                logHomeScreenEvent(TrackingAware.MENU_CLICKED, TrackEventkeys.NAVIGATION_CTX,
                        TrackEventkeys.NAVIGATION_CTX_TOPNAV); //todo check with sid
                toolbar.setTitle(formatToolbarTitle(mTitle));
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                logHomeScreenEvent(TrackingAware.MENU_CLICKED, TrackEventkeys.NAVIGATION_CTX,
                        TrackEventkeys.NAVIGATION_CTX_TOPNAV); //todo check with sid
                trackEvent(TrackingAware.MENU_SHOWN, null);
                toolbar.setTitle(formatToolbarTitle(mDrawerTitle));
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadNavigationItems();
        Intent intent = getIntent();
        handleIntent(intent, savedInstanceState);
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    public void addToMainLayout(AbstractFragment fragment) {
        addToMainLayout(fragment, null);
    }

    public void addToMainLayout(AbstractFragment fragment, String tag) {

        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            setViewBasketButtonStateOnActivityResume();
        }
        FragmentTransaction ft = fm.beginTransaction();
        String ftTag = TextUtils.isEmpty(tag) ? fragment.getFragmentTxnTag() : tag;
        this.currentFragmentTag = ftTag;
        ft.add(R.id.content_frame, fragment, ftTag);
        ft.addToBackStack(ftTag);
        ft.commit();
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setOptionsMenu(menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    protected void setOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.action_menu, menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        setTitle(title.toString());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mDrawerToggle != null) {
            mDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    public void startFragment() {
        int fragmentCode = getIntent().getIntExtra(Constants.FRAGMENT_CODE, -1);
        startFragment(fragmentCode);
    }

    public void startFragment(int fragmentCode) {
        switch (fragmentCode) {
            case FragmentCodes.START_HOME:
                addToMainLayout(new HomeFragment(), Constants.HOME);
                break;
            case FragmentCodes.START_UPDATE_PROFILE:
                addToMainLayout(new UpdateProfileFragment());
                break;
            case FragmentCodes.START_CHANGE_PASSWD:
                addToMainLayout(new ChangePasswordFragment());
                break;
            case FragmentCodes.START_VIEW_DELIVERY_ADDRESS:
                MemberAddressListFragment memberAddressListFragment = new MemberAddressListFragment();
                Bundle addressbundle = new Bundle();
                addressbundle.putBoolean(Constants.FROM_ACCOUNT_PAGE,
                        getIntent().getBooleanExtra(Constants.FROM_ACCOUNT_PAGE, false));
                memberAddressListFragment.setArguments(addressbundle);
                addToMainLayout(memberAddressListFragment);
                break;
            case FragmentCodes.START_CHANGE_PIN:
                addToMainLayout(new UpdatePinFragment());
                break;
            case FragmentCodes.START_ADDRESS_SELECTION:
                addToMainLayout(new MemberAddressListFragment());
                break;
            case FragmentCodes.START_SLOT_SELECTION:
                addToMainLayout(new SlotSelectionFragment());
                break;
            case FragmentCodes.START_ACCOUNT_SETTING:
                addToMainLayout(new AccountSettingFragment());
                break;
            case FragmentCodes.START_WALLET_FRAGMENT:
                addToMainLayout(new DoWalletFragment());
                break;
            case FragmentCodes.START_VIEW_BASKET:
                launchViewBasket();
                break;
            case FragmentCodes.START_COMMUNICATION_HUB:
                launchKonotor();
                break;
            case FragmentCodes.START_PRODUCT_DETAIL:
                ProductDetailFragment productDetailFragment = new ProductDetailFragment();
                Bundle bundle = new Bundle();
                if (getIntent().getStringExtra(Constants.SKU_ID) != null)
                    bundle.putString(Constants.SKU_ID, getIntent().getStringExtra(Constants.SKU_ID));
                else
                    bundle.putString(Constants.EAN_CODE, getIntent().getStringExtra(Constants.EAN_CODE));
                productDetailFragment.setArguments(bundle);
                addToMainLayout(productDetailFragment);
                break;
            case FragmentCodes.START_ORDER_THANKYOU:
                ArrayList<Order> orders = getIntent().getParcelableArrayListExtra(Constants.ORDERS);
                OrderThankYouFragment orderThankYouFragment = new OrderThankYouFragment();
                bundle = new Bundle();
                bundle.putParcelableArrayList(Constants.ORDERS, orders);
                orderThankYouFragment.setArguments(bundle);
                addToMainLayout(orderThankYouFragment);
                break;
            case FragmentCodes.START_CATEGORY_LANDING:
                CategoryLandingFragment categoryLandingFragment = new CategoryLandingFragment();
                Bundle subCatBundle = new Bundle();
                subCatBundle.putString(Constants.TOP_CATEGORY_SLUG, getIntent().getStringExtra(Constants.TOP_CATEGORY_SLUG));
                subCatBundle.putString(Constants.TOP_CATEGORY_NAME, getIntent().getStringExtra(Constants.TOP_CATEGORY_NAME));
                categoryLandingFragment.setArguments(subCatBundle);
                addToMainLayout(categoryLandingFragment);
                break;
            case FragmentCodes.START_PROMO_DETAIL:
                int promoId = getIntent().getIntExtra(Constants.PROMO_ID, -1);
                Bundle promoDetailBundle = new Bundle();
                promoDetailBundle.putInt(Constants.PROMO_ID, promoId);
                promoDetailBundle.putParcelable(Constants.PROMO_CATS,
                        getIntent().getParcelableExtra(Constants.PROMO_CATS));
                promoDetailBundle.putString(Constants.PROMO_NAME,
                        getIntent().getStringExtra(Constants.PROMO_NAME));
                PromoDetailFragment promoDetailFragment = new PromoDetailFragment();
                promoDetailFragment.setArguments(promoDetailBundle);
                addToMainLayout(promoDetailFragment);
                break;
            case FragmentCodes.START_ORDER_PRODUCT_LIST_FRAGMENT:
                String orderId = getIntent().getStringExtra(Constants.ORDER_ID);
                Bundle orderProductListBundle = new Bundle();
                orderProductListBundle.putString(Constants.ORDER_ID, orderId);
                orderProductListBundle.putString(TrackEventkeys.NAVIGATION_CTX,
                        getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX));
                ShopFromOrderFragment shopFromOrderFragment = new ShopFromOrderFragment();
                shopFromOrderFragment.setArguments(orderProductListBundle);
                addToMainLayout(shopFromOrderFragment);
                break;
            case FragmentCodes.START_SHOPPING_LIST_LANDING:
                addToMainLayout(new ShoppingListFragment());
                break;
//            case FragmentCodes.START_SEARCH:
//                doSearch(getIntent().getStringExtra(Constants.SEARCH_QUERY));
//                break;
//            case FragmentCodes.START_GENERIC_PRODUCT_LIST:
//                ArrayList<NameValuePair> nameValuePairs = getIntent().getParcelableArrayListExtra(Constants.PRODUCT_QUERY);
//                String title = getIntent().getStringExtra(Constants.TITLE);
//                if (nameValuePairs != null && !nameValuePairs.isEmpty()) {
//                    GenericProductListFragment productListFragment = new GenericProductListFragment();
//                    Bundle productListArgs = new Bundle();
//                    productListArgs.putString(TrackEventkeys.NAVIGATION_CTX,
//                            getIntent().getStringExtra(TrackEventkeys.NAVIGATION_CTX));
//                    productListArgs.putParcelableArrayList(Constants.PRODUCT_QUERY, nameValuePairs);
//                    if (!TextUtils.isEmpty(title)) {
//                        productListArgs.putString(Constants.TITLE, title);
//                    }
//                    productListFragment.setArguments(productListArgs);
//                    addToMainLayout(productListFragment);
//                }
//                break;
            case FragmentCodes.START_PROMO_CATEGORY:
                addToMainLayout(new PromoCategoryFragment());
                break;
            case FragmentCodes.START_PROMO_SET_PRODUCTS:
                bundle = getIntent().getExtras();
                PromoSetProductsFragment promoSetProductsFragment = new PromoSetProductsFragment();
                promoSetProductsFragment.setArguments(bundle);
                addToMainLayout(promoSetProductsFragment);
                break;
            case FragmentCodes.START_DYNAMIC_SCREEN:
                DynamicScreenFragment dynamicScreenFragment = new DynamicScreenFragment();
                bundle = new Bundle();
                bundle.putString(Constants.SCREEN, getIntent().getStringExtra(Constants.SCREEN));
                dynamicScreenFragment.setArguments(bundle);
                addToMainLayout(dynamicScreenFragment);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_search:
                Intent searchIntent = new Intent(this, SearchableActivity.class);
                startActivityForResult(searchIntent, NavigationCodes.START_SEARCH);
                return false;
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_signup:
                logHomeScreenEvent(TrackingAware.REGISTRATION_CLICKED, TrackEventkeys.NAVIGATION_CTX,
                        TrackEventkeys.NAVIGATION_CTX_TOPNAV);
                launchRegistrationPage();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showViewBasketFragment() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (!(fragments != null && fragments.size() > 0 &&
                fragments.get(fragments.size() - 1) instanceof ShowCartFragment)) {
            launchViewBasket();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == NavigationCodes.START_SEARCH) {
            if (data != null) {
                String searchQuery = data.getStringExtra(Constants.SEARCH_QUERY);
                if (!TextUtils.isEmpty(searchQuery)) {
                    doSearch(searchQuery);
                }
            }
        }
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {
        addToMainLayout(newFragment);
    }

    @Override
    public void setBasketOperationResponse(BasketOperationResponse basketOperationResponse) {
        this.basketOperationResponse = basketOperationResponse;
    }

    @Override
    public void updateUIAfterBasketOperationFailed(BasketOperation basketOperation, TextView basketCountTextView,
                                                   View viewDecQty, View viewIncQty, Button btnAddToBasket,
                                                   EditText editTextQty, Product product, String qty,
                                                   String errorType, @Nullable View productView) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(this, "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
        if (editTextQty != null && !isSuspended()) {
            hideKeyboard(getCurrentActivity(), editTextQty);
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty,
                                                    @Nullable View productView) {


        int productQtyInBasket = 0;
        if (basketOperationResponse.getBasketResponseProductInfo() != null) {
            productQtyInBasket = Integer.parseInt(basketOperationResponse.getBasketResponseProductInfo().getTotalQty());
        }
        int totalProductsInBasket = basketOperationResponse.getCartSummary().getNoOfItems();

        if (productQtyInBasket == 0) {
            if (viewDecQty != null) {
                viewDecQty.setVisibility(View.GONE);
            }
            if (viewIncQty != null) {
                viewIncQty.setVisibility(View.GONE);
            }
            if (btnAddToBasket != null) {
                btnAddToBasket.setVisibility(View.VISIBLE);
            }
            if (editTextQty != null) {
                editTextQty.setText("1");
                editTextQty.setVisibility(View.VISIBLE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setVisibility(View.GONE);
            }
            if (productView != null) {
                productView.setBackgroundColor(Color.WHITE);
            }
        } else {
            if (viewDecQty != null) {
                viewDecQty.setVisibility(View.VISIBLE);
            }
            if (viewIncQty != null) {
                viewIncQty.setVisibility(View.VISIBLE);
            }
            if (btnAddToBasket != null) {
                btnAddToBasket.setVisibility(View.GONE);
            }
            if (editTextQty != null) {
                editTextQty.setVisibility(View.GONE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setText(String.valueOf(productQtyInBasket));
                basketCountTextView.setVisibility(View.VISIBLE);
            }
        }

        if (editTextQty != null && !isSuspended()) {
            hideKeyboard(getCurrentActivity(), editTextQty);
        }

        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.GET_CART, String.valueOf(totalProductsInBasket));
        editor.commit();
        cartInfo.setNoOfItems(totalProductsInBasket);
    }

    @Override
    public CartSummary getCartInfo() {
        return cartInfo;
    }

    @Override
    public void setCartInfo(CartSummary cartInfo) {
        this.cartInfo = cartInfo;
    }

    @Override
    public void updateUIForCartInfo() {
        if (cartInfo == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.GET_CART, String.valueOf(cartInfo.getNoOfItems()));
        editor.commit();
        updateCartCountHeaderTextView();
    }

    @Override
    public void markBasketDirty() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putBoolean(Constants.IS_BASKET_COUNT_DIRTY, true);
        editor.commit();
    }

    @Override
    public void syncBasket() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove(Constants.IS_BASKET_COUNT_DIRTY);
        editor.commit();
        new GetCartCountTask<>(this).startTask();
    }

    private void updateCartCountHeaderTextView() {
        FloatingBadgeCountView btnViewBasket = getViewBasketFloatingButton();
        if (cartInfo != null && btnViewBasket != null) {
            if (cartInfo.getNoOfItems() <= 0) {
                btnViewBasket.setText(null);
            } else {
                btnViewBasket.setText(String.valueOf(cartInfo.getNoOfItems()));
            }
        }
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        handleIntent(intent, null);
    }

    public void handleIntent(Intent intent, Bundle savedInstanceState) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction()) && savedInstanceState == null) {
            // User has entered something in search, and pressed enter and this is not due to a screen rotation
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                doSearch(query);
                logSearchEvent(query);
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction()) && savedInstanceState == null) {
            // User has selected a suggestion and this is not due to a screen rotation
            String categoryUrl = intent.getStringExtra(SearchManager.EXTRA_DATA_KEY);
            Uri data = intent.getData();
            if (data != null) {
                String query = data.getLastPathSegment();
                if (!TextUtils.isEmpty(categoryUrl) && categoryUrl.contains("/")) {
                    String[] categoryUrlElements = categoryUrl.split("/");
                    String slug = categoryUrlElements[categoryUrlElements.length - 1];
                    launchCategoryProducts(query, categoryUrl, slug);
                } else {
                    doSearch(query);
                }
            }
        } else {
            currentFragmentTag = savedInstanceState != null ? savedInstanceState.getString(Constants.FRAGMENT_TAG) : null;
            if (TextUtils.isEmpty(currentFragmentTag) ||
                    getSupportFragmentManager().findFragmentByTag(currentFragmentTag) == null) {
                startFragment();
            }
        }
    }

    private void logSearchEvent(String query) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.QUERY, query);
        map.put(TrackEventkeys.NAVIGATION_CTX, TrackEventkeys.NAVIGATION_CTX_TOPNAV);
        trackEvent(TrackingAware.SEARCH, map);
    }

    private void launchCategoryProducts(String categoryName, String categoryUrl,
                                        String categorySlug) {
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(categoryName, categoryUrl);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY.get()));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, categorySlug));
        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
        intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

//    private void launchProductCategoryFragment(String categorySlug, String filter,
//                                               String sortOn, String title) {
//        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
//        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.CATEGORY.get()));
//        nameValuePairs.add(new NameValuePair(Constants.SLUG, categorySlug));
//        nameValuePairs.add(new NameValuePair(Constants.SORT_ON, sortOn));
//        nameValuePairs.add(new NameValuePair(Constants.FILTER_ON, filter));
//        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
//        intent.putExtra(Constants.PRODUCT_QUERY, nameValuePairs);
//        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
//        Bundle bundle = new Bundle();
//        bundle.putString(Constants.SLUG_NAME_CATEGORY, categorySlug);
//        if (!TextUtils.isEmpty(filter))
//            bundle.putString(Constants.FILTER, filter);
//        if (!TextUtils.isEmpty(sortOn))
//            bundle.putString(Constants.SORT_BY, sortOn);
//        bundle.putString(Constants.CATEGORY_TITLE, title);
//        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
//        categoryProductsFragment.setArguments(bundle);
//        addToMainLayout(categoryProductsFragment);
//    }

    private void logHomeScreenEvent(String trackAwareName, String eventKeyName,
                                    String navigationCtx) {
        Map<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(eventKeyName, navigationCtx);
        trackEvent(trackAwareName, eventAttribs);
    }

    public void doSearch(String searchQuery) {
        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH.get()));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery.trim()));
        intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    public void onChangeTitle(String title) {
        setTitle(title);
    }

    public void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            mTitle = title == null ? "" : title;
            actionBar.setTitle(formatToolbarTitle(mTitle));
        }
    }

    private SpannableString formatToolbarTitle(String title) {
        SpannableString spannableString = new SpannableString(title);
        spannableString.setSpan(new CustomTypefaceSpan("", faceRobotoRegular),
                0, spannableString.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName)) {
            switch (sourceName) {
                case Constants.LOGOUT:
                    onLogoutRequested();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
        }
    }

    private void loadNavigationItems() {
        if (mNavRecyclerView == null) return;
        TextView txtNavSalutation = (TextView) findViewById(R.id.txtNavSalutation);
        txtNavSalutation.setTypeface(faceRobotoRegular);
        AuthParameters authParameters = AuthParameters.getInstance(this);

        if (!authParameters.isAuthTokenEmpty()) {
            txtNavSalutation.setText("Welcome " + authParameters.getMemberFullName().split(" ")[0]);
        } else {
            txtNavSalutation.setText("Welcome BigBasketeer");
        }
        txtNavSalutation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNavigationArea();
            }
        });

        ArrayList<SectionNavigationItem> sectionNavigationItems = getSectionNavigationItems();

        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);
        new AccountView<>(this, lstMyAccount);
        NavigationAdapter navigationAdapter = new NavigationAdapter(this, faceRobotoLight, sectionNavigationItems,
                SectionManager.MAIN_MENU);
        mNavRecyclerView.setAdapter(navigationAdapter);
    }

    private void toggleNavigationArea() {
        if (mNavRecyclerView == null) return;

        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);

        if (mNavRecyclerView.getVisibility() == View.VISIBLE) {
            mNavRecyclerView.setVisibility(View.GONE);
            lstMyAccount.setVisibility(View.VISIBLE);
        } else {
            lstMyAccount.setVisibility(View.GONE);
            mNavRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private ArrayList<SectionNavigationItem> getSectionNavigationItems() {
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();
        sectionNavigationItems.add(getHomeSectionNavItem());

        SectionManager sectionManager = new SectionManager(this, SectionManager.MAIN_MENU);
        SectionData sectionData = sectionManager.getStoredSectionData(true);
        if (sectionData != null && sectionData.getSections() != null && sectionData.getSections().size() > 0) {
            for (Section section : sectionData.getSections()) {
                if (section == null || section.getSectionItems() == null || section.getSectionItems().size() == 0)
                    continue;
                if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
                    sectionNavigationItems.add(new SectionNavigationItem(section));
                }
                setSectionNavigationItemList(sectionNavigationItems, section.getSectionItems(),
                        section);
            }
        }
        return sectionNavigationItems;
    }

    private void setSectionNavigationItemList(ArrayList<SectionNavigationItem> sectionNavigationItems,
                                              ArrayList<SectionItem> sectionItems,
                                              Section section) {
        for (SectionItem sectionItem : sectionItems) {
            if (sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText())) {
                sectionNavigationItems.add(new SectionNavigationItem(section, sectionItem));
            }
        }
    }

    private SectionNavigationItem getHomeSectionNavItem() {
        SectionItem homeSectionItem = new SectionItem(new SectionTextItem(getString(R.string.home), 0),
                null, Constants.LOCAL_RES_URL + "nav_home", -1, new DestinationInfo(DestinationInfo.HOME, null));
        ArrayList<SectionItem> sectionItems = new ArrayList<>();
        sectionItems.add(homeSectionItem);
        Section section = new Section(null, null, Section.MSG, sectionItems, null);
        return new SectionNavigationItem(section, homeSectionItem);
    }

    @Override
    public void onSubNavigationRequested(Section section, SectionItem sectionItem) {
        ArrayList<SectionItem> subNavigationSectionItems = sectionItem.getSubSectionItems();
        if (subNavigationSectionItems == null) return;

        final AnimatedLinearLayout layoutSubNavigationItems =
                (AnimatedLinearLayout) findViewById(R.id.layoutSubNavigationItems);

        layoutSubNavigationItems.setVisibility(View.VISIBLE);
        final RecyclerView listSubNavigation = (RecyclerView) findViewById(R.id.listSubNavigation);

        TextView txtNavMainItem = (TextView) findViewById(R.id.txtNavMainItem);
        txtNavMainItem.setTypeface(faceRobotoLight);
        txtNavMainItem.setText(sectionItem.getTitle() != null ?
                sectionItem.getTitle().getText() : "");
        txtNavMainItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listSubNavigation.setAdapter(null);
                layoutSubNavigationItems.setVisibility(View.GONE);
            }
        });

        listSubNavigation.setHasFixedSize(false);
        listSubNavigation.setLayoutManager(new LinearLayoutManager(this));

        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();
        setSectionNavigationItemList(sectionNavigationItems, subNavigationSectionItems, section);
        NavigationAdapter navigationAdapter = new NavigationAdapter(this, faceRobotoLight, sectionNavigationItems,
                SectionManager.MAIN_MENU);
        listSubNavigation.setAdapter(navigationAdapter);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (!TextUtils.isEmpty(currentFragmentTag)) {
            outState.putString(Constants.FRAGMENT_TAG, currentFragmentTag);
        }
        super.onSaveInstanceState(outState);
    }

    protected FrameLayout getContentView() {
        return (FrameLayout) findViewById(R.id.content_frame);
    }

    public String getScreenTag() {
        return null;
    }

    public Menu getMenu() {
        return mMenu;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        setViewBasketButtonStateOnActivityResume();
    }

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager sfm = getSupportFragmentManager();
        if (sfm == null || sfm.getFragments() == null || sfm.getFragments().size() == 0) {
            LocalyticsWrapper.onResume(getScreenTag());
        }

        if (isBasketDirty()) {
            syncBasket();
        } else {
            syncCartInfoFromPreference();
        }
    }

    private void syncCartInfoFromPreference() {
        if (cartInfo != null && cartInfo.getNoOfItems() == 0) {
            // Update from preference
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
            String cartCountStr = preferences.getString(Constants.GET_CART, null);
            if (!TextUtils.isEmpty(cartCountStr) && TextUtils.isDigitsOnly(cartCountStr)) {
                cartInfo.setNoOfItems(Integer.parseInt(cartCountStr));
            }
        }
    }
}