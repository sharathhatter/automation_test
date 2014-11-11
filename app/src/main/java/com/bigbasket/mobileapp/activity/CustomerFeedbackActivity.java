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
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.impl.client.BasicCookieStore;

import java.util.HashMap;

public class CustomerFeedbackActivity extends BaseActivity {

    private static final String TAG = CustomerFeedbackActivity.class.getName();
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
        HashMap<String, String> postParams = new HashMap<String, String>() {
            {
                put(Constants.CASE_ID, caseId);
            }

            {
                put(Constants.RATING, String.valueOf(Math.round(ratingBar.getRating())));
            }
        };
        String comments = editTextComments.getText().toString();
        if (!TextUtils.isEmpty(comments)) {
            postParams.put(Constants.COMMENTS, comments);
        }
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.POST_FEEDBACK,
                postParams, true, AuthParameters.getInstance(this), new BasicCookieStore());
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        super.onAsyncTaskComplete(httpOperationResult);
        String responseString = httpOperationResult.getReponseString();
        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
        int status = jsonObject.get(Constants.STATUS).getAsInt();
        if (status == 0) {
            JsonObject responseJson = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
            boolean successVal = responseJson.get(Constants.SUCCESS).getAsBoolean();
            if (successVal) {
                showToast("You feedback was submitted successfully!");
                finish();
            } else {
                showAlertDialog(this, null, "Failed to submit your feedback. Please try later");
            }
        } else {
            showAlertDialog(this, null, "Failed to submit your feedback. Please try later");
        }
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


    @Override
    public String getTag() {
        return TAG;
    }
}