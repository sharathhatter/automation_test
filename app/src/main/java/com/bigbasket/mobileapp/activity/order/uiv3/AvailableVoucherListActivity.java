package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.selectAndApplyVoucher));
        ArrayList<ActiveVouchers> activeVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        renderVouchers(activeVouchersList);
    }

    private void renderVouchers(final ArrayList<ActiveVouchers> activeVouchersList) {

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        contentLayout.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_apply_voucher, contentLayout, false);

        final EditText editTextVoucherCode = (EditText) base.findViewById(R.id.editTextVoucherCode);
        editTextVoucherCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    String voucherCode = editTextVoucherCode.getText().toString();
                    if (!TextUtils.isEmpty(voucherCode)) {
                        applyVoucher(voucherCode);
                    }
                    hideKeyboard(getCurrentActivity(), editTextVoucherCode);
                }
                return false;
            }
        });
        TextView lblApplyVoucher = (TextView) base.findViewById(R.id.lblApply);
        final ListView listVoucher = (ListView) base.findViewById(R.id.lstAvailableVouchers);

        editTextVoucherCode.setTypeface(faceRobotoLight);
        if (activeVouchersList == null || activeVouchersList.size() == 0) {
            listVoucher.setVisibility(View.GONE);
        } else {
            ActiveVoucherListAdapter activeVoucherListAdapter = new ActiveVoucherListAdapter(activeVouchersList);
            listVoucher.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listVoucher.setAdapter(activeVoucherListAdapter);
            listVoucher.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ActiveVouchers activeVouchers = activeVouchersList.get(position);
                    if (activeVouchers.canApply()) {
                        applyVoucher(activeVouchers.getCode());
                    }
                }
            });
        }

        lblApplyVoucher.setTypeface(faceRobotoRegular);
        lblApplyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(editTextVoucherCode.getText().toString())) {
                    BaseActivity.hideKeyboard(getCurrentActivity(), editTextVoucherCode);
                    String voucherCode = editTextVoucherCode.getText().toString();
                    applyVoucher(voucherCode);
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

    @Override
    public String getScreenTag() {
        return TrackEventkeys.APPLY_EVOUCHER_SCREEN;
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

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            ActiveVoucherViewHolder activeVoucherViewHolder;
            final ActiveVouchers activeVouchers = activeVouchersList.get(position);
            if (row == null) {
                LayoutInflater inflater = (LayoutInflater) getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.uiv3_active_voucher_list_row, parent, false);
                ((TextView) row.findViewById(R.id.lblApply)).setTypeface(faceRobotoRegular);
                activeVoucherViewHolder = new ActiveVoucherViewHolder(row);
                row.setTag(activeVoucherViewHolder);
            } else {
                activeVoucherViewHolder = (ActiveVoucherViewHolder) row.getTag();
            }
            TextView txtVoucherCode = activeVoucherViewHolder.getTxtVoucherCode();
            TextView txtVoucherDesc = activeVoucherViewHolder.getTxtVoucherDesc();
            TextView txtVoucherValidity = activeVoucherViewHolder.getTxtVoucherValidity();
            TextView txtVoucherMsg = activeVoucherViewHolder.getTxtVoucherMsg();
            TextView txtLblApply = activeVoucherViewHolder.getTxtLblApply();

            if (activeVouchers.canApply()) {
                txtVoucherMsg.setTextColor(getResources().getColor(R.color.uiv3_secondary_text_color));
            } else {
                txtVoucherMsg.setTextColor(getResources().getColor(R.color.dark_red));
                txtLblApply.setTextColor(getResources().getColor(R.color.uiv3_secondary_text_color));
            }

            txtVoucherCode.setText(activeVouchers.getCode());
            txtVoucherDesc.setText(activeVouchers.getCustomerDesc());
            txtVoucherMsg.setText(activeVouchers.getMessage());
            txtVoucherValidity.setText(getString(R.string.pleaseNote) + " " +
                    activeVouchers.getValidity());
            return row;
        }

        private class ActiveVoucherViewHolder {
            private View base;
            private TextView txtVoucherCode;
            private TextView txtVoucherDesc;
            private TextView txtVoucherMsg;
            private TextView txtVoucherValidity;
            private TextView txtLblApply;

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
                    txtVoucherCode.setTypeface(faceRobotoMedium);
                }
                return txtVoucherCode;
            }

            public TextView getTxtLblApply() {
                if (txtLblApply == null) {
                    txtLblApply = (TextView) base.findViewById(R.id.lblApply);
                    txtLblApply.setTypeface(faceRobotoRegular);
                }
                return txtLblApply;
            }
        }
    }
}