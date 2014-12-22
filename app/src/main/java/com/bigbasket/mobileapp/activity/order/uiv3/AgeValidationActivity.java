package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.BaseApiResponse;
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.general.MessageInfo;
import com.bigbasket.mobileapp.model.general.MessageParamInfo;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.MarketPlaceAgeCheck;
import com.bigbasket.mobileapp.model.order.PharmaPrescriptionInfo;
import com.bigbasket.mobileapp.model.order.SavedPrescription;
import com.bigbasket.mobileapp.task.COMarketPlaceCheckTask;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.ApiErrorCodes;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.FragmentCodes;
import com.bigbasket.mobileapp.util.MessageFormatUtil;
import com.bigbasket.mobileapp.util.NavigationCodes;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AgeValidationActivity extends BackButtonActivity {
    private MarketPlace marketPlace;
    private HashMap<String, Boolean> hashMapRadioBtnAgeCheckNo = new HashMap<>(); // todo need to save this to savedInstanceState
    private boolean isPharmaRadioBtnNoSelected;
    private Button btnContinueOrUploadPrescription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // todo back button override for GO_TO_HOME
        if (savedInstanceState != null) {
            marketPlace = savedInstanceState.getParcelable(Constants.MARKET_PLACE_INTENT);
            if (marketPlace != null) {
                renderMarketPlaceValidationErrors();
                return;
            }
        }
        marketPlace = getIntent().getParcelableExtra(Constants.MARKET_PLACE_INTENT);
        renderMarketPlaceValidationErrors();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            goToHome();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goToHome();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        isActivitySuspended = false;
        if (resultCode == Constants.PRESCRIPTION_UPLOADED) {
            //do nothing
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (marketPlace != null) {
            outState.putParcelable(Constants.MARKET_PLACE_INTENT, marketPlace);
        }
        super.onSaveInstanceState(outState);
    }

    //After bulk remove
    @Override
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        this.marketPlace = marketPlace;
        isPharmaRadioBtnNoSelected = false;
        hashMapRadioBtnAgeCheckNo.clear();
        if (!marketPlace.isPharamaPrescriptionNeeded() && !marketPlace.isAgeCheckRequired()) {
            proceedToQc();
        } else {
            renderMarketPlaceValidationErrors();
        }
    }

    private void proceedToQc() {
        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
        String pharmaPrescriptionId = prefer.getString(Constants.PHARMA_PRESCRIPTION_ID, null);
        new COReserveQuantityCheckTask<>(getCurrentActivity(), pharmaPrescriptionId).startTask();
    }

    private void renderMarketPlaceValidationErrors() {
        if (marketPlace == null) return;
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        if (contentView == null) return;
        contentView.removeAllViews();
        ScrollView scrollView = new ScrollView(this);
        LinearLayout base = new LinearLayout(this);
        base.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(base);
        renderAgeValidations(base);
        renderPharmaPrescriptionValidations(base);

        btnContinueOrUploadPrescription = new Button(this);
        btnContinueOrUploadPrescription.setTextColor(getResources().getColor(R.color.uiv3_primary_text_color));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int btnMargin = (int) getResources().getDimension(R.dimen.margin_large);
        layoutParams.setMargins(btnMargin, btnMargin, btnMargin, btnMargin);
        btnContinueOrUploadPrescription.setLayoutParams(layoutParams);
        btnContinueOrUploadPrescription.setTypeface(faceRobotoRegular);
        if (marketPlace.isPharamaPrescriptionNeeded()) {
            btnContinueOrUploadPrescription.setText(getString(R.string.uploadPrescription));
            btnContinueOrUploadPrescription.setTextSize(getResources().getDimension(R.dimen.very_small_text_size));
            btnContinueOrUploadPrescription.setTag(Constants.UPLOAD_PRESCRIPTION_BTN_TAG);
        } else {
            btnContinueOrUploadPrescription.setText("Continue");
            btnContinueOrUploadPrescription.setTextSize(getResources().getDimension(R.dimen.secondary_text_size));
            btnContinueOrUploadPrescription.setTag(Constants.CONTINUE_BTN_TAG);
        }
        btnContinueOrUploadPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPharmaRadioBtnNoSelected) { // check if pharma radio btn no selected
                    showAlertDialog(getResources().getString(R.string.pharma_msg));
                } else if (hashMapRadioBtnAgeCheckNo.size() > 0) {  //check if any age-validation radio btn no selected
                    showAlertDialog(getResources().getString(R.string.age_validation_required));
                } else {
                    if (String.valueOf(btnContinueOrUploadPrescription.getTag()).equals(Constants.CONTINUE_BTN_TAG)) {
                        proceedToQc();
                    } else {
                        ArrayList<SavedPrescription> savedPrescriptionArrayList = marketPlace.getSavedPrescription();
                        if (savedPrescriptionArrayList != null && savedPrescriptionArrayList.size() > 0) {
                            Intent intent = new Intent(getCurrentActivity(), PrescriptionListActivity.class);
                            intent.putExtra(Constants.MARKET_PLACE_INTENT, marketPlace);
                            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                        } else {
                            Intent intent = new Intent(getCurrentActivity(), UploadNewPrescriptionActivity.class);
                            startActivityForResult(intent, NavigationCodes.GO_TO_HOME);
                            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
                        }


                    }
                }
            }
        });
        base.addView(btnContinueOrUploadPrescription);


        contentView.addView(scrollView);
    }

    private void renderAgeValidations(LinearLayout base) {
        if (!marketPlace.isAgeCheckRequired() || marketPlace.getAgeCheckRequiredDetail() == null)
            return;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (final MarketPlaceAgeCheck marketPlaceAgeCheck : marketPlace.getAgeCheckRequiredDetail()) {
            View ageLayout = inflater.inflate(R.layout.uiv3_age_validation_layout, null);
            TextView txtAgeMsg = (TextView) ageLayout.findViewById(R.id.txtAgeMsg);
            txtAgeMsg.setTypeface(faceRobotoRegular);
            txtAgeMsg.setText(marketPlaceAgeCheck.getAgeMessage());

            final RadioButton rbtnYes = (RadioButton) ageLayout.findViewById(R.id.radioBtnYes);
            rbtnYes.setTag(marketPlaceAgeCheck.getFulfillmentId());
            final RadioButton rbtnNo = (RadioButton) ageLayout.findViewById(R.id.radioBtnNo);
            rbtnNo.setTag(marketPlaceAgeCheck.getFulfillmentId());
            rbtnYes.setTypeface(faceRobotoRegular);
            rbtnNo.setTypeface(faceRobotoRegular);
            rbtnYes.setText(getString(R.string.preYesRadioBtnAgeMsg) + " " + marketPlaceAgeCheck.getAgeLimit() + " year");
            rbtnNo.setText(getString(R.string.preNoRadioBtnAgeMsg) + " " + marketPlaceAgeCheck.getAgeLimit()
                    + " year, remove " + marketPlaceAgeCheck.getDisplayName() + " from this order");

            rbtnYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        hashMapRadioBtnAgeCheckNo.remove(rbtnYes.getTag().toString());
                    }
                }
            });

            rbtnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        hashMapRadioBtnAgeCheckNo.put(rbtnNo.getTag().toString(), true);
                        showAlertDialog(null,
                                "Remove all " + marketPlaceAgeCheck.getDisplayName() + " products from Basket?",
                                DialogButton.YES, DialogButton.NO, Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET,
                                marketPlaceAgeCheck.getFulfillmentId(), "Yes");
                    }
                }
            });
            base.addView(ageLayout);
        }
    }

    private void renderPharmaPrescriptionValidations(LinearLayout base) {
        if (!marketPlace.isPharamaPrescriptionNeeded() || marketPlace.getPharmaPrescriptionInfo() == null)
            return;
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        PharmaPrescriptionInfo pharmaPrescriptionInfo = marketPlace.getPharmaPrescriptionInfo();
        View ageLayout = inflater.inflate(R.layout.uiv3_age_validation_layout, null);

        // pharma Prescription header
        TextView txtheaderMsg = (TextView) ageLayout.findViewById(R.id.txtAgeMsg);
        txtheaderMsg.setTypeface(faceRobotoRegular);
        txtheaderMsg.setTextColor(getResources().getColor(R.color.uiv3_primary_text_color));
        //txtAgeMsg.setText(getString(R.string.pharmaPrescriptionHeading));


        // Setting info-message
        //TextView txtInfoMsg = (TextView) ageLayout.findViewById(R.id.txtInfoMsg);
        //txtInfoMsg.setTypeface(faceRobotoRegular);

        MessageInfo msgInfo = pharmaPrescriptionInfo.getMsgInfo();

        if (msgInfo != null && msgInfo.getParams() != null) {
            ArrayList<Class<?>> activitiesList = new ArrayList<>();
            ArrayList<Integer> fragmentCodeArrayList = new ArrayList<>();
            for (int i = 0; i < msgInfo.getParams().size(); i++) {
                activitiesList.add(BackButtonActivity.class); //todo why backbtn activity
                fragmentCodeArrayList.add(FragmentCodes.START_VIEW_BASKET);
            }

            if (!TextUtils.isEmpty(msgInfo.getMessageStr())) {
                MessageFormatUtil messageFormatUtil = new MessageFormatUtil();
                SpannableStringBuilder msgContent = messageFormatUtil.
                        replaceStringArgWithDisplayNameAndLink(this, msgInfo.getMessageStr(),
                                msgInfo.getParams(), activitiesList, fragmentCodeArrayList);
                txtheaderMsg.setMovementMethod(LinkMovementMethod.getInstance());
                if (msgContent != null) {
                    txtheaderMsg.setText(msgContent, TextView.BufferType.SPANNABLE);
                    txtheaderMsg.setSelected(true);
                    txtheaderMsg.setVisibility(View.VISIBLE);
                }
            }
        }


        RadioButton rbtnYes = (RadioButton) ageLayout.findViewById(R.id.radioBtnYes);
        final RadioButton rbtnNo = (RadioButton) ageLayout.findViewById(R.id.radioBtnNo);
        rbtnYes.setTypeface(faceRobotoRegular);
        rbtnNo.setTypeface(faceRobotoRegular);
        rbtnYes.setText(getString(R.string.YesRadioBtnPharmaMsg));
        rbtnNo.setText(getString(R.string.NoRadioBtnPharmaMsg));

        ArrayList<MessageParamInfo> messageParamInfos = marketPlace.getPharmaPrescriptionInfo().getMsgInfo().getParams();
        String fulFillmentIds = null;
        int pharmaInfoSize = messageParamInfos.size();
        if (pharmaInfoSize == 1) {
            fulFillmentIds = messageParamInfos.get(0).getInternalValue();
        } else {
            fulFillmentIds = messageParamInfos.get(0).getInternalValue();
            for (int j = 1; j < pharmaInfoSize; j++) {
                fulFillmentIds = "," + messageParamInfos.get(j).getInternalValue();
            }
        }
        if (fulFillmentIds != null) {
            rbtnNo.setTag(fulFillmentIds);
        } else {
            return; //todo check
        }

        rbtnYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnContinueOrUploadPrescription.setText(getString(R.string.uploadPrescription));
                    btnContinueOrUploadPrescription.setTextSize(getResources().getDimension(R.dimen.very_small_text_size));
                    btnContinueOrUploadPrescription.setTag(Constants.UPLOAD_PRESCRIPTION_BTN_TAG);
                    isPharmaRadioBtnNoSelected = false;
                }
            }
        });
        rbtnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    btnContinueOrUploadPrescription.setText("Continue");
                    btnContinueOrUploadPrescription.setTextSize(getResources().getDimension(R.dimen.secondary_text_size));
                    btnContinueOrUploadPrescription.setTag(Constants.CONTINUE_BTN_TAG);
                    isPharmaRadioBtnNoSelected = true;
                    showAlertDialog(null,
                            "Remove all pharma products from basket", DialogButton.YES,
                            DialogButton.NO, Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET, String.valueOf(rbtnNo.getTag()));
                }
            }
        });


        base.addView(ageLayout);

        /*
        // Add upload/choose prescription button
        Button btnUploadPrescription = UIUtil.getPrimaryButton(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int btnMargin = (int) getResources().getDimension(R.dimen.margin_large);
        layoutParams.setMargins(btnMargin, btnMargin, btnMargin, btnMargin);
        btnUploadPrescription.setTypeface(faceRobotoRegular);
        btnUploadPrescription.setText(getString(R.string.uploadPrescription));
        btnUploadPrescription.setLayoutParams(layoutParams);
        btnUploadPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        base.addView(btnUploadPrescription);
        */

        // Display any additional terms and condition
        if (msgInfo != null && msgInfo.getParams() != null) {
            int layoutPadding = (int) getResources().getDimension(R.dimen.padding_normal);
            int miniPadding = (int) getResources().getDimension(R.dimen.padding_mini);
            float textSize = this.getResources().getDimension(R.dimen.small_text_size);
            for (MessageParamInfo messageParamInfo : msgInfo.getParams()) {
                if (!TextUtils.isEmpty(messageParamInfo.getExtraInfo())) {

                    LinearLayout layoutTC = new LinearLayout(this);
                    layoutTC.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
                    layoutTC.setOrientation(LinearLayout.HORIZONTAL);
                    layoutTC.setPadding(layoutPadding, layoutPadding, layoutPadding,
                            layoutPadding);

                    TextView startSign = new TextView(getCurrentActivity());
                    startSign.setText(getResources().getString(R.string.asterisk));
                    startSign.setTextColor(getResources().getColor(R.color.red));
                    startSign.setTextSize(12);
                    layoutTC.addView(startSign);


                    TextView txtTCMsg = new TextView(getCurrentActivity());
                    txtTCMsg.setPadding(miniPadding, 0, 0, 0);
                    txtTCMsg.setTextColor(getResources().getColor(R.color.red));
                    txtTCMsg.setTextSize(12);
                    txtTCMsg.setTypeface(faceRobotoRegular);
                    txtTCMsg.setText(messageParamInfo.getExtraInfo());
                    layoutTC.addView(txtTCMsg);
                    base.addView(layoutTC);
                }
            }
        }
    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName, final Object valuePassed) {
        if (sourceName != null) {
            switch (sourceName) {
                case Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET:
                    BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getCurrentActivity());
                    showProgressDialog(getString(R.string.please_wait));
                    bigBasketApiService.cartBulkRemove(valuePassed.toString(), new Callback<BaseApiResponse>() {
                        @Override
                        public void success(BaseApiResponse cartBulkRemoveApiResponseCallback, Response response) {
                            if (isSuspended()) return;
                            try {
                                hideProgressDialog();
                            } catch (IllegalArgumentException e) {
                                return;
                            }
                            if (cartBulkRemoveApiResponseCallback.status == 0) {
                                CartSummary cartInfo = cartBulkRemoveApiResponseCallback.cartSummary;
                                ((CartInfoAware) getCurrentActivity()).setCartInfo(cartInfo);
                                ((CartInfoAware) getCurrentActivity()).updateUIForCartInfo();
                                SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getCurrentActivity());
                                SharedPreferences.Editor editor = prefer.edit();
                                editor.putString(Constants.GET_CART, String.valueOf(cartInfo.getNoOfItems()));
                                editor.commit();
                                if (cartInfo.getNoOfItems() == 0) {
                                    showAlertDialogFinish(null, getResources().getString(R.string.basketEmpty));
                                } else {
                                    new COMarketPlaceCheckTask<>(getCurrentActivity()).startTask();
                                }
                            } else {
                                handler.sendEmptyMessage(cartBulkRemoveApiResponseCallback.status);
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
                    break;
                case ApiErrorCodes.BASKET_EMPTY_STR:
                    goToHome();
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }
}
