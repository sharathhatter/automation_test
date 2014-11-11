package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DialogButton;

import java.util.ArrayList;


public class AvailableVoucherListActivity extends BackButtonActivity {

    private static final String TAG = AvailableVoucherListActivity.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.selectVoucher));
        ArrayList<ActiveVouchers> activeVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        renderVouchers(activeVouchersList);
    }

    private void renderVouchers(final ArrayList<ActiveVouchers> activeVouchersList) {
        ListView listVoucher = new ListView(this);

        ActiveVoucherListAdapter activeVoucherListAdapter = new ActiveVoucherListAdapter(activeVouchersList);
        listVoucher.setAdapter(activeVoucherListAdapter);
        listVoucher.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ActiveVouchers activeVouchers = activeVouchersList.get(position);
                if (activeVouchers.canApply()) {
                    showAlertDialog(getCurrentActivity(), "Apply " + activeVouchers.getCode() + "?",
                            "Are you sure you want to apply \"" + activeVouchers.getCode() + "\"?",
                            DialogButton.YES, DialogButton.NO, Constants.EVOUCHER_CODE, activeVouchers.getCode());
                } else {
                    Toast.makeText(getCurrentActivity(), "Sorry, but you can't apply this voucher", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public String getTag() {
        return TAG;
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    @Override
    protected void onPositiveButtonClicked(DialogInterface dialogInterface, int id, String sourceName,
                                           Object valuePassed) {
        if (sourceName.equals(Constants.EVOUCHER_CODE)) {
            Intent data = new Intent();
            data.putExtra(Constants.EVOUCHER_CODE, valuePassed.toString());
            setResult(Constants.VOUCHER_APPLIED, data);
            finish();
        } else {
            super.onPositiveButtonClicked(dialogInterface, id, sourceName, valuePassed);
        }
    }

    private class ActiveVoucherListAdapter extends BaseAdapter {
        private ArrayList<ActiveVouchers> activeVouchersList;

        public ActiveVoucherListAdapter(ArrayList<ActiveVouchers> activeVouchersList) {
            this.activeVouchersList = activeVouchersList;
        }

        @Override
        public int getCount() {
            return activeVouchersList.size();
        }

        @Override
        public Object getItem(int position) {
            return activeVouchersList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ActiveVoucherViewHolder {
            private View base;
            private TextView txtVoucherCode;
            private TextView txtVoucherDesc;
            private TextView txtVoucherMsg;
            private TextView txtVoucherValidity;

            private ActiveVoucherViewHolder(View base) {
                this.base = base;
            }

            public TextView getTxtVoucherDesc() {
                if (txtVoucherDesc == null) {
                    txtVoucherDesc = (TextView) base.findViewById(R.id.txtVoucherDesc);
                    txtVoucherDesc.setTypeface(faceRobotoRegular);
                }
                return txtVoucherDesc;
            }

            public TextView getTxtVoucherMsg() {
                if (txtVoucherMsg == null) {
                    txtVoucherMsg = (TextView) base.findViewById(R.id.txtVoucherMsg);
                    txtVoucherMsg.setTypeface(faceRobotoRegular);
                }
                return txtVoucherMsg;
            }

            public TextView getTxtVoucherValidity() {
                if (txtVoucherValidity == null) {
                    txtVoucherValidity = (TextView) base.findViewById(R.id.txtVoucherValidity);
                    txtVoucherValidity.setTypeface(faceRobotoRegular);
                }
                return txtVoucherValidity;
            }

            public TextView getTxtVoucherCode() {
                if (txtVoucherCode == null) {
                    txtVoucherCode = (TextView) base.findViewById(R.id.txtVoucherCode);
                    txtVoucherCode.setTypeface(faceRobotoRegular);
                }
                return txtVoucherCode;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ActiveVoucherViewHolder activeVoucherViewHolder;
            final ActiveVouchers activeVouchers = activeVouchersList.get(position);
            if (row == null) {
                row = getInflatedView();
                activeVoucherViewHolder = new ActiveVoucherViewHolder(row);
                row.setTag(activeVoucherViewHolder);
            } else {
                activeVoucherViewHolder = (ActiveVoucherViewHolder) row.getTag();
            }
            TextView txtVoucherCode = activeVoucherViewHolder.getTxtVoucherCode();
            TextView txtVoucherDesc = activeVoucherViewHolder.getTxtVoucherDesc();
            TextView txtVoucherValidity = activeVoucherViewHolder.getTxtVoucherValidity();
            TextView txtVoucherMsg = activeVoucherViewHolder.getTxtVoucherMsg();

            if (activeVouchers.canApply()) {
            } else {

            }

            txtVoucherCode.setText(activeVouchers.getCode());
            txtVoucherCode.setBackgroundColor(randomColor());
            txtVoucherDesc.setText(activeVouchers.getCustomerDesc());
            txtVoucherMsg.setText(activeVouchers.getMessage());
            txtVoucherValidity.setText(activeVouchers.getValidity());
            return row;
        }

        private View getInflatedView() {
            LayoutInflater inflater = (LayoutInflater) getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater.inflate(R.layout.uiv3_active_voucher_list_row, null);
        }
    }
}