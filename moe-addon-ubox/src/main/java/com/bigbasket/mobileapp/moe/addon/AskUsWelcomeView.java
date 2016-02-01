package com.bigbasket.mobileapp.moe.addon;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moengage.addon.ubox.R;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

/**
 * Created by muniraju on 20/01/16.
 */
public class AskUsWelcomeView extends RelativeLayout implements View.OnClickListener {
    private TextView mMessageView;
    private ImageView mButtonView;
    private boolean mExpanded = true;
    private int mMsgWidth;
    private int mMsgHeight;
    private onMsgSeenListener mMsgSeenListener;

    public AskUsWelcomeView(Context context) {
        super(context);
    }

    public AskUsWelcomeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AskUsWelcomeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AskUsWelcomeView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mMessageView = (TextView)findViewById(R.id.message);
        mMessageView.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bkgrnd_msg));
        setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bkgrnd_msg));
        mButtonView = (ImageView)findViewById(R.id.close_icon);
        mButtonView.setOnClickListener(this);
        update();
    }

    private void update() {
        if(mExpanded){
            Picasso.with(getContext()).load(R.drawable.ic_close_black_18dp)
                    .noFade().memoryPolicy(MemoryPolicy.NO_STORE).into(mButtonView);
        } else {
            Picasso.with(getContext()).load(R.drawable.ic_announcement_black_24dp)
                    .noFade().memoryPolicy(MemoryPolicy.NO_STORE).into(mButtonView);
            setBackgroundColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onClick(View v) {
        if(mMsgSeenListener != null){
            mMsgSeenListener.onMsgSeen();
        }
        if(mExpanded){
            //animate collapse
            collapse();
        } else {
            //animate expand
            expand();
        }
        mExpanded = !mExpanded;
        update();

    }

    public void setExpanded(boolean expanded){
        if(expanded){
            expand();
        } else {
            collapse();
        }
        mExpanded = expanded;
        update();
    }

    public void setMsgSeenListener(onMsgSeenListener msgSeenListener) {
        this.mMsgSeenListener = msgSeenListener;
    }

    private void expand() {
        if(mMessageView.getVisibility() == VISIBLE){
            return;
        }
        mMessageView.setVisibility(View.VISIBLE);
        if(mMsgWidth == 0) {
            mMsgWidth = mMessageView.getMeasuredWidth();
            mMsgHeight = mMessageView.getMeasuredHeight();
        }
        if(mMsgWidth > 0 && mMsgHeight > 0) {
            ViewCompat.animate(mMessageView)
                    .translationXBy(-mMsgWidth)
                    .translationYBy(mMsgHeight)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bkgrnd_msg));
                        }
                    });
        } else {
            setBackgroundColor(ContextCompat.getColor(getContext(), R.color.bkgrnd_msg));
        }
    }

    private void collapse() {
        if(mMessageView.getVisibility() == GONE){
            return;
        }
        mMsgWidth = mMessageView.getWidth();
        mMsgHeight = mMessageView.getHeight();
        if(mMsgWidth > 0 && mMsgHeight > 0) {
            ViewCompat.animate(mMessageView).translationXBy(mMsgWidth)
                    .translationYBy(-mMsgHeight)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            mMessageView.setVisibility(View.GONE);
                        }
                    });
        } else {
            mMessageView.setVisibility(View.GONE);
            setBackgroundColor(Color.TRANSPARENT);
        }
    }


    public interface onMsgSeenListener {
        void onMsgSeen();
    }

}
