package com.bigbasket.mobileapp.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.ArrayList;

public class PaymentMethodsView extends LinearLayout {
    private Context context;
    private LayoutInflater inflater;
    private ImageView previousClickedImageView;
    private RelativeLayout previousSelectedLayout;
    private String mSelectedPaymentMethod;
    private OnClickListener paymentTypeClicked;
    private int selectedDefaultPosition = -1;

    public PaymentMethodsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public PaymentMethodsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PaymentMethodsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public PaymentMethodsView(Context context) {
        super(context);
        init();
    }

    public void setSelectedPaymentMethod(String mSelectedPaymentMethod) {
        this.mSelectedPaymentMethod = mSelectedPaymentMethod;
    }

    public void setSelectedDefaultPosition(int selectedDefaultPosition) {
        this.selectedDefaultPosition = selectedDefaultPosition;
    }

    private void init() {
        setOrientation(VERTICAL);
        this.context = getContext();
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paymentTypeClicked = new OnClickListener() {
            @Override
            public void onClick(View view) {
                onPaymentTypeSelection(view);
            }
        };
    }

    /**
     * this method is called to set the payment method types
     *
     * @param paymentTypeList         :arraylist of the payment types available
     * @param selectedDefaultPosition :set the default position of selected payment type
     * @param showDefaultSelection:   true - to show default selection
     * @param isInHDFCPayMode:        true if the app has been opened through the PayZapp by the user.
     */
    public void setPaymentMethods(ArrayList<PaymentType> paymentTypeList, int selectedDefaultPosition, boolean showDefaultSelection, boolean isInHDFCPayMode) {
        setSelectedDefaultPosition(selectedDefaultPosition);
        setPaymentMethods(paymentTypeList, showDefaultSelection, isInHDFCPayMode);
    }

    public void setPaymentMethods(ArrayList<PaymentType> paymentTypeList, boolean showDefaultSelection, boolean isInHDFCPayMode) {
        int i = 0;
        for (final PaymentType paymentType : paymentTypeList) {
            if (isInHDFCPayMode && !paymentType.getValue().equals(Constants.HDFC_POWER_PAY)) {
                mSelectedPaymentMethod = Constants.HDFC_POWER_PAY;
                continue;
            }

            /**
             * initializing the views
             */
            View paymentOptionRow = inflater.inflate(R.layout.payment_option_row, this, false);
            RelativeLayout mPaymentParentRelativelayout = (RelativeLayout) paymentOptionRow.findViewById(R.id.mPaymentParentRelativelayout);
            TextView mPaymentDisplayNameTextView = (TextView) paymentOptionRow.findViewById(R.id.mPaymentDisplayTextView);
            TextView mPaymentOfferTextView = (TextView) paymentOptionRow.findViewById(R.id.mPaymentOfferTextView);
            ImageView mSelectionImageView = (ImageView) paymentOptionRow.findViewById(R.id.mSelectionImageView);

            /**setting the font**/
            mPaymentDisplayNameTextView.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());
            mPaymentOfferTextView.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());

            if (!TextUtils.isEmpty(paymentType.getDisplayName())) {
                mPaymentDisplayNameTextView.setText(paymentType.getDisplayName());
            }
            if (!TextUtils.isEmpty(paymentType.getOfferMsg())) {
                mPaymentOfferTextView.setVisibility(View.VISIBLE);
                mPaymentOfferTextView.setText(paymentType.getOfferMsg());
            } else {
                mPaymentOfferTextView.setVisibility(View.GONE);
            }

            /** setting the default payment method**/
            if ((showDefaultSelection && paymentType.isSelected()) || (selectedDefaultPosition == i)
                    || (!TextUtils.isEmpty(mSelectedPaymentMethod) && paymentType.getValue().equalsIgnoreCase(mSelectedPaymentMethod))) {
                mPaymentParentRelativelayout.setSelected(true);
                mSelectionImageView.setSelected(true);

                previousClickedImageView = mSelectionImageView;
                previousSelectedLayout = mPaymentParentRelativelayout;

                mSelectedPaymentMethod = paymentType.getValue();
                ((OnPaymentOptionSelectionListener) context).onPaymentOptionSelected(mSelectedPaymentMethod);

            } else {
                mPaymentParentRelativelayout.setSelected(false);
                mSelectionImageView.setSelected(false);
            }

            mPaymentParentRelativelayout.setTag(R.id.payment_value, paymentType.getValue());
            mPaymentParentRelativelayout.setOnClickListener(paymentTypeClicked);
            addView(paymentOptionRow);
            i++;
        }
    }

    private void onPaymentTypeSelection(View view) {
        /**
         * checking if previously any payment method is selected and then deselecting it
         */
        if (previousClickedImageView != null) {
            previousClickedImageView.setSelected(false);
            previousSelectedLayout.setSelected(false);
        }
        ImageView imageView = (ImageView) view.findViewById(R.id.mSelectionImageView);
        if (imageView != null) {
            mSelectedPaymentMethod = view.getTag(R.id.payment_value).toString();
            previousClickedImageView = imageView;
            previousSelectedLayout = (RelativeLayout) view;
            /**
             * making the imageview and the layout selected
             */
            imageView.setSelected(true);
            view.setSelected(true);
            if (context instanceof OnPaymentOptionSelectionListener) {
                ((OnPaymentOptionSelectionListener) context).onPaymentOptionSelected(mSelectedPaymentMethod);
            }
        }
    }

    /**
     * interface to implement to get call back when the payment method changes
     */
    public interface OnPaymentOptionSelectionListener {
        void onPaymentOptionSelected(String paymentTypeValue);
    }
}
