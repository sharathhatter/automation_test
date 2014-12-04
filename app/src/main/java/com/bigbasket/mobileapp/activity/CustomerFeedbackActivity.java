package com.bigbasket.mobileapp.activity;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostFeedbackApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class CustomerFeedbackActivity extends BaseActivity {

    private View base;
    private String caseId;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        caseId = getIntent().getStringExtra(Constants.CASE_ID);
        if (TextUtils.isEmpty(caseId)) {
            showAlertDialog(this, null, "No case-id provided");
            return;
        }
        showFeedbackLayout();
    }

    private void showFeedbackLayout() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        base = inflater.inflate(R.layout.customer_feedback, null);
        if (base == null)
            return;
//        View contentLayout =
//        contentLayout.addView(base);
//        TextView labelRating = (TextView) base.findViewById(R.id.labelRating);
//        EditText editTextComments = (EditText) base.findViewById(R.id.editTextComments);
//        Button btnSubmit = (Button) base.findViewById(R.id.btnSubmit);
//        Button btnCancel = (Button) base.findViewById(R.id.btnCancel);
//        labelRating.setTypeface(faceLatoNormal);
//        editTextComments.setTypeface(faceLatoNormal);
//        btnSubmit.setTypeface(faceLatoNormal);
//        btnCancel.setTypeface(faceLatoNormal);
    }

    private void postFeedback() {
        if (!DataUtil.isInternetAvailable(this)) {
            showAlertDialog(this, null, "No internet connection found. Please try later");
            return;
        }
        EditText editTextComments = (EditText) base.findViewById(R.id.editTextComments);
        if (editTextComments.getText() == null)
            return;
        final RatingBar ratingBar = (RatingBar) base.findViewById(R.id.ratingBar);
        String comments = editTextComments.getText().toString().trim();

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        bigBasketApiService.postCaseFeedback(caseId, String.valueOf(Math.round(ratingBar.getRating())),
                comments, new Callback<ApiResponse<PostFeedbackApiResponseContent>>() {
                    @Override
                    public void success(ApiResponse<PostFeedbackApiResponseContent> postFeedbackApiResponse, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        // TODO : Improve error handling
                        switch (postFeedbackApiResponse.status) {
                            case 0:
                                if (postFeedbackApiResponse.apiResponseContent.success) {
                                    showToast("You feedback was submitted successfully!");
                                    finish();
                                } else {
                                    showAlertDialog(getCurrentActivity(), null, "Failed to submit your feedback. Please try later");
                                }
                                break;
                            default:
                                showAlertDialog(getCurrentActivity(), null, "Failed to submit your feedback. Please try later");
                                break;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        // TODO : Improve error handling
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        showAlertDialog(getCurrentActivity(), null, "Failed to submit your feedback. Please try later");
                    }
                });
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    public void OnSubmitButtonClicked(View v) {
        postFeedback();
    }

    public void OnCancelButtonClicked(View v) {
        finish();
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

}