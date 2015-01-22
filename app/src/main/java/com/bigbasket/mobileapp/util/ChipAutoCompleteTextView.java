package com.bigbasket.mobileapp.util;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import com.bigbasket.mobileapp.R;

import java.util.ArrayList;

/**
 * Created by jugal on 14/1/15.
 */
public class ChipAutoCompleteTextView extends MultipleEmailAutoComplete {

    private Context context;
    private ArrayList<String> arrayListEmails = new ArrayList<>(); //for duplicate email address

    public ChipAutoCompleteTextView(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    public ChipAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context);
    }

    public ChipAutoCompleteTextView(Context context, AttributeSet attrs,
                                    int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        init(context);
    }

    public void init(Context context) {
        this.context = context;
        setOnItemClickListener(this);
        addTextChangedListener(textWatcher);
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (count >= 1) {
                if (s.charAt(start) == ' ') {
                    setChips();
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    public void setChips() {
        if (getText().toString().contains(getSeparator())) {
            SpannableStringBuilder ssb = new SpannableStringBuilder(getText());
            String chips[] = getText().toString().trim().split(getSeparator());


            int x = 0;
            for (String chip : chips) {
                if (!UIUtil.isValidEmail(chip)) {
                    UIUtil.reportFormInputFieldError(this, context.getString(R.string.invalid_email));
                    break;
                }
//                if(arrayListEmails.contains(chip)) //todo duplicate email if
//                    continue;
                arrayListEmails.add(chip);
                LayoutInflater lf = (LayoutInflater) getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                TextView txtChipView = (TextView) lf.inflate(R.layout.chip_txtview, null);

                txtChipView.setText(chip);

                int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
                txtChipView.measure(spec, spec);
                txtChipView.layout(0, 0, txtChipView.getMeasuredWidth(), txtChipView.getMeasuredHeight());
                Bitmap b = Bitmap.createBitmap(txtChipView.getWidth(), txtChipView.getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(b);
                canvas.translate(-txtChipView.getScrollX(), -txtChipView.getScrollY());
                txtChipView.draw(canvas);
                txtChipView.setDrawingCacheEnabled(true);
                Bitmap cacheBmp = txtChipView.getDrawingCache();
                Bitmap viewBmp = cacheBmp.copy(Bitmap.Config.ARGB_8888, true);
                txtChipView.destroyDrawingCache();
                BitmapDrawable bmpDrawable = new BitmapDrawable(viewBmp);
                bmpDrawable.setBounds(0, 0, bmpDrawable.getIntrinsicWidth(), bmpDrawable.getIntrinsicHeight());
                ssb.setSpan(new ImageSpan(bmpDrawable), x, x + chip.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                x = x + chip.length() + 1;
            }
            setText(ssb);
            setSelection(getText().length());
        }
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //if(!arrayListEmails.contains(getAdapter().getItem(position).toString()))
        setChips();
    }
}
