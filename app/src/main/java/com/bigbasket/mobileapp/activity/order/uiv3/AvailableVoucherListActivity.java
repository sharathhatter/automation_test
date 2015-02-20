package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.activity.base.uiv3.BackButtonActivity;
import com.bigbasket.mobileapp.fragment.base.AbstractFragment;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;


public class AvailableVoucherListActivity extends BackButtonActivity {

    private int mSelectedIndex = ListView.INVALID_POSITION;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.applyVoucher));
        ArrayList<ActiveVouchers> activeVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        renderVouchers(activeVouchersList);
    }

    private void renderVouchers(final ArrayList<ActiveVouchers> activeVouchersList) {

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        contentLayout.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_apply_voucher, contentLayout, false);

        final EditText editTextVoucherCode = (EditText) base.findViewById(R.id.editTextVoucherCode);
        TextView lblOr = (TextView) base.findViewById(R.id.lblOr);
        Button btnApplyVoucher = (Button) base.findViewById(R.id.btnApplyVoucher);
        final ListView listVoucher = (ListView) base.findViewById(R.id.lstAvailableVouchers);

        editTextVoucherCode.setTypeface(faceRobotoRegular);
        if (activeVouchersList == null || activeVouchersList.size() == 0) {
            lblOr.setVisibility(View.GONE);
            listVoucher.setVisibility(View.GONE);
        } else {
            lblOr.setTypeface(faceRobotoRegular);
            ActiveVoucherListAdapter activeVoucherListAdapter = new ActiveVoucherListAdapter(activeVouchersList);
            listVoucher.setAdapter(activeVoucherListAdapter);
            listVoucher.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSelectedIndex = position;
                    Toast.makeText(getCurrentActivity(), getString(R.string.nowClickApplyVoucher),
                            Toast.LENGTH_LONG).show();
                }
            });
        }

        btnApplyVoucher.setTypeface(faceRobotoRegular);
        btnApplyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editTextVoucherCode.getText().toString())) {
                    String voucherCode = editTextVoucherCode.getText().toString();
                    if (!TextUtils.isEmpty(voucherCode)) {
                        applyVoucher(voucherCode);
                    }
                } else if (activeVouchersList != null && activeVouchersList.size() > 0
                        && mSelectedIndex != ListView.INVALID_POSITION) {
                    ActiveVouchers activeVouchers = activeVouchersList.get(mSelectedIndex);
                    if (activeVouchers.canApply()) {
                        applyVoucher(activeVouchers.getCode());
                    } else {
                        showAlertDialog(getString(R.string.voucherCannotBeApplied),
                                activeVouchers.getMessage());
                    }
                }
            }
        });

        contentLayout.addView(base);
    }

    @Override
    public BaseActivity getCurrentActivity() {
        return this;
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    private void applyVoucher(String voucherCode) {
        Intent data = new Intent();
        data.putExtra(Constants.EVOUCHER_CODE, voucherCode);
        setResult(NavigationCodes.VOUCHER_APPLIED, data);
        finish();
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
                LayoutInflater inflater = (LayoutInflater) getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.uiv3_active_voucher_list_row, parent, false);
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
                txtVoucherMsg.setTextColor(getResources().getColor(R.color.uiv3_secondary_text_color));
            } else {
                txtVoucherMsg.setTextColor(getResources().getColor(R.color.dark_red));
            }

            txtVoucherCode.setText(activeVouchers.getCode());
            txtVoucherCode.setBackgroundColor(randomColor());
            txtVoucherDesc.setText(activeVouchers.getCustomerDesc());
            txtVoucherMsg.setText(activeVouchers.getMessage());
            txtVoucherValidity.setText(getString(R.string.pleaseNote) + " " +
                    activeVouchers.getValidity());
            return row;
        }
    }

    @Override
    public String getScreenTag() {
        return TrackEventkeys.APPLY_EVOUCHER_SCREEN;
    }
}