package com.bigbasket.mobileapp.view.uiv3;

import android.content.Context;
import android.os.IBinder;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.bigbasket.mobileapp.util.analytics.LocalyticsWrapper;


public abstract class AbstractDialogFragment extends AppCompatDialogFragment {

    public void onResume() {
        super.onResume();
        LocalyticsWrapper.tagScreen(getScreenTag());
    }

    public void hideKeyboard(Context context, View view) {
        if (context == null || view == null) return;
        IBinder token = view.getWindowToken();
        if (token == null) return;
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(token, 0);
    }


    public void showToast(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
    }

    protected abstract String getScreenTag();
}
