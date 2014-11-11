package com.bigbasket.mobileapp.fragment.product;

import android.app.Activity;
import android.database.sqlite.SQLiteException;
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


public class SubCategoryListFragment extends BaseFragment {

    private TopCategoryModel topCategoryModel;
    private SubCategoryModel subCategoryModel;
    private int imageCounter = 0;
    int screenWidth, imagewidth, height, layoutheight, width, imageheight, btmCategoryLength;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        topCategoryModel = getArguments().getParcelable(Constants.TOP_CATEGORY);
        final DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        width = metrics.widthPixels;
        imagewidth = metrics.widthPixels * 25 / 100;
        height = metrics.heightPixels * 30 / 100;
        layoutheight = metrics.heightPixels * 8 / 100;
        imageheight = metrics.heightPixels * 18 / 100;
        switch (metrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                screenWidth *= 0.75;
                imagewidth *= 0.75;
                height *= 0.75;
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                break;
            case DisplayMetrics.DENSITY_HIGH:
                screenWidth *= 1.5;
                imagewidth *= 1.5;
                height *= 1.5;
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                screenWidth *= 2;
                imagewidth *= 2;
                height *= 2;
                break;
            default:
                break;
        }
        loadSubCategoryList();
    }

    private void loadSubCategoryList() {
        if (checkInternetConnection()) {
            SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());
            String version = subCategoryAdapter.getVersion(topCategoryModel.getSlug());
            String url = MobileApiUrl.getBaseAPIUrl() + "category-landing/?category_slug=" +
                    topCategoryModel.getSlug();
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

        LayoutInflater inflater = getActivity().getLayoutInflater();
        SubCategoryAdapter subCategoryAdapter = new SubCategoryAdapter(getActivity());

        ArrayList<Object> result;
        String bannerUrl;
        if (!response_ok) {
            try {
                Gson gson = new Gson();
                bannerUrl = gson.toJson(categoriesJsonObject);
                subCategoryAdapter.insert(subCategoryModel, responseVersion, bannerUrl, topCategoryModel.getSlug());
                subCategoryAdapter.close();
            } catch (Exception e) {
                subCategoryAdapter.close();
                e.printStackTrace();
                showErrorMsg("Please try again later");
            } finally {
                if (subCategoryAdapter != null)
                    subCategoryAdapter.close();
            }
        } else {
            try {
                result = subCategoryAdapter.getSubCategory(topCategoryModel.getSlug());
                subCategoryModel = (SubCategoryModel) result.get(0);
                bannerUrl = (String) result.get(1);
                categoriesJsonObject = new JsonParser().parse(bannerUrl).getAsJsonObject();
            } catch (SQLiteException e) {
                subCategoryAdapter.close();
                e.printStackTrace();
                showErrorMsg("Please try again later");

            } finally {
                if (subCategoryAdapter != null)
                    subCategoryAdapter.close();
            }
        }

        try {
            final ArrayList<String> parentList = new ArrayList<>();
            final ArrayList<String> parentItemCount = new ArrayList<>();
            final ArrayList<String> parentSlug = new ArrayList<>();
            final ArrayList<Object> childList = new ArrayList<>();
            final ArrayList<Object> childListSgn = new ArrayList<>();
            ArrayList<String> child;
            ArrayList<String> childSgn;
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
                if (bannerArrList != null && bannerArrList.size() > 0) {
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

            parentList.add("New Launches");
            parentItemCount.add("extra");
            parentSlug.add("launches");
            child = new ArrayList<>();
            childSgn = new ArrayList<>();
            childListSgn.add(childSgn);
            childList.add(child);

            parentList.add("Offers");
            parentItemCount.add("extra");
            parentSlug.add("offers");
            child = new ArrayList<>();
            childSgn = new ArrayList<>();
            childListSgn.add(childSgn);
            childList.add(child);


            parentList.add("All " + topCategoryModel.getName().trim());  // need to change here
            parentItemCount.add("extra");
            parentSlug.add(topCategoryModel.getSlug());
            child = new ArrayList<>();
            childSgn = new ArrayList<>();
            childListSgn.add(childSgn);
            childList.add(child);

            for (int i = 0; i < subCategoryModel.getCategory().size(); i++) {
                Category subCat = subCategoryModel.getCategory().get(i);
                if (subCat.getName() != null) {
                    parentList.add(subCat.getName());
                }
                if (subCat.getSlug() != null) {
                    parentSlug.add(subCat.getSlug());
                }
                if (subCat.getNumberItems() != 0) {
                    parentItemCount.add(Integer.toString(subCat.getNumberItems()));
                } else {
                    parentItemCount.add("0");
                }
                child = new ArrayList<>();
                childSgn = new ArrayList<>();
                if (subCat.getCategory() != null) {
                    child.add("Offers");
                    childSgn.add("offerssub");
                    child.add("All " + subCategoryModel.getCategory().get(i).getName());
                    childSgn.add(subCat.getSlug());

                    for (int j = 0; j < subCat.getCategory().size(); j++) {
                        Category buttomCat = subCat.getCategory().get(j);
                        if (buttomCat.getSlug() != null) {
                            childSgn.add(buttomCat.getSlug());
                        }
                        if (buttomCat.getName() != null) {
                            child.add(buttomCat.getName());
                        }
                        if (subCat.getCategory().size() > btmCategoryLength)
                            btmCategoryLength = subCat.getCategory().size();
                    }
                    childListSgn.add(childSgn);
                    childList.add(child);
                } else {
                    child = new ArrayList<>();
                    childSgn = new ArrayList<>();
                    childListSgn.add(childSgn);
                    childList.add(child);
                }
            }
            final ExpandableListView expandableListView = new ExpandableListView(getActivity());
//            int numCategories = parentList.size();
//            int extraHeight = 0;
//            if (numCategories > 6) { // Listview is not showing more than 6 right now
//                extraHeight = Math.abs(70 * (numCategories - 6));
//                extraHeight = scaleToScreenIndependentPixel(extraHeight + 100);    //100
//            }
//            extraHeight += scaleToScreenIndependentPixel(btmCategoryLength * 30);
            expandableListView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));//getParentHeight() + extraHeight
            expandableListView.setScrollContainer(true);
            expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {

                    for (int i = 0; i < parentList.size(); i++) {
                        if (i != groupPosition) {
                            expandableListView.collapseGroup(i);
                        }
                    }
                }
            });
            expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {


                }
            });


            final View view1 = new View(getActivity());
            view1.setBackgroundColor(getResources().getColor(R.color.strokeLine));
            view1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1));
            contentView.addView(view1);


            expandableListView.setGroupIndicator(null);
            expandableListView.setDividerHeight(1);
            expandableListView.setDivider(getResources().getDrawable(R.drawable.expandapledivider));
            expandableListView.setClickable(true);
            NewAdapter mNewAdapter = new NewAdapter(parentList, childList, parentItemCount, childListSgn, parentSlug);
            mNewAdapter.setInflater(inflater, getActivity());
            expandableListView.setAdapter(mNewAdapter);
            contentView.addView(expandableListView);


            final View view = new View(getActivity());
            view.setBackgroundColor(0xFFCECECE);
            view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) 1));
            contentView.addView(view);
        } catch (Exception e) {

            showErrorMsg("Please try again later");
            e.printStackTrace();
        }
    }


    class NewAdapter extends BaseExpandableListAdapter {

        public ArrayList<String> groupItem, tempChild, iteamcount, tempsg, parentslug1;
        public ArrayList<Object> ChildItem = new ArrayList<>();
        public ArrayList<Object> childListsgn = new ArrayList<>();
        public LayoutInflater minflater;
        public Activity activity;

        public NewAdapter(ArrayList<String> grList, ArrayList<Object> childItem, ArrayList<String> parentiteamcount,
                          ArrayList<Object> childListsgn, ArrayList<String> parentslug) {
            groupItem = grList;
            this.ChildItem = childItem;
            iteamcount = parentiteamcount;
            this.childListsgn = childListsgn;
            parentslug1 = parentslug;
        }

        public void setInflater(LayoutInflater mInflater, Activity act) {
            this.minflater = mInflater;
            activity = act;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;        //                 ChildItem.get(childPosition)
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;           //     childPosition
        }


        @SuppressWarnings("unchecked")
        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View v;
            tempChild = (ArrayList<String>) ChildItem.get(groupPosition);
            tempsg = (ArrayList<String>) childListsgn.get(groupPosition);
            TextView text = null;
            convertView = minflater.inflate(R.layout.expandchild, null);
            text = (TextView) convertView.findViewById(R.id.shoptxt);
            text.setTypeface(faceRobotoRegular, 1);

            if (childPosition == 0) {
                text.setTextColor(getResources().getColor(R.color.active_order_red_color));
                text.setText(tempChild.get(childPosition));
//                TextView count = (TextView) convertView.findViewById(R.id.groupNos);
//                count.setTypeface(faceRobotoSlabNrml);
//                count.setVisibility(View.INVISIBLE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //Intent for calling activity
                        Bundle bundle = new Bundle();
                        bundle.putString("parentVal", topCategoryModel.getSlug());
                        bundle.putString("name", topCategoryModel.getName());
                        bundle.putString("brow", tempChild.get(childPosition));
                        bundle.putString("parentslug", topCategoryModel.getSlug());
                        bundle.putString("type", "offerssub");
                        bundle.putString("pcslug", groupItem.get(groupPosition));
                        bundle.putString("slug_name_category", parentslug1.get(groupPosition));
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                });

            } else if (childPosition == 1) {
                text.setTextColor(getResources().getColor(R.color.active_order_red_color));
                text.setText(tempChild.get(childPosition));
//                TextView count = (TextView) convertView.findViewById(R.id.groupNos);
//                count.setTypeface(faceRobotoSlabNrml);
//                count.setVisibility(View.INVISIBLE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("parentVal", topCategoryModel.getSlug());
                        bundle.putString("name", topCategoryModel.getName());
                        bundle.putString("brow", tempChild.get(childPosition));
                        bundle.putString("parentslug", topCategoryModel.getSlug());
                        bundle.putString("type", "allproductssub");
                        bundle.putString("pcslug", groupItem.get(groupPosition));
                        bundle.putString("slug_name_category", parentslug1.get(groupPosition));
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                });


            } else {
                text.setText(tempChild.get(childPosition));
//                TextView count = (TextView) convertView.findViewById(R.id.groupNos);
//                count.setTypeface(faceRobotoSlabNrml);
//                count.setVisibility(View.INVISIBLE);
//                count.setBackgroundResource(R.drawable.count);
                convertView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("parentVal", topCategoryModel.getSlug());
                        bundle.putString("name", topCategoryModel.getName());
                        bundle.putString("brow", tempChild.get(childPosition));
                        bundle.putString("parentslug", topCategoryModel.getSlug());
                        bundle.putString("type", "");
                        bundle.putString("pcslug", groupItem.get(groupPosition));
                        bundle.putString("slug_name_category", tempsg.get(groupPosition));
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                });

            }

            if (childPosition == 0) {
                text.setText(tempChild.get(childPosition));
                text.setTextColor(getResources().getColor(R.color.active_order_red_color));
            } else {
                text.setText(tempChild.get(childPosition));
            }
            return convertView;
        }

        @SuppressWarnings("unchecked")
        @Override
        public int getChildrenCount(int groupPosition) {
            return ((ArrayList<String>) ChildItem.get(groupPosition)).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groupItem.get(groupPosition); //null;
        }

        @Override
        public int getGroupCount() {
            return groupItem.size();
        }

        @Override
        public void onGroupCollapsed(int groupPosition) {
            super.onGroupCollapsed(groupPosition);
        }

        @Override
        public void onGroupExpanded(int groupPosition) {
            super.onGroupExpanded(groupPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;           //            groupPosition
        }

        @Override
        public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            convertView = minflater.inflate(R.layout.expandparent, null);

            LinearLayout parentLayout = (LinearLayout) convertView.findViewById(R.id.layout);
            CheckedTextView arrow = (CheckedTextView) convertView.findViewById(R.id.arrow);
            TextView titleView = (TextView) convertView.findViewById(R.id.shoptxt);
            titleView.setText(groupItem.get(groupPosition));
            titleView.setTypeface(faceRobotoRegular, 1);

            TextView countView = (TextView) convertView.findViewById(R.id.groupNos);
            countView.setVisibility(View.INVISIBLE);
            if (groupPosition == 0) {
                arrow.setVisibility(View.INVISIBLE);
                titleView.setTextColor(getResources().getColor(R.color.active_order_red_color));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("parentVal", topCategoryModel.getSlug());
                        bundle.putString("name", topCategoryModel.getName());
                        bundle.putString("parentslug", topCategoryModel.getSlug());
                        bundle.putString("type", "launches");
                        bundle.putString("pcslug", groupItem.get(groupPosition));
                        bundle.putString("slug_name_category", parentslug1.get(groupPosition));
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                });
            } else if (groupPosition == 1) {
                arrow.setVisibility(View.INVISIBLE);
                titleView.setTextColor(getResources().getColor(R.color.active_order_red_color));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("parentVal", topCategoryModel.getSlug());
                        bundle.putString("name", topCategoryModel.getName());
                        bundle.putString("parentslug", topCategoryModel.getSlug());
                        bundle.putString("type", "offers");
                        bundle.putString("pcslug", groupItem.get(groupPosition));
                        bundle.putString("slug_name_category", parentslug1.get(groupPosition));
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                });
            } else if (groupPosition == 2) {
                arrow.setVisibility(View.INVISIBLE);
                titleView.setTextColor(getResources().getColor(R.color.active_order_red_color));
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Intent for calling activity
//                        Intent i1 = new Intent(SubCategoryActivity.this, CategoryProductsActivity.class);
//                        i1.putExtra("parentVal", parentCatName);
//                        i1.putExtra("parentslug", sub_name);
//                        i1.putExtra("pcslug", sub_name);
//                        i1.putExtra("type", "allproducts");
//                        i1.putExtra("name", titleintent);
//                        i1.putExtra("slug_name_category", parentslug1.get(groupPosition));
//                        startActivityForResult(i1, Constants.GO_TO_HOME);
//                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                        Bundle bundle = new Bundle();
                        bundle.putString("parentVal", topCategoryModel.getSlug());
                        bundle.putString("name", topCategoryModel.getName());
                        bundle.putString("parentslug", topCategoryModel.getSlug());
                        bundle.putString("type", "allproducts");
                        bundle.putString("pcslug", groupItem.get(groupPosition));
                        bundle.putString("slug_name_category", parentslug1.get(groupPosition));
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                });
            } else {
                arrow.setChecked(isExpanded);
            }

            if (iteamcount.get(groupPosition).equalsIgnoreCase("0")) {
                arrow.setVisibility(View.INVISIBLE);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle bundle = new Bundle();
                        bundle.putString("parentVal", topCategoryModel.getSlug());
                        bundle.putString("name", topCategoryModel.getName());
                        bundle.putString("parentslug", topCategoryModel.getSlug());
                        bundle.putString("type", "allproductssub");
                        bundle.putString("pcslug", groupItem.get(groupPosition));
                        bundle.putString("slug_name_category", parentslug1.get(groupPosition));
                        CategoryProductsFragment categoryProductsFragment = new CategoryProductsFragment();
                        categoryProductsFragment.setArguments(bundle);
                        changeFragment(categoryProductsFragment);
                    }
                });
            }

            return convertView;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
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