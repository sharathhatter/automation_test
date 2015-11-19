package com.bigbasket.mobileapp.activity.shoppinglist;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.SearchActivity;
import com.bigbasket.mobileapp.adapter.TabPagerAdapterWithFragmentRegistration;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.AddAllShoppingListItemResponse;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.GetShoppingListSummaryResponse;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.fragment.shoppinglist.ShoppingListProductFragment;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.product.Product;
import com.bigbasket.mobileapp.model.section.Section;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListName;
import com.bigbasket.mobileapp.model.shoppinglist.ShoppingListSummary;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.InputDialog;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;
import com.bigbasket.mobileapp.util.UIUtil;
import com.bigbasket.mobileapp.view.uiv3.BBTab;
import com.bigbasket.mobileapp.view.uiv3.HeaderSpinnerView;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Call;

public class ShoppingListSummaryActivity extends SearchActivity {

    private String baseImgUrl;
    @Nullable
    private ShoppingListName mShoppingListName;
    @Nullable
    private ViewPager viewPager;
    private TextView mTxtToolbarDropdown;
    private int currentTabIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadShoppingListSummary();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        setSuspended(false);
        if (resultCode == NavigationCodes.SHOPPING_LIST_MODIFIED ||
                resultCode == NavigationCodes.BASKET_CHANGED) {
            loadShoppingListSummary();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    public int getMainLayout() {
        return R.layout.uiv3_shopping_list_summary_layout;
    }

    @Override
    public void onNoFragmentsInLayout() {
        finish();
    }

    @Override
    protected void setOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.shopping_list_summary_menu, menu);
        if (mShoppingListName != null && mShoppingListName.isSystem()) {
            menu.findItem(R.id.menuEditShoppingList).setVisible(false);
            menu.findItem(R.id.menuDeleteShoppingList).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuEditShoppingList) {
            showEditShoppingListDialog();
            return true;
        } else if (item.getItemId() == R.id.menuDeleteShoppingList) {
            showDeleteShoppingListDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void loadShoppingListSummary() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError(true);
            return;
        }
        if (mShoppingListName == null) {
            mShoppingListName = getIntent().getParcelableExtra(Constants.SHOPPING_LIST_NAME);
        }
        if (mShoppingListName == null) {
            return;
        }
        setTitle(null);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressDialog(getString(R.string.please_wait));
        Call<ApiResponse<GetShoppingListSummaryResponse>> call = bigBasketApiService.getShoppingListSummary(
                getCurrentNavigationContext(), mShoppingListName.getSlug());
        call.enqueue(new BBNetworkCallback<ApiResponse<GetShoppingListSummaryResponse>>(this, true) {
            @Override
            public void onSuccess(ApiResponse<GetShoppingListSummaryResponse> getShoppingListSummaryApiResponse) {
                switch (getShoppingListSummaryApiResponse.status) {
                    case 0:
                        if (mShoppingListName != null) {
                            mShoppingListName.setAsSystem(getShoppingListSummaryApiResponse.apiResponseContent.isSystem);
                        }
                        renderShoppingListSummary(mShoppingListName,
                                getShoppingListSummaryApiResponse.apiResponseContent.shoppingListSummaries,
                                getShoppingListSummaryApiResponse.apiResponseContent.baseImgUrl,
                                getShoppingListSummaryApiResponse.apiResponseContent.headerSection,
                                getShoppingListSummaryApiResponse.apiResponseContent.headerSelectedOn);
                        break;
                    default:
                        handler.sendEmptyMessage(getShoppingListSummaryApiResponse.status,
                                getShoppingListSummaryApiResponse.message, true);
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

    private void showNoShoppingListView(ViewGroup contentView) {
        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_empty_data_text, contentView, false);
        ImageView imgEmptyPage = (ImageView) base.findViewById(R.id.imgEmptyPage);
        imgEmptyPage.setImageResource(R.drawable.empty_smart_basket);

        TextView txtEmptyMsg1 = (TextView) base.findViewById(R.id.txtEmptyMsg1);
        txtEmptyMsg1.setText(R.string.noSmartBasketProducts);
        TextView txtEmptyMsg2 = (TextView) base.findViewById(R.id.txtEmptyMsg2);
        txtEmptyMsg2.setVisibility(View.GONE);
        Button btnBlankPage = (Button) base.findViewById(R.id.btnBlankPage);
        btnBlankPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getCurrentActivity() == null) return;
                getCurrentActivity().goToHome(false);
            }
        });
        contentView.addView(base);
    }

    private void renderHeaderDropDown(@Nullable final Section headSection,
                                      int headerSelectedOn) {
        Toolbar toolbar = getToolbar();
        if (mTxtToolbarDropdown == null) {
            mTxtToolbarDropdown = (TextView) getLayoutInflater().
                    inflate(R.layout.uiv3_product_header_text, toolbar, false);
        }
        new HeaderSpinnerView.HeaderSpinnerViewBuilder<>()
                .withCtx(this)
                .withDefaultSelectedIdx(headerSelectedOn)
                .withFallbackHeaderTitle(mShoppingListName != null ? mShoppingListName.getName() : "")
                .withHeadSection(headSection)
                .withImgCloseChildDropdown((ImageView) findViewById(R.id.imgCloseChildDropdown))
                .withLayoutChildToolbarContainer((ViewGroup) findViewById(R.id.layoutChildToolbarContainer))
                .withLayoutListHeader((ViewGroup) findViewById(R.id.layoutListHeader))
                .withListHeaderDropdown((ListView) findViewById(R.id.listHeaderDropdown))
                .withToolbar(getToolbar())
                .withTxtChildDropdownTitle((TextView) findViewById(R.id.txtListDialogTitle))
                .withTxtToolbarDropdown(mTxtToolbarDropdown)
                .withTypeface(faceRobotoRegular)
                .build()
                .setView();
    }

    private void renderShoppingListSummary(final ShoppingListName shoppingListName,
                                           final ArrayList<ShoppingListSummary> shoppingListSummaries,
                                           String baseImgUrl, @Nullable Section headerSection,
                                           int headerSelectedOn) {
        this.baseImgUrl = baseImgUrl;
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        LayoutInflater inflater = getLayoutInflater();
        renderHeaderDropDown(headerSection, headerSelectedOn);
        if (shoppingListSummaries == null || shoppingListSummaries.size() == 0) {
            if (shoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG)) {
                showNoShoppingListView(contentFrame);
            } else {
                RelativeLayout relativeLayout = (RelativeLayout) inflater.inflate(R.layout.shopping_list_empty, contentFrame, false);
                TextView txtShoppingListMsg1 = (TextView) relativeLayout.findViewById(R.id.txtShoppingListMsg1);
                txtShoppingListMsg1.setTypeface(faceRobotoRegular);
                TextView txtShoppingListMsg2 = (TextView) relativeLayout.findViewById(R.id.txtShoppingListMsg2);
                txtShoppingListMsg2.setTypeface(faceRobotoRegular);
                TextView txtSearchProducts = (TextView) relativeLayout.findViewById(R.id.txtSearchProducts);
                txtSearchProducts.setTypeface(faceRobotoRegular);
                TextView txtSearchResultMsg1 = (TextView) relativeLayout.findViewById(R.id.txtSearchResultMsg1);
                txtSearchResultMsg1.setTypeface(faceRobotoRegular);
                TextView txtSearchResultMsg2 = (TextView) relativeLayout.findViewById(R.id.txtSearchResultMsg2);
                txtSearchResultMsg2.setTypeface(faceRobotoRegular);
                contentFrame.addView(relativeLayout);
            }
            findViewById(R.id.slidingTabs).setVisibility(View.GONE);
            findViewById(R.id.layoutAddAll).setVisibility(View.GONE);
            return;
        }

        final int numTabs = shoppingListSummaries.size();

        final String nc = getNc();
        setNextScreenNavigationContext(nc);


        final View layoutAddAll = findViewById(R.id.layoutAddAll);
        if (numTabs == 1) {
            findViewById(R.id.slidingTabs).setVisibility(View.GONE);
            Bundle bundle = getBundleForShoppingListProductFragment(shoppingListSummaries.get(0),
                    shoppingListName, baseImgUrl);
            ShoppingListProductFragment shoppingListProductFragment = new ShoppingListProductFragment();
            shoppingListProductFragment.setArguments(bundle);
            replaceToMainLayout(shoppingListProductFragment, ShoppingListProductFragment.class.getName(), false,
                    contentFrame);
        } else {
            findViewById(R.id.slidingTabs).setVisibility(View.VISIBLE);

            viewPager = (ViewPager) getLayoutInflater().inflate(R.layout.uiv3_viewpager, contentFrame, false);
            TabPagerAdapterWithFragmentRegistration tabPagerAdapterWithFragmentRegistration = new TabPagerAdapterWithFragmentRegistration(getCurrentActivity(),
                    getSupportFragmentManager(), getTabs(shoppingListSummaries, shoppingListName, baseImgUrl));
            if (viewPager != null) {
                int oldCurrentTabIndex = currentTabIndex;
                viewPager.setAdapter(tabPagerAdapterWithFragmentRegistration);
                viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    }

                    @Override
                    public void onPageSelected(int position) {
                        currentTabIndex = position;
                        ShoppingListSummary shoppingListSummary = shoppingListSummaries.get(position);
                        if (Product.areAllProductsOutOfStock(shoppingListSummary.getProducts()) ||
                                shoppingListName.isSystem() && !shoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG)) {
                            layoutAddAll.setVisibility(View.GONE);
                        } else {
                            layoutAddAll.setVisibility(View.VISIBLE);
                        }
                        setNextScreenNavigationContext(nc + "." + shoppingListSummaries.get(position).getFacetSlug());
                        HashMap<String, String> eventAttribs = new HashMap<>();
                        eventAttribs.put(Constants.TAB_NAME, shoppingListSummaries.get(position).getFacetSlug());
                        String tabName = !TextUtils.isEmpty(nc) && nc.equals(TrackEventkeys.SB)
                                ? "SmartBasket" : "ShoppingList";
                        eventAttribs.put(TrackEventkeys.NAVIGATION_CTX, getNextScreenNavigationContext());
                        trackEvent(tabName + "." + TrackingAware.TAB_CHANGED, eventAttribs);
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                    }
                });

                TabLayout pagerSlidingTabStrip = (TabLayout) findViewById(R.id.slidingTabs);
                pagerSlidingTabStrip.setupWithViewPager(viewPager);

                contentFrame.addView(viewPager);
                viewPager.setCurrentItem(oldCurrentTabIndex);
            }
        }

        layoutAddAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentItemIdx = viewPager != null ? viewPager.getCurrentItem() : 0;
                if (currentItemIdx >= shoppingListSummaries.size()) return;
                ShoppingListSummary shoppingListSummary = shoppingListSummaries.get(currentItemIdx);
                if (Product.areAllProductsOutOfStock(shoppingListSummary.getProducts())) {
                    String msg = numTabs > 1 ? getString(R.string.allAreOutOfStockForTabPrefix) + " "
                            + shoppingListSummary.getFacetName() + " " + getString(R.string.allAreOutOfStockForTabSuffix)
                            : getString(R.string.allAreOutOfStockForSingleTab);
                    showAlertDialog(null, msg);
                } else {
                    String msg = getString(R.string.addAllProducts);
                    msg += numTabs > 1 ? " from " + shoppingListSummary.getFacetName()
                            + " " + getString(R.string.toBasket) : "?";
                    msg += "\n" + getString(R.string.outOfStockExcluded);
                    showAlertDialog(null, msg,
                            DialogButton.YES, DialogButton.CANCEL, Constants.ADD_ALL,
                            shoppingListSummary,
                            getString(R.string.yesTxt));
                }
            }
        });


        ShoppingListSummary shoppingListSummary = shoppingListSummaries.get(0);
        logShoppingListingEvent(shoppingListSummary);
        if (Product.areAllProductsOutOfStock(shoppingListSummary.getProducts()) || (shoppingListName.isSystem() &&
                !shoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG))) {
            layoutAddAll.setVisibility(View.GONE);
        } else {
            layoutAddAll.setVisibility(View.VISIBLE);
            ((TextView) findViewById(R.id.txtAddAll)).setTypeface(faceRobotoRegular);
        }
    }

    private void logShoppingListingEvent(ShoppingListSummary shoppingListSummary) {
        ShoppingListName shoppingListName = shoppingListSummary.getShoppingListName();
        HashMap<String, String> eventAttribs = new HashMap<>();
        eventAttribs.put(Constants.TAB_NAME, shoppingListSummary.getFacetSlug());
        if (shoppingListName != null) {
            String nc = shoppingListName.getNc();
            nc += "." + shoppingListSummary.getFacetSlug();
            setNextScreenNavigationContext(nc);
            eventAttribs.put(Constants.NAME, shoppingListName.getSlug());
            if (!shoppingListName.getSlug().equals(Constants.SMART_BASKET_SLUG)) {
                trackEvent(TrackingAware.SHOP_LST_SUMMARY_SHOWN, eventAttribs);
            } else {
                trackEvent(TrackingAware.SMART_BASKET_SUMMARY_SHOWN, eventAttribs);
            }
        }
    }

    private ArrayList<BBTab> getTabs(ArrayList<ShoppingListSummary> shoppingListSummaries,
                                     ShoppingListName shoppingListName, String baseImgUrl) {
        ArrayList<BBTab> bbTabs = new ArrayList<>();
        for (ShoppingListSummary shoppingListSummary : shoppingListSummaries) {
            if (shoppingListSummary.getNumProducts() <= 0) {
                continue;
            }
            Bundle bundle = getBundleForShoppingListProductFragment(shoppingListSummary, shoppingListName, baseImgUrl);
            bbTabs.add(new BBTab<>(shoppingListSummary.getTitle(), ShoppingListProductFragment.class, bundle));
        }
        return bbTabs;
    }

    private Bundle getBundleForShoppingListProductFragment(ShoppingListSummary shoppingListSummary,
                                                           ShoppingListName shoppingListName,
                                                           String baseImgUrl) {
        Bundle bundle = new Bundle();
        shoppingListSummary.setShoppingListName(shoppingListName);
        bundle.putParcelable(Constants.SHOPPING_LIST_SUMMARY, shoppingListSummary);
        bundle.putString(Constants.BASE_IMG_URL, baseImgUrl);
        bundle.putString(Constants.TAB_NAME, shoppingListSummary.getFacetSlug());
        return bundle;
    }


    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, String sourceName, Object valuePassed) {
        if (!TextUtils.isEmpty(sourceName) && sourceName.equalsIgnoreCase(Constants.ADD_ALL)) {
            if (!checkInternetConnection()) {
                handler.sendOfflineError();
                return;
            }
            addAllItemsToBasket((ShoppingListSummary) valuePassed);
        } else {
            super.onPositiveButtonClicked(dialogInterface, sourceName, valuePassed);
        }
    }

    @Nullable
    private Fragment getCurrentFragment() {
        if (viewPager == null) return null;
        int currentPosition = viewPager.getCurrentItem();
        return ((TabPagerAdapterWithFragmentRegistration) viewPager.getAdapter()).getRegisteredFragment(currentPosition);
    }

    private void setProductCount(HashMap<String, Integer> cartInfo,
                                 ShoppingListSummary shoppingListSummary) {
        if (cartInfo != null && cartInfo.size() > 0) {
            Fragment fragment = getCurrentFragment();
            if (fragment != null) {
                ((ShoppingListProductFragment) fragment).notifyDataChanged(cartInfo,
                        shoppingListSummary, baseImgUrl, shoppingListSummary.getFacetName());
            }
        }
    }

    private void addAllItemsToBasket(final ShoppingListSummary shoppingListSummary) {
        if (mShoppingListName == null) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        showProgressView();
        String shoppingListSlug = mShoppingListName.getSlug();
        if (shoppingListSlug.equals(Constants.SMART_BASKET_SLUG)) {
            trackEvent(TrackingAware.SMART_BASKET + "." + shoppingListSummary.getFacetName() + " Add All", null);
            Call<AddAllShoppingListItemResponse> call = bigBasketApiService.addAllToBasketSmartBasket(
                    shoppingListSlug, shoppingListSummary.getFacetSlug());
            call.enqueue(new BBNetworkCallback<AddAllShoppingListItemResponse>(this) {
                @Override
                public void onSuccess(AddAllShoppingListItemResponse addAllToBasketSmartBasketCallBack) {
                    switch (addAllToBasketSmartBasketCallBack.status) {
                        case Constants.OK:
                            setCartSummary(addAllToBasketSmartBasketCallBack);
                            updateUIForCartInfo();
                            markBasketDirty();
                            if (viewPager != null) {
                                setProductCount(addAllToBasketSmartBasketCallBack.cartInfo,
                                        shoppingListSummary);
                            } else {
                                loadShoppingListSummary();
                            }

                            break;
                        case Constants.ERROR:
                            handler.sendEmptyMessage(addAllToBasketSmartBasketCallBack.getErrorTypeAsInt(),
                                    addAllToBasketSmartBasketCallBack.message, false);
                            break;
                    }
                }

                @Override
                public boolean updateProgress() {
                    hideProgressView();
                    return true;
                }
            });

        } else {
            trackEvent(TrackingAware.SHOPPING_LIST + "." + shoppingListSummary.getFacetName() + " Add All", null);
            Call<AddAllShoppingListItemResponse> call = bigBasketApiService.addAllToBasketShoppingList(
                    shoppingListSlug, shoppingListSummary.getFacetSlug());
            call.enqueue(new BBNetworkCallback<AddAllShoppingListItemResponse>(this) {
                @Override
                public void onSuccess(AddAllShoppingListItemResponse addAllToBasketShoppingListCallBack) {
                    switch (addAllToBasketShoppingListCallBack.status) {
                        case Constants.OK:
                            setCartSummary(addAllToBasketShoppingListCallBack);
                            updateUIForCartInfo();
                            markBasketDirty();
                            if (viewPager != null) {
                                setProductCount(addAllToBasketShoppingListCallBack.cartInfo,
                                        shoppingListSummary);
                            } else {
                                loadShoppingListSummary();
                            }
                            break;
                        case Constants.ERROR:
                            handler.sendEmptyMessage(addAllToBasketShoppingListCallBack.getErrorTypeAsInt(),
                                    addAllToBasketShoppingListCallBack.message, false);
                            break;
                    }
                }

                @Override
                public boolean updateProgress() {
                    hideProgressView();
                    return true;
                }
            });
        }
    }

    private void showEditShoppingListDialog() {
        if (mShoppingListName == null) return;
        if (mShoppingListName.isSystem()) {
            if (getCurrentActivity() != null) {
                getCurrentActivity().showAlertDialog(null, getString(R.string.isSystemShoppingListMsg));
            }
            return;
        }
        new InputDialog<ShoppingListSummaryActivity>(this, R.string.change, R.string.cancel,
                R.string.renameShoppingList, R.string.shoppingListNameDialogTextHint,
                mShoppingListName.getName()) {
            @Override
            public void onPositiveButtonClicked(String inputText) {
                if (getCurrentActivity() == null) return;
                if (!UIUtil.isAlphaNumericString(inputText.trim())) {
                    showAlertDialog(getResources().getString(R.string.shoppingListNameAlphaNumeric));
                } else if (mShoppingListName != null && mShoppingListName.getName().equalsIgnoreCase(inputText.trim()))
                    showAlertDialog("Shopping List with name \"" + inputText.trim() + "\" already exits");
                else {
                    editShoppingListName(mShoppingListName, inputText);
                }
            }
        }.show();
    }

    private void showDeleteShoppingListDialog() {
        if (mShoppingListName == null) return;
        if (mShoppingListName.isSystem()) {
            if (getCurrentActivity() != null) {
                getCurrentActivity().showAlertDialog(null, getString(R.string.isSystemShoppingListMsg));
            }
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getCurrentActivity())
                .setTitle(R.string.deleteQuestion)
                .setMessage(R.string.deleteShoppingListText)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteShoppingList(mShoppingListName);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog alertDialog = builder.create();
        if (isSuspended())
            return;
        alertDialog.setOnShowListener(new OnDialogShowListener());
        alertDialog.show();
    }

    private void editShoppingListName(ShoppingListName shoppingListName, String newName) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<OldBaseApiResponse> call = bigBasketApiService.editShoppingList(shoppingListName.getSlug(), newName);
        call.enqueue(new BBNetworkCallback<OldBaseApiResponse>(this) {
            @Override
            public void onSuccess(OldBaseApiResponse oldBaseApiResponse) {
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        Toast.makeText(getCurrentActivity(), getString(R.string.shoppingListUpdated),
                                Toast.LENGTH_LONG).show();
                        trackEvent(TrackingAware.SHOP_LST_NAME_CHANGED, null);
                        notifyListChanged();
                        break;
                    default:
                        handler.sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                                oldBaseApiResponse.message);
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

    private void deleteShoppingList(final ShoppingListName shoppingListName) {
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        Call<OldBaseApiResponse> call = bigBasketApiService.deleteShoppingList(shoppingListName.getSlug());
        call.enqueue(new BBNetworkCallback<OldBaseApiResponse>(this) {
            @Override
            public void onSuccess(OldBaseApiResponse oldBaseApiResponse) {
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        String msg = "\"" + shoppingListName.getName() + "\" was deleted successfully";
                        Toast.makeText(getCurrentActivity(), msg, Toast.LENGTH_LONG).show();
                        trackEvent(TrackingAware.SHOP_LST_DELETED, null);
                        notifyListChanged();
                        break;
                    default:
                        handler.sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(),
                                oldBaseApiResponse.message);
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

    private void notifyListChanged() {
        setResult(NavigationCodes.SHOPPING_LIST_CHANGED);
        finish();
    }

    @Override
    public void onBasketChanged(Intent data) {
        super.onBasketChanged(data);
        handleIntent(getIntent());
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.SHOPPING_LIST_CATEGORY_LISTING_SCREEN;
    }

    @Nullable
    private String getNc() {
        if (mShoppingListName == null) return null;
        return mShoppingListName.getNc();
    }

    @Override
    public void launchShoppingList(ShoppingListName shoppingListName) {
        mShoppingListName = shoppingListName;
        loadShoppingListSummary();
    }

    @Override
    protected String getCategoryId() {
        return getString(R.string.my_basket_header);
    }
}
