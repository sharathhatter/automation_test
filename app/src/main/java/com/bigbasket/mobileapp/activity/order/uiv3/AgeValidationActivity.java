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
import com.bigbasket.mobileapp.interfaces.CartInfoAware;
import com.bigbasket.mobileapp.model.cart.CartSummary;
import com.bigbasket.mobileapp.model.general.MessageInfo;
import com.bigbasket.mobileapp.model.general.MessageParamInfo;
import com.bigbasket.mobileapp.model.order.MarketPlace;
import com.bigbasket.mobileapp.model.order.MarketPlaceAgeCheck;
import com.bigbasket.mobileapp.model.order.PharmaPrescriptionInfo;
import com.bigbasket.mobileapp.model.request.AuthParameters;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.task.COMarketPlaceCheckTask;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;
import com.bigbasket.mobileapp.util.ExceptionUtil;
import com.bigbasket.mobileapp.util.MessageFormatUtil;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.bigbasket.mobileapp.util.ParserUtil;
import com.bigbasket.mobileapp.util.UIUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.impl.client.BasicCookieStore;

import java.util.ArrayList;
import java.util.HashMap;

public class AgeValidationActivity extends BackButtonActivity {
    private MarketPlace marketPlace;
    private HashMap<String, Boolean> hashMapRadioBtnAgeCheckNo = new HashMap<>(); // todo need to save this to savedInstanceState
    private boolean fromOnCreate, isPharmaRadioBtnNoSelected;
    private Button btnContinueOrUploadPrescription;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // todo back button override for GO_TO_HOME
        if (savedInstanceState != null) {
            fromOnCreate = false;
            marketPlace = savedInstanceState.getParcelable(Constants.MARKET_PLACE_INTENT);
            if (marketPlace != null) {
                renderMarketPlaceValidationErrors();
                return;
            }
        }
        fromOnCreate = true;
        marketPlace = getIntent().getParcelableExtra(Constants.MARKET_PLACE_INTENT);
        renderMarketPlaceValidationErrors();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (marketPlace != null) {
            outState.putParcelable(Constants.MARKET_PLACE_INTENT, marketPlace);
        }
        super.onSaveInstanceState(outState);
    }

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
        new COReserveQuantityCheckTask<>(getCurrentActivity(), pharmaPrescriptionId).execute();
    }

    private void renderMarketPlaceValidationErrors() {
        if (marketPlace == null) return;
        FrameLayout contentView = (FrameLayout) findViewById(R.id.content_frame);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout base = new LinearLayout(this);
        base.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(base);
        renderAgeValidations(base);
        renderPharmaPrescriptionValidations(base);

        btnContinueOrUploadPrescription = UIUtil.getPrimaryButton(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int btnMargin = (int) getResources().getDimension(R.dimen.margin_large);
        layoutParams.setMargins(btnMargin, btnMargin, btnMargin, btnMargin);
        btnContinueOrUploadPrescription.setLayoutParams(layoutParams);
        btnContinueOrUploadPrescription.setTypeface(faceRobotoRegular);
        if (marketPlace.isPharamaPrescriptionNeeded()){
            btnContinueOrUploadPrescription.setText(getString(R.string.uploadPrescription));
            btnContinueOrUploadPrescription.setTag(Constants.UPLOAD_PRESCRIPTION_BTN_TAG);
        }else {
            btnContinueOrUploadPrescription.setText("Continue");
            btnContinueOrUploadPrescription.setTag(Constants.CONTINUE_BTN_TAG);
        }
        btnContinueOrUploadPrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isPharmaRadioBtnNoSelected){ // check if pharma radio btn no selected
                    showAlertDialog(getResources().getString(R.string.pharma_msg));
                }else if(hashMapRadioBtnAgeCheckNo.size()>0){  //check if any age-validation radio btn no selected
                    showAlertDialog(getResources().getString(R.string.age_validation_required));
                }else {
                    if(String.valueOf(btnContinueOrUploadPrescription.getTag()).equals(Constants.CONTINUE_BTN_TAG)){
                        proceedToQc();
                    }else {
                        Intent intent = new Intent(getCurrentActivity(), PrescriptionListActivity.class);
                        intent.putExtra(Constants.FROM_AGE_VALIDATION, true);
                        intent.putExtra(Constants.MARKET_PLACE_INTENT, marketPlace);
                        startActivityForResult(intent, Constants.GO_TO_HOME);
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left);
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
                        showAlertDialog(getCurrentActivity(), null,
                                "Remove all " + marketPlaceAgeCheck.getDisplayName() + " products from Basket?",
                                DialogButton.YES, DialogButton.NO, Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET,
                                marketPlaceAgeCheck.getFulfillmentId(), "Remove " + marketPlaceAgeCheck.getDisplayName());
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
        TextView txtAgeMsg = (TextView) ageLayout.findViewById(R.id.txtAgeMsg);
        txtAgeMsg.setTypeface(faceRobotoRegular);
        txtAgeMsg.setTextColor(getResources().getColor(R.color.uiv3_primary_text_color));
        txtAgeMsg.setText(getString(R.string.pharmaPrescriptionHeading));

        RadioButton rbtnYes = (RadioButton) ageLayout.findViewById(R.id.radioBtnYes);
        final RadioButton rbtnNo = (RadioButton) ageLayout.findViewById(R.id.radioBtnNo);
        rbtnYes.setTypeface(faceRobotoRegular);
        rbtnNo.setTypeface(faceRobotoRegular);
        rbtnYes.setText(getString(R.string.YesRadioBtnPharmaMsg));
        rbtnNo.setText(getString(R.string.NoRadioBtnPharmaMsg));

        ArrayList<MessageParamInfo> messageParamInfos = marketPlace.getPharmaPrescriptionInfo().getMsgInfo().getParams();
        String fulFillmentIds = null;
        int pharmaInfoSize = messageParamInfos.size();
        if(pharmaInfoSize == 1){
            fulFillmentIds = messageParamInfos.get(0).getInternalValue();
        }else {
            fulFillmentIds = messageParamInfos.get(0).getInternalValue();
            for(int j=1; j<pharmaInfoSize; j++) {
                fulFillmentIds ="," +messageParamInfos.get(j).getInternalValue();
            }
        }
        if(fulFillmentIds!=null) {
            rbtnNo.setTag(fulFillmentIds);
        }else {
            assert false:"Pharma Prescription Id is null"; return; //todo check
        }

        rbtnYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    btnContinueOrUploadPrescription.setText(getString(R.string.uploadPrescription));
                    btnContinueOrUploadPrescription.setTag(Constants.UPLOAD_PRESCRIPTION_BTN_TAG);
                    isPharmaRadioBtnNoSelected = false;
                }
            }
        });
        rbtnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    btnContinueOrUploadPrescription.setText("Continue");
                    btnContinueOrUploadPrescription.setTag(Constants.CONTINUE_BTN_TAG);
                    isPharmaRadioBtnNoSelected = true;
                    showAlertDialog(getCurrentActivity(), null,
                            "Remove all pharma products from basket", DialogButton.YES,
                            DialogButton.NO, Constants.REMOVE_ALL_MARKETPLACE_FROM_BASKET, String.valueOf(rbtnNo.getTag()));
                }
            }
        });

        // Setting info-message
        TextView txtInfoMsg = (TextView) ageLayout.findViewById(R.id.txtInfoMsg);
        txtInfoMsg.setTypeface(faceRobotoRegular);

        MessageInfo msgInfo = pharmaPrescriptionInfo.getMsgInfo();

        if (msgInfo != null && msgInfo.getParams() != null) {
            ArrayList<Class<?>> activitiesList = new ArrayList<>();
            for (int i = 0; i < msgInfo.getParams().size(); i++) {
                activitiesList.add(BackButtonActivity.class);
            }

            if (!TextUtils.isEmpty(msgInfo.getMessageStr())) {
                MessageFormatUtil messageFormatUtil = new MessageFormatUtil();
                SpannableStringBuilder msgContent = messageFormatUtil.
                        replaceStringArgWithDisplayNameAndLink(this, msgInfo.getMessageStr(),
                                msgInfo.getParams(), activitiesList);
                txtInfoMsg.setMovementMethod(LinkMovementMethod.getInstance());
                if (msgContent != null) {
                    txtInfoMsg.setText(msgContent, TextView.BufferType.SPANNABLE);
                    txtInfoMsg.setSelected(true);
                }
            }
        }
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
            float textSize = getResources().getDimension(R.dimen.small_text_size);
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
                    startSign.setTextSize(textSize);
                    layoutTC.addView(startSign);


                    TextView txtTCMsg = new TextView(getCurrentActivity());
                    txtTCMsg.setPadding(miniPadding, 0, 0, 0);
                    txtTCMsg.setTextColor(getResources().getColor(R.color.dark_red));
                    txtTCMsg.setTextSize(textSize);
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
                    AuthParameters authParameters = AuthParameters.getInstance(getCurrentActivity());
                    startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.C_BULK_REMOVE,
                            new HashMap<String, String>() {
                                {
                                    put(Constants.FULFILLMENT_ID, String.valueOf(valuePassed));
                                }
                            }, false,
                            authParameters,
                            new BasicCookieStore()
                    );
                    break;
                default:
                    super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
                    break;
            }
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        super.onAsyncTaskComplete(httpOperationResult);
        String responseString = httpOperationResult.getReponseString();
        JsonObject jsonObject = new JsonParser().parse(responseString).getAsJsonObject();
        if (httpOperationResult.getUrl().contains(Constants.C_BULK_REMOVE)) {
            int status = jsonObject.get(Constants.STATUS).getAsInt();
            if (status == 0) {
                CartSummary cartInfo = ParserUtil.parseGetCartSummaryResponse(jsonObject.
                        get(Constants.CART_SUMMARY).getAsJsonObject());
                ((CartInfoAware) getCurrentActivity()).setCartInfo(cartInfo);
                ((CartInfoAware) getCurrentActivity()).updateUIForCartInfo();
                SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefer.edit();
                editor.putString(Constants.GET_CART, String.valueOf(cartInfo.getNoOfItems()));
                editor.commit();
                if (cartInfo.getNoOfItems() == 0) {
                    showAlertDialogFinish(this, null, getResources().getString(R.string.basketEmpty));
                } else {
                    new COMarketPlaceCheckTask<>(getCurrentActivity()).execute();
                }
            } else {
                String msgString = status == ExceptionUtil.INTERNAL_SERVER_ERROR ?
                        getResources().getString(R.string.INTERNAL_SERVER_ERROR) :
                        jsonObject.get(Constants.MESSAGE).getAsString();
                showAlertDialog(this, null, msgString, Constants.GO_TO_HOME_STRING);
            }
        }
    }
}
