package com.bigbasket.mobileapp.activity.account.uiv3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.adapter.account.MemberReferralGridAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.MemberReferralProduct;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.MemberReferralUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.view.uiv3.TermAndConditionDialog;
import com.facebook.UiLifecycleHelper;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MemberReferralActivity extends BackButtonActivity {

    private MemberReferralUtil memberReferralUtil;
    private UiLifecycleHelper uiHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Refer & Earn");

        uiHelper = new UiLifecycleHelper(getCurrentActivity(), null);
        uiHelper.onCreate(savedInstanceState);
        callMemberReferralApi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
        uiHelper.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }


    private void callMemberReferralApi() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) {
            handler.sendOfflineError();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.getReferralProduct(new Callback<ApiResponse<MemberReferralProduct>>() {
            @Override
            public void success(ApiResponse<MemberReferralProduct> getRefProductApiResponseCallback, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                if (getRefProductApiResponseCallback.status == 0) {
                    loadMemberReferral(getRefProductApiResponseCallback.apiResponseContent);
                    trackEvent(TrackingAware.MEMBER_REFERRAL_SHOWN, null);
                } else {
                    handler.sendEmptyMessage(getRefProductApiResponseCallback.status);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error, true);
            }
        });
    }

    private void loadMemberReferral(final MemberReferralProduct memberReferralProduct) {
        FrameLayout base = (FrameLayout) findViewById(R.id.content_frame);
        LinearLayout contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        base.addView(contentView);

        contentView.removeAllViews();
        contentView.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        if (!TextUtils.isEmpty(memberReferralProduct.incentiveDesc)) { //todo test this is not referral data, check will Sid
            LayoutInflater inflater = getCurrentActivity().getLayoutInflater();
            View emptyView = inflater.inflate(R.layout.uiv3_empty_data_text, null);
            TextView txtEmptyDataMsg = (TextView) emptyView.findViewById(R.id.txtEmptyDataMsg);
            txtEmptyDataMsg.setText(memberReferralProduct.incentiveDesc);
            contentView.removeAllViews();
            contentView.addView(base);
            contentView.addView(emptyView);
        } else {
            final LayoutInflater inflater = getCurrentActivity().getLayoutInflater();
            View referralView = inflater.inflate(R.layout.member_referral_tc, null);
            TextView txtRefClientMsg = (TextView) referralView.findViewById(R.id.txtRefClientMsg);
            TextView txtTermAndConditionLink = (TextView) referralView.findViewById(R.id.txtTermAndConditionLink);
            if (!TextUtils.isEmpty(memberReferralProduct.incentiveType) &&
                    memberReferralProduct.incentiveType.equals("credit")) {

                if (!TextUtils.isEmpty(memberReferralProduct.memberCreditAmount)) {
                    String prefix1 = "Invite and earn `";
                    String refAmountStr1 = memberReferralProduct.memberCreditAmount + " bigbasketCash " +
                            "for every friend who installs app sign up using your referral code.";
                    int prefixLen1 = prefix1.length();
                    SpannableString spannableRefAmount1 = new SpannableString(prefix1 + refAmountStr1);
                    spannableRefAmount1.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen1 - 1,
                            prefixLen1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtRefClientMsg.setText(spannableRefAmount1);
                }

                if (!TextUtils.isEmpty(memberReferralProduct.productDesc) &&
                        !TextUtils.isEmpty(memberReferralProduct.skuId)) {
                    String prefix2 = " Your friend also gets " + memberReferralProduct.productDesc;
                    String refAmountStr2 = " as a complimentary gift along with their first BigBasket order!";
                    int lenProductDesc = memberReferralProduct.productDesc.length();
                    int prefixLen2 = prefix2.length();
                    SpannableStringBuilder spannableProductDesc = new SpannableStringBuilder(prefix2 + refAmountStr2);
                    txtRefClientMsg.setMovementMethod(LinkMovementMethod.getInstance());
                    spannableProductDesc.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            showProductDetail(memberReferralProduct.skuId);
                        }

                        @Override
                        public void updateDrawState(TextPaint textPaint) {
                            textPaint.setColor(getResources().getColor(R.color.link_color));
                            textPaint.setUnderlineText(false);
                        }
                    }, prefixLen2 - lenProductDesc, prefixLen2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtRefClientMsg.append(spannableProductDesc);
                }


                if (memberReferralProduct.termAndCondition.size() > 0) {
                    txtTermAndConditionLink.setText("Terms & Conditions");
                    txtTermAndConditionLink.setVisibility(View.VISIBLE);
                    txtTermAndConditionLink.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            TermAndConditionDialog termAndConditionDialog = TermAndConditionDialog.
                                    newInstance(memberReferralProduct.termAndCondition);
                            termAndConditionDialog.show(getSupportFragmentManager(), Constants.OTP_REFERRAL_DIALOG);
                        }
                    });
                } else {
                    txtTermAndConditionLink.setVisibility(View.GONE);
                }

            } else {
                if (!TextUtils.isEmpty(memberReferralProduct.voucherCode)) {
                    String voucherMsg = "Invite and earn " +
                            memberReferralProduct.voucherCode;

                    if (!TextUtils.isEmpty(memberReferralProduct.voucherCodeDesc)) {
                        voucherMsg = " which gives you" + memberReferralProduct.voucherCodeDesc;
                    }

                    voucherMsg += " for every friend who installs app sign up using your referral code.";
                    txtRefClientMsg.setText(voucherMsg);
                }

                if (!TextUtils.isEmpty(memberReferralProduct.productDesc) &&
                        !TextUtils.isEmpty(memberReferralProduct.skuId)) {
                    String prefix2 = " Your friend also gets " + memberReferralProduct.productDesc;
                    String refAmountStr2 = "as a complimentary gift along with their first BigBasket order!";
                    int lenProductDesc = memberReferralProduct.productDesc.length();
                    int prefixLen2 = prefix2.length();
                    SpannableString spannableProductDesc = new SpannableString(prefix2 + refAmountStr2);
                    txtRefClientMsg.setMovementMethod(LinkMovementMethod.getInstance());
                    spannableProductDesc.setSpan(new ClickableSpan() {
                        @Override
                        public void onClick(View widget) {
                            showProductDetail(memberReferralProduct.skuId);
                        }

                        @Override
                        public void updateDrawState(TextPaint textPaint) {
                            textPaint.setColor(getResources().getColor(R.color.link_color));
                            textPaint.setUnderlineText(false);
                        }
                    }, prefixLen2 - lenProductDesc, prefixLen2, 0);
                }
                txtTermAndConditionLink.setVisibility(View.GONE);
            }

            contentView.addView(referralView);
            memberReferralUtil = new MemberReferralUtil<>(getCurrentActivity(),
                    memberReferralProduct.referralMsg, memberReferralProduct.playStoreLink,
                    memberReferralProduct.refImageUrl, memberReferralProduct.maxMsgCharLen,
                    memberReferralProduct.maxEmailLen, memberReferralProduct.maxMsgLen,
                    memberReferralProduct.emailBody, uiHelper);
            ArrayList<Object> listMemberRefOption = memberReferralUtil.populateReferralOptions();
            ArrayList<Integer> referralImageArrayList = (ArrayList<Integer>) listMemberRefOption.get(0);
            ArrayList<String> referralStringArrayList = (ArrayList<String>) listMemberRefOption.get(1);
            GridView layoutGridView = (GridView) referralView.findViewById(R.id.layoutGridView);
            renderMemberReferralList(referralImageArrayList, referralStringArrayList, layoutGridView);
        }
    }

    private void renderMemberReferralList(ArrayList<Integer> referralImageArrayList,
                                          ArrayList<String> referralStringArrayList,
                                          GridView layoutGridView) {
        MemberReferralGridAdapter memberReferralGridAdapter = new MemberReferralGridAdapter<>(getCurrentActivity(),
                referralImageArrayList, referralStringArrayList, faceRobotoRegular);
        layoutGridView.setAdapter(memberReferralGridAdapter);
    }


    public void messageHandler(View view) {
        switch ((String) view.getTag()) {
            case Constants.FREE_MSG:
                sendFreeSMS();
                break;
            case Constants.WHATS_APP:
                memberReferralUtil.sendWhatsAppMsg();
                break;
            case Constants.FACEBOOK:
                memberReferralUtil.useFacebookReferral();
                break;
            case Constants.REF_EMAIL:
                useBBmail();
                break;
            case Constants.G_PLUS:
                memberReferralUtil.useGplus();
                break;
            case Constants.GMAIL:
                memberReferralUtil.useGmailApp();
                break;
            case Constants.HIKE:
                memberReferralUtil.useHikeApp();
                break;
            case Constants.SHARE_VIA_OTHER:
                memberReferralUtil.useOther();
                break;
        }
    }

    private void sendFreeSMS() {
        trackEvent(TrackingAware.MEMBER_REFERRAL_FREE_SMS_SHOWN, null);
        createContactList();
    }

    private void createContactList() {
        Intent intent = new Intent(getCurrentActivity(), ContactListActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
    }

    @Override
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        switch (resultCode) {
            case (NavigationCodes.CONTACT_NUMBER_SELECTED):
                ArrayList<String> selectedContactNumbers = (ArrayList<String>) data.getSerializableExtra(Constants.CONTACT_SELECTED);
                String selectedContactNos = memberReferralUtil.getMobileNumberFromIds(selectedContactNumbers);
                sendServerPhoneNumber(selectedContactNos);
                break;
            case NavigationCodes.REF_EMAIL_LIST:
                if (memberReferralUtil.isMessageAndMailLenValid(data.getIntExtra(Constants.REF_EMAIL_LEN, 0), 0))
                    sendEmailMsgToServer(data.getStringExtra(Constants.REF_EMAIL_LIST),
                            data.getStringExtra(Constants.MESSAGE), "email");
                break;
            default:
                memberReferralUtil.facebookCallBack(reqCode, resultCode, data);
                break;
        }
    }


    public void sendServerPhoneNumber(String selectedContactNumbers) {
        if (getCurrentActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postReferralSms("message", selectedContactNumbers,
                new Callback<ApiResponse>() {
                    @Override
                    public void success(ApiResponse postReferralApiResponseCallback, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (postReferralApiResponseCallback.status == 0) {
                            showToast(postReferralApiResponseCallback.message);
                        } else {
                            showAlertDialog(postReferralApiResponseCallback.message);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });

    }

    private void useBBmail() {
        Intent intent = new Intent(getCurrentActivity(), BBReferralMailActivity.class);
        startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
//        ReferralDialog memberRefDialog = new ReferralDialog(getCurrentActivity(), this, memberReferralUtil);
//        memberRefDialog.show(getCurrentActivity().getSupportFragmentManager(),
//                Constants.REF_DIALOG_FLAG);
    }

    /*
    public static class ReferralDialog extends BaseReferralDialog {
        private MemberReferralActivity memberReferralActivity;
        private MemberReferralUtil memberReferralUtil;

        public ReferralDialog() {
        }

        @SuppressLint("ValidFragment")
        public ReferralDialog(BaseActivity baseActivity,
                              MemberReferralActivity memberReferralActivity,
                              MemberReferralUtil memberReferralUtil) {
            super(baseActivity, faceRobotoRegular);
            this.memberReferralActivity = memberReferralActivity;
            this.memberReferralUtil = memberReferralUtil;
        }

        @Override
        public void sendEmailList(String emailList, String message, int emailLen) {
            if (memberReferralUtil.isMessageAndMailLenValid(emailLen, message.length()))
                memberReferralActivity.sendEmailMsgToServer(emailList, message, "email");
        }

        @Override
        public void populateAutoComplete() {
            List<String> emailAddressCollection = new ArrayList<>();
            memberReferralUtil.getEmailFromContacts(emailAddressCollection, this);
        }
    }

    */


    private void sendEmailMsgToServer(String emailList, String message, String refType) {
        // call to server
        if (getCurrentActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getCurrentActivity())) return;
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.postProduct(emailList, message, refType,
                new Callback<ApiResponse>() {
                    @Override
                    public void success(ApiResponse postReferralApiResponseCallback, Response response) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        if (postReferralApiResponseCallback.status == 0) {
                            showToast(postReferralApiResponseCallback.message);
                        } else {
                            showAlertDialog(postReferralApiResponseCallback.message);
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {
                        if (isSuspended()) return;
                        try {
                            hideProgressDialog();
                        } catch (IllegalArgumentException e) {
                            return;
                        }
                        handler.handleRetrofitError(error);
                    }
                });
    }

    private void showProductDetail(String productId) {
        ProductDetailFragment productDetailFragment = new ProductDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SKU_ID, productId);
        productDetailFragment.setArguments(bundle);
        addToMainLayout(productDetailFragment);
    }
}
