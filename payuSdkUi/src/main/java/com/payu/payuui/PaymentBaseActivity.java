package com.payu.payuui;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by manu on 24/12/15.
 */
public class PaymentBaseActivity extends AppCompatActivity implements TransactionDialogFragment.TransactionDialogListener {

    protected void handleUnknownErrorCondition() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        TransactionDialogFragment transactionDialogFragment = TransactionDialogFragment.newInstance(getString(R.string.unknown_error_message), Constants.UNKNOWN_ERROR_CODE, getString(R.string.ok), null);
        transactionDialogFragment.show(fragmentManager, getClass().getName());

    }

    @Override
    public void onDialogConfirmed(int reqCode, boolean isPositive) {
        switch (reqCode) {
            case Constants.UNKNOWN_ERROR_CODE:
                if (isPositive) {
                    finish();
                }
                break;
        }

    }
}
