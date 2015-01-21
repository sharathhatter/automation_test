package com.bigbasket.mobileapp.activity.account.uiv3;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.MemberReferralProduct;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.view.uiv3.ReferralTCDialog;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MemberReferralTCActivity extends BackButtonActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Refer & Earn");
        callMemberReferralApi();
    }

    private void callMemberReferralApi() {
        if (!DataUtil.isInternetAvailable(getCurrentActivity())){ handler.sendOfflineError(); return;}
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
        showProgressView();
        bigBasketApiService.getReferralProduct(new Callback<ApiResponse<MemberReferralProduct>>() {
            @Override
            public void success(ApiResponse<MemberReferralProduct> getRefProductApiResponseCallback, Response response) {
                if (isSuspended()) return;
                hideProgressView();
                if (getRefProductApiResponseCallback.status == 0) {
                    loadMemberReferral(getRefProductApiResponseCallback.apiResponseContent);
                } else {
                    handler.sendEmptyMessage(getRefProductApiResponseCallback.status);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (isSuspended()) return;
                hideProgressView();
                handler.handleRetrofitError(error);
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

        if(!TextUtils.isEmpty(memberReferralProduct.incentiveDesc)){
            LayoutInflater inflater = getCurrentActivity().getLayoutInflater();
            View emptyView = inflater.inflate(R.layout.uiv3_empty_data_text, null);
            TextView txtEmptyDataMsg = (TextView) emptyView.findViewById(R.id.txtEmptyDataMsg);
            txtEmptyDataMsg.setText(memberReferralProduct.incentiveDesc);
            contentView.removeAllViews();
            contentView.addView(base);
            contentView.addView(emptyView);
        }else {

            final LayoutInflater inflater = getCurrentActivity().getLayoutInflater();
            View referralView = inflater.inflate(R.layout.member_referral_tc, null);
            TextView txtRefEarn = (TextView) referralView.findViewById(R.id.txtRefEarn);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
            String city = preferences.getString(Constants.CITY, null);
            TextView txtTermAndCondition = (TextView) referralView.findViewById(R.id.txtTermAndCondition);
            TextView txtTermAndConditionLink = (TextView) referralView.findViewById(R.id.txtTermAndConditionLink);


            if(memberReferralProduct.incentiveType.equals("credit")){
                String prefix1 = "Refer you friends and earn `";
                String refAmountStr1 = memberReferralProduct.memberCreditAmount + " with each referral!";
                int prefixLen1 = prefix1.length();
                SpannableString spannableRefAmount1 = new SpannableString(prefix1 + refAmountStr1);
                spannableRefAmount1.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen1 - 1,
                        prefixLen1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtRefEarn.setText(spannableRefAmount1);

                txtTermAndCondition.setText(getString(R.string.HowItWorksStr)
                        +" Your friend needs to register with BigBasket "+ city + " using that link.");

                String prefix2 = " Your BigBasket Wallet will be credited with `";
                String refAmountStr2 = memberReferralProduct.memberCreditAmount + " with each referral,";
                int prefixLen2 = prefix2.length();
                SpannableString spannableRefAmount2 = new SpannableString(prefix2 + refAmountStr2);
                spannableRefAmount2.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen2 - 1,
                        prefixLen2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                txtTermAndCondition.append(spannableRefAmount2);

                String prefix3 = " within 2 days of delivery of their first order";
                if(memberReferralProduct.minOrderVal>10){
                    prefix3 += " worth `";
                    String refAmountStr3 = memberReferralProduct.minOrderVal + "";
                    int prefixLen3 = prefix3.length();
                    SpannableString spannableOrderWorth = new SpannableString(prefix3 + refAmountStr3);
                    spannableOrderWorth.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen3 - 1,
                            prefixLen3, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtTermAndCondition.append(spannableOrderWorth);
                }else {
                    txtTermAndCondition.append(spannableRefAmount2);
                }
                txtTermAndConditionLink.setText("Terms & Conditions apply (*)");
                txtTermAndConditionLink.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new TermAndConditionDialog(getCurrentActivity(),
                                memberReferralProduct.termAndCondition).show(getCurrentActivity().getSupportFragmentManager(),
                                Constants.OTP_REFERRAL_DIALOG);
                    }
                });

            }else {
                String voucherMsg = "Refer you friends and get " +
                        memberReferralProduct.voucherCode + "with each referral";
                if(!TextUtils.isEmpty(memberReferralProduct.voucherCodeDesc)){
                    voucherMsg =  " which gives you" + memberReferralProduct.voucherCodeDesc;
                }
                txtRefEarn.setText(voucherMsg);
                txtTermAndCondition.setText("Your friend needs to register with BigBasket using that link.");
                String giftMsgString = " You will be given "+ memberReferralProduct.voucherCode +
                        " voucher within 2 days of delivery of their first order ";
                if(memberReferralProduct.minOrderVal>10){
                    giftMsgString += "worth `";
                    String refMinOrderVal = memberReferralProduct.minOrderVal + "";
                    int giftMsgStringLen = giftMsgString.length();
                    SpannableString spannableVoucherGift = new SpannableString(giftMsgString + refMinOrderVal);
                    spannableVoucherGift.setSpan(new CustomTypefaceSpan("", faceRupee), giftMsgStringLen - 1,
                            giftMsgStringLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtTermAndCondition.append(spannableVoucherGift);
                }else {
                    txtTermAndCondition.append(giftMsgString);
                }
                txtTermAndConditionLink.setVisibility(View.GONE);
            }

            ImageView imgFreeProduct = (ImageView)referralView.findViewById(R.id.imgFreeProduct);
            if(!TextUtils.isEmpty(memberReferralProduct.refImageUrl)){
            ImageLoader.getInstance().displayImage(memberReferralProduct.refImageUrl, imgFreeProduct);
            }else {
                imgFreeProduct.setImageResource(R.drawable.noimage);
            }
            imgFreeProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProductDetail(memberReferralProduct.skuId);
                }
            });

            TextView txtProductDesc = (TextView)referralView.findViewById(R.id.txtProductDesc);
            txtProductDesc.setText(memberReferralProduct.productDesc);
            txtProductDesc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showProductDetail(memberReferralProduct.skuId);
                }
            });

            contentView.addView(referralView);


            Button btnRefFriend = (Button)referralView.findViewById(R.id.btnRefFriend);
            btnRefFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getCurrentActivity(), MemberReferralOptionsActivity.class);
                    intent.putExtra(Constants.REF_LINK, memberReferralProduct.refLink);
                    intent.putExtra(Constants.REF_LINK_FB, memberReferralProduct.refLinkFb);
                    intent.putExtra(Constants.MAX_MSG_LEN, memberReferralProduct.maxMsgLen);
                    intent.putExtra(Constants.MAX_MSG_CHAR_LEN, memberReferralProduct.maxMsgCharLen);
                    intent.putExtra(Constants.MAX_EMAIL_LEN, memberReferralProduct.maxEmailLen);
                    intent.putExtra(Constants.REFERRAL_MSG, memberReferralProduct.referralMsg);
                    intent.putExtra(Constants.P_DESC, memberReferralProduct.productDesc);
                    intent.putExtra(Constants.REF_IMAGE_URL, memberReferralProduct.refImageUrl);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
            });
        }

    }

    private void showProductDetail(String productId){
        ProductDetailFragment productDetailFragment = new ProductDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Constants.SKU_ID, productId);
        productDetailFragment.setArguments(bundle);
        addToMainLayout(productDetailFragment);
    }

    public static class TermAndConditionDialog extends ReferralTCDialog {

        public TermAndConditionDialog() {
        }

        @SuppressLint("ValidFragment")
        public TermAndConditionDialog(Activity context, ArrayList<String> termAndCondition ) {
            super(context, faceRobotoRegular, termAndCondition);
        }

    }}
