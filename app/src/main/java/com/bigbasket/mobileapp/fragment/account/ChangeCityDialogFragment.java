package com.bigbasket.mobileapp.fragment.account;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.OldBaseApiResponse;
import com.bigbasket.mobileapp.handler.BigBasketMessageHandler;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.PinCodeAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.model.account.City;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ChangeCityDialogFragment extends DialogFragment
        implements ProgressIndicationAware, CancelableAware, PinCodeAware, ActivityAware,
        HandlerAware {

    private Spinner mSpinnerChangeCity;
    private ProgressBar mProgressBarChangeCity;
    private ArrayList<City> mCities;
    private City mSelectedCity;
    private String mCurrentCityName;
    private boolean isSuspended;
    private BigBasketMessageHandler handler;

    public static ChangeCityDialogFragment newInstance() {
        return new ChangeCityDialogFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSuspended = false;
        handler = new BigBasketMessageHandler<>(getActivity());
        if (savedInstanceState != null) {
            mCities = savedInstanceState.getParcelableArrayList(Constants.CITIES);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isSuspended = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isSuspended = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        isSuspended = true;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View base = getActivity().getLayoutInflater().inflate(R.layout.uiv3_change_city, null);

        mSpinnerChangeCity = (Spinner) base.findViewById(R.id.spinnerChooseCity);
        mProgressBarChangeCity = (ProgressBar) base.findViewById(R.id.progressBarChangeCity);
        mSpinnerChangeCity.setVisibility(View.GONE);
        mProgressBarChangeCity.setVisibility(View.VISIBLE);

        return UIUtil.getMaterialDialogBuilder(getActivity())
                .title(R.string.changeCityHome)
                .positiveText(R.string.changeCityHome)
                .negativeText(R.string.cancel)
                .customView(base, false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                    }
                })
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mCities != null && mCities.size() > 0) {
            displayCities();
        } else {
            getCities();
        }

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog == null) return;
        Button btnPositive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeCity();
            }
        });
    }

    private void getCities() {
        View base = getView();
        assert base != null;

        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgressDialog(getString(R.string.please_wait));
        bigBasketApiService.listCities(new Callback<ArrayList<City>>() {
            @Override
            public void success(ArrayList<City> cities, Response response) {
                hideProgressDialog();
                mCities = cities;
                displayCities();
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
            }
        });
    }

    private void onCityChanged() {
        Toast.makeText(getActivity(), getResources().getString(R.string.cityChangeSuccess), Toast.LENGTH_SHORT).show();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.CITY, mSelectedCity.getName());
        editor.putString(Constants.CITY_ID, String.valueOf(mSelectedCity.getId()));
        editor.commit();
        dismiss();
        getCurrentActivity().goToHome();
    }

    private void showProgress(boolean visible) {
        if (mProgressBarChangeCity == null) return;
        if (visible) {
            mProgressBarChangeCity.setVisibility(View.VISIBLE);
            mSpinnerChangeCity.setVisibility(View.GONE);
        } else {
            mProgressBarChangeCity.setVisibility(View.GONE);
            mSpinnerChangeCity.setVisibility(View.VISIBLE);
        }
    }

    private void displayCities() {
        if (getActivity() == null) return;

        SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mCurrentCityName = prefer.getString("city", "");

        ArrayAdapter<City> citySpinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item, mCities);
        citySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerChangeCity.setVisibility(View.VISIBLE);
        mSpinnerChangeCity.setAdapter(citySpinnerAdapter);
        mSpinnerChangeCity.setSelection(City.getCurrentCityIndex(mCities, mCurrentCityName));
        mSpinnerChangeCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSelectedCity = mCities.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void changeCity() {
        if (TextUtils.isEmpty(mCurrentCityName) || mSelectedCity == null
                || mSelectedCity.getName().equalsIgnoreCase(mCurrentCityName)) {
            dismiss();
            return;
        }
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(getActivity());
        showProgress(true);
        bigBasketApiService.changeCity(String.valueOf(mSelectedCity.getId()), new Callback<OldBaseApiResponse>() {
            @Override
            public void success(OldBaseApiResponse oldBaseApiResponse, Response response) {
                hideProgressDialog();
                switch (oldBaseApiResponse.status) {
                    case Constants.OK:
                        onCityChanged();
                        break;
                    default:
                        dismiss();
                        handler.sendEmptyMessage(oldBaseApiResponse.getErrorTypeAsInt(), oldBaseApiResponse.message);
                        break;
                }
            }

            @Override
            public void failure(RetrofitError error) {
                hideProgressDialog();
                dismiss();
                handler.handleRetrofitError(error);
            }
        });
    }

    @Override
    public boolean isSuspended() {
        return isSuspended;
    }

    @Override
    public void setSuspended(boolean state) {
        isSuspended = state;
    }

    @Override
    public void onPinCodeFetchSuccess() {

    }

    @Override
    public void onPinCodeFetchFailure() {

    }

    @Override
    public void showProgressDialog(String msg) {
        showProgress(true);
    }

    @Override
    public void hideProgressDialog() {
        showProgress(false);
    }

    @Override
    public void showProgressView() {
        showProgress(true);
    }

    @Override
    public void hideProgressView() {
        showProgress(false);
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return getActivity() instanceof BaseActivity ? (BaseActivity) getActivity() : null;
    }

    @Override
    public BigBasketMessageHandler getHandler() {
        return handler;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCities != null) {
            outState.putParcelableArrayList(Constants.CITIES, mCities);
        }
        super.onSaveInstanceState(outState);
    }
}
