package com.bigbasket.mobileapp.fragment.product;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.SubCategoryAdapter;
import com.bigbasket.mobileapp.adapter.product.SubCategoryListAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.product.Category;
import com.bigbasket.mobileapp.model.product.SubCategoryModel;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.List;


public class SubCategoryListFragment extends BaseFragment {

    private SubCategoryModel subCategoryModel;
    private String topCatSlug;
    private String topCatName;
    private int imageCounter = 0;
    private int width, imageheight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        topCatSlug = getArguments().getString(Constants.TOP_CATEGORY_SLUG);
        topCatName = getArguments().getString(Constants.TOP_CATEGORY_NAME);


        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        width = metrics.widthPixels;
        imageheight = metrics.heightPixels * 18 / 100;
        loadSubCategoryList();
    }

    private void loadSubCategoryList() {
        if (checkInternetConnection()) {
            SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());
            String version = subCategoryAdapter.getVersion(topCatSlug);
            String url = MobileApiUrl.getBaseAPIUrl() + "category-landing/?category_slug=" +
                    topCatSlug;
            if (version != null) {
                url += "&version=" + version;
            }
            startAsyncActivity(url, null, false, true, null);
        } else {
            //alert box
            String msg = "Cannot proceed with the operation. No network connection.";
            showErrorMsg(msg);
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        super.onAsyncTaskComplete(httpOperationResult);
        String responseJson = httpOperationResult.getReponseString();
        int responseCode = httpOperationResult.getResponseCode();
        if (responseJson != null) {
            if (!httpOperationResult.isPost()) {
                try {
                    JsonObject jsonObject = new JsonParser().parse(responseJson).getAsJsonObject();
                    JsonObject responseJsonObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                    String responseVersion = responseJsonObject.get(Constants.VERSION).getAsString();
                    boolean response_ok = responseJsonObject.get(Constants.A_OK).getAsBoolean();
                    JsonObject categoriesJsonObject = responseJsonObject.get(Constants.CATEGORIES).getAsJsonObject();
                    if (!response_ok) {
                        JsonObject subCategoryJsonObject = categoriesJsonObject.get(Constants.SUB_CATEGORY_ITEMS).getAsJsonObject();
                        Gson gson = new Gson();
                        subCategoryModel = gson.fromJson(subCategoryJsonObject, SubCategoryModel.class);
                    }

                    renderSubCategory(responseVersion, response_ok, categoriesJsonObject, subCategoryModel);
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorMsg("Please try again later");
                }
            }
        } else {
            switch (responseCode) {

                case Constants.invalidInputRespCode:
                    String msgInvalidInput = "Input is Invalid";
                    showErrorMsg(msgInvalidInput);
                    break;

                case Constants.notMemberRespCode:
                    String msgInvalidUser = "The logged in user is not a member";
                    showErrorMsg(msgInvalidUser);
                    break;

                default:
                    String defaultMsg = "Server Error";
                    showErrorMsg(defaultMsg);
                    break;
            }
        }
    }

    private void renderSubCategory(String responseVersion, boolean response_ok, JsonObject categoriesJsonObject,
                                   SubCategoryModel subCategoryModel) {

        if (getActivity() == null) return;

        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());

        ArrayList<Object> result;
        String bannerUrl;
        if (!response_ok) {
            try {
                Gson gson = new Gson();
                bannerUrl = gson.toJson(categoriesJsonObject);
                subCategoryAdapter.insert(subCategoryModel, responseVersion, bannerUrl, topCatSlug);
                subCategoryAdapter.close();
            } catch (Exception e) {
                subCategoryAdapter.close();
                e.printStackTrace();
                showErrorMsg("Please try again later");
            } finally {
                subCategoryAdapter.close();
            }
        } else {
            try {
                result = subCategoryAdapter.getSubCategory(topCatSlug);
                subCategoryModel = (SubCategoryModel) result.get(0);
                bannerUrl = (String) result.get(1);
                categoriesJsonObject = new JsonParser().parse(bannerUrl).getAsJsonObject();
            } catch (SQLiteException e) {
                subCategoryAdapter.close();
                e.printStackTrace();
                showErrorMsg("Please try again later");

            } finally {
                subCategoryAdapter.close();
            }
        }

        try {
            // banner images
            renderBanner(categoriesJsonObject, contentView);

            final List<Category> categoryArrayList = new ArrayList<>();

            // subcat added by client
            Category newLaunchesCategory = new Category("New Launches", "launches");
            categoryArrayList.add(newLaunchesCategory);

            Category offersCategory = new Category("Offers", "offers");
            categoryArrayList.add(offersCategory);

            Category allCategories = new Category("All " + topCatName.trim(), topCatSlug);
            categoryArrayList.add(allCategories);

            for (int i = 0; i < subCategoryModel.getCategory().size(); i++) {
                Category subCat = subCategoryModel.getCategory().get(i);
                if(subCat.getCategory()!=null && subCat.getCategory().size()>0){
                    Category offersBottomCategory = new Category("Offers", "offerssub");
                    subCat.getCategory().add(0,offersBottomCategory);

                    Category allBottomCategory = new Category("All "+subCategoryModel.getCategory().get(i).getName(),
                            subCat.getSlug());
                    subCat.getCategory().add(1,allBottomCategory);
                }
                categoryArrayList.add(subCat);

            }

            final ExpandableListView subCategoryExpandableView = new ExpandableListView(getActivity());
            subCategoryExpandableView.setGroupIndicator(null);
            subCategoryExpandableView.setDivider(new ColorDrawable(getResources().getColor(R.color.strokeLine)));
            subCategoryExpandableView.setDividerHeight(1);
            subCategoryExpandableView.setLayoutParams(new
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));


            SubCategoryListAdapter subCategoryListAdapter = new SubCategoryListAdapter(this, categoryArrayList, getActivity());
            subCategoryExpandableView.setAdapter(subCategoryListAdapter);
            contentView.addView(subCategoryExpandableView);

            subCategoryExpandableView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    if (categoryArrayList.get(groupPosition).getCategory()!=null &&
                            categoryArrayList.get(groupPosition).getCategory().size()>0) {
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("slug_name_category", categoryArrayList.get(groupPosition).getSlug());
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                        return false;
                    }
                    return false;
                }
            });

            subCategoryExpandableView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    if (childPosition == 1) {
                        Bundle bundle = new Bundle();
                        bundle.putString("slug_name_category", categoryArrayList.get(groupPosition).getSlug());
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    } else {
                        Bundle bundle = new Bundle();
                        bundle.putString("slug_name_category", categoryArrayList.get(groupPosition).getCategory().get(childPosition).getSlug());
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                    return false;
                }
            });

        } catch (Exception e) {

            showErrorMsg("Please try again later");
            e.printStackTrace();
        }
    }

    private void renderBanner(JsonObject categoriesJsonObject, LinearLayout contentView){
        final ArrayList<String> bannerArrList = new ArrayList<>();
        if (categoriesJsonObject.has(Constants.SUB_CATEGORY_BANNER_IMAGE)) {
            JsonArray banner = categoriesJsonObject.getAsJsonArray(Constants.SUB_CATEGORY_BANNER_IMAGE);
            for (int im = 0; im < banner.size(); im++) {
                if (banner.get(im).getAsString().startsWith("//")) {
                    bannerArrList.add("http:" + banner.get(im).getAsString());
                } else {
                    bannerArrList.add(banner.get(im).getAsString());
                }
            }
            if (bannerArrList.size() > 0) {
                final LinearLayout childfirst1 = new LinearLayout(getActivity());
                final LinearLayout.LayoutParams childParams1 =
                        new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, imageheight);
                childfirst1.setLayoutParams(childParams1);
                childfirst1.setOrientation(LinearLayout.VERTICAL);
                contentView.addView(childfirst1);
                final ProgressBar parentprocessBar = new ProgressBar(getActivity(), null,
                        android.R.attr.progressBarStyleInverse);
                final LinearLayout.LayoutParams processBarParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                processBarParams.gravity = Gravity.CENTER;
                parentprocessBar.setLayoutParams(processBarParams);
                childfirst1.addView(parentprocessBar);

                final ImageView imageView = new ImageView(getActivity());
                if (bannerArrList.size() > 1) {
                    final Runnable runnable = new Runnable() {
                        @Override
                        public void run() {

                            final LinearLayout.LayoutParams processBarParams1 = new LinearLayout.LayoutParams(width, imageheight);
                            imageView.setLayoutParams(processBarParams1);
                            ImageLoader.getInstance().displayImage(bannerArrList.get(imageCounter), imageView);
                            imageCounter++;
                            if (imageCounter >= bannerArrList.size()) {
                                imageCounter = 0;
                            }
                            childfirst1.postDelayed(this, 5000);
                        }
                    };
                    childfirst1.postDelayed(runnable, 5000);
                    childfirst1.removeAllViews();
                    childfirst1.addView(imageView);
                } else if (bannerArrList.size() == 1) {
                    ImageLoader.getInstance().displayImage(bannerArrList.get(imageCounter), imageView);
                    childfirst1.removeAllViews();
                    childfirst1.addView(imageView);
                } else {
                    imageView.setBackgroundColor(0);
                    imageView.setBackgroundResource(R.drawable.noimage);
                }
            }
        }

    }

    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }

    @Override
    public String getTitle() {
        return "Browse by Category";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return SubCategoryListFragment.class.getName();
    }

}