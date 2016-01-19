package com.bigbasket.mobileapp.view.uiv3;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ListView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.adapter.order.QcListAdapter;
import com.bigbasket.mobileapp.apiservice.models.response.CreatePotentialOrderResponseContent;
import com.bigbasket.mobileapp.handler.OnDialogShowListener;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.CreatePotentialOrderAware;
import com.bigbasket.mobileapp.util.FontHolder;

public class OrderQcDialog<T> {
    public void show(final T ctx,
                     final CreatePotentialOrderResponseContent createPotentialOrderResponseContent) {
        final BaseActivity activity = ((AppOperationAware) ctx).getCurrentActivity();
        View baseView = activity.getLayoutInflater().inflate(R.layout.uiv3_qc_dialog, null);
        ListView listView = (ListView) baseView.findViewById(R.id.lstQc);

        final boolean allOutOfStock = createPotentialOrderResponseContent.orderDetails.getTotalItems() == 0;

        QcListAdapter qcListAdapter = new QcListAdapter(activity, createPotentialOrderResponseContent.qcErrorDatas,
                FontHolder.getInstance(activity).getFaceRobotoRegular());
        listView.setAdapter(qcListAdapter);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(allOutOfStock ? R.string.noProductsUnavailable : R.string.productsUnavailable)
                .setView(baseView)
                .setPositiveButton(allOutOfStock ? android.R.string.ok : R.string.continueAnyway, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (allOutOfStock) {
                            ((CreatePotentialOrderAware) ctx).onAllProductsHavingQcError();
                        } else {
                            ((CreatePotentialOrderAware) ctx).postOrderQc(createPotentialOrderResponseContent);
                        }
                    }
                });
        if (!allOutOfStock) {
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new OnDialogShowListener());
        dialog.show();
    }
}
