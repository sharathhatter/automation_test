package com.bigbasket.mobileapp.activity.base.uiv3;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.*;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.OrderListActivity;
import com.bigbasket.mobileapp.activity.account.uiv3.SignInSignUpActivity;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.NavigationListAdapter;
import com.bigbasket.mobileapp.adapter.db.MostSearchesAdapter;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.fragment.HomeFragment;
import com.bigbasket.mobileapp.fragment.ShopFragment;
import com.bigbasket.mobileapp.fragment.account.*;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.fragment.order.MemberAddressListFragment;
import com.bigbasket.mobileapp.fragment.order.ShowCartFragment;
import com.bigbasket.mobileapp.fragment.order.SlotSelectionFragment;
import com.bigbasket.mobileapp.fragment.product.BrowseByOffersFragment;
import com.bigbasket.mobileapp.fragment.product.CategoryProductsFragment;
import com.bigbasket.mobileapp.fragment.product.SearchFragment;
import com.bigbasket.mobileapp.fragment.product.ShopInShopFragment;
import com.bigbasket.mobileapp.fragment.product.SubCategoryListFragment;
import com.bigbasket.mobileapp.fragment.promo.PromoCategoryFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListFragment;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListSummaryFragment;
import com.bigbasket.mobileapp.handler.MessageHandler;
import com.bigbasket.mobileapp.interfaces.BasketOperationAware;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.model.cart.BasketOperation;
import com.bigbasket.mobileapp.model.cart.BasketOperationResponse;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.navigation.NavigationItem;
import com.bigbasket.mobileapp.model.navigation.NavigationSubItem;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.view.uiv3.BBDrawerLayout;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


public class BBActivity extends BaseActivity implements BasketOperationAware,
        CartInfoAware, HandlerAware {

    private static final String TAG = BBActivity.class.getName();

    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private BasketOperationResponse basketOperationResponse;
    private CartSummary cartInfo = new CartSummary();
    protected Handler handler;
    private BBDrawerLayout mDrawerLayout;
    private String currentFragmentTag;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.uiv3_main_layout);

        handler = new MessageHandler(this);
        mTitle = mDrawerTitle = getTitle();

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
    public String getTag() {
        return TAG;
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
        ft.addToBackStack(tag);
        ft.commit();
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        setOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    protected void setOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            menuInflater.inflate(R.menu.action_menu, menu);

            MenuItem searchMenuItem = menu.findItem(R.id.action_search);
            SearchView searchView = (SearchView) searchMenuItem.getActionView();

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
        } else {
            menuInflater.inflate(R.menu.action_menu_pre_honeycomb, menu);
        }
        if (AuthParameters.getInstance(this).isAuthTokenEmpty()) {
            hideMemberMenuItems(menu);
        } else {
            hideGuestMenuItems(menu);
        }
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
            case FragmentCodes.START_SHOP_IN_SHOP:
                addToMainLayout(new ShopInShopFragment());
                break;
            case FragmentCodes.START_SHOP_LIST:
                addToMainLayout(new ShopFragment());
                break;
            case FragmentCodes.START_CHANGE_CITY:
                addToMainLayout(new ChangeCityFragment());
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
            case FragmentCodes.START_ORDER_LIST:
                Intent orderListIntent = new Intent(this, OrderListActivity.class);
                String orderType = getIntent().getStringExtra(Constants.ORDER);
                orderListIntent.putExtra(Constants.ORDER, orderType);
                startActivityForResult(orderListIntent, Constants.GO_TO_HOME);
                break;
            default:
                addToMainLayout(new HomeFragment(), Constants.HOME);
                break;
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
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
            case R.id.action_communication_hub:
                launchKonotor();
                return true;
            case R.id.action_login:
                Intent intent = new Intent(this, SignInSignUpActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_LOGIN);
                startActivityForResult(intent, Constants.GO_TO_HOME);
                return true;
            case R.id.action_register:
                intent = new Intent(this, SignInSignUpActivity.class);
                intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_REGISTER);
                startActivityForResult(intent, Constants.GO_TO_HOME);
                return true;
            case R.id.action_view_basket:
                addToMainLayout(new ShowCartFragment());
                return true;
            case R.id.action_active_orders:
                Intent orderListIntent = new Intent(this, OrderListActivity.class);
                orderListIntent.putExtra(Constants.ORDER, getString(R.string.active_label));
                startActivityForResult(orderListIntent, Constants.GO_TO_HOME);
                return true;
            case R.id.action_order_history:
                orderListIntent = new Intent(this, OrderListActivity.class);
                orderListIntent.putExtra(Constants.ORDER, getString(R.string.past_label));
                startActivityForResult(orderListIntent, Constants.GO_TO_HOME);
                return true;
            case R.id.action_update_profile:
                addToMainLayout(new UpdateProfileFragment());
                return true;
            case R.id.action_change_password:
                addToMainLayout(new ChangePasswordFragment());
                return true;
            case R.id.action_wallet_activity:
                addToMainLayout(new DoWalletFragment());
                return true;
            case R.id.action_delivery_address:
                MemberAddressListFragment memberAddressListFragment = new MemberAddressListFragment();
                Bundle addressbundle = new Bundle();
                addressbundle.putBoolean(Constants.FROM_ACCOUNT_PAGE, true);
                memberAddressListFragment.setArguments(addressbundle);
                addToMainLayout(memberAddressListFragment);
                return true;
            case R.id.action_change_pin:
                addToMainLayout(new UpdatePinFragment());
                return true;
            case R.id.action_logout:
                showAlertDialog(this, getString(R.string.signOut), getString(R.string.signoutConfirmation),
                        DialogButton.YES, DialogButton.NO, Constants.LOGOUT);
                return true;
            case R.id.action_change_city:
                addToMainLayout(new ChangeCityFragment());
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
                                                   ImageView imgDecQty, ImageView imgIncQty, Button btnAddToBasket,
                                                   EditText editTextQty, Product product, String qty) {
        if (basketOperationResponse.getErrorType().equals(Constants.PRODUCT_ID_NOT_FOUND)) {
            Toast.makeText(this, "0 added to basket.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void updateUIAfterBasketOperationSuccess(BasketOperation basketOperation, TextView basketCountTextView,
                                                    ImageView imgDecQty, ImageView imgIncQty, Button btnAddToBasket,
                                                    EditText editTextQty, Product product, String qty) {

        int productQtyInBasket = Integer.parseInt(basketOperationResponse.getTotalQuantity());
        int totalProductsInBasket = Integer.parseInt(basketOperationResponse.getNoOfItems());

        if (basketOperation == BasketOperation.ADD) {
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
            if (imgDecQty != null) {
                imgDecQty.setVisibility(View.GONE);
            }
            if (imgIncQty != null) {
                imgIncQty.setVisibility(View.GONE);
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
            if (imgDecQty != null) {
                imgDecQty.setVisibility(View.VISIBLE);
            }
            if (imgIncQty != null) {
                imgIncQty.setVisibility(View.VISIBLE);
            }
            if (btnAddToBasket != null) {
                btnAddToBasket.setVisibility(View.GONE);
            }
            if (editTextQty != null) {
                editTextQty.setVisibility(View.GONE);
            }
            if (basketCountTextView != null) {
                basketCountTextView.setText(productQtyInBasket + " in basket");
                basketCountTextView.setVisibility(View.VISIBLE);
            }
        }

        if (product != null) {
            product.setNoOfItemsInCart(productQtyInBasket);
        }

        SharedPreferences.Editor editor = getSharedPreferences("myCustomSharedPrefs", Activity.MODE_PRIVATE).edit();
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
        SharedPreferences.Editor editor = getSharedPreferences("myCustomSharedPrefs", Activity.MODE_PRIVATE).edit();
        editor.putString(Constants.GET_CART, "" + cartInfo.getNoOfItems());
        editor.commit();
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        handleIntent(intent, null);
    }

    private void handleIntent(Intent intent, Bundle savedInstanceState) {
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
                    launchCategoryProducts(query, slug);
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

    private void launchCategoryProducts(String categoryName, String categorySlug) {
        MostSearchesAdapter mostSearchesAdapter = new MostSearchesAdapter(this);
        mostSearchesAdapter.update(categoryName, categorySlug);
        Bundle bundle = new Bundle();
        bundle.putString("slug_name_category", categorySlug);
        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
        categoryProductsFragment.setArguments(bundle);
        addToMainLayout(categoryProductsFragment);
    }

    private void doSearch(String searchQuery) {
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
            actionBar.setTitle(StringUtils.abbreviate(mTitle.toString(), 25));
        }
    }

    private void hideGuestMenuItems(Menu menu) {
        MenuItem loginMenuItem = menu.findItem(R.id.action_login);
        loginMenuItem.setVisible(false);

        MenuItem registerMenuItem = menu.findItem(R.id.action_register);
        registerMenuItem.setVisible(false);

        MenuItem changeCityRegisterMenu = menu.findItem(R.id.action_change_city);
        changeCityRegisterMenu.setVisible(false);
    }

    private void hideMemberMenuItems(Menu menu) {
        MenuItem activeOrderMenuItem = menu.findItem(R.id.action_active_orders);
        MenuItem orderHistoryMenuItem = menu.findItem(R.id.action_order_history);
        MenuItem updateProfileMenuItem = menu.findItem(R.id.action_update_profile);
        MenuItem changePasswdMenuItem = menu.findItem(R.id.action_change_password);
        MenuItem walletActivityMenuItem = menu.findItem(R.id.action_wallet_activity);
        MenuItem deliveryAddressesMenuItem = menu.findItem(R.id.action_delivery_address);
        MenuItem changePinMenuItem = menu.findItem(R.id.action_change_pin);
        MenuItem logoutMenuItem = menu.findItem(R.id.action_logout);

        activeOrderMenuItem.setVisible(false);
        orderHistoryMenuItem.setVisible(false);
        updateProfileMenuItem.setVisible(false);
        changePasswdMenuItem.setVisible(false);
        walletActivityMenuItem.setVisible(false);
        deliveryAddressesMenuItem.setVisible(false);
        changePinMenuItem.setVisible(false);
        logoutMenuItem.setVisible(false);
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName)) {
            switch (sourceName) {
                case Constants.LOGOUT:
                    doLogout();
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

    public void refreshNavigation() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
        invalidateOptionsMenu();
        loadNavigationItems();
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

        ExpandableListView listNavigation = (ExpandableListView) findViewById(R.id.listNavigation);
        listNavigation.setGroupIndicator(null);
        listNavigation.setDivider(null);
        listNavigation.setDividerHeight(0);
        ArrayList<NavigationItem> navigationItems = getNavigationItems();
        NavigationListAdapter navigationListAdapter = new NavigationListAdapter(this,
                navigationItems, faceRobotoRegular);
        listNavigation.setAdapter(navigationListAdapter);
        listNavigation.setOnGroupClickListener(new NavigationListGroupClickListener(navigationItems));
        listNavigation.setOnChildClickListener(new NavigationListChildClickListener(navigationItems));
        navigationListAdapter.notifyDataSetChanged();
    }

    private ArrayList<NavigationItem> getNavigationItems() {
        ArrayList<NavigationItem> navigationItems = new ArrayList<>();
        boolean isLoggedIn = !AuthParameters.getInstance(this).isAuthTokenEmpty();

        if (!isLoggedIn) {
            navigationItems.add(new NavigationItem(getString(R.string.signIn),
                    R.drawable.main_nav_login_arrow, Constants.LOGIN, false));
            navigationItems.add(new NavigationItem(getString(R.string.registerHomepageTxt),
                    R.drawable.main_nav_person_add, Constants.REGISTER_MEMBER, false));
        }

        // Populate top-category list
        ArrayList<TopCategoryModel> topCategoryModels = getStoredTopCategories();
        if (topCategoryModels != null && topCategoryModels.size() > 0) {
            ArrayList<NavigationSubItem> browseByCatNavSubItem = new ArrayList<>();
            for (TopCategoryModel topCategoryModel : topCategoryModels) {
                browseByCatNavSubItem.add(new NavigationSubItem(topCategoryModel.getName(),
                        topCategoryModel.getImagePath(), Constants.BROWSE_CAT, false));
            }
            NavigationItem browseByTopCatNavigationItem = new NavigationItem(getString(R.string.browseByCats),
                    R.drawable.main_nav_category, Constants.BROWSE_CAT, true, browseByCatNavSubItem);
            navigationItems.add(browseByTopCatNavigationItem);
        }

        // Populate browse by offers
        ArrayList<NavigationSubItem> browseByOffersSubNavItems = new ArrayList<>();
        browseByOffersSubNavItems.add(new NavigationSubItem(getString(R.string.discount),
                -1, Constants.DISCOUNT_TYPE, false));
        browseByOffersSubNavItems.add(new NavigationSubItem(getString(R.string.promotions),
                -1, Constants.PROMO, false));
        NavigationItem browseByOffersNavItem = new NavigationItem(getString(R.string.browseByOffers),
                R.drawable.main_nav_discount, Constants.BROWSE_OFFERS, true, browseByOffersSubNavItems);
        navigationItems.add(browseByOffersNavItem);

        // Populate other items
        navigationItems.add(new NavigationItem(getString(R.string.shoppingList),
                R.drawable.main_nav_shopping_list, Constants.SHOP_LST, false));
        navigationItems.add(new NavigationItem(Constants.SMART_BASKET, R.drawable.main_nav_shopping_list,
                Constants.SMART_BASKET_SLUG, false));
        navigationItems.add(new NavigationItem(getString(R.string.viewBasket), R.drawable.main_nav_basket,
                Constants.CART, false));
        navigationItems.add(new NavigationItem(getString(R.string.bbCommHub), R.drawable.main_nav_account,
                Constants.FEEDBACK, false));
        if (isLoggedIn) {
            navigationItems.add(new NavigationItem(getString(R.string.myAccount), R.drawable.main_nav_account,
                    Constants.FROM_ACCOUNT_PAGE, false));
            navigationItems.add(new NavigationItem(getString(R.string.signOut), R.drawable.main_nav_logout,
                    Constants.LOGOUT, false));
        }
        return navigationItems;
    }

    public ArrayList<TopCategoryModel> getStoredTopCategories() {
        CategoryAdapter categoryAdapter = new CategoryAdapter(this);
        return categoryAdapter.getAllTopCategories();
    }

    private class NavigationListGroupClickListener implements ExpandableListView.OnGroupClickListener {

        private ArrayList<NavigationItem> navigationItems;

        private NavigationListGroupClickListener(ArrayList<NavigationItem> navigationItems) {
            this.navigationItems = navigationItems;
        }

        @Override
        public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
            NavigationItem navigationItem = navigationItems.get(groupPosition);
            if (!navigationItem.isExpandable()) {
                switch (navigationItem.getTag()) {
                    case Constants.SHOP_LST:
                        if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                            showAlertDialog(getCurrentActivity(), null,
                                    "Please sign-in to view your shopping lists", Constants.LOGIN_REQUIRED);
                        } else {
                            addToMainLayout(new ShoppingListFragment());
                        }
                        break;
                    case Constants.SMART_BASKET_SLUG:
                        if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                            showAlertDialog(getCurrentActivity(), null,
                                    "Please sign-in to view your smart basket", Constants.LOGIN_REQUIRED);
                        } else {
                            ShoppingListName shoppingListName = new ShoppingListName(Constants.SMART_BASKET,
                                    Constants.SMART_BASKET_SLUG, true);
                            ShoppingListSummaryFragment shoppingListSummaryFragment = new ShoppingListSummaryFragment();
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(Constants.SHOPPING_LIST_NAME, shoppingListName);
                            shoppingListSummaryFragment.setArguments(bundle);
                            onChangeFragment(shoppingListSummaryFragment);
                        }
                        break;
                    case Constants.CART:
                        onChangeFragment(new ShowCartFragment());
                        break;
                    case Constants.FROM_ACCOUNT_PAGE:
                        if (AuthParameters.getInstance(getCurrentActivity()).isAuthTokenEmpty()) {
                            showAlertDialog(getCurrentActivity(), null,
                                    "Please sign-in to view your accounts page", Constants.LOGIN_REQUIRED);
                        } else {
                            onChangeFragment(new AccountSettingFragment());
                        }
                        break;
                    case Constants.LOGIN:
                        Intent intent = new Intent(getCurrentActivity(), SignInSignUpActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_LOGIN);
                        startActivityForResult(intent, Constants.GO_TO_HOME);
                        break;
                    case Constants.REGISTER_MEMBER:
                        intent = new Intent(getCurrentActivity(), SignInSignUpActivity.class);
                        intent.putExtra(Constants.FRAGMENT_CODE, FragmentCodes.START_REGISTER);
                        startActivityForResult(intent, Constants.GO_TO_HOME);
                        break;
                    case Constants.FEEDBACK:
                        launchKonotor();
                        break;
                    case Constants.LOGOUT:
                        doLogout();
                        break;
                }
                return true;
            }
            return false;
        }
    }

    private class NavigationListChildClickListener implements ExpandableListView.OnChildClickListener {

        private ArrayList<NavigationItem> navigationItems;

        private NavigationListChildClickListener(ArrayList<NavigationItem> navigationItems) {
            this.navigationItems = navigationItems;
        }

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
            List<NavigationSubItem> navigationSubItems = navigationItems.get(groupPosition).getNavigationSubItems();
            if (navigationItems.get(groupPosition).getTag() != null &&
                    navigationItems.get(groupPosition).getTag().equals(Constants.BROWSE_CAT)) {
                SubCategoryListFragment subCategoryListFragment = new SubCategoryListFragment();
                Bundle subCatBundle = new Bundle();
                subCatBundle.putString(Constants.TOP_CATEGORY_SLUG,
                        navigationItems.get(groupPosition).getNavigationSubItems().get(childPosition).getTag());
                subCatBundle.putString(Constants.TOP_CATEGORY_NAME,
                        navigationItems.get(groupPosition).getNavigationSubItems().get(childPosition).getTag());
                subCategoryListFragment.setArguments(subCatBundle);
                addToMainLayout(subCategoryListFragment);

            }else if (navigationSubItems != null) {
                NavigationSubItem navigationSubItem = navigationSubItems.get(childPosition);
                if (navigationSubItem.getTag() != null) {
                    switch (navigationSubItem.getTag()) {
                        case Constants.DISCOUNT_TYPE:
                            addToMainLayout(new BrowseByOffersFragment());
                            return true;
                        case Constants.PROMO:
                            addToMainLayout(new PromoCategoryFragment());
                            return true;
                    }
                }
            }
            return false;
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
}