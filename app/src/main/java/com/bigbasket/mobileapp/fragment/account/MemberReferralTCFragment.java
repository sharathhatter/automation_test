package com.bigbasket.mobileapp.fragment.account;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.account.uiv3.MemberReferralOptions;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.MemberReferralProduct;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.fragment.product.ProductDetailFragment;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.nostra13.universalimageloader.core.ImageLoader;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MemberReferralTCFragment extends BaseFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_list_container, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callMemberReferralApi();
    }

    private void callMemberReferralApi() {
        if (getActivity() == null) return;
        if (!DataUtil.isInternetAvailable(getActivity())) return;
        trackEvent(TrackingAware.BASKET_VIEW, null);
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
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
        if (getActivity() == null) return;
        LinearLayout contentView = getContentView();
        if (contentView == null) return;

        contentView.removeAllViews();
        contentView.setBackgroundColor(getResources().getColor(R.color.uiv3_list_bkg_color));

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View referralView = inflater.inflate(R.layout.member_referral_tc, null);

        if(!TextUtils.isEmpty(memberReferralProduct.getIncentiveDesc())){
            //todo need to refer Sid
        }else {
            TextView txtPara1 = (TextView) referralView.findViewById(R.id.txtPara1);

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String city = preferences.getString(Constants.CITY, null);
            TextView txtPara5City = (TextView) referralView.findViewById(R.id.txtPara5);
            TextView txtPara6 = (TextView) referralView.findViewById(R.id.txtPara6);
            TextView txtPara7 = (TextView) referralView.findViewById(R.id.txtPara7);


            if(memberReferralProduct.getIncentiveType().equals("credit")){
                String prefix1 = "Refer you friends and earn `";
                String refAmountStr1 = memberReferralProduct.getMemberCreditAmount() + " with each referral!";
                int prefixLen1 = prefix1.length();
                SpannableString spannableRefAmount1 = new SpannableString(prefix1 + refAmountStr1);
                spannableRefAmount1.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen1 - 1,
                        prefixLen1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                txtPara1.setText(spannableRefAmount1);

                txtPara5City.setText("Your friend needs to register with BigBasket "+ city + " using that link.");


                String prefix2 = "Your BigBasket Wallet will be credited with `";
                String refAmountStr2 = memberReferralProduct.getMemberCreditAmount() + " with each referral!";
                int prefixLen2 = prefix2.length();
                SpannableString spannableRefAmount2 = new SpannableString(prefix2 + refAmountStr2);
                spannableRefAmount2.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen2 - 1,
                        prefixLen2, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                String prefix3 = " within 2 days of delivery of their first order";
                if(memberReferralProduct.getMinorderVal()>10){
                    prefix3 += " worth `";
                    String refAmountStr3 = memberReferralProduct.getMinorderVal() + "";
                    int prefixLen3 = prefix3.length();
                    SpannableString spannableOrderWorth = new SpannableString(spannableRefAmount2
                            +prefix3 + refAmountStr3);
                    spannableOrderWorth.setSpan(new CustomTypefaceSpan("", faceRupee), prefixLen3 - 1,
                            prefixLen3, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtPara6.setText(spannableOrderWorth);
                }else {
                    txtPara6.setText(spannableRefAmount2);
                }
                txtPara7.setText("Terms & Conditions apply (*)");

            }else {
                String voucherMsg = "Refer you friends and get " +
                        memberReferralProduct.getVoucherCode() + "with each referral";
                if(!TextUtils.isEmpty(memberReferralProduct.getVoucherCodeDesc())){
                    voucherMsg =  " which gives you" + memberReferralProduct.getVoucherCodeDesc();
                }
                txtPara1.setText(voucherMsg);
                txtPara5City.setText("Your friend needs to register with BigBasket using that link.");
                String giftMsgString = "You will be given "+ memberReferralProduct.getVoucherCode() +
                        " voucher within 2 days of delivery of their first order ";
                if(memberReferralProduct.getMinorderVal()>10){
                    giftMsgString += "worth `";
                    String refMinOrderVal = memberReferralProduct.getMinorderVal() + "";
                    int giftMsgStringLen = giftMsgString.length();
                    SpannableString spannableVoucherGift = new SpannableString(giftMsgString + refMinOrderVal);
                    spannableVoucherGift.setSpan(new CustomTypefaceSpan("", faceRupee), giftMsgStringLen - 1,
                            giftMsgStringLen, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    txtPara6.setText(spannableVoucherGift);
                }else {
                    txtPara6.setText(giftMsgString);
                }
                txtPara7.setVisibility(View.GONE);
            }

            ImageView imgFreeProduct = (ImageView)referralView.findViewById(R.id.imgFreeProduct);
            if(!TextUtils.isEmpty(memberReferralProduct.getRefImageUrl())){
            ImageLoader.getInstance().displayImage(memberReferralProduct.getRefImageUrl(), imgFreeProduct);
            }else {
                imgFreeProduct.setImageResource(R.drawable.noimage);
            }
            imgFreeProduct.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ProductDetailFragment productDetailFragment = new ProductDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SKU_ID, memberReferralProduct.getSkuId());
                    productDetailFragment.setArguments(bundle);
                    changeFragment(productDetailFragment);
                }
            });

            LinearLayout layoutInnerTC = (LinearLayout) referralView.findViewById(R.id.layoutInnerTC);
            int i = 1;
            for(String termAndCondition : memberReferralProduct.getTermAndCondition()){
                TextView txtTermCondition = new TextView(getActivity());
                txtTermCondition.setTextSize(10);
                txtTermCondition.setText(i+". "+termAndCondition);
                layoutInnerTC.addView(txtTermCondition);
                i++;
            }

            contentView.addView(referralView);


            Button btnRefFriend = (Button)referralView.findViewById(R.id.btnRefFriend);
            btnRefFriend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getCurrentActivity(), MemberReferralOptions.class);
                    startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                }
            });
        }

    }

    @Override
    public String getTitle() {
        return "Referral";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return MemberReferralTCFragment.class.getName();
    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.uiv3LayoutListContainer) : null;
    }
}
