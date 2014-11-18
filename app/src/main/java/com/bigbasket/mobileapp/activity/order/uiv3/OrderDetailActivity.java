package com.bigbasket.mobileapp.activity.order.uiv3;

import android.os.Bundle;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.uiv3.TabActivity;
import com.bigbasket.mobileapp.fragment.order.OrderInvoiceItemsListFragment;
import com.bigbasket.mobileapp.fragment.order.OrderInvoiceSummaryFragment;
import com.bigbasket.mobileapp.fragment.order.OrderModificationFragment;
import com.bigbasket.mobileapp.model.order.OrderInvoice;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.view.uiv3.BBTab;

import java.util.ArrayList;


public class OrderDetailActivity extends TabActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Order Details");
    }

    @Override
    public ArrayList<BBTab> getTabs() {
        OrderInvoice orderInvoice = getIntent().getParcelableExtra(Constants.ORDER_REVIEW_SUMMARY);

        ArrayList<BBTab> bbTabs = new ArrayList<>();

        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.ACTION_TAB_TAG, orderInvoice);
        bbTabs.add(new BBTab<>(getString(R.string.summary), OrderInvoiceSummaryFragment.class, bundle));
        bbTabs.add(new BBTab<>(getString(R.string.items), OrderInvoiceItemsListFragment.class, bundle));

        if (orderInvoice.getOrderModifications() != null && orderInvoice.getOrderModifications().size() > 0) {
            bbTabs.add(new BBTab<>(getString(R.string.orderModification), OrderModificationFragment.class, bundle));
        }
        return bbTabs;
    }
}