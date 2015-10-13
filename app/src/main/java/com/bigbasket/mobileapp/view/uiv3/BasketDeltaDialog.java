package com.bigbasket.mobileapp.view.uiv3;

import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.order.QcListAdapter;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.BasketDeltaUserActionListener;
import com.bigbasket.mobileapp.model.order.QCErrorData;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.ArrayList;

public class BasketDeltaDialog<T extends BasketDeltaUserActionListener> {
    public void show(final T ctx, @Nullable String title, @Nullable String msg, boolean hasQcErrors,
                     @Nullable ArrayList<QCErrorData> qcErrorDatas,
                     @Nullable final String addressId, String positiveMsg,
                     @Nullable final String lat,
                     @Nullable final String lng) {
        final BaseActivity activity = ((ActivityAware) ctx).getCurrentActivity();

        View baseView = null;
        boolean hasItems = hasQcErrors && qcErrorDatas != null && qcErrorDatas.size() > 0;
        if (hasItems) {
            baseView = activity.getLayoutInflater().inflate(R.layout.uiv3_qc_dialog, null);
            ListView listView = (ListView) baseView.findViewById(R.id.lstQc);

            QcListAdapter qcListAdapter = new QcListAdapter(activity, qcErrorDatas,
                    FontHolder.getInstance(activity).getFaceRobotoRegular());
            if (!TextUtils.isEmpty(msg)) {
                TextView txt = new TextView(((ActivityAware) ctx).getCurrentActivity());
                txt.setTextSize(14);
                txt.setTextColor(activity.getResources().getColor(R.color.uiv3_primary_text_color));
                txt.setText(msg);
                listView.addHeaderView(txt);
            }
            listView.setAdapter(qcListAdapter);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        if (!TextUtils.isEmpty(title)) {
            builder.setTitle(title);
        }
        if (!TextUtils.isEmpty(msg) && !hasItems) {
            builder.setMessage(msg);
        }
        if (baseView != null) {
            builder.setView(baseView);
        }
        builder.setPositiveButton(positiveMsg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ctx.onUpdateBasket(addressId, lat, lng);
            }
        }).setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ctx.onNoBasketUpdate();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnDialogShowListener());
        dialog.show();
    }
}
