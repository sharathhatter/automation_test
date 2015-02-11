package com.bigbasket.mobileapp.activity.base.uiv3;

import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.ShopFromOrderFragment;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.NavigationAdapter;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.fragment.HomeFragment;
import com.bigbasket.mobileapp.fragment.account.AccountSettingFragment;
import com.bigbasket.mobileapp.fragment.account.ChangeCityDialogFragment;
import com.bigbasket.mobileapp.fragment.account.ChangePasswordFragment;
import com.bigbasket.mobileapp.fragment.account.DoWalletFragment;
import com.bigbasket.mobileapp.fragment.account.UpdatePinFragment;
import com.bigbasket.mobileapp.fragment.account.UpdateProfileFragment;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.order.MemberAddressListFragment;
import com.bigbasket.mobileapp.fragment.order.OrderThankYouFragment;
import com.bigbasket.mobileapp.fragment.order.ShowCartFragment;
import com.bigbasket.mobileapp.fragment.order.SlotSelectionFragment;
import com.bigbasket.mobileapp.fragment.product.BrowseByOffersFragment;
import com.bigbasket.mobileapp.fragment.product.CategoryProductsFragment;
import com.bigbasket.mobileapp.fragment.product.GenericProductListFragment;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.fragment.product.ProductListDialogFragment;
import com.bigbasket.mobileapp.fragment.product.SearchFragment;
import com.bigbasket.mobileapp.fragment.product.SubCategoryListFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoDetailFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoSetProductsFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListSummaryFragment;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProductListDialogAware;
import com.bigbasket.mobileapp.model.SectionManager;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.navigation.SectionNavigationItem;
import com.bigbasket.mobileapp.model.order.Order;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.uiv2.ProductListType;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.section.SectionData;
import com.bigbasket.mobileapp.model.section.SectionItem;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;

import java.util.ArrayList;


public class BBActivity extends BaseActivity implements BasketOperationAware,
        CartInfoAware, HandlerAware, ProductListDialogAware {

    private ActionBarDrawerToggle mDrawerToggle;
    private String mDrawerTitle;
    private String mTitle;
    private BasketOperationResponse basketOperationResponse;
    private CartSummary cartInfo = new CartSummary();
    protected BigBasketMessageHandler handler;
    private BBDrawerLayout mDrawerLayout;
    private String currentFragmentTag;
    private TextView mTextCartCount;
    private RecyclerView mNavRecyclerView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getMainLayout());

        mNavRecyclerView = (RecyclerView) findViewById(R.id.listNavigation);
        mNavRecyclerView.setHasFixedSize(false);
        mNavRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                        finish();
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

        if (cartInfo != null && cartInfo.getNoOfItems() == 0) {
            // Update from preference
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
            String cartCountStr = preferences.getString(Constants.GET_CART, null);
            if (!TextUtils.isEmpty(cartCountStr) && TextUtils.isDigitsOnly(cartCountStr)) {
                cartInfo.setNoOfItems(Integer.parseInt(cartCountStr));
            }
        }
    }

    public int getMainLayout() {
        return R.layout.uiv3_main_layout;
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
                toolbar.setTitle("  " + mTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                toolbar.setTitle("  " + mDrawerTitle);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
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
        initializeCartCountTextView(menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void setOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            menuInflater.inflate(R.menu.action_menu, menu);

            final MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) searchMenuItem.getActionView();

            // Setting the search listener
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setOnSearchClickListener(new View.OnClickListener() {
                private boolean extended = false;

                @Override
                public void onClick(View v) {
                    if (!extended) {
                        extended = true;
                        ViewGroup.LayoutParams lp = v.getLayoutParams();
                        lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    }
                }
            });
            searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        if (!hasFocus) {
                            searchMenuItem.collapseActionView();
                            searchView.setQuery("", false);
                        }
                    }
                }
            });
        } else {
            menuInflater.inflate(R.menu.action_menu_pre_honeycomb, menu);
        }
        if (AuthParameters.getInstance(this).isAuthTokenEmpty()) {
            hideMemberMenuItems(menu);
        } else {
            hideGuestMenuItems(menu);
            MenuItem userInfoMenuItem = menu.findItem(R.id.action_user_info);
            String memberFullName = AuthParameters.getInstance(this).getMemberFullName();
            if (TextUtils.isEmpty(memberFullName)) {
                userInfoMenuItem.setVisible(false);
            } else {
                userInfoMenuItem.setTitle(memberFullName);
            }
        }
    }

    public void initializeCartCountTextView(Menu menu) {
        MenuItem menuItemViewBasket = menu.findItem(R.id.action_view_basket);
        if (menuItemViewBasket == null) return;
        MenuItemCompat.setActionView(menuItemViewBasket, R.layout.uiv3_basket_count_icon);
        View basketCountView = MenuItemCompat.getActionView(menuItemViewBasket);
        if (basketCountView != null) {
            basketCountView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    launchViewBasket();
                }
            });
            mTextCartCount = (TextView) basketCountView.findViewById(R.id.txtNumItemsInBasket);
            if (mTextCartCount != null) {
                mTextCartCount.setTypeface(faceRobotoRegular);
                updateCartCountHeaderTextView();
            }
        }
    }

    private void launchViewBasket() {
        Intent intent = new Intent(this, BackButtonActivity.class);
        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_VIEW_BASKET);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
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
                addressbundle.putBoolean(Constants.FROM_ACCOUNT_PAGE, true);
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
                ShowCartFragment showCartFragment = new ShowCartFragment();
                Bundle cartBundle = new Bundle();
                cartBundle.putString(Constants.INTERNAL_VALUE, getIntent().getStringExtra(Constants.INTERNAL_VALUE));
                showCartFragment.setArguments(cartBundle);
                addToMainLayout(showCartFragment);
                break;
            case FragmentCodes.START_PRODUCT_DETAIL:
                ProductDetailFragment productDetailFragment = new ProductDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString(Constants.SKU_ID, getIntent().getStringExtra(Constants.SKU_ID));
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
            case FragmentCodes.START_BROWSE_BY_OFFERS:
                addToMainLayout(new BrowseByOffersFragment());
                break;
            case FragmentCodes.START_CATEGORY_LANDING:
                SubCategoryListFragment subCategoryListFragment = new SubCategoryListFragment();
                Bundle cubCatBundle = new Bundle();
                cubCatBundle.putString(Constants.TOP_CATEGORY_SLUG, getIntent().getStringExtra(Constants.TOP_CATEGORY_NAME));
                cubCatBundle.putString(Constants.TOP_CATEGORY_NAME, getIntent().getStringExtra(Constants.TOP_CATEGORY_NAME));
                subCategoryListFragment.setArguments(cubCatBundle);
                addToMainLayout(subCategoryListFragment);
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
                ShopFromOrderFragment shopFromOrderFragment = new ShopFromOrderFragment();
                shopFromOrderFragment.setArguments(orderProductListBundle);
                addToMainLayout(shopFromOrderFragment);
                break;
            case FragmentCodes.START_NOW_AT_BB:
                GenericProductListFragment productListFragment = new GenericProductListFragment();
                Bundle productListArgs = new Bundle();
                productListArgs.putString(Constants.TYPE, ProductListType.NOW_AT_BB.get());
                productListFragment.setArguments(productListArgs);
                addToMainLayout(productListFragment);
                break;
            case FragmentCodes.START_NEW_AT_BB:
                productListFragment = new GenericProductListFragment();
                productListArgs = new Bundle();
                productListArgs.putString(Constants.TYPE, ProductListType.NEW_AT_BB.get());
                productListFragment.setArguments(productListArgs);
                addToMainLayout(productListFragment);
                break;
            case FragmentCodes.START_BUNDLE_PACK:
                productListFragment = new GenericProductListFragment();
                productListArgs = new Bundle();
                productListArgs.putString(Constants.TYPE, ProductListType.BUNDLE_PACK.get());
                productListFragment.setArguments(productListArgs);
                addToMainLayout(productListFragment);
                break;
            case FragmentCodes.START_PRODUCT_CATEGORY:
                launchProductCategoryFragment(getIntent().getStringExtra(Constants.CATEGORY_SLUG));
                break;
            case FragmentCodes.START_SHOPPING_LIST_SUMMARY:
                bundle = new Bundle();
                bundle.putParcelable(Constants.SHOPPING_LIST_NAME,
                        getIntent().getParcelableExtra(Constants.SHOPPING_LIST_NAME));
                ShoppingListSummaryFragment shoppingListSummaryFragment = new ShoppingListSummaryFragment();
                shoppingListSummaryFragment.setArguments(bundle);
                addToMainLayout(shoppingListSummaryFragment);
                break;
            case FragmentCodes.START_SHOPPING_LIST_LANDING:
                addToMainLayout(new ShoppingListFragment());
                break;
            case FragmentCodes.START_SEARCH:
                doSearch(getIntent().getStringExtra(Constants.SEARCH_QUERY));
                break;
            case FragmentCodes.START_GENERIC_PRODUCT_LIST:
                String type = getIntent().getStringExtra(Constants.TYPE);
                String slug = getIntent().getStringExtra(Constants.SLUG);
                String title = getIntent().getStringExtra(Constants.TITLE);
                if (!TextUtils.isEmpty(type)) {
                    productListFragment = new GenericProductListFragment();
                    productListArgs = new Bundle();
                    productListArgs.putString(Constants.TYPE, type);
                    if (!TextUtils.isEmpty(slug)) {
                        productListArgs.putString(Constants.SLUG, slug);
                    }
                    if (!TextUtils.isEmpty(title)) {
                        productListArgs.putString(Constants.TITLE, title);
                    }
                    productListFragment.setArguments(productListArgs);
                    addToMainLayout(productListFragment);
                }
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
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.action_search_icon:
                onSearchRequested();
                return false;
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_account:
                if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                    showAlertDialog(null, "Please sign in to view/edit your Account",
                            NavigationCodes.GO_TO_LOGIN);
                } else {
                    Intent intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_ACCOUNT_SETTING);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
                return true;
            case R.id.action_communication_hub:
                launchKonotor();
                return true;
            case R.id.action_shopping_list:
                if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                    showAlertDialog(null,
                            "Please sign-in to view your shopping lists", NavigationCodes.GO_TO_LOGIN);
                } else {
                    Intent intent = new Intent(this, BackButtonActivity.class);
                    intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_SHOPPING_LIST_LANDING);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
                return true;
            case R.id.action_smart_basket:
                if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                    showAlertDialog(null,
                            "Please sign-in to view your smart basket", NavigationCodes.GO_TO_LOGIN);
                } else {
                    ShoppingListName shoppingListName = new ShoppingListName(Constants.SMART_BASKET,
                            Constants.SMART_BASKET_SLUG, true);
                    ShoppingListSummaryFragment shoppingListSummaryFragment = new ShoppingListSummaryFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable(Constants.SHOPPING_LIST_NAME, shoppingListName);
                    shoppingListSummaryFragment.setArguments(bundle);
                    addToMainLayout(shoppingListSummaryFragment);
                }
                return true;
            case R.id.action_refer_friend:
                return true;
            case R.id.action_rate_app:
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + Constants.BASE_PKG_NAME)));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + Constants.BASE_PKG_NAME)));
                }
                return true;
            case R.id.action_login:
                Intent intent = new Intent(this, SignInActivity.class);
                startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                return true;
            case R.id.action_view_basket:
                launchViewBasket();
                return true;
            case R.id.action_logout:
                if (isSocialLogin()) {
                    onLogoutRequested();
                } else {
                    showAlertDialog(getString(R.string.signOut), getString(R.string.signoutConfirmation),
                            DialogButton.YES, DialogButton.NO, Constants.LOGOUT);
                }
                return true;
            case R.id.action_change_city:
                ChangeCityDialogFragment changeCityDialog = ChangeCityDialogFragment.newInstance();
                changeCityDialog.show(getSupportFragmentManager(), Constants.CITIES);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                                                   String errorType) {
        if (errorType.equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(this, "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    View viewDecQty, View viewIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty) {

        int productQtyInBasket = Integer.parseInt(basketOperationResponse.getBasketResponseProductInfo().getTotalQty());
        int totalProductsInBasket = basketOperationResponse.getCartSummary().getNoOfItems();

        if (basketOperation == BasketOperation.INC) {
            if (productQtyInBasket == 1) {
                Toast.makeText(this, "Product added to basket.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product quantity increased in the basket.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (basketOperation == BasketOperation.EMPTY || productQtyInBasket == 0) {
                Toast.makeText(this, "Product removed from basket.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Product quantity reduced from basket.", Toast.LENGTH_SHORT).show();
            }
        }

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
                basketCountTextView.setText(productQtyInBasket + " in");
                basketCountTextView.setVisibility(View.VISIBLE);
            }
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
    public void setCartInfo(CartSummary cartInfo) {
        this.cartInfo = cartInfo;
    }

    @Override
    public CartSummary getCartInfo() {
        return cartInfo;
    }

    @Override
    public void updateUIForCartInfo() {
        if (cartInfo == null) return;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity()).edit();
        editor.putString(Constants.GET_CART, String.valueOf(cartInfo.getNoOfItems()));
        editor.commit();

        if (cartInfo.getAnalyticsEngine() != null) {
            AuthParameters.getInstance(getCurrentActivity()).setMoEngaleLocaliticsEnabled(cartInfo.getAnalyticsEngine().isMoEngageEnabled(),
                    cartInfo.getAnalyticsEngine().isAnalyticsEnabled(), getCurrentActivity());
        }
        updateCartCountHeaderTextView();
        AuthParameters.updateInstance(getCurrentActivity());
    }

    private void updateCartCountHeaderTextView() {
        if (cartInfo != null && mTextCartCount != null) {
            if (cartInfo.getNoOfItems() <= 0) {
                mTextCartCount.setVisibility(View.GONE);
            } else {
                mTextCartCount.setVisibility(View.VISIBLE);
                mTextCartCount.setText(String.valueOf(cartInfo.getNoOfItems()));
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

    private void launchCategoryProducts(String categoryName, String categoryUrl,
                                        String categorySlug) {
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(categoryName, categoryUrl);
        launchProductCategoryFragment(categorySlug);
    }

    private void launchProductCategoryFragment(String categorySlug) {
        Bundle bundle = new Bundle();
        bundle.putString("slug_name_category", categorySlug);
        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
        categoryProductsFragment.setArguments(bundle);
        addToMainLayout(categoryProductsFragment);
    }

    private void doSearch(String searchQuery) {
        searchQuery = searchQuery.trim();
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(searchQuery);
        SearchFragment searchFragment = new SearchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SEARCH_QUERY, searchQuery);
        searchFragment.setArguments(bundle);
        addToMainLayout(searchFragment);
    }

    public void onChangeTitle(String title) {
        setTitle(title);
    }

    public void setTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            mTitle = title == null ? "" : "  " + title;
            if (mTitle.length() > 25) {
                mTitle = mTitle.substring(0, 22);
                mTitle += "...";
            }
            actionBar.setTitle(mTitle);
        }
    }

    private void hideGuestMenuItems(Menu menu) {
        MenuItem loginMenuItem = menu.findItem(R.id.action_login);
        MenuItem changeCityRegisterMenu = menu.findItem(R.id.action_change_city);

        loginMenuItem.setVisible(false);
        changeCityRegisterMenu.setVisible(false);
    }

    private void hideMemberMenuItems(Menu menu) {
        MenuItem userInfoMenuItem = menu.findItem(R.id.action_user_info);
        MenuItem logoutMenuItem = menu.findItem(R.id.action_logout);
        MenuItem shoppingListMenuItem = menu.findItem(R.id.action_shopping_list);
        MenuItem smartBasketMenuItem = menu.findItem(R.id.action_smart_basket);
        MenuItem referFriendsMenuItem = menu.findItem(R.id.action_refer_friend);

        logoutMenuItem.setVisible(false);
        userInfoMenuItem.setVisible(false);
        shoppingListMenuItem.setVisible(false);
        smartBasketMenuItem.setVisible(false);
        referFriendsMenuItem.setVisible(false);
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName)) {
            switch (sourceName) {
                case Constants.LOGOUT:
                    onLogoutRequested();
                    this.invalidateOptionsMenu();
                    addToMainLayout(new HomeFragment(), Constants.HOME);
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    private void loadNavigationItems() {

        TextView txtNavSalutation = (TextView) findViewById(R.id.txtNavSalutation);
        txtNavSalutation.setTypeface(faceRobotoRegular);
        AuthParameters authParameters = AuthParameters.getInstance(this);
        if (!authParameters.isAuthTokenEmpty()) {
            txtNavSalutation.setText("Welcome " + authParameters.getMemberFullName().split(" ")[0]);
        } else {
            txtNavSalutation.setText("Welcome Guest");
        }

        ArrayList<SectionNavigationItem> sectionNavigationItems = getSectionNavigationItems();

        NavigationAdapter navigationAdapter = new NavigationAdapter(this, faceRobotoRegular, sectionNavigationItems);
        mNavRecyclerView.setAdapter(navigationAdapter);

    }

    private ArrayList<SectionNavigationItem> getSectionNavigationItems() {
        ArrayList<SectionNavigationItem> sectionNavigationItems = new ArrayList<>();

        SectionNavigationItem sniHome = new SectionNavigationItem(false, true, null, null);
        sectionNavigationItems.add(sniHome);
        SectionManager sectionManager = new SectionManager(this, SectionManager.MAIN_MENU);
        SectionData sectionData = sectionManager.getStoredSectionData(true);
        if (sectionData != null && sectionData.getSections() != null && sectionData.getSections().size() > 0) {
            int numSections = sectionData.getSections().size();
            for (int i = 0; i < numSections; i++) {
                Section section = sectionData.getSections().get(i);
                if (section.getSectionItems() == null || section.getSectionItems().size() == 0)
                    continue;
                for (SectionItem sectionItem : section.getSectionItems()) {
                    SectionNavigationItem sni = new SectionNavigationItem(false, section, sectionItem);
                    sectionNavigationItems.add(sni);
                }
                if (i != numSections - 1) {
                    sectionNavigationItems.add(new SectionNavigationItem(true));
                }
            }
        }
        return sectionNavigationItems;
    }

    @Override
    public void showDialog(String title, ArrayList<Product> products, int productCount, String baseImgUrl,
                           boolean asDialog, String tagName) {
        if (asDialog) {
            ProductListDialogFragment productListDialogFragment = ProductListDialogFragment.
                    newInstance(title, products, productCount, baseImgUrl, 10, 20);
            productListDialogFragment.show(getSupportFragmentManager(),
                    Constants.SHOPPING_LISTS);
        } else {
            ProductListDialogFragment productListDialogFragment = ProductListDialogFragment.
                    newInstance(title, products, productCount, baseImgUrl, ProductListDialogFragment.SHOW_ALL,
                            ProductListDialogFragment.SHOW_ALL);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.content_frame, productListDialogFragment, tagName);
            if (tagName != null) {
                ft.addToBackStack(tagName);
            }
            ft.commit();
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

    protected BBDrawerLayout getDrawerLayout() {
        return mDrawerLayout;
    }
}