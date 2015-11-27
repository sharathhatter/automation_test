package com.bigbasket.mobileapp.activity.base.uiv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.BuildConfig;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ShopFromOrderFragment;
import com.bigbasket.mobileapp.activity.account.uiv3.SocialLoginActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.NavigationAdapter;
import com.bigbasket.mobileapp.adapter.db.AppDataDynamicDbHelper;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.devconfig.DevConfigViewHandler;
import com.bigbasket.mobileapp.fragment.DynamicScreenFragment;
import com.bigbasket.mobileapp.fragment.FlatPageFragment;
import com.bigbasket.mobileapp.fragment.HomeFragment;
import com.bigbasket.mobileapp.fragment.account.AccountView;
import com.bigbasket.mobileapp.fragment.account.ChangePasswordFragment;
import com.bigbasket.mobileapp.fragment.account.UpdateProfileFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.order.GiftOptionsFragment;
import com.bigbasket.mobileapp.fragment.order.MemberAddressListFragment;
import com.bigbasket.mobileapp.fragment.product.CategoryLandingFragment;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoSetProductsFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.BasketDeltaUserActionListener;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.FloatingBasketUIAware;
import com.bigbasket.mobileapp.interfaces.NavigationDrawerAware;
import com.bigbasket.mobileapp.interfaces.NavigationSelectedValueAware;
import com.bigbasket.mobileapp.interfaces.NavigationSelectionAware;
import com.bigbasket.mobileapp.interfaces.OnAddressChangeListener;
import com.bigbasket.mobileapp.interfaces.SubNavigationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.managers.CityManager;
import com.bigbasket.mobileapp.model.AppDataDynamic;
import com.bigbasket.mobileapp.model.account.AddressSummary;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.Renderer;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.section.SubSectionItem;
import com.bigbasket.mobileapp.receivers.DynamicScreenLoaderCallback;
import com.bigbasket.mobileapp.service.AbstractDynamicPageSyncService;
import com.bigbasket.mobileapp.service.AreaPinInfoIntentService;
import com.bigbasket.mobileapp.service.GetAppDataDynamicIntentService;
import com.bigbasket.mobileapp.task.GetCartCountTask;
import com.bigbasket.mobileapp.task.uiv3.ChangeAddressTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.LoaderIds;
import com.bigbasket.mobileapp.util.MemberAddressPageMode;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.SectionCursorHelper;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;
import com.bigbasket.mobileapp.view.uiv3.AnimatedRelativeLayout;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;
import com.bigbasket.mobileapp.view.uiv3.BasketDeltaDialog;
import com.bigbasket.mobileapp.view.uiv3.FloatingBadgeCountView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public abstract class BBActivity extends SocialLoginActivity implements BasketOperationAware,
        CartInfoAware, AppOperationAware, SubNavigationAware, FloatingBasketUIAware,
        OnAddressChangeListener, BasketDeltaUserActionListener, NavigationSelectionAware, NavigationDrawerAware {

    protected BigBasketMessageHandler handler;
    protected String mTitle;
    @Nullable
    private ActionBarDrawerToggle mDrawerToggle;
    private BasketOperationResponse basketOperationResponse;
    private CartSummary cartSummary = new CartSummary();
    @Nullable
    private BBDrawerLayout mDrawerLayout;
    private String currentFragmentTag;
    @Nullable
    private RecyclerView mNavRecyclerView;
    @Nullable
    private AnimatedRelativeLayout mSubNavLayout;
    @Nullable
    private FloatingBadgeCountView mBtnViewBasket;
    @Nullable
    private RecyclerView mListSubNavigation;

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
        if (CityManager.isAreaPinInfoDataStale(getCurrentActivity())) {
            startService(new Intent(getCurrentActivity(), AreaPinInfoIntentService.class));
        }
        readAppDataDynamic();
    }

    public void readAppDataDynamic() {
        getSupportLoaderManager().initLoader(LoaderIds.APP_DATA_DYNAMIC_ID, null,
                new LoaderManager.LoaderCallbacks<Cursor>() {
                    @Override
                    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
                        return new CursorLoader(getCurrentActivity(), AppDataDynamicDbHelper.CONTENT_URI,
                                AppDataDynamicDbHelper.getDefaultProjection(), null, null, null);
                    }

                    @Override
                    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
                        AppDataDynamic appDataDynamic = AppDataDynamic.getInstance(getCurrentActivity());
                        boolean success = appDataDynamic.updateInstance(getCurrentActivity(), data);
                        if (success) {
                            onDataSynced();
                        } else {
                            onDataSyncFailure();
                        }
                    }

                    @Override
                    public void onLoaderReset(Loader<Cursor> loader) {

                    }
                });
    }

    public void onDataSynced() {
        ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(this).getAddressSummaries();
        if (addressSummaries != null && addressSummaries.size() > 0) {
            AddressSummary defaultAddress = addressSummaries.get(0);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putString(Constants.CITY, defaultAddress.getCityName());
            editor.putString(Constants.CITY_ID, String.valueOf(defaultAddress.getCityId()));
            editor.apply();
            setCityText(defaultAddress.toString());
        }

        HomeFragment homeFragment = (HomeFragment) getSupportFragmentManager()
                .findFragmentByTag(HomeFragment.class.getName());
        if (homeFragment != null) {
            homeFragment.syncDynamicTiles();
        }
    }

    public void onDataSyncFailure() {

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
                /**
                 * commenting this line BB-6889-Title text wrong when selecting a category
                 * title text was showing the previous category title even when the user navigates to new category
                 * the value for getting overidden here
                 */
//                setTitle(formatToolbarTitle(mTitle));
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
                if (mNavRecyclerView != null && mNavRecyclerView.getAdapter() != null) {
                    mNavRecyclerView.getAdapter().notifyDataSetChanged();
                }
                if (mListSubNavigation != null && mListSubNavigation.getAdapter() != null)
                    mListSubNavigation.getAdapter().notifyDataSetChanged();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.setStatusBarBackground(R.color.uiv3_status_bar_background);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        loadNavigationItems();
        handleIntent(savedInstanceState);
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

    public BBDrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected abstract void setOptionsMenu(Menu menu);

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
                addToMainLayout(new HomeFragment());
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
            case FragmentCodes.START_GIFTFRAGMENT:
                GiftOptionsFragment giftOptionsFragment = new GiftOptionsFragment();
                Bundle giftBundle = getIntent().getExtras();
                giftOptionsFragment.setArguments(giftBundle);
                addToMainLayout(giftOptionsFragment);
                break;
            case FragmentCodes.START_VIEW_DELIVERY_ADDRESS:
                MemberAddressListFragment memberAddressListFragment = new MemberAddressListFragment();
                Bundle addressbundle = new Bundle();
                addressbundle.putInt(Constants.ADDRESS_PAGE_MODE,
                        getIntent().getIntExtra(Constants.ADDRESS_PAGE_MODE, MemberAddressPageMode.CHECKOUT));
                addressbundle.putString(Constants.TOTAL_BASKET_VALUE,
                        getIntent().getStringExtra(Constants.TOTAL_BASKET_VALUE));
                memberAddressListFragment.setArguments(addressbundle);
                addToMainLayout(memberAddressListFragment);
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
                String orderNumber = getIntent().getStringExtra(Constants.ORDER_NUMBER);
                Bundle orderProductListBundle = new Bundle();
                orderProductListBundle.putString(Constants.ORDER_ID, orderId);
                orderProductListBundle.putString(Constants.ORDER_NUMBER, orderNumber);
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
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setCurrentDeliveryAddress(String addressId) {
        new ChangeAddressTask<>(this, addressId, null, null, null, true).startTask();
    }

    @Override
    public void onAddressChanged(ArrayList<AddressSummary> addressSummaries, String selectedAddressId) {
        if (addressSummaries != null && addressSummaries.size() > 0) {
            City newCity = new City(addressSummaries.get(0).getCityName(),
                    addressSummaries.get(0).getCityId());
            changeCity(newCity);
            startService(new Intent(getCurrentActivity(), GetAppDataDynamicIntentService.class));
        } else {
            showToast(getString(R.string.unknownError));
        }
    }

    @Override
    public void onAddressNotSupported(String msg) {
        showAlertDialog(msg);
    }

    @Override
    public void onBasketDelta(String addressId, String lat, String lng,
                              String title, String msg, @Nullable String area,
                              boolean hasQcError,
                              ArrayList<QCErrorData> qcErrorDatas) {
        new BasketDeltaDialog<>().show(this, title, msg, hasQcError, qcErrorDatas, addressId,
                getString(R.string.change), lat, lng, area);
    }

    @Override
    public void onNoBasketDelta(String addressId, String lat, String lng, @Nullable String area) {
        new ChangeAddressTask<>(this, addressId, lat, lng, area, false).startTask();
    }

    @Override
    public void onUpdateBasket(String addressId, String lat, String lng, @Nullable String area) {
        new ChangeAddressTask<>(this, addressId, lat, lng, area, false).startTask();
    }

    @Override
    public void onNoBasketUpdate() {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == NavigationCodes.LAUNCH_FRAGMENT && data != null) {
            createFragmentFromName(data.getStringExtra(Constants.FRAGMENT_CLASS_NAME), data.getExtras(),
                    data.getStringExtra(Constants.FRAGMENT_TAG));
            return;
        } else if (resultCode == NavigationCodes.ACCOUNT_UPDATED) {
            setTxtNavSalutation();
            return;
        } else if (resultCode == NavigationCodes.ADDRESS_CREATED_MODIFIED && data != null) {
            String addressId = data.getStringExtra(Constants.ADDRESS_ID);
            if (!TextUtils.isEmpty(addressId)) {
                setCurrentDeliveryAddress(addressId);
                return;
            }
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
    public void updateUIAfterBasketOperationFailed(@BasketOperation.Mode int basketOperation,
                                                   @Nullable WeakReference<TextView> basketCountTextViewRef,
                                                   @Nullable WeakReference<View> viewDecQtyRef,
                                                   @Nullable WeakReference<View> viewIncQtyRef,
                                                   @Nullable WeakReference<View> btnAddToBasketRef,
                                                   Product product, String qty, String errorType,
                                                   @Nullable WeakReference<View> productViewRef,
                                                   @Nullable WeakReference<EditText> editTextQtyRef) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(this, "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
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

        Log.d("BB", "BB ACTIVITY updateUIAfterBasketOperationSuccess");

        int productQtyInBasket = 0;
        if (basketOperationResponse.getBasketResponseProductInfo() != null) {
            productQtyInBasket = Integer.parseInt(basketOperationResponse.getBasketResponseProductInfo().getTotalQty());
        }
        int totalProductsInBasket = basketOperationResponse.getCartSummary().getNoOfItems();

        if (productQtyInBasket == 0) {
            if (viewDecQtyRef != null && viewDecQtyRef.get() != null) {
                viewDecQtyRef.get().setVisibility(View.GONE);
            }
            if (viewIncQtyRef != null && viewIncQtyRef.get() != null) {
                viewIncQtyRef.get().setVisibility(View.GONE);
            }
            if (btnAddToBasketRef != null && btnAddToBasketRef.get() != null) {
                btnAddToBasketRef.get().setVisibility(View.VISIBLE);
            }
            if (editTextQtyRef != null && editTextQtyRef.get() != null
                    && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQtyRef.get().setText("1");
                editTextQtyRef.get().setVisibility(View.VISIBLE);
            }
            if (basketCountTextViewRef != null && basketCountTextViewRef.get() != null) {
                basketCountTextViewRef.get().setVisibility(View.GONE);
            }
        } else {
            if (viewDecQtyRef != null && viewDecQtyRef.get() != null) {
                viewDecQtyRef.get().setVisibility(View.VISIBLE);
            }
            if (viewIncQtyRef != null && viewIncQtyRef.get() != null) {
                viewIncQtyRef.get().setVisibility(View.VISIBLE);
            }
            if (btnAddToBasketRef != null && btnAddToBasketRef.get() != null) {
                btnAddToBasketRef.get().setVisibility(View.GONE);
            }
            if (basketCountTextViewRef != null && basketCountTextViewRef.get() != null) {
                basketCountTextViewRef.get().setText(String.valueOf(productQtyInBasket));
                basketCountTextViewRef.get().setVisibility(View.VISIBLE);
            }
            if (editTextQtyRef != null && editTextQtyRef.get() != null
                    && AuthParameters.getInstance(getCurrentActivity()).isKirana()) {
                editTextQtyRef.get().setVisibility(View.GONE);
            }
        }

        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
            if (cartInfoMapRef != null && cartInfoMapRef.get() != null) {
                cartInfoMapRef.get().put(product.getSku(), productQtyInBasket);
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
        editor.apply();
        updateCartCountHeaderTextView();
    }

    @Override
    public void markBasketDirty() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putBoolean(Constants.IS_BASKET_COUNT_DIRTY, true);
        editor.apply();
    }

    public boolean isBasketDirty() {
        return PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).
                getBoolean(Constants.IS_BASKET_COUNT_DIRTY, false);
    }

    @Override
    public void syncBasket() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove(Constants.IS_BASKET_COUNT_DIRTY);
        editor.apply();
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

    public void handleIntent(Bundle savedInstanceState) {
        currentFragmentTag = savedInstanceState != null ?
                savedInstanceState.getString(Constants.FRAGMENT_TAG) : null;
        if (TextUtils.isEmpty(currentFragmentTag) ||
                getSupportFragmentManager().findFragmentByTag(currentFragmentTag) == null) {
            startFragment();
        }
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
/*
    Looks like Constants.LOGOUT dialog is not used at all
    @Override
    protected void onPositiveButtonClicked(int sourceName, Bundle valuePassed) {
        switch (sourceName) {
                case Constants.LOGOUT:
                    onLogoutRequested();
                    break;
                default:
                    super.onPositiveButtonClicked(sourceName, valuePassed);
                    break;
            }
    }
*/

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
        getSupportLoaderManager().initLoader(LoaderIds.MAIN_MENU_ID, null,
                new DynamicScreenLoaderCallback(this) {
                    @Override
                    public void onCursorNonEmpty(Cursor data) {
                        showMenuLoading(false);
                        displayMainMenuHeader();
                        SectionCursorHelper.getNavigationAdapterAsync(getCurrentActivity(), data, getCategoryId(),
                                new SectionCursorHelper.NavigationCallback() {
                                    @Override
                                    public void onNavigationAdapterCreated(NavigationAdapter navigationAdapter) {
                                        mNavRecyclerView.setAdapter(navigationAdapter);
                                    }
                                });
                    }

                    @Override
                    public void onCursorLoadingInProgress() {
                        showMenuLoading(true);
                    }
                });
    }

    private void displayMainMenuHeader() {
        TextView txtNavSalutation = (TextView) findViewById(R.id.txtNavSalutation);
        txtNavSalutation.setTypeface(faceRobotoMedium);
        TextView lblWelCome = (TextView) findViewById(R.id.lblWelcome);
        lblWelCome.setTypeface(faceRobotoMedium);
        ImageView imgSwitchNav = (ImageView) findViewById(R.id.imgSwitchNav);

        AuthParameters authParameters = AuthParameters.getInstance(this);

        ArrayList<AddressSummary> addressSummaries = AppDataDynamic.getInstance(this).getAddressSummaries();
        if (addressSummaries != null && addressSummaries.size() > 0) {
            setCityText(addressSummaries.get(0).toString());
        } else {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
            setCityText(preferences.getString(Constants.CITY, ""));
        }
        if (!authParameters.isAuthTokenEmpty()) {
            txtNavSalutation.setText(!TextUtils.isEmpty(authParameters.getMemberFullName()) ?
                    authParameters.getMemberFullName() : authParameters.getMemberEmail());
        } else {
            txtNavSalutation.setText(getString(R.string.bigbasketeer));
        }
        if (BuildConfig.DEBUG && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            DevConfigViewHandler.setView(lblWelCome);
        }

        imgSwitchNav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleNavigationArea((ImageView) v);
            }
        });
        imgSwitchNav.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.settings));
        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);
        lstMyAccount.setVisibility(View.GONE);
        new AccountView<>(this, lstMyAccount);
    }

    @Override
    public void doLogout() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
        super.doLogout();
    }

    private void showMenuLoading(boolean visible) {
        ProgressBar progressBarMenu = (ProgressBar) findViewById(R.id.progressBarMenu);
        if (visible) {
            progressBarMenu.setVisibility(View.VISIBLE);
            if (mNavRecyclerView != null) {
                mNavRecyclerView.setVisibility(View.GONE);
            }
        } else {
            progressBarMenu.setVisibility(View.GONE);
            if (mNavRecyclerView != null) {
                mNavRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setCityText(String text) {
        TextView txtCityName = (TextView) findViewById(R.id.txtCityName);
        Button btnChangeAddress = (Button) findViewById(R.id.btnChangeAddress);
        if (txtCityName == null || btnChangeAddress == null) return;
        txtCityName.setTypeface(faceRobotoMedium);

        AuthParameters authParameters = AuthParameters.getInstance(this);
        if (authParameters.isAuthTokenEmpty() || authParameters.isMultiCityEnabled()) {
            View.OnClickListener changeAddressOnClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeAddressRequested();
                }
            };
            txtCityName.setOnClickListener(changeAddressOnClickListener);
            btnChangeAddress.setOnClickListener(changeAddressOnClickListener);
        } else {
            btnChangeAddress.setVisibility(View.GONE);
        }
        txtCityName.setText(text);
    }

    public void changeAddressRequested() {
        if (AuthParameters.getInstance(this).isAuthTokenEmpty()) {
            showChangeCity(false, getNextScreenNavigationContext(), false);
        } else {
            Intent intent = new Intent(this, BackButtonActivity.class);
            intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_DELIVERY_ADDRESS);
            intent.putExtra(Constants.ADDRESS_PAGE_MODE, MemberAddressPageMode.ADDRESS_SELECT);
            startActivityForResult(intent, NavigationCodes.ADDRESS_CREATED_MODIFIED);
        }
    }

    private void toggleNavigationArea(ImageView imgSwitchNav) {
        if (mNavRecyclerView == null) return;
        ProgressBar progressBarMenu = (ProgressBar) findViewById(R.id.progressBarMenu);
        ListView lstMyAccount = (ListView) findViewById(R.id.lstMyAccount);

        int drawableId;
        if (mNavRecyclerView.getVisibility() == View.VISIBLE) {
            if (progressBarMenu.getVisibility() != View.VISIBLE) {  // If main menu is downloading, then don't toggle any visibility
                mNavRecyclerView.setVisibility(View.GONE);
            }
            lstMyAccount.setVisibility(View.VISIBLE);
            drawableId = R.drawable.ic_menu_white_36dp;
        } else {
            lstMyAccount.setVisibility(View.GONE);
            if (progressBarMenu.getVisibility() != View.VISIBLE) { // If main menu is downloading, then don't toggle any visibility
                mNavRecyclerView.setVisibility(View.VISIBLE);
            }
            drawableId = R.drawable.settings;
        }
        imgSwitchNav.setImageDrawable(ContextCompat.getDrawable(this, drawableId));
    }

    /**
     * @return the category selected by the user
     * method to be overridden in classes extending BBActivity
     */
    protected String getCategoryId() {
        return null;
    }

    protected String getSubCategoryId() {
        return null;
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
        SectionCursorHelper.setSectionNavigationItemList(sectionNavigationItems,
                subNavigationSectionItems, section);
        String selectedId = getSubCategoryId();

        NavigationAdapter navigationAdapter = new NavigationAdapter(this, faceRobotoMedium,
                sectionNavigationItems,
                AbstractDynamicPageSyncService.MAIN_MENU, baseImgUrl, rendererHashMap, sectionItem);
        if (!TextUtils.isEmpty(selectedId)) {
            navigationAdapter.setSelectedCategoryString(selectedId);
        }
        mListSubNavigation.setAdapter(navigationAdapter);
        mSubNavLayout.setVisibility(View.VISIBLE, true);
//        navigationAdapter.notifyDataSetChanged();
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

        if (AppDataDynamic.isStale(getCurrentActivity())) {
            startService(new Intent(getCurrentActivity(), GetAppDataDynamicIntentService.class));
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
        if (cartSummary == null || TextUtils.isEmpty(cartCountStr) || !TextUtils.isDigitsOnly(cartCountStr)) {
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
                return;
            } else {
                mDrawerLayout.closeDrawers();
                return;
            }
        }
        super.onBackPressed();
    }

    /**
     * @param name: name of the category user has selected in the navigation drawer
     *              passing the selected category name to the adapter
     */
    @Override
    public void onNavigationSelection(String name) {
        if (mNavRecyclerView != null && mNavRecyclerView.getAdapter() != null) {
            ((NavigationSelectedValueAware) mNavRecyclerView.getAdapter()).setSelectedNavigationCategory(name);
        }
    }

    @Override
    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }
}