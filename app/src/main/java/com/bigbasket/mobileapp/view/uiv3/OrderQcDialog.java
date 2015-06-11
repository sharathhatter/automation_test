package com.bigbasket.mobileapp.view.uiv3;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.order.QcListAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.util.FontHolder;

public class OrderQcDialog<T> {
    public void show(T ctx,
                     final CreatePotentialOrderResponseContent createPotentialOrderResponseContent) {
        final BaseActivity activity = ((ActivityAware) ctx).getCurrentActivity();
        ListView listView = new ListView(activity);
        listView.setDivider(null);
        listView.setDividerHeight(0);

        QcListAdapter qcListAdapter = new QcListAdapter(activity, createPotentialOrderResponseContent.qcErrorDatas,
                FontHolder.getInstance(activity).getFaceRobotoRegular());
        listView.setAdapter(qcListAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.productsUnavailable)
                .setView(listView)
                .setPositiveButton(R.string.continueAnyway, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.postOrderQc(createPotentialOrderResponseContent);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnDialogShowListener());
        dialog.show();
    }
}
