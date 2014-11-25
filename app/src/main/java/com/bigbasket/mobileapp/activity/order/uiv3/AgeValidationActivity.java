package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.DialogInterface;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
    public void onCoMarketPlaceSuccess(MarketPlace marketPlace) {
        this.marketPlace = marketPlace;
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

        Button btnContinue = UIUtil.getPrimaryButton(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        int btnMargin = (int) getResources().getDimension(R.dimen.margin_large);
        layoutParams.setMargins(btnMargin, btnMargin, btnMargin, btnMargin);
        btnContinue.setLayoutParams(layoutParams);
        btnContinue.setTypeface(faceRobotoRegular);
        btnContinue.setText("Continue");
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                proceedToQc();
            }
        });
        base.addView(btnContinue);

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

            RadioButton rbtnYes = (RadioButton) ageLayout.findViewById(R.id.radioBtnYes);
            final RadioButton rbtnNo = (RadioButton) ageLayout.findViewById(R.id.radioBtnNo);
            rbtnYes.setTypeface(faceRobotoRegular);
            rbtnNo.setTypeface(faceRobotoRegular);
            rbtnYes.setText(getString(R.string.preYesRadioBtnAgeMsg) + " " + marketPlaceAgeCheck.getAgeLimit() + " year");
            rbtnNo.setText(getString(R.string.preNoRadioBtnAgeMsg) + " " + marketPlaceAgeCheck.getAgeLimit()
                    + " year, remove " + marketPlaceAgeCheck.getDisplayName() + " from this order");
            rbtnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
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
        RadioButton rbtnNo = (RadioButton) ageLayout.findViewById(R.id.radioBtnNo);
        rbtnYes.setTypeface(faceRobotoRegular);
        rbtnNo.setTypeface(faceRobotoRegular);
        rbtnYes.setText(getString(R.string.YesRadioBtnPharmaMsg));
        rbtnNo.setText(getString(R.string.NoRadioBtnPharmaMsg));
        rbtnYes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
        rbtnNo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

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
