package com.bigbasket.mobileapp.activity.base.uiv3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.internal.VersionUtils;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ChangeCityActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.ShopFromOrderFragment;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.SearchableActivity;
import com.bigbasket.mobileapp.activity.product.ProductListActivity;
import com.bigbasket.mobileapp.adapter.NavigationAdapter;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.DynamicScreenFragment;
import com.bigbasket.mobileapp.fragment.FlatPageFragment;
import com.bigbasket.mobileapp.fragment.HomeFragment;
import com.bigbasket.mobileapp.fragment.account.AccountView;
import com.bigbasket.mobileapp.fragment.account.ChangePasswordFragment;
import com.bigbasket.mobileapp.fragment.account.UpdateProfileFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.order.MemberAddressListFragment;
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
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.DestinationInfo;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SectionTextItem;
import com.bigbasket.mobileapp.model.section.SubSectionItem;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.task.GetDynamicPageTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.view.uiv3.AnimatedRelativeLayout;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;
import com.bigbasket.mobileapp.view.uiv3.FloatingBadgeCountView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class BBActivity extends SocialLoginActivity implements BasketOperationAware,
        CartInfoAware, HandlerAware, SubNavigationAware, FloatingBasketUIAware {

    protected BigBasketMessageHandler handler;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mTitle;
    private BasketOperationResponse basketOperationResponse;
    private CartSummary cartSummary = new CartSummary();
    private BBDrawerLayout mDrawerLayout;
    private String currentFragmentTag;
    private RecyclerView mNavRecyclerView;
    private AnimatedRelativeLayout mSubNavLayout;
    private FloatingBadgeCountView mBtnViewBasket;
    private RecyclerView mListSubNavigation;
    private boolean mSyncNeeded;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getMainLayout());

        mNavRecyclerView = (RecyclerView) findViewById(R.id.listNavigation);
        if (mNavRecyclerView != null) {
            mNavRecyclerView.setHasFixedSize(false);
            mNavRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        }

        mListSubNavigation = (RecyclerView) findViewById(R.id.listSubNavigation);
        if (mListSubNavigation != null) {
            // Assign a layout manager as soon as RecyclerView is inflated to prevent onDestroy crash
            mListSubNavigation.setHasFixedSize(false);
            mListSubNavigation.setLayoutManager(new LinearLayoutManager(this));
        }

        handler = new BigBasketMessageHandler<>(this);
        mTitle = getTitle().toString();

        Toolbar toolbar = getToolbar();
        setSupportActionBar(toolbar);
        if (VersionUtils.isAtLeastL() && getSupportActionBar() != null) {
            getSupportActionBar().setElevation(20);
        }

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
            syncCartInfoFromPreference();
            updateCartCountHeaderTextView();
            btnViewBasket.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HashMap<String, String> map = new HashMap<>();
                    map.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                    trackEvent(TrackingAware.BASKET_VIEW_CLICKED, map, null, null, false, true);
                    launchViewBasketScreen();
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
                toolbar.setTitle(formatToolbarTitle(mTitle));
                invalidateOptionsMenu();
                if (mSubNavLayout != null && mSubNavLayout.getVisibility() == View.VISIBLE) {
                    onSubNavigationHideRequested(false);
                }
                if (mNavRecyclerView != null) {
                    if (mNavRecyclerView.getVisibility() != View.VISIBLE) {
                        // User was in settings menu, now restore the default state to avoid confusion
                        ImageView imgSwitchNav = (ImageView) findViewById(R.id.imgSwitchNav);
                        if (imgSwitchNav != null) {
                            toggleNavigationArea(imgSwitchNav);
                        }
                    }
                    mNavRecyclerView.scrollToPosition(0);
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                Map<String, String> eventAttribs = new HashMap<>();
                eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                trackEvent(TrackingAware.MENU_SHOWN, eventAttribs);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setStatusBarBackground(R.color.uiv3_status_bar_background);
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
        addToMainLayout(fragment, tag, false);
    }

    public void replaceToMainLayout(AbstractFragment fragment, String tag, boolean stateLess,
                                    FrameLayout frameLayout) {
        if (frameLayout == null) return;
        UIUtil.addNavigationContextToBundle(fragment, getNextScreenNavigationContext());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        String ftTag = TextUtils.isEmpty(tag) ? fragment.getFragmentTxnTag() : tag;
        this.currentFragmentTag = ftTag;
        ft.replace(frameLayout.getId(), fragment, ftTag);
        if (stateLess) {
            ft.commitAllowingStateLoss();
        } else {
            ft.commit();
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    public void addToMainLayout(AbstractFragment fragment, String tag, boolean stateLess) {
        if (fragment == null) return;
        UIUtil.addNavigationContextToBundle(fragment, getNextScreenNavigationContext());
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        String ftTag = TextUtils.isEmpty(tag) ? fragment.getFragmentTxnTag() : tag;
        this.currentFragmentTag = ftTag;
        ft.add(R.id.content_frame, fragment, ftTag);
        ft.addToBackStack(ftTag);
        if (stateLess) {
            ft.commitAllowingStateLoss();
        } else {
            ft.commit();
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    //method for add bundle

    public BBDrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void setOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
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
                UpdateProfileFragment updateProfileFragment = new UpdateProfileFragment();
                Bundle updateProfileBundle = new Bundle();
                updateProfileBundle.putParcelable(Constants.UPDATE_PROFILE_OBJ,
                        getIntent().getParcelableExtra(Constants.UPDATE_PROFILE_OBJ));
                updateProfileFragment.setArguments(updateProfileBundle);
                addToMainLayout(updateProfileFragment);
                break;
            case FragmentCodes.START_CHANGE_PASSWD:
                addToMainLayout(new ChangePasswordFragment());
                break;
            case FragmentCodes.START_VIEW_DELIVERY_ADDRESS:
                MemberAddressListFragment memberAddressListFragment = new MemberAddressListFragment();
                Bundle addressbundle = new Bundle();
                addressbundle.putBoolean(Constants.FROM_ACCOUNT_PAGE,
                        getIntent().getBooleanExtra(Constants.FROM_ACCOUNT_PAGE, false));
                addressbundle.putString(Constants.TOTAL_BASKET_VALUE,
                        getIntent().getStringExtra(Constants.TOTAL_BASKET_VALUE));
                memberAddressListFragment.setArguments(addressbundle);
                addToMainLayout(memberAddressListFragment);
                break;
            case FragmentCodes.START_ADDRESS_SELECTION:
                addToMainLayout(new MemberAddressListFragment());
                break;
            case FragmentCodes.START_COMMUNICATION_HUB:
                launchMoEngageCommunicationHub();
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
            case FragmentCodes.START_WEBVIEW:
                bundle = new Bundle();
                bundle.putString(Constants.WEBVIEW_URL, getIntent().getStringExtra(Constants.WEBVIEW_URL));
                bundle.putString(Constants.WEBVIEW_TITLE, getIntent().getStringExtra(Constants.WEBVIEW_TITLE));
                FlatPageFragment flatPageFragment = new FlatPageFragment();
                flatPageFragment.setArguments(bundle);
                addToMainLayout(flatPageFragment);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.START_SEARCH) {
            if (data != null) {
                String searchQuery = data.getStringExtra(Constants.SEARCH_QUERY);
                String nc = data.getStringExtra(TrackEventkeys.NAVIGATION_CTX);
                if (!TextUtils.isEmpty(searchQuery)) {
                    doSearch(searchQuery.trim(), nc);
                    return;
                }
            }
        } else if (resultCode == NavigationCodes.LAUNCH_FRAGMENT && data != null) {
            createFragmentFromName(data.getStringExtra(Constants.FRAGMENT_CLASS_NAME), data.getExtras(),
                    data.getStringExtra(Constants.FRAGMENT_TAG));
            return;
        } else if (resultCode == NavigationCodes.ACCOUNT_UPDATED) {
            setTxtNavSalutation();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void createFragmentFromName(String fragmentClassName, Bundle fragmentArgs, String tag) {
        Fragment newFragment = Fragment.instantiate(getCurrentActivity(), fragmentClassName, fragmentArgs);
        if (newFragment instanceof AbstractFragment) {
            addToMainLayout((AbstractFragment) newFragment, tag, true);
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
                                                   View viewDecQty, View viewIncQty, View btnAddToBasket,
                                                   Product product, String qty,
                                                   String errorType, @Nullable View productView,
                                                   @Nullable EditText editTextQty) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(this, "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, View btnAddToBasket,
                                                    Product product, String qty,
                                                    @Nullable View productView,
                                                    @Nullable HashMap<String, Integer> cartInfoMap,
                                                    @Nullable EditText editTextQty) {

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
            if (editTextQty != null && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQty.setText("1");
                editTextQty.setVisibility(View.VISIBLE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setVisibility(View.GONE);
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
            if (basketCountTextView != null) {
                basketCountTextView.setText(String.valueOf(productQtyInBasket));
                basketCountTextView.setVisibility(View.VISIBLE);
            }
            if (editTextQty != null && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQty.setVisibility(View.GONE);
            }
        }

        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
            if (cartInfoMap != null) {
                cartInfoMap.put(product.getSku(), productQtyInBasket);
            }
        }

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.GET_CART, String.valueOf(totalProductsInBasket));
        editor.apply();
        cartSummary.setNoOfItems(totalProductsInBasket);
    }

    @Override
    public CartSummary getCartSummary() {
        return cartSummary;
    }

    @Override
    public void setCartSummary(CartSummary cartSummary) {
        this.cartSummary = cartSummary;
    }

    @Override
    public void updateUIForCartInfo() {
        if (cartSummary == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.GET_CART, String.valueOf(cartSummary.getNoOfItems()));
        editor.commit();
        updateCartCountHeaderTextView();
    }

    @Override
    public void markBasketDirty() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putBoolean(Constants.IS_BASKET_COUNT_DIRTY, true);
        editor.commit();
    }

    public boolean isBasketDirty() {
        return PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).
                getBoolean(Constants.IS_BASKET_COUNT_DIRTY, false);
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
        if (cartSummary != null && btnViewBasket != null) {
            btnViewBasket.setImg(R.drawable.filled_basket);
            if (cartSummary.getNoOfItems() <= 0) {
                btnViewBasket.setText(null);
            } else {
                btnViewBasket.setText(cartSummary.getNoOfItems() < 10 ? "0" + cartSummary.getNoOfItems() :
                        String.valueOf(cartSummary.getNoOfItems()));
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

    public void handleIntent(Intent intent) {
        handleIntent(intent, null);
    }

    public void handleIntent(Intent intent, Bundle savedInstanceState) {
        currentFragmentTag = savedInstanceState != null ? savedInstanceState.getString(Constants.FRAGMENT_TAG) : null;
        if (TextUtils.isEmpty(currentFragmentTag) ||
                getSupportFragmentManager().findFragmentByTag(currentFragmentTag) == null) {
            startFragment();
        }
    }

    public void doSearch(String searchQuery, String referrer) {
        Intent intent = new Intent(getCurrentActivity(), ProductListActivity.class);
        ArrayList<NameValuePair> nameValuePairs = new ArrayList<>();
        nameValuePairs.add(new NameValuePair(Constants.TYPE, ProductListType.SEARCH.get()));
        nameValuePairs.add(new NameValuePair(Constants.SLUG, searchQuery.trim()));
        intent.putParcelableArrayListExtra(Constants.PRODUCT_QUERY, nameValuePairs);
        intent.putExtra(Constants.TITLE, searchQuery);
        setNextScreenNavigationContext(referrer);
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

    public void setTxtNavSalutation() {
        AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
        TextView txtNavSalutation = (TextView) findViewById(R.id.txtNavSalutation);
        if (txtNavSalutation != null) {
            txtNavSalutation.setText(!TextUtils.isEmpty(authParameters.getMemberFullName()) ?
                    authParameters.getMemberFullName() : authParameters.getMemberEmail());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadNavigationItems() {
        if (mNavRecyclerView == null || mDrawerLayout == null) return;
        TextView txtNavSalutation = (TextView) findViewById(R.id.txtNavSalutation);
        txtNavSalutation.setTypeface(faceRobotoMedium);
        ((TextView) findViewById(R.id.lblWelcome)).setTypeface(faceRobotoMedium);
        TextView txtCityName = (TextView) findViewById(R.id.txtCityName);
        txtCityName.setTypeface(faceRobotoMedium);
        ImageView imgSwitchNav = (ImageView) findViewById(R.id.imgSwitchNav);

        AuthParameters authParameters = AuthParameters.getInstance(this);

        if (!authParameters.isAuthTokenEmpty()) {
            txtNavSalutation.setText(!TextUtils.isEmpty(authParameters.getMemberFullName()) ?
                    authParameters.getMemberFullName() : authParameters.getMemberEmail());
        } else {
            txtNavSalutation.setText(getString(R.string.bigbasketeer));
            txtCityName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getCurrentActivity(), ChangeCityActivity.class);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
            });
        }
        imgSwitchNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNavigationArea((ImageView) v);
            }
        });

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        txtCityName.setText(preferences.getString(Constants.CITY, ""));

        Object[] data = getSectionNavigationItems();
        ArrayList<SectionNavigationItem> sectionNavigationItems = (ArrayList<SectionNavigationItem>) data[0];
        String baseImgUrl = (String) data[1];
        HashMap<Integer, Renderer> rendererHashMap = (HashMap<Integer, Renderer>) data[2];

        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);
        new AccountView<>(this, lstMyAccount);
        NavigationAdapter navigationAdapter = new NavigationAdapter(this, faceRobotoMedium,
                sectionNavigationItems, SectionManager.MAIN_MENU, baseImgUrl, rendererHashMap);
        mNavRecyclerView.setAdapter(navigationAdapter);
    }

    private void toggleNavigationArea(ImageView imgSwitchNav) {
        if (mNavRecyclerView == null) return;

        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);

        int drawableId;
        if (mNavRecyclerView.getVisibility() == View.VISIBLE) {
            mNavRecyclerView.setVisibility(View.GONE);
            lstMyAccount.setVisibility(View.VISIBLE);
            drawableId = R.drawable.ic_menu_white_36dp;
        } else {
            lstMyAccount.setVisibility(View.GONE);
            mNavRecyclerView.setVisibility(View.VISIBLE);
            drawableId = R.drawable.settings;
        }
        imgSwitchNav.setImageDrawable(ContextCompat.getDrawable(this, drawableId));
    }

    private Object[] getSectionNavigationItems() {
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();
        sectionNavigationItems.addAll(getPreBakedNavigationItems());

        SectionManager sectionManager = new SectionManager(this, SectionManager.MAIN_MENU);
        SectionData sectionData = sectionManager.getStoredSectionData(true);
        if (sectionData != null && sectionData.getSections() != null && sectionData.getSections().size() > 0) {
            for (Section section : sectionData.getSections()) {
                if (section == null || section.getSectionItems() == null || section.getSectionItems().size() == 0)
                    continue;
                if (section.getTitle() != null && !TextUtils.isEmpty(section.getTitle().getText())) {
                    sectionNavigationItems.add(new SectionNavigationItem(section));
                }
                setSectionNavigationItemList(this, sectionNavigationItems, section.getSectionItems(),
                        section, sectionData.getBaseImgUrl());
            }
        }
        return new Object[]{sectionNavigationItems, sectionData != null ? sectionData.getBaseImgUrl() : null,
                sectionData != null ? sectionData.getRenderersMap() : null};
    }

    private static <T extends SectionItem> void
    setSectionNavigationItemList(Context context, ArrayList<SectionNavigationItem> sectionNavigationItems,
                                 ArrayList<T> sectionItems,
                                 Section section, String baseImgUrl) {
        for (int i = 0; i < sectionItems.size(); i++) {
            SectionItem sectionItem = sectionItems.get(i);
            if ((sectionItem.getTitle() != null && !TextUtils.isEmpty(sectionItem.getTitle().getText()))
                    || (sectionItem instanceof SubSectionItem && ((SubSectionItem) sectionItem).isLink())) {
                if (sectionItem.hasImage()) {
                    UIUtil.preLoadImage(TextUtils.isEmpty(sectionItem.getImage()) ?
                                    sectionItem.constructImageUrl(context, baseImgUrl) : sectionItem.getImage(),
                            context);
                }
                if (i == 0 && ((sectionItem instanceof SubSectionItem) && !((SubSectionItem) sectionItem).isLink())) {
                    // Duplicate the first element as it'll be used to display the back arrow
                    sectionNavigationItems.add(new SectionNavigationItem<>(section, sectionItem));
                }
                sectionNavigationItems.add(new SectionNavigationItem<>(section, sectionItem));
            }
        }
    }

    private ArrayList<SectionNavigationItem> getPreBakedNavigationItems() {
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();
        SectionItem homeSectionItem = new SectionItem(new SectionTextItem(getString(R.string.home), 0),
                null, null, -1, new DestinationInfo(DestinationInfo.HOME, null));
        ArrayList<SectionItem> homeSectionItems = new ArrayList<>();
        homeSectionItems.add(homeSectionItem);
        Section homeSection = new Section(null, null, Section.MSG, homeSectionItems, null);
        sectionNavigationItems.add(new SectionNavigationItem<>(homeSection, homeSectionItem));

        SectionItem myBasketSectionItem = new SectionItem(new SectionTextItem(getString(R.string.my_basket_header), 0),
                null, null, -1, new DestinationInfo(DestinationInfo.BASKET, null));
        ArrayList<SectionItem> myBasketSectionItems = new ArrayList<>();
        myBasketSectionItems.add(myBasketSectionItem);
        Section myBasketSection = new Section(null, null, Section.MSG, myBasketSectionItems, null);
        sectionNavigationItems.add(new SectionNavigationItem<>(myBasketSection, myBasketSectionItem));

        String smartBasketDeepLink = "bigbasket://smart-basket/";
        SectionItem smartBasketSectionItem = new SectionItem(new SectionTextItem(getString(R.string.smartBasket), 0),
                null, null, -1, new DestinationInfo(DestinationInfo.DEEP_LINK, smartBasketDeepLink));
        ArrayList<SectionItem> smartBasketSectionItems = new ArrayList<>();
        smartBasketSectionItems.add(smartBasketSectionItem);
        Section smartBasketSection = new Section(null, null, Section.MSG, smartBasketSectionItems, null);
        sectionNavigationItems.add(new SectionNavigationItem<>(smartBasketSection, smartBasketSectionItem));
        return sectionNavigationItems;
    }

    @Override
    public void onSubNavigationRequested(Section section, SectionItem sectionItem, String baseImgUrl,
                                         HashMap<Integer, Renderer> rendererHashMap) {
        ArrayList<SubSectionItem> subNavigationSectionItems = sectionItem.getSubSectionItems();
        if (subNavigationSectionItems == null || subNavigationSectionItems.size() == 0) return;

        if (mSubNavLayout == null) {
            mSubNavLayout = (AnimatedRelativeLayout) findViewById(R.id.layoutSubNavigationItems);
        }

        if (mListSubNavigation == null) {
            mListSubNavigation = (RecyclerView) findViewById(R.id.listSubNavigation);
            mListSubNavigation.setHasFixedSize(false);
            mListSubNavigation.setLayoutManager(new LinearLayoutManager(this));
        }
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();
        setSectionNavigationItemList(this, sectionNavigationItems, subNavigationSectionItems, section,
                baseImgUrl);
        NavigationAdapter navigationAdapter = new NavigationAdapter(this, faceRobotoMedium,
                sectionNavigationItems,
                SectionManager.MAIN_MENU, baseImgUrl, rendererHashMap, sectionItem);
        mListSubNavigation.setAdapter(navigationAdapter);
        mSubNavLayout.setVisibility(View.VISIBLE, true);
    }

    @Override
    public void onSubNavigationHideRequested(boolean animated) {
        if (mListSubNavigation != null) {
            mListSubNavigation.setAdapter(null);
        }
        if (mSubNavLayout != null) {
            mSubNavLayout.setVisibility(View.GONE, animated);
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        FragmentManager sfm = getSupportFragmentManager();
        if (sfm == null || sfm.getFragments() == null || sfm.getFragments().size() == 0) {
            LocalyticsWrapper.tagScreen(getScreenTag());
        }
        LocalyticsWrapper.onResume();

        if (isBasketDirty()) {
            syncBasket();
        } else {
            syncCartInfoFromPreference();
        }

        if (mSyncNeeded) {
            mSyncNeeded = false;
            loadNavigationItems();
        } else {
            syncMainMenuIfNeeded();
        }
    }

    private void syncMainMenuIfNeeded() {
        SectionManager sectionManager = new SectionManager(getCurrentActivity(), SectionManager.MAIN_MENU);
        SectionData sectionData = sectionManager.getStoredSectionData();
        if (sectionData == null || sectionData.getSections() == null || sectionData.getSections().size() == 0) {
            if (!checkInternetConnection()) return;
            // Need to refresh
            mSyncNeeded = true;
            new GetDynamicPageTask<>(getCurrentActivity(), SectionManager.MAIN_MENU, false, false, true, true).startTask();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
    }

    private void syncCartInfoFromPreference() {
        // Update from preference
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
        String cartCountStr = preferences.getString(Constants.GET_CART, "0");
        if (cartSummary == null || cartCountStr == null || !TextUtils.isDigitsOnly(cartCountStr)) {
            cartSummary = new CartSummary(0, 0, Integer.parseInt(cartCountStr));
        } else if (!TextUtils.isEmpty(cartCountStr) && TextUtils.isDigitsOnly(cartCountStr)) {
            cartSummary.setNoOfItems(Integer.parseInt(cartCountStr));
        }
        updateCartCountHeaderTextView();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            if (mSubNavLayout != null && mSubNavLayout.getVisibility() == View.VISIBLE) {
                mSubNavLayout.setVisibility(View.GONE, true);
            } else {
                mDrawerLayout.closeDrawers();
            }
        } else {
            super.onBackPressed();
        }
    }
}