package com.bigbasket.mobileapp.adapter.specialityshops;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.common.FixedLayoutViewHolder;
import com.bigbasket.mobileapp.interfaces.ActivityAware;
import com.bigbasket.mobileapp.model.specialityshops.SpecialityStore;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;

import java.util.List;

public class StoreListRecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_DATA = 1;
    public static final int VIEW_TYPE_EMPTY = 2;

    private T context;
    private String baseImgUrl;
    private List<SpecialityStore> storeList;

    public StoreListRecyclerAdapter(T context, String baseImgUrl, List<SpecialityStore> storeList) {
        this.context = context;
        this.baseImgUrl = baseImgUrl;
        this.storeList = storeList;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= storeList.size()) return VIEW_TYPE_EMPTY;
        return position == storeList.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = ((ActivityAware) this.context).getCurrentActivity();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        switch (viewType) {
            case VIEW_TYPE_DATA:
                View row = inflater.inflate(R.layout.uiv3_sshops_row, parent, false);
                return new StoreListRowHolder(row, FontHolder.getInstance(context).getFaceRobotoRegular(), FontHolder.getInstance(context).getFaceRobotoLight());
            case VIEW_TYPE_LOADING:
                row = inflater.inflate(R.layout.uiv3_list_loading_footer, parent, false);
                return new FixedLayoutViewHolder(row);
            case VIEW_TYPE_EMPTY:
                row = new View(((ActivityAware) context).getCurrentActivity());
                return new FixedLayoutViewHolder(row);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_DATA) {
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
            LinearLayout layoutDelivery = rowHolder.getLayoutDelivery();
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
                layoutDelivery.setVisibility(View.VISIBLE);
            } else {
                layoutDelivery.setVisibility(View.INVISIBLE);
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
        private LinearLayout layoutDelivery;
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

        public LinearLayout getLayoutDelivery() {
            if (layoutDelivery == null)
                layoutDelivery = (LinearLayout) itemView.findViewById(R.id.layoutDelivery);
            return layoutDelivery;
        }

        @Override
        public void onClick(View v) {
            final int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {

            }
        }
    }
}
