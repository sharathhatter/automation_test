package com.bigbasket.mobileapp.adapter.order;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.apiservice.BigBasketApiAdapter;
import com.bigbasket.mobileapp.apiservice.BigBasketApiService;
import com.bigbasket.mobileapp.apiservice.models.response.ApiResponse;
import com.bigbasket.mobileapp.apiservice.models.response.PrescriptionImageUrls;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.interfaces.CancelableAware;
import com.bigbasket.mobileapp.interfaces.HandlerAware;
import com.bigbasket.mobileapp.interfaces.ProgressIndicationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.order.SavedPrescription;
import com.bigbasket.mobileapp.task.COReserveQuantityCheckTask;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.DataUtil;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class PrescriptionListAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private T ctx;
    private LayoutInflater inflater;
    private ArrayList<SavedPrescription> savedPrescriptionArrayList;
    private Typeface faceRobotoRegular;
    private LinearLayout visibleChoosePrescriptionAndViewPrescriptionImages;

    public PrescriptionListAdapter(T ctx, ArrayList<SavedPrescription> savedPrescriptionArrayList,
                                   Typeface faceRobotoRegular) {
        this.ctx = ctx;
        this.savedPrescriptionArrayList = savedPrescriptionArrayList;
        this.faceRobotoRegular = faceRobotoRegular;
        this.inflater = (LayoutInflater) ((ActivityAware) ctx).getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = (LayoutInflater) ((ActivityAware) ctx).getCurrentActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View base = inflater.inflate(R.layout.uiv3_prescription_list_row, viewGroup, false);
        return new ViewHolder(base);
    }

    @Override
    public int getItemCount() {
        return this.savedPrescriptionArrayList != null ? savedPrescriptionArrayList.size() : 0;
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder1, int position) {
        ViewHolder viewHolder = (ViewHolder) viewHolder1;
        final SavedPrescription savedPrescription = savedPrescriptionArrayList.get(position);
        TextView dateCreated = viewHolder.getDateCreated();
        TextView patientName = viewHolder.getPatientName();
        TextView doctorName = viewHolder.getDoctorName();

        LinearLayout layoutListView = viewHolder.getLayoutListView();
        LinearLayout layoutViewPrescriptionImageAndChoosePrescription = viewHolder.getLayoutViewPrescriptionImageAndChoosePrescription();
        layoutListView.setTag(layoutViewPrescriptionImageAndChoosePrescription);
        layoutListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onListItemClick(view);
            }
        });

        final TextView txtViewPrescriptionImages = viewHolder.getTxtViewPrescriptionImages();
        txtViewPrescriptionImages.setTag(savedPrescription.getPharmaPrescriptionId());
        txtViewPrescriptionImages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFrameOnDialogClose();
                getImageUrls(String.valueOf(txtViewPrescriptionImages.getTag()));
            }
        });
        TextView txtChoosePrescription = viewHolder.getTxtChoosePrescription();
        txtChoosePrescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefer = PreferenceManager.getDefaultSharedPreferences(((ActivityAware) ctx).getCurrentActivity());
                SharedPreferences.Editor editor = prefer.edit();
                editor.putString(Constants.PHARMA_PRESCRIPTION_ID,
                        String.valueOf(savedPrescription.getPharmaPrescriptionId()));
                editor.commit();
                Toast.makeText(((ActivityAware) ctx).getCurrentActivity(),
                        ((ActivityAware) ctx).getCurrentActivity().getResources().getString(R.string.prescriptionIDSaved), Toast.LENGTH_SHORT).show();
                ((ActivityAware) ctx).getCurrentActivity().trackEvent(TrackingAware.PRE_CHECKOUT_PHARMA_PRESCRIPTION_CHOSEN, null);
                hideFrameOnDialogClose();
                new COReserveQuantityCheckTask<>(((ActivityAware) ctx).getCurrentActivity(),
                        String.valueOf(savedPrescription.getPharmaPrescriptionId())).startTask();
            }
        });
        TextView prescriptionName = viewHolder.getPrescriptionName();

        dateCreated.setText(savedPrescription.getDateCreated());
        patientName.setText(savedPrescription.getPatientName());
        doctorName.setText(savedPrescription.getDoctorName());
        prescriptionName.setText(savedPrescription.getPrescriptionName());

    }

    private void hideFrameOnDialogClose() {
        visibleChoosePrescriptionAndViewPrescriptionImages.setVisibility(View.GONE);
    }

    private void onListItemClick(View view) {
        if (visibleChoosePrescriptionAndViewPrescriptionImages != null) {
            if (visibleChoosePrescriptionAndViewPrescriptionImages == (LinearLayout) view.getTag() &&
                    visibleChoosePrescriptionAndViewPrescriptionImages.getVisibility() == View.VISIBLE) {
                visibleChoosePrescriptionAndViewPrescriptionImages.setVisibility(View.GONE);
            } else {
                if (visibleChoosePrescriptionAndViewPrescriptionImages.getVisibility() == View.VISIBLE) {
                    // hide current view
                    visibleChoosePrescriptionAndViewPrescriptionImages.setVisibility(View.GONE);
                }
                // show new view
                visibleChoosePrescriptionAndViewPrescriptionImages = (LinearLayout) view.getTag();
                visibleChoosePrescriptionAndViewPrescriptionImages.setVisibility(View.VISIBLE);

            }
        } else {
            visibleChoosePrescriptionAndViewPrescriptionImages = (LinearLayout) view.getTag();
            visibleChoosePrescriptionAndViewPrescriptionImages.setVisibility(View.VISIBLE);
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView dateCreated;
        private TextView patientName;
        private TextView doctorName;
        private TextView prescriptionName;
        public View base;
        private LinearLayout layoutViewPrescriptionImageAndChoosePrescription;
        private TextView txtViewPrescriptionImages;
        private TextView txtChoosePrescription;
        private LinearLayout layoutListView;

        public ViewHolder(View base) {
            super(base);
            this.base = base;
        }


        public LinearLayout getLayoutListView() {
            if (layoutListView == null) {
                layoutListView = (LinearLayout) base.findViewById(R.id.layoutListView);
            }
            return layoutListView;
        }

        public TextView getTxtViewPrescriptionImages() {
            if (txtViewPrescriptionImages == null) {
                txtViewPrescriptionImages = (TextView) base.findViewById(R.id.txtViewPrescriptionImages);
            }
            return txtViewPrescriptionImages;
        }

        public TextView getTxtChoosePrescription() {
            if (txtChoosePrescription == null) {
                txtChoosePrescription = (TextView) base.findViewById(R.id.txtChoosePrescription);
            }
            return txtChoosePrescription;
        }

        public LinearLayout getLayoutViewPrescriptionImageAndChoosePrescription() {
            if (layoutViewPrescriptionImageAndChoosePrescription == null)
                layoutViewPrescriptionImageAndChoosePrescription = (LinearLayout) base.findViewById(R.id.layoutViewPrescriptionImageAndChoosePrescription);
            return layoutViewPrescriptionImageAndChoosePrescription;
        }

        public TextView getDateCreated() {
            if (dateCreated == null) {
                dateCreated = (TextView) base.findViewById(R.id.txtDateCreated);
                dateCreated.setTypeface(faceRobotoRegular);
            }
            return dateCreated;
        }

        public TextView getPatientName() {
            if (patientName == null) {
                patientName = (TextView) base.findViewById(R.id.txtPatientName);
                patientName.setTypeface(faceRobotoRegular);
            }
            return patientName;
        }

        public TextView getDoctorName() {
            if (doctorName == null) {
                doctorName = (TextView) base.findViewById(R.id.txtDoctorName);
                doctorName.setTypeface(faceRobotoRegular);
            }
            return doctorName;
        }

        public TextView getPrescriptionName() {
            if (prescriptionName == null) {
                prescriptionName = (TextView) base.findViewById(R.id.txtPrescriptionName);
                prescriptionName.setTypeface(faceRobotoRegular);
            }
            return prescriptionName;
        }
    }

    private void getImageUrls(final String pharmaPrescriptionId) {
        if (!DataUtil.isInternetAvailable(((ActivityAware) ctx).getCurrentActivity()))
            ((ActivityAware) ctx).getCurrentActivity().getHandler().sendOfflineError();
        BigBasketApiService bigBasketApiService = BigBasketApiAdapter.getApiService(((ActivityAware) ctx).getCurrentActivity());
        ((ActivityAware) ctx).getCurrentActivity().showProgressDialog(((ActivityAware) ctx).getCurrentActivity().getString(R.string.please_wait));
        bigBasketApiService.getPrescriptionImageUrls(pharmaPrescriptionId, new Callback<ApiResponse<PrescriptionImageUrls>>() {
            @Override
            public void success(ApiResponse<PrescriptionImageUrls> imageUrlsCallback, Response response) {
                if (((CancelableAware) ctx).isSuspended()) return;
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                if (imageUrlsCallback.status == 0) {
                    ArrayList<String> imageUrls = imageUrlsCallback.apiResponseContent.arrayListPrescriptionImages;
                    if (imageUrls != null && imageUrls.size() > 0) {
                        ArrayList<Object> arrayListImgUrls = new ArrayList<>();
                        arrayListImgUrls.addAll(imageUrls);
                        showPrescriptionImageDialog(arrayListImgUrls);
                    } else {
                        (((ActivityAware) ctx)).getCurrentActivity().showAlertDialog(null, ((ActivityAware) ctx).getCurrentActivity().getString(R.string.imageUploading));
                    }
                } else {
                    ((ActivityAware) ctx).getCurrentActivity().getHandler().sendEmptyMessage(imageUrlsCallback.status,
                            imageUrlsCallback.message);
                }
            }

            @Override
            public void failure(RetrofitError error) {
                if (((CancelableAware) ctx).isSuspended()) return;
                try {
                    ((ProgressIndicationAware) ctx).hideProgressDialog();
                } catch (IllegalArgumentException e) {
                    return;
                }
                ((HandlerAware) ctx).getHandler().handleRetrofitError(error);
            }
        });
    }


    private void showPrescriptionImageDialog(ArrayList<Object> uploadImageList) {
        final Dialog prescriptionImageDialog = new Dialog(((ActivityAware) ctx).getCurrentActivity());
        prescriptionImageDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        prescriptionImageDialog.setCanceledOnTouchOutside(true);
        ListView listView = new ListView(((ActivityAware) ctx).getCurrentActivity());
        listView.setDividerHeight(0);
        listView.setDivider(null);
        prescriptionImageDialog.setContentView(listView);

        Rect displayRectangle = new Rect();
        ((ActivityAware) ctx).getCurrentActivity().getWindow().getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        prescriptionImageDialog.getWindow().setLayout(displayRectangle.width() - 20,
                (int) (displayRectangle.height() * 0.7f));

        MultipleImagesPrescriptionAdapter multipleImagesPrescriptionAdapter = new MultipleImagesPrescriptionAdapter<>(((ActivityAware) ctx).getCurrentActivity(),
                uploadImageList);
        listView.setAdapter(multipleImagesPrescriptionAdapter);
        prescriptionImageDialog.show();

    }

}