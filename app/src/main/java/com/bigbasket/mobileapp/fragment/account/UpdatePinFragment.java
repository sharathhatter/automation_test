package com.bigbasket.mobileapp.fragment.account;

/**
 * Created by jugal on 18/9/14.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.animation.AnimationFactory;
import com.bigbasket.mobileapp.fragment.base.BaseFragment;
import com.bigbasket.mobileapp.model.request.HttpOperationResult;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.MobileApiUrl;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;

public class UpdatePinFragment extends BaseFragment {
    private String currentPin;
    //private LinearLayout layoutEditPin, layoutCurrentPin;
    private TextView txtPinValue;
    private EditText editTextNewPin;
    private ViewAnimator viewAnimator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.uiv3_updatepin, container, false);
        //renderCurrentMemberPin(view);
        //clickEventHandler(view);
        //return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        renderCurrentMemberPin();
        clickEventHandler();
        getCurrentMemberPin();
    }

    private void setCurrentPin(String currentPin){
        txtPinValue.setText(!TextUtils.isEmpty(currentPin) ? currentPin : getString(R.string.blankPin));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if(currentPin!=null)
            outState.putString(Constants.CURRENT_PIN, currentPin);
        super.onSaveInstanceState(outState);
    }

    private void clickEventHandler() {
        if (getActivity() == null) return;
        LinearLayout view = getContentView();
        if (view == null) return;
        //layoutEditPin = (LinearLayout) view.findViewById(R.id.layoutEditPin);
        //layoutCurrentPin = (LinearLayout) view.findViewById(R.id.layoutCurrentPin);
        ImageView imgEditPin = (ImageView) view.findViewById(R.id.imgEditPin);
        imgEditPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //layoutEditPin.setVisibility(View.VISIBLE);
                //layoutCurrentPin.setVisibility(View.GONE);
                AnimationFactory.flipTransition(viewAnimator, AnimationFactory.FlipDirection.LEFT_RIGHT);
                editTextNewPin.requestFocus();
                BaseActivity.showKeyboard(editTextNewPin);
            }
        });


        TextView txtCancel = (TextView) view.findViewById(R.id.txtCancel);
        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                layoutEditPin.setVisibility(View.GONE);
//                layoutCurrentPin.setVisibility(View.VISIBLE);
                AnimationFactory.flipTransition(viewAnimator, AnimationFactory.FlipDirection.LEFT_RIGHT);
                BaseActivity.hideKeyboard((BaseActivity) getActivity(), editTextNewPin);
            }
        });

        TextView txtUpdate = (TextView) view.findViewById(R.id.txtUpdate);
        txtUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpdatePinButtonClicked();
            }
        });

    }

    private void getCurrentMemberPin() {
        startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.GET_PIN,
                null, false, false, getString(R.string.please_wait), null);
    }

    @Override
    public void onAsyncTaskComplete(HttpOperationResult httpOperationResult) {
        super.onAsyncTaskComplete(httpOperationResult);
        String responseJson = httpOperationResult.getReponseString();
        int responseCode = httpOperationResult.getResponseCode();
        if (responseJson != null) {
            if (!httpOperationResult.isPost()) {
                JsonObject jsonObject = new JsonParser().parse(responseJson).getAsJsonObject();
                JsonObject responseJsonObject = jsonObject.get(Constants.RESPONSE).getAsJsonObject();
                currentPin = responseJsonObject.get(Constants.CURRENT_PIN).getAsString();
                setCurrentPin(currentPin);
            } else {
                switch (responseCode) {
                    case Constants.successRespCode:
                        //layoutEditPin.setVisibility(View.GONE);
                        //layoutCurrentPin.setVisibility(View.VISIBLE);
                        AnimationFactory.flipTransition(viewAnimator, AnimationFactory.FlipDirection.LEFT_RIGHT);
                        currentPin = editTextNewPin.getText().toString();
                        setCurrentPin(editTextNewPin.getText().toString());
                        BaseActivity.hideKeyboard((BaseActivity) getActivity(), editTextNewPin);
                        break;
                    case Constants.notMemberRespCode:
                        String msgInvalidUser = getString(R.string.login_required);
                        //showAlertDialog(this, null, msgInvalidUser); //todo handle for sign-in page
                        //Intent intent = new Intent(getCurrentActivity(), Signin.class);
                        //intent.putExtra("from", "changePin");
                        //startActivityForResult(intent, Constants.GO_TO_HOME);
                        //overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
//                        finish();
                        break;
                    default:
                        showErrorMsg(getString(R.string.INTERNAL_SERVER_ERROR));
                        break;
                }

            }
        }
    }

    public void onUpdatePinButtonClicked() {
        if (editTextNewPin != null) {
            final String newPin = editTextNewPin.getText().toString();
            if (newPin != null && newPin.length() > 0) {
                if (newPin.equalsIgnoreCase(currentPin)) {
                    showErrorMsg(getString(R.string.samePin));
                    editTextNewPin.setText("");
                } else if (newPin.length() != 4) {
                    showErrorMsg(getString(R.string.min4digit));
                    editTextNewPin.setText("");
                } else if (newPin.equals(Constants.FOUR_ZERO)) {
                    showErrorMsg(getString(R.string.not0000));
                    editTextNewPin.setText("");
                } else {
                    startAsyncActivity(MobileApiUrl.getBaseAPIUrl() + Constants.CHANGE_PIN_URL,
                            new HashMap<String, String>() {
                                {
                                    put(Constants.NEW_PIN, newPin);
                                }
                            }, true,
                            false, getString(R.string.please_wait), null
                    );
                }
            } else {
                showErrorMsg(getString(R.string.enterPin));
            }
        }
    }

    private void renderCurrentMemberPin() {
        if (getActivity() == null) return;
        LinearLayout view = getContentView();
        if (view == null) return;

        viewAnimator = (ViewAnimator) view.findViewById(R.id.viewFlipper);

        TextView textPinLabel = (TextView) view.findViewById(R.id.textPinLabel);
        textPinLabel.setTypeface(faceRobotoRegular);

        txtPinValue = (TextView) view.findViewById(R.id.txtPinValue);
        txtPinValue.setTypeface(faceRobotoRegular);
        txtPinValue.setText(!TextUtils.isEmpty(currentPin) ? currentPin : getString(R.string.blankPin));

        editTextNewPin = (EditText) view.findViewById(R.id.editTextNewPin);

        TextView textEditPinLabel = (TextView) view.findViewById(R.id.textEditPinLabel);
        textEditPinLabel.setTypeface(faceRobotoRegular);

        EditText editTextNewPin = (EditText) view.findViewById(R.id.editTextNewPin);
        editTextNewPin.setTypeface(faceRobotoRegular);

        TextView textUpdatePinInfo1 = (TextView) view.findViewById(R.id.textUpdatePinInfo1);
        textUpdatePinInfo1.setTypeface(faceRobotoRegular);
        TextView textUpdatePinInfo2 = (TextView) view.findViewById(R.id.textUpdatePinInfo2);
        textUpdatePinInfo2.setTypeface(faceRobotoRegular);

    }

    @Override
    public LinearLayout getContentView() {
        return getView() != null ? (LinearLayout) getView().findViewById(R.id.layoutUpdatePin) : null;
    }

    @Override
    public String getTitle() {
        return "Update Pin";
    }

    @NonNull
    @Override
    public String getFragmentTxnTag() {
        return UpdatePinFragment.class.getName();
    }
}
