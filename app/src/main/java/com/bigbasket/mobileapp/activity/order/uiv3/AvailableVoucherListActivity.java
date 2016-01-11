package com.bigbasket.mobileapp.activity.order.uiv3;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.ActiveVouchers;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.NavigationCodes;
import com.bigbasket.mobileapp.util.TrackEventkeys;

import java.util.ArrayList;
import java.util.HashMap;


public class AvailableVoucherListActivity extends BackButtonActivity {

    private EditText mEditTextVoucherCode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(getString(R.string.selectAndApplyVoucher));
        ArrayList<ActiveVouchers> activeVouchersList = getIntent().getParcelableArrayListExtra(Constants.VOUCHERS);
        renderVouchers(activeVouchersList);
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getPreviousScreenName());
        trackEvent(TrackingAware.EVOUCHER_SHOWN, map);
    }

    private void renderVouchers(final ArrayList<ActiveVouchers> activeVouchersList) {

        FrameLayout contentLayout = (FrameLayout) findViewById(R.id.content_frame);
        contentLayout.removeAllViews();

        LayoutInflater inflater = getLayoutInflater();
        View base = inflater.inflate(R.layout.uiv3_apply_voucher, contentLayout, false);

        mEditTextVoucherCode = (EditText) base.findViewById(R.id.editTextVoucherCode);
        mEditTextVoucherCode.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent keyEvent) {
                if (((keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) ||
                        actionId == EditorInfo.IME_ACTION_DONE) {
                    String voucherCode = mEditTextVoucherCode.getText().toString();
                    if (!TextUtils.isEmpty(voucherCode)) {
                        trackEvent(AVAILABLE_EVOUCHER_KEYBOARD_APPLY_CLICKED,null);
                        applyVoucher(voucherCode);
                    }
                    hideKeyboard(getCurrentActivity(), mEditTextVoucherCode);
                }
                return false;
            }
        });
        TextView lblApplyVoucher = (TextView) base.findViewById(R.id.lblApply);
        final ListView listVoucher = (ListView) base.findViewById(R.id.lstAvailableVouchers);

        mEditTextVoucherCode.setTypeface(faceRobotoLight);
        if (activeVouchersList == null || activeVouchersList.size() == 0) {
            listVoucher.setVisibility(View.GONE);
            base.findViewById(R.id.divider).setVisibility(View.GONE);
        } else {
            ActiveVoucherListAdapter activeVoucherListAdapter = new ActiveVoucherListAdapter(activeVouchersList);
            listVoucher.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
            listVoucher.setAdapter(activeVoucherListAdapter);
            listVoucher.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ActiveVouchers activeVouchers = activeVouchersList.get(position);
                    if (activeVouchers.canApply()) {
                        trackEvent(AVAILABLE_EVOUCHER_SELECTED, null);
                        applyVoucher(activeVouchers.getCode());
                    }
                }
            });
        }

        lblApplyVoucher.setTypeface(faceRobotoRegular);
        lblApplyVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!TextUtils.isEmpty(mEditTextVoucherCode.getText().toString())) {
                    trackEvent(AVAILABLE_EVOUCHER_USER_ENTERED, null);
                    BaseActivity.hideKeyboard(getCurrentActivity(), mEditTextVoucherCode);
                    String voucherCode = mEditTextVoucherCode.getText().toString();
                    applyVoucher(voucherCode);
                }
            }
        });

        contentLayout.addView(base);
    }

    @Override
    public void onChangeFragment(AbstractFragment newFragment) {

    }

    private void applyVoucher(String voucherCode) {
        HashMap<String, String> map = new HashMap<>();
        map.put(TrackEventkeys.NAVIGATION_CTX, getCurrentScreenName());
        trackEvent(TrackingAware.AVAILABLE_EVOUCHER_APPLIED, map);

        Intent data = new Intent();
        data.putExtra(Constants.EVOUCHER_CODE, voucherCode);
        setResult(NavigationCodes.VOUCHER_APPLIED, data);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mEditTextVoucherCode != null) {
            hideKeyboard(this, mEditTextVoucherCode);
        }
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
                txtVoucherMsg.setTextColor(ContextCompat.getColor(getCurrentActivity(), R.color.uiv3_secondary_text_color));
                txtLblApply.setTextColor(ContextCompat.getColor(getCurrentActivity(), R.color.uiv3_dialog_header_text_bkg));
                txtLblApply.setBackgroundResource(R.drawable.apply_button_rounded_background);
            } else {
                txtVoucherMsg.setTextColor(ContextCompat.getColor(getCurrentActivity(), R.color.dark_red));
                txtLblApply.setTextColor(ContextCompat.getColor(getCurrentActivity(), R.color.uiv3_secondary_text_color));
                txtLblApply.setBackgroundResource(R.drawable.grey_border);
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