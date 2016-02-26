package com.bigbasket.mobileapp.adapter.communicationhub;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.util.UIUtil;
import com.moe.pushlibrary.models.UnifiedInboxMessage;
import com.moengage.addon.ubox.UBoxManager;
import com.moengage.addon.ubox.UBoxUtils;

import org.json.JSONException;
import org.json.JSONObject;

//import com.moengage.addon.ubox.UboxUtils;

/**
 * Created by muniraju on 14/01/16.
 */
public class AskUsAdapter extends UBoxManager.UboxAdapter<AskUsAdapter.ChatItemViewHolder> {
    private Resources resources;

    public AskUsAdapter(Resources resources) {
        this.resources = resources;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup, LayoutInflater layoutInflater) {
        View view = layoutInflater.inflate(R.layout.chat_item, viewGroup, false);
        ChatItemViewHolder viewHolder = new ChatItemViewHolder();
        viewHolder.txtMessage = (TextView) view.findViewById(R.id.txt_msg);
        viewHolder.txtStatus = (TextView) view.findViewById(R.id.txt_status);
        viewHolder.imgChatMessage = (ImageView) view.findViewById(R.id.img_chat);
        viewHolder.holderView = view.findViewById(R.id.chat_item_holder);
        viewHolder.itemHolder = new UBoxManager.ItemHolder();
        view.setTag(viewHolder.itemHolder);
        view.setTag(R.id.chat_item_view_holder_tag, viewHolder);
        return view;
    }

    @Override
    public void bindData(AskUsAdapter.ChatItemViewHolder viewHolder, Context context, Cursor cursor) {
        UnifiedInboxMessage msg = viewHolder.message;
        if (msg == null) {
            bindDataInternal(cursor, viewHolder);
        }
        JSONObject obj = null;
        String imageUrl = null;


        if (!TextUtils.isEmpty(msg.details)) {
            try {
                obj = new JSONObject(msg.details);
                obj.put("_id", cursor.getColumnIndex("_id"));
                msg.details = obj.toString();
            } catch (JSONException var7) {
                obj = null;
            }
        } else {
            //FIXME: msg.messageType is coming as 1 even for an Image
            imageUrl = msg.serverUri;
            if (TextUtils.isEmpty(imageUrl)) {
                imageUrl = msg.localUri;
            }
        }

        TextView statusView = viewHolder.txtStatus;
        TextView msgView = viewHolder.txtMessage;
        ImageView chatImageView = viewHolder.imgChatMessage;
        if (msg.author != null && msg.author.equals(UnifiedInboxMessage.AUTHOR_USER)) {
            //Outgoing
            ViewGroup.LayoutParams layoutParams = viewHolder.holderView.getLayoutParams();
            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
                ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                viewHolder.holderView.setLayoutParams(layoutParams);
            }
            viewHolder.holderView.setBackgroundResource(R.drawable.bubble_outgoing);
            if (imageUrl == null) {
                if (null == obj) {
                    viewHolder.txtMessage.setText(msg.details);
                } else {
                    String m = UBoxUtils.getAlternateMessageDetails(obj);
                    if (m != null) {
                        viewHolder.txtMessage.setText(m);
                    }
                }
                msgView.setVisibility(View.VISIBLE);
                chatImageView.setVisibility(View.GONE);
                //pass null to cancel the async loading of recyceled view
                UIUtil.displayAsyncImage(chatImageView, null);
            } else {
                msgView.setVisibility(View.GONE);
                chatImageView.setVisibility(View.VISIBLE);
                UIUtil.displayAsyncImage(chatImageView, imageUrl);
            }
            boolean isFailure = false;
            switch (msg.status) {
                case UnifiedInboxMessage.STATUS_SENDING:
                    statusView.setText(R.string.lbl_sending_inprogress);
                    viewHolder.itemHolder.retryRequired = false;
                    break;
                case UnifiedInboxMessage.STATUS_UPLOADING:
                    statusView.setText(R.string.lbl_sending_upload);
                    viewHolder.itemHolder.retryRequired = false;
                    break;
                case UnifiedInboxMessage.STATUS_FAILED_TO_SEND:
                    statusView.setText(R.string.lbl_sending_failed);
                    viewHolder.itemHolder.retryRequired = true;
                    isFailure = true;
                    break;
                case UnifiedInboxMessage.STATUS_UPLOADING_FAILED:
                    statusView.setText(R.string.lbl_upload_failed);
                    viewHolder.itemHolder.retryRequired = true;
                    isFailure = true;
                    break;
                case UnifiedInboxMessage.STATUS_SENT:
                    viewHolder.itemHolder.retryRequired = false;
                default:
                    if (TextUtils.isEmpty(msg.timestamp)) {
                        msg.setTimestamp(msg.gtime);
                    }
                    statusView.setText(msg.timestamp);
                    break;
            }
            if (!isFailure) {
                statusView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                statusView.setTextColor(ContextCompat.getColor(context, R.color.chat_timestamp_color));
            } else {
                statusView.setCompoundDrawablesWithIntrinsicBounds(null, null,
                        ContextCompat.getDrawable(context, R.drawable.ic_warning_red), null);
                statusView.setTextColor(ContextCompat.getColor(context, R.color.red));
            }

        } else {
            //Incoming
            viewHolder.holderView.setBackgroundResource(R.drawable.bubble_incoming);
            ViewGroup.LayoutParams layoutParams = viewHolder.holderView.getLayoutParams();
            if (layoutParams instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
                ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                viewHolder.holderView.setLayoutParams(layoutParams);
            }

            statusView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            statusView.setTextColor(ContextCompat.getColor(context, R.color.chat_timestamp_color));
            if (TextUtils.isEmpty(msg.timestamp)) {
                msg.setTimestamp(msg.gtime);
            }
            statusView.setText(msg.timestamp);
            switch (msg.messageType) {
                case UnifiedInboxMessage.MSG_TYPE_HTML:
                    String html = UBoxUtils.getAlternateMessageDetails(obj);
                    if (!TextUtils.isEmpty(html)) {
                        msgView.setText(Html.fromHtml(html));
                    }
                    msgView.setVisibility(View.VISIBLE);
                    chatImageView.setVisibility(View.GONE);
                    break;
                case UnifiedInboxMessage.MSG_TYPE_PIC:
                    msgView.setVisibility(View.GONE);
                    chatImageView.setVisibility(View.VISIBLE);
                    UIUtil.displayAsyncImage(chatImageView, imageUrl);
                    break;
                case UnifiedInboxMessage.MSG_TYPE_NOTIFICATION:
                    //TODO: update the tag for message as "alert"
                case UnifiedInboxMessage.MSG_TYPE_NOTIFICATION_WITH_COUPON:
                case UnifiedInboxMessage.MSG_TYPE_NOTIFICATION_WITH_IMAGE:
                    //TODO: update the tag for message as "offers"
                case UnifiedInboxMessage.MSG_TYPE_TEXT:
                    String payloadMsg = null;
                    if (obj != null) {
                        payloadMsg = UBoxUtils.getAlternateMessageDetails(obj);
                        if (payloadMsg == null) {
                            payloadMsg = UBoxUtils.getMessage(obj);
                        }
                    }
                    msgView.setText(payloadMsg);
                    msgView.setVisibility(View.VISIBLE);
                    chatImageView.setVisibility(View.GONE);
                    break;
            }
        }
    }

    @Override
    public boolean onItemClick(View view, Context context) {
        return false;
    }

    @Override
    public AskUsAdapter.ChatItemViewHolder getViewHolder(View view) {

        return (AskUsAdapter.ChatItemViewHolder) view.getTag(R.id.chat_item_view_holder_tag);
    }

    public static class ChatItemViewHolder extends UBoxManager.ViewHolder {
        TextView txtMessage;
        TextView txtStatus;
        View holderView;
        ImageView imgChatMessage;
        UBoxManager.ItemHolder itemHolder;
    }
}
