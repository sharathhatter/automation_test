package com.bigbasket.mobileapp.activity;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PostFeedbackApiResponseContent;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.handler.network.BBNetworkCallback;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import retrofit2.Call;


public class CustomerFeedbackActivity extends BackButtonActivity {

    private View base;
    private String caseId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        caseId = getIntent().getStringExtra(Constants.CASE_ID);
        if (TextUtils.isEmpty(caseId)) {
            showAlertDialog(null, "No case-id provided");
            return;
        }
        setTitle(getString(R.string.feedback));
        showFeedbackLayout();
    }

    private void showFeedbackLayout() {
        FrameLayout contentFrame = (FrameLayout) findViewById(R.id.content_frame);
        LayoutInflater inflater = getLayoutInflater();
        base = inflater.inflate(R.layout.customer_feedback, contentFrame, false);
        if (base == null)
            return;
        TextView labelRating = (TextView) base.findViewById(R.id.labelRating);
        EditText editTextComments = (EditText) base.findViewById(R.id.editTextComments);
        Button btnSubmit = (Button) base.findViewById(R.id.btnSubmit);

        final RatingBar ratingBar = (RatingBar) base.findViewById(R.id.ratingBar);
        ratingBar.setProgress(0);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Math.round(ratingBar.getRating()) == 0) {
                    showToast(getResources().getString(R.string.rating_toast_msg));
                    return;
                }
                postFeedback();
            }
        });
        Button btnCancel = (Button) base.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        labelRating.setTypeface(faceRobotoRegular);
        editTextComments.setTypeface(faceRobotoRegular);
        btnSubmit.setTypeface(faceRobotoRegular);
        btnCancel.setTypeface(faceRobotoRegular);

        contentFrame.removeAllViews();
        contentFrame.addView(base);
    }

    private void postFeedback() {
        if (!checkInternetConnection()) {
            handler.sendOfflineError();
            return;
        }
        EditText editTextComments = (EditText) base.findViewById(R.id.editTextComments);
        final RatingBar ratingBar = (RatingBar) base.findViewById(R.id.ratingBar);
        String comments = editTextComments.getText().toString().trim();

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(this);
        Call<ApiResponse<PostFeedbackApiResponseContent>> call =
                bigBasketApiService.postCaseFeedback(caseId, String.valueOf(Math.round(ratingBar.getRating())),
                        comments);
        call.enqueue(new BBNetworkCallback<ApiResponse<PostFeedbackApiResponseContent>>(this) {
            @Override
            public void onSuccess(ApiResponse<PostFeedbackApiResponseContent> postFeedbackApiResponse) {
                switch (postFeedbackApiResponse.status) {
                    case 0:
                        if (postFeedbackApiResponse.apiResponseContent.success) {
                            showToast("You feedback was submitted successfully!");
                            finish();
                        } else {
                            showAlertDialog(null, "Failed to submit your feedback. Please try later");
                        }
                        break;
                    default:
                        handler.sendEmptyMessage(postFeedbackApiResponse.status,
                                postFeedbackApiResponse.message);
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

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    public void onChangeTitle(String title) {

    }

    public String getScreenTag() {
        return TrackEventkeys.CUSTOMER_FEEDBACK_SCREEN;
    }

}