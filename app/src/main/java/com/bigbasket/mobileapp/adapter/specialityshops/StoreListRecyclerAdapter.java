package com.bigbasket.mobileapp.adapter.specialityshops;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.handler.click.OnSectionItemClickListener;
import com.bigbasket.mobileapp.interfaces.AnalyticsNavigationContextAware;
import com.bigbasket.mobileapp.interfaces.AppOperationAware;
import com.bigbasket.mobileapp.interfaces.TrackingAware;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityStore;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.List;

public class StoreListRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private T context;
    private String baseImgUrl;
    private List<SpecialityStore> storeList;

    public StoreListRecyclerAdapter(T context, String baseImgUrl, List<SpecialityStore> storeList) {
        this.context = context;
        this.baseImgUrl = baseImgUrl;
        this.storeList = storeList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = ((AppOperationAware) this.context).getCurrentActivity();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.uiv3_speciality_shops_row, parent, false);
        return new StoreListRowHolder(row, FontHolder.getInstance(context).getFaceRobotoRegular(), FontHolder.getInstance(context).getFaceRobotoLight());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        StoreListRowHolder rowHolder = (StoreListRowHolder) holder;

        SpecialityStore store = storeList.get(position);
        String storeName = store.getStoreName();
        String storeLoc = store.getStoreLocation();
        String storeDelTime = store.getStoreDeliveryTime();
        String storeOpTime = store.getStoreTimings();
        String strImgStore = store.getStoreImg();

        TextView txtStoreName = rowHolder.getTxtStoreName();
        TextView txtStoreLoc = rowHolder.getTxtStoreLoc();
        TextView txtStoreDeliveryTime = rowHolder.getTxtStoreDeliveryTime();
        TextView txtStoreTimings = rowHolder.getTxtStoreTimings();
        ImageView imgStore = rowHolder.getImgStoreImg();

        if (!TextUtils.isEmpty(storeName)) {
            txtStoreName.setText(storeName);
            txtStoreName.setVisibility(View.VISIBLE);
        } else {
            txtStoreName.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(storeLoc)) {
            txtStoreLoc.setText(storeLoc);
            txtStoreLoc.setVisibility(View.VISIBLE);
        } else {
            txtStoreLoc.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(storeDelTime)) {
            txtStoreDeliveryTime.setText(storeDelTime);
            txtStoreDeliveryTime.setVisibility(View.VISIBLE);
            txtStoreDeliveryTime.setVisibility(View.VISIBLE);
        } else {
            txtStoreDeliveryTime.setVisibility(View.INVISIBLE);
            txtStoreDeliveryTime.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(storeOpTime)) {
            txtStoreTimings.setText(storeOpTime);
            txtStoreTimings.setVisibility(View.VISIBLE);
        } else {
            txtStoreTimings.setVisibility(View.INVISIBLE);
        }

        if (!TextUtils.isEmpty(strImgStore)) {
            UIUtil.displayProductImage(baseImgUrl, strImgStore, imgStore);
        }
    }

    @Override
    public int getItemCount() {
        return storeList.size();
    }

    private class StoreListRowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView storeImg;
        private TextView txtStoreName;
        private TextView txtStoreLoc;
        private TextView txtDeliveryTime;
        private TextView txtStoreTimings;
        private Typeface typefaceRobotoRegular, typefaceRobotoLight;

        private StoreListRowHolder(View itemView, Typeface typefaceRobotoRegular, Typeface typefaceRobotoLight) {
            super(itemView);
            this.typefaceRobotoRegular = typefaceRobotoRegular;
            this.typefaceRobotoLight = typefaceRobotoLight;
            itemView.setOnClickListener(this);
        }

        public TextView getTxtStoreName() {
            if (txtStoreName == null) {
                txtStoreName = (TextView) itemView.findViewById(R.id.txtStoreName);
                txtStoreName.setTypeface(typefaceRobotoRegular);
            }
            return txtStoreName;
        }

        public TextView getTxtStoreLoc() {
            if (txtStoreLoc == null) {
                txtStoreLoc = (TextView) itemView.findViewById(R.id.txtStoreLoc);
                txtStoreLoc.setTypeface(typefaceRobotoLight);
            }
            return txtStoreLoc;
        }

        public TextView getTxtStoreDeliveryTime() {
            if (txtDeliveryTime == null) {
                txtDeliveryTime = (TextView) itemView.findViewById(R.id.txtDeliveryTime);
                txtDeliveryTime.setTypeface(typefaceRobotoLight);
            }
            return txtDeliveryTime;
        }

        public TextView getTxtStoreTimings() {
            if (txtStoreTimings == null) {
                txtStoreTimings = (TextView) itemView.findViewById(R.id.txtTimings);
                txtStoreTimings.setTypeface(typefaceRobotoLight);
            }
            return txtStoreTimings;
        }

        public ImageView getImgStoreImg() {
            if (storeImg == null)
                storeImg = (ImageView) itemView.findViewById(R.id.imgStore);
            return storeImg;
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                if (context instanceof AnalyticsNavigationContextAware) {
                    ((AnalyticsNavigationContextAware) context).setCurrentScreenName(TrackingAware.SPECIALITYSHOPS +
                            storeList.get(position).getStoreName());
                }
                new OnSectionItemClickListener<>((AppOperationAware)context)
                        .handleDestinationClick(storeList.get(position).getDestinationInfo());
            }
        }
    }
}
