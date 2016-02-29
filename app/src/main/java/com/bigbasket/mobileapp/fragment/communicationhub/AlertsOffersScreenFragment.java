package com.bigbasket.mobileapp.fragment.communicationhub;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.Constants;
import com.bigbasket.mobileapp.util.FontHolder;
import com.bigbasket.mobileapp.util.UIUtil;
import com.crashlytics.android.Crashlytics;
import com.moe.pushlibrary.providers.MoEDataContract;
import com.moengage.addon.ubox.UBoxUtils;
import com.squareup.picasso.Callback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by manu on 12/1/16.
 */

/***********************************
 * IMPORTANT***********************************
 * <p/>
 * According to MOEngage documentation for Communication Hub
 * <p/>
 * if a notification has tag as "offers" it falls in Offers category and is inserted in MESSAGES table with msg_tag as offers
 * any other notification,  if not tagged falls under the General category
 * In the app,all such messages that are not tagged as 'offers' are considered as  Alerts and is displayed in the list
 *******************************************************************************/


public class AlertsOffersScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int ALERTS_LOADER_ID = 4000;
    private static final int OFFERS_LOADER_ID = 4001;
    private RecyclerView offersRecycleView;
    private AlertsOffersAdapter alertsOffersAdapter;
    private int mLoaderId;
    private TextView txtEmptyMessage;

    public AlertsOffersScreenFragment() {
    }

    /**
     * creating the instance of the fragment
     * setting the targetFragment to same fragment to get the context of the fragment
     */
    public static AlertsOffersScreenFragment newInstance(Bundle bundle) {
        AlertsOffersScreenFragment fragment = new AlertsOffersScreenFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.communication_offers_fragment, container, false);
        offersRecycleView = (RecyclerView) rootView.findViewById(R.id.offers_recycleview);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        offersRecycleView.setLayoutManager(linearLayoutManager);
        offersRecycleView.setHasFixedSize(false);
        txtEmptyMessage = (TextView) rootView.findViewById(R.id.txt_emptymessage);
        //getting the filter tag from the bundle
        String filterTag = null;
        if (null != getArguments() && getArguments().containsKey(Constants.COMMUNICATION_HUB_FILTER)) {
            filterTag = getArguments().getString(Constants.COMMUNICATION_HUB_FILTER);
        }
        if (!TextUtils.isEmpty(filterTag)) {
            if (filterTag.equalsIgnoreCase(Constants.COMMUNICATION_HUB_OFFER)) {
                txtEmptyMessage.setText(getString(R.string.emptyoffers_message));
            } else {
                txtEmptyMessage.setText(getString(R.string.emptyalert_message));
            }
        }

        mLoaderId = ALERTS_LOADER_ID;
        if (!TextUtils.isEmpty(filterTag) && filterTag.equals(Constants.COMMUNICATION_HUB_OFFER)) {
            mLoaderId = OFFERS_LOADER_ID;
        }
        //creating instance of AlertsOfferAdapter for the recycleview
        alertsOffersAdapter = new AlertsOffersAdapter(getContext(), mLoaderId);
        offersRecycleView.setAdapter(alertsOffersAdapter);
        getLoaderManager().initLoader(mLoaderId, getArguments(), this);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        getLoaderManager().destroyLoader(mLoaderId);
    }

    private static JSONObject getBBOfferObj(String payload) {
        JSONObject obj = null;
        try {
            obj = new JSONObject(payload);
            if (obj.has(Constants.COMMUNICATION_HUB_BB_KEY)) {
                //getting the value of the bb key and removing the extra '\', result is a json string
                String bbValue = obj.getString(Constants.COMMUNICATION_HUB_BB_KEY).replace("\\", "");
                //converting the json string to JSOnObject
                JSONObject bbOfferobj = new JSONObject(bbValue);
                return bbOfferobj;
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

    private boolean isExpired(String payload) {
        JSONObject bbOfferobj = getBBOfferObj(payload);
        if (bbOfferobj != null && bbOfferobj.has("expiry")) {
            try {
                long expiryDate = bbOfferobj.getLong("expiry");
                if (System.currentTimeMillis() > expiryDate) {
                    return true;
                } else {
                    return false;
                }
            } catch (JSONException e) {
                Crashlytics.logException(e);
            }

        }

        return false;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection;
        String[] selectionArg;
        String filterTag = args != null ? args.getString(Constants.COMMUNICATION_HUB_FILTER) : null;
        if (!TextUtils.isEmpty(filterTag) &&
                filterTag.equalsIgnoreCase(Constants.COMMUNICATION_HUB_OFFER)) {
            selection = MoEDataContract.MessageEntity.MSG_TAG + " = ? ";
            selectionArg = new String[]{Constants.COMMUNICATION_HUB_OFFER};
        } else {
            selection = MoEDataContract.MessageEntity.MSG_TAG + " = ? OR "
                    + MoEDataContract.MessageEntity.MSG_TAG + " = ?";
            selectionArg = new String[]{Constants.COMMUNICATION_HUB_ALERT,
                    Constants.COMMUNICATION_HUB_GENERAL};
        }

        return new CursorLoader(getContext(), MoEDataContract.MessageEntity.getContentUri(getContext()),
                MoEDataContract.MessageEntity.PROJECTION, selection, selectionArg,
                MoEDataContract.MessageEntity.DEFAULT_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor dataCursor) {
        //getting values from the cursor
        /**
         * in MESSAGE table, msg column a json string is saved containing all the values regarding the message like title, imageurl etc.
         */
        if (dataCursor != null && dataCursor.moveToFirst()) {
            offersRecycleView.setVisibility(View.VISIBLE);
            txtEmptyMessage.setVisibility(View.GONE);
            ArrayList<String> expiredIdList = new ArrayList<>(dataCursor.getCount());
            do {
                //adding the string into the arraylist
                String msg_details = dataCursor.getString(MoEDataContract.MessageEntity.COLUMN_INDEX_MSG_DETAILS);
                if (isExpired(msg_details)) {
                    expiredIdList.add(dataCursor.getString(MoEDataContract.MessageEntity.COLUMN_INDEX_ID));
                }

            } while (dataCursor.moveToNext());
            if (!expiredIdList.isEmpty()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    AsyncTask.THREAD_POOL_EXECUTOR.execute(new CleanupRunnable(getContext(),
                            expiredIdList));
                } else {
                    new Thread(new CleanupRunnable(getContext(),
                            expiredIdList)).start();
                }
            }
        } else {
            offersRecycleView.setVisibility(View.GONE);
            txtEmptyMessage.setVisibility(View.VISIBLE);
        }

        alertsOffersAdapter.changeCursor(dataCursor);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        alertsOffersAdapter.changeCursor(null);
    }

    //creating a view holder for the  recycleAdapter
    private static class OffersHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView message;
        public String code;
        public TextView timeStamp;
        public View alertLayout;
        public View imageLayout;
        public ImageView offerImageView;
        public View progressBar;
        private long id;
        private String details;

        public OffersHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.txtoffertitle);
            alertLayout = itemView.findViewById(R.id.alert_layout);
            FontHolder fontHolder = FontHolder.getInstance(itemView.getContext());
            title = (TextView) itemView.findViewById(R.id.title);
            title.setTypeface(fontHolder.getFaceRobotoMedium());
            message = (TextView) itemView.findViewById(R.id.message);
            message.setTypeface(fontHolder.getFaceRobotoRegular());
            timeStamp = (TextView) itemView.findViewById(R.id.date);
            timeStamp.setTypeface(fontHolder.getFaceRobotoLight());
            offerImageView = (ImageView) itemView.findViewById(R.id.offer_imageview);
            imageLayout = itemView.findViewById(R.id.image_layout);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public synchronized long getId() {
            return id;
        }

        public synchronized void setId(long id) {
            this.id = id;
        }

        public synchronized String getDetails() {
            return details;
        }

        public synchronized void setDetails(String details) {
            this.details = details;
        }
    }

    private static class AlertsOffersAdapter extends RecyclerView.Adapter<OffersHolder> implements View.OnClickListener {

        private Cursor mCursor;
        private Context mContext;
        private int mLoaderId;

        public AlertsOffersAdapter(Context mContext, int mLoaderId) {
            this.mContext = mContext;
            this.mLoaderId = mLoaderId;
        }

        public void changeCursor(Cursor cursor) {
            Cursor oldCursor = mCursor;

            if (oldCursor != cursor) {
                if (oldCursor != null && !oldCursor.isClosed()) {
                    oldCursor.close();
                }
                mCursor = cursor;
                notifyDataSetChanged();
            }
        }

        @Override
        public OffersHolder onCreateViewHolder(ViewGroup parent, int pos) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.inbox_row, parent, false);
            view.setOnClickListener(this);
            return new OffersHolder(view);
        }

        @Override
        public void onBindViewHolder(OffersHolder holder, final int pos) {

            if (mCursor.moveToPosition(pos)) {
                String msgDetails = mCursor.getString(
                        MoEDataContract.MessageEntity.COLUMN_INDEX_MSG_DETAILS);
                holder.itemView.setTag(R.id.chat_item_view_holder_tag, msgDetails);

                String campaignImageUrl = null;
                if (mLoaderId == OFFERS_LOADER_ID) {
                    campaignImageUrl = getCampaignImageUrl(msgDetails);
                }
                holder.setId(mCursor.getLong(MoEDataContract.MessageEntity.COLUMN_INDEX_ID));
                holder.setDetails(msgDetails);
                JSONObject msgObj = null;
                try {
                    msgObj = new JSONObject(msgDetails);
                } catch (JSONException e) {
                    //Ignore
                }
                holder.timeStamp.setVisibility(View.VISIBLE);
                holder.alertLayout.setVisibility(View.VISIBLE);
                holder.imageLayout.setVisibility(View.VISIBLE);
                if (msgObj != null) {
                    holder.title.setText(UBoxUtils.getTitle(msgObj));
                    holder.message.setText(UBoxUtils.getMessage(msgObj));
                    holder.timeStamp.setText(UBoxUtils.getTimeStamp(msgObj,
                            "MMMM d, yyyy \'at\' h:mm a"));
                } else {
                    holder.alertLayout.setVisibility(View.GONE);
                    return;
                }

                if (!TextUtils.isEmpty(campaignImageUrl)) {
                    holder.offerImageView.setVisibility(View.GONE);
                    holder.progressBar.setVisibility(View.VISIBLE);
                    displayOfferImage(campaignImageUrl, holder);
                    holder.timeStamp.setVisibility(View.GONE);
                } else {
                    UIUtil.displayAsyncImage(holder.offerImageView, null);
                    holder.imageLayout.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mCursor != null ? mCursor.getCount() : 0;
        }

        private String constructOfferImageUrl(String source) {
            if (source != null) {
                int index = source.lastIndexOf('/');
                if (index > 0 && index < source.length()) {
                    return source.substring(0, index) + "/"
                            + UIUtil.getScreenDensity(mContext) + source.substring(index);
                }
            }
            return source;

        }

        /**
         * to extract the image url and inject the device type to get proper image
         * eg : "bb":"{\"image\": \"http:\/\/testaws.bigbasket.com\/media\/uploads\/mobile_campaign_images\/test\/images\/6473c65cbf5e11e5b701a0999b1565_540_264.png\", \"type\": \"offers\"}"
         *
         * @param payload: msg column string (json format)
         * @return image url
         */
        private String getCampaignImageUrl(String payload) {
            JSONObject bbOfferobj = getBBOfferObj(payload);
            if (bbOfferobj != null) {
                try {
                    //getting the value of 'image' from the JSONObject and the injecting the screen density before the filename of the image.
                    return constructOfferImageUrl(bbOfferobj.getString("image"));
                } catch (JSONException e) {
                    Crashlytics.logException(e);
                }
            }
            return null;
        }

        /**
         * displayOfferImage(msg_details,offersHolder): used to calculate the imageview size to display the offer image
         *
         * @param imageUrl:     Image Url
         * @param offersHolder: holder in which the image has to be displayed
         */
        private boolean displayOfferImage(String imageUrl, OffersHolder offersHolder) {
            //getting the image url
            if (imageUrl == null) {
                return false;
            }
            Resources res = mContext.getResources();

            /**
             * get the width of the screen using the displaymetrics
             * subtracting the padding from the screen width and
             * then multiplying it with the co-efficient of dpi to get actual width according to the screen density
             */
            ViewGroup.LayoutParams layoutParams = offersHolder.offerImageView.getLayoutParams();
            int margin = 0;
            if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
                margin = MarginLayoutParamsCompat.getMarginStart((ViewGroup.MarginLayoutParams) layoutParams)
                        + MarginLayoutParamsCompat.getMarginEnd((ViewGroup.MarginLayoutParams) layoutParams);
            }
            int widthScreen = res.getDisplayMetrics().widthPixels - margin;
            //getting the name of the image by getting the substring with last occurrence of /
            int index = imageUrl.lastIndexOf('/');
            String fileName;
            int widthImage = 0;
            int heightImage = 0;
            if (index > 0 && index < imageUrl.length()) {
                fileName = imageUrl.substring(index + 1);
                if (!TextUtils.isEmpty(fileName)) {
                    index = fileName.lastIndexOf('.');
                    if (index > 0 && index < fileName.length()) {
                        //getting the name of the image removing the extension of the image like .jpg,.png etc
                        String fileNameWithoutExtn = fileName.substring(0, index);
                        /**
                         * the name of the image contains the actual image size
                         * eg : a05413fabf6f11e5b73aa0999b1565_540_264.png
                         * splitting the name of image with '_' and the getting the dimensions
                         * array[1]: contains  width of image
                         * array[2]: contains height of image
                         */
                        String[] imageSizeArray = fileNameWithoutExtn.split("_");
                        if (imageSizeArray.length >= 3) {
                            try {
                                heightImage = Integer.parseInt(imageSizeArray[imageSizeArray.length - 1]);
                                widthImage = Integer.parseInt(imageSizeArray[imageSizeArray.length - 2]);
                            } catch (NumberFormatException ex) {
                                //Ignore
                            }
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
            /**
             * getting the height of the imageview for the given device.
             *
             * aspect_ratio_of_image= widthOfImage/heightOfImage
             * to maintain same aspect ratio on the device.
             *
             * get the width of the screen and the calculate the height for ImageView using the aspect ratio
             *
             * height for imageview or targetImageHeight= width_of_screen/aspect_ratio_of_image
             */
            if (widthImage != 0 && heightImage != 0 && widthImage > widthScreen) {
                widthImage = widthScreen;
                heightImage = UIUtil.adjustHeightForScreenWidth(widthImage, heightImage, widthScreen);
            }
            UIUtil.displayAsyncImage(offersHolder.offerImageView, imageUrl, false, 0,
                    widthImage, heightImage, true, UIUtil.CENTER_INSIDE,
                    new OfferImageLoaderCallback(offersHolder, offersHolder.getId()));
            return true;
        }

        @Override
        public void onClick(View v) {
            Object object = v.getTag(R.id.chat_item_view_holder_tag);
            if (object == null) {
                return;
            }
            String msgDetails = (String) object;
            try {
                Intent intent = UBoxUtils.getRedirectionIntent(msgDetails, v.getContext());
                if (intent != null) {
                    mContext.startActivity(intent);
                }
            } catch (ActivityNotFoundException | ArrayIndexOutOfBoundsException ex) {
                Crashlytics.logException(ex);
            }
        }
    }

    private static class OfferImageLoaderCallback extends Callback.EmptyCallback {

        private OffersHolder holder;
        private long id;

        public OfferImageLoaderCallback(OffersHolder offerViewHolder, long id) {
            this.holder = offerViewHolder;
            this.id = id;
        }

        @Override
        public void onSuccess() {
            super.onSuccess();
            if (holder.getId() == id) {
                holder.offerImageView.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
            }
        }

        @Override
        public void onError() {
            if (holder.getId() == id) {
                //TODO: Synchronize the below code as well
                holder.alertLayout.setVisibility(View.VISIBLE);
                holder.offerImageView.setVisibility(View.GONE);
            } else {
                //View might have recycled, Ignore
            }
        }
    }


    private static class CleanupRunnable implements Runnable {

        private final ArrayList<String> expiredIdList;
        private Context mContext;

        public CleanupRunnable(Context mContext, ArrayList<String> expiredIdList) {
            this.mContext = mContext;
            this.expiredIdList = expiredIdList;
        }

        @Override
        public void run() {
            if (mContext != null && expiredIdList != null && !expiredIdList.isEmpty()) {
                Uri uri = MoEDataContract.MessageEntity.getContentUri(mContext);
                StringBuilder selectionBuilder = new StringBuilder(MoEDataContract.MessageEntity._ID);
                selectionBuilder.append(" IN (");
                boolean isFirst = true;
                for (String p : expiredIdList) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        selectionBuilder.append(", ");
                    }
                    selectionBuilder.append('?');
                }
                selectionBuilder.append(')');

                mContext.getContentResolver().delete(uri, selectionBuilder.toString(),
                        expiredIdList.toArray(new String[expiredIdList.size()]));
                //Fixme: Delete is not triggering data change, loader is not reloaded
                mContext.getContentResolver().notifyChange(
                        MoEDataContract.MessageEntity.getContentUri(mContext), null);
            }
        }
    }

}
