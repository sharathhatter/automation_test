package com.bigbasket.mobileapp.util;

import android.os.Build;
import android.text.SpannableStringBuilder;

/**
 * Created by muniraju on 07/03/16.
 */
public class SpannableStringBuilderCompat extends SpannableStringBuilder {
    public SpannableStringBuilderCompat() {
        super();
    }

    public SpannableStringBuilderCompat(CharSequence text) {
        super(text);
    }

    public SpannableStringBuilderCompat(CharSequence text, int start, int end) {
        super(text, start, end);
    }

    @Override
    public SpannableStringBuilderCompat append(CharSequence text, Object what, int flags) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            super.append(text, what, flags);
        } else {
            int start = length();
            append(text);
            setSpan(what, start, length(), flags);
        }
        return this;
    }

    @Override
    public SpannableStringBuilderCompat append(CharSequence text) {
        super.append(text);
        return this;
    }

    @Override
    public SpannableStringBuilderCompat append(CharSequence text, int start, int end) {
        super.append(text, start, end);
        return this;
    }

    @Override
    public SpannableStringBuilderCompat append(char text) {
        super.append(text);
        return this;
    }

    @Override
    public SpannableStringBuilderCompat insert(int where, CharSequence tb, int start, int end) {
        super.insert(where, tb, start, end);
        return this;
    }

    @Override
    public SpannableStringBuilderCompat replace(int start, int end, CharSequence tb) {
        super.replace(start, end, tb);
        return this;
    }

    @Override
    public SpannableStringBuilderCompat replace(int start, int end, CharSequence tb, int tbstart, int tbend) {
        super.replace(start, end, tb, tbstart, tbend);
        return this;
    }
}
