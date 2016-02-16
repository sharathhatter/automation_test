package com.payu.payuui;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

/**
 * Created by manu on 24/12/15.
 */
public class PaymentBaseActivity extends AppCompatActivity implements TransactionDialogFragment.TransactionDialogListener {

    boolean finishOnDialogConfirmation ;

    protected void handleUnknownErrorCondition() {
        handleUnknownErrorCondition(null, true);
    }

    protected void handleUnknownErrorCondition(String message, boolean finishOnDialogConfirmation) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        if(TextUtils.isEmpty(message)){
            message = getString(R.string.unknown_error_message);
        }
        TransactionDialogFragment transactionDialogFragment =
                TransactionDialogFragment.newInstance(message, Constants.UNKNOWN_ERROR_CODE,
                        getString(R.string.ok), null);
        try {
            transactionDialogFragment.show(fragmentManager, getClass().getName());
            this.finishOnDialogConfirmation = finishOnDialogConfirmation;
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

    }

    @Override
    public void onDialogConfirmed(int reqCode, boolean isPositive) {
        switch (reqCode) {
            case Constants.UNKNOWN_ERROR_CODE:
                if (isPositive && finishOnDialogConfirmation) {
                    finish();
                }
                break;
        }

    }
}
