package com.bigbasket.mobileapp.fragment.product;

import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.adapter.product.CategoryAdapter;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.product.TopCategoryModel;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;


public class TopCategoryListFragment extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        showTopCategoryList();
    }

    private void showTopCategoryList() {
        if (checkInternetConnection()) {
            CategoryAdapter catAdapter = new CategoryAdapter(getActivity());
            String version = catAdapter.getCategoriesVersion();
            String url = MobileApiUrl.getBaseAPIUrl() + "browse-category/";
            if (!TextUtils.isEmpty(version)) {
                url += "?version=" + version;
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
        super.onAsyncTaskComplete(httpOperationResult);
        String responseJson = httpOperationResult.getReponseString();
        int responseCode = httpOperationResult.getResponseCode();
        if (responseJson != null) {
            if (!httpOperationResult.isPost()) {
                try {
                    JsonObject jsonObject = new JsonParser().parse(responseJson).getAsJsonObject();
                    JsonObject responseJsonObject = jsonObject.get("response").getAsJsonObject();
                    String responseVersion = responseJsonObject.get("version").getAsString();
                    boolean response_ok = responseJsonObject.get("a_ok").getAsBoolean();
                    JsonArray categoriesJsonObject = responseJsonObject.get("categories").getAsJsonArray();
                    ArrayList<TopCategoryModel> topCategoryModel = ParserUtil.parseTopCategory(categoriesJsonObject);
                    renderTopCategoryList(topCategoryModel, responseVersion, response_ok);
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

    private void renderTopCategoryList(ArrayList<TopCategoryModel> categoryListArray, String version, boolean a_ok) {
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;
        contentView.removeAllViews();
        if (categoryListArray == null) {
            showErrorMsg("Please try again later");     //2
        } else {
            CategoryAdapter categoryAdapter = null;
            categoryAdapter = new CategoryAdapter(getActivity());
            if (!a_ok) {
                try {
                    categoryAdapter.insert(categoryListArray, version);
                    categoryAdapter.close();
                } catch (Exception e) {
                    categoryAdapter.close();
                    e.printStackTrace();
                    showErrorMsg("Please try again later");    //24
                } finally {
                    if (categoryAdapter != null)
                        categoryAdapter.close();
                }
            } else {
                try {
                    categoryListArray = categoryAdapter.getAllTopCategories();
                } catch (SQLiteException e) {
                    categoryAdapter.close();
                    e.printStackTrace();
                    showErrorMsg("Please try again later");

                } finally {
                    if (categoryAdapter != null)
                        categoryAdapter.close();
                }
            }
            LayoutInflater inflater = getActivity().getLayoutInflater();

            for (final TopCategoryModel topCategoryModel : categoryListArray) {
                try {

                    RelativeLayout topCategoryListRow = (RelativeLayout) inflater.inflate(R.layout.uiv3_list_icon_and_text_row, null);

                    String imagePath = topCategoryModel.getImagePath();
                    ImageView productTopCategoryImage = (ImageView) topCategoryListRow.findViewById(R.id.itemImg);
                    if (imagePath != null)
                        ImageLoader.getInstance().displayImage(imagePath, productTopCategoryImage);
                    else
                        productTopCategoryImage.setImageResource(R.drawable.image_404_top_cat);

                    final TextView productTopCategoryName = (TextView) topCategoryListRow.findViewById(R.id.itemTitle);
                    productTopCategoryName.setText(topCategoryModel.getName());
                    productTopCategoryName.setTypeface(faceRobotoRegular);

                    topCategoryListRow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            SubCategoryListFragment subCategoryListFragment = new SubCategoryListFragment();
                            Bundle bundle = new Bundle();
                            bundle.putParcelable(Constants.TOP_CATEGORY, topCategoryModel);
                            subCategoryListFragment.setArguments(bundle);
                            changeFragment(subCategoryListFragment);
                        }
                    });
                    contentView.addView(topCategoryListRow);
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorMsg("Please try again later");
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
        return TopCategoryListFragment.class.getName();
    }
}