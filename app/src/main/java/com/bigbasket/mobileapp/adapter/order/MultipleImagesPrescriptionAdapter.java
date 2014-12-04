package com.bigbasket.mobileapp.adapter.order;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.bigbasket.mobileapp.R;
import com.bigbasket.mobileapp.activity.base.BaseActivity;
import com.bigbasket.mobileapp.common.CustomTypefaceSpan;
import com.bigbasket.mobileapp.model.order.ImageUploadModel;
import com.bigbasket.mobileapp.util.TouchImageView;
import com.google.gson.JsonArray;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by jugal on 28/8/14.
 */
public class MultipleImagesPrescriptionAdapter extends BaseAdapter {

    private BaseActivity activity;
    private ArrayList<Object> imageUploadModelArrayList;
    private LayoutInflater inflater;

    public MultipleImagesPrescriptionAdapter(BaseActivity activity,
                                             ArrayList<Object> imageUploadModelArrayList) {
        this.activity = activity;
        this.imageUploadModelArrayList = imageUploadModelArrayList;
        this.inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return imageUploadModelArrayList.size();
    }

    public Object getItem(int position) {
        return imageUploadModelArrayList.get(position);
    }

    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        Object imageUrl = getItem(position);
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.multiple_image_list, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if(imageUrl instanceof String){
            TouchImageView imgNewPrescriptionImage = viewHolder.getImgNewPrescriptionImage();
            if ((String.valueOf(imageUrl)) != null) {
                ImageLoader.getInstance().displayImage(String.valueOf(imageUrl), imgNewPrescriptionImage);
            }else {
                imgNewPrescriptionImage.setImageResource(R.drawable.image_404_top_cat);
            }
        }else {
            Bitmap bitmapImage = (Bitmap) imageUrl;
            ImageView imgNewPrescriptionImage = viewHolder.getImgNewPrescriptionImage();
            imgNewPrescriptionImage.setImageBitmap(bitmapImage);
        }


        return convertView;
    }

    private class ViewHolder{
        private TouchImageView imgNewPrescriptionImage;
        public View base;

        public ViewHolder(View base) {
            this.base = base;
        }

        public TouchImageView getImgNewPrescriptionImage() {
            if(imgNewPrescriptionImage == null)
                imgNewPrescriptionImage = (TouchImageView) base.findViewById(R.id.imgNewPrescriptionImage);
            return imgNewPrescriptionImage;
        }

    }

}
