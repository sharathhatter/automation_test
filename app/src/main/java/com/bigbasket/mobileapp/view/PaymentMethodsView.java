package com.bigbasket.mobileapp.view;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.model.order.PaymentType;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;

import java.util.ArrayList;

/**
 * Created by manu on 20/11/15.
 */
public class PaymentMethodsView extends LinearLayout {
    private Context context;
    private LayoutInflater inflater;
    private ImageView previousClickedImageView;
    private RelativeLayout previousSelectedLayout;
    private String mSelectedPaymentMethod;
    private OnClickListener paymentTypeClicked;
    private int selectedDefaultPosition;

    public PaymentMethodsView(Context context) {
        super(context);
        this.context = context;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.paymentTypeClicked = new OnClickListener() {
            @Override
            public void onClick(View view) {
                onPaymentTypeSelection(view);
            }
        };
        init();
    }

    public void setSelectedPaymentMethod(String mSelectedPaymentMethod) {
        this.mSelectedPaymentMethod = mSelectedPaymentMethod;
    }

    public void setSelectedDefaultPosition(int selectedDefaultPosition) {
        this.selectedDefaultPosition = selectedDefaultPosition;
    }

    private void init() {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        setOrientation(VERTICAL);
        layoutParams.setMargins(0, 0, 0, 0);
        setLayoutParams(layoutParams);
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
            LinearLayout linearLayout = new LinearLayout(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            setOrientation(VERTICAL);
            layoutParams.setMargins(0, 10, 0, 10);
            linearLayout.setLayoutParams(layoutParams);

            /**
             * initializing the views
             */
            View paymentOptionRow = inflater.inflate(R.layout.payment_option_row, null);
            RelativeLayout mPaymentParentRelativelayout = (RelativeLayout) paymentOptionRow.findViewById(R.id.mPaymentParentRelativelayout);
            TextView mPaymentDisplayNameTextView = (TextView) paymentOptionRow.findViewById(R.id.mPaymentDisplayTextView);
            TextView mPaymentOfferTextView = (TextView) paymentOptionRow.findViewById(R.id.mPaymentOfferTextView);
            ImageView mSelectionImageView = (ImageView) paymentOptionRow.findViewById(R.id.mSelectionImageView);

            /**setting the font**/
            mPaymentDisplayNameTextView.setTypeface(FontHolder.getInstance(context).getFaceRobotoRegular());
            mPaymentOfferTextView.setTypeface(FontHolder.getInstance(context).getFaceRobotoLight());

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
            if (paymentType.isSelected() || (showDefaultSelection && selectedDefaultPosition == i)
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
            linearLayout.addView(paymentOptionRow);
            addView(linearLayout);
            i++;
        }
    }

    /**
     * saving the instance state
     * @return saved state
     */
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState, mSelectedPaymentMethod);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        setSelectedPaymentMethod(savedState.getSelectedPaymentMethodSavedState());
    }

    /** overriding to ensure children don't save and restore their state as well.**/
    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        super.dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        super.dispatchThawSelfOnly(container);
    }

    private void onPaymentTypeSelection(View view) {
        /**
         * checking if previously any payment method is selected and then deselecting it
         */
        if (previousClickedImageView != null) {
            previousClickedImageView.setSelected(false);
            previousSelectedLayout.setSelected(false);
        }
        for (int i = 0; i < ((ViewGroup) view).getChildCount(); ++i) {
            View nextChild = ((ViewGroup) view).getChildAt(i);
            if (nextChild instanceof ImageView) {
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
                    ((OnPaymentOptionSelectionListener) context).onPaymentOptionSelected(mSelectedPaymentMethod);
                    break;
                }
            }
        }

    }

    /**
     * interface to implement to get call back when the payment method changes
     */
    public interface OnPaymentOptionSelectionListener {
        void onPaymentOptionSelected(String paymentTypeValue);
    }

    protected static class SavedState extends BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final String mSelectedPaymentMethodSavedState;

        private SavedState(Parcelable superState, String mSelectedPaymentMethodSavedState) {
            super(superState);
            this.mSelectedPaymentMethodSavedState = mSelectedPaymentMethodSavedState;
        }

        private SavedState(Parcel in) {
            super(in);
            mSelectedPaymentMethodSavedState = in.readString();
        }

        public String getSelectedPaymentMethodSavedState() {
            return mSelectedPaymentMethodSavedState;
        }

        @Override
        public void writeToParcel(Parcel destination, int flags) {
            super.writeToParcel(destination, flags);
            destination.writeString(mSelectedPaymentMethodSavedState);
        }
    }

}
